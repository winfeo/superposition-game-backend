package io.github.winfeo.superpositiongame.backend.game.core.service;

import io.github.winfeo.superpositiongame.backend.config.GamePresenceProperties;
import io.github.winfeo.superpositiongame.backend.game.core.GameEngine;
import io.github.winfeo.superpositiongame.backend.game.core.GameLoop;
import io.github.winfeo.superpositiongame.backend.game.dto.ActiveGameDTO;
import io.github.winfeo.superpositiongame.backend.game.dto.ActiveGameResponseDTO;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameEndReason;
import io.github.winfeo.superpositiongame.backend.game.model.game.GamePhase;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameSession;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameSessionStatus;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;
import io.github.winfeo.superpositiongame.backend.game.model.move.Surrender;
import io.github.winfeo.superpositiongame.backend.repository.memory.ActiveGameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameServiceImpl implements GameService {
    private static final long MINIMUM_RESUMED_TURN_TIME_MS = 1_000L;

    private final GameResultService gameResultService;
    private final ActiveGameRepository repository;
    private final ConcurrentHashMap<String, Set<String>> readyPlayers = new ConcurrentHashMap<>();
    private final GameEngine gameEngine;
    private final GameEventPublisher publisher;
    private final GameLoop gameLoop;
    private final GamePresenceProperties presenceProperties;

    public GameServiceImpl(
            GameResultService gameResultService,
            ActiveGameRepository repository,
            GameEngine gameEngine,
            GameEventPublisher publisher,
            GameLoop gameLoop,
            GamePresenceProperties presenceProperties
    ) {
        this.gameResultService = gameResultService;
        this.repository = repository;
        this.publisher = publisher;
        this.gameEngine = gameEngine;
        this.gameLoop = gameLoop;
        this.presenceProperties = presenceProperties;
    }

    @Override
    public synchronized void createGame(String playerA, String playerB) {
        if (repository.findByPlayerId(playerA) != null
                || repository.findByPlayerId(playerB) != null) {
            return;
        }

        String gameId = UUID.randomUUID().toString();
        GameState initialState = gameLoop.startGame(playerA, playerB);
        GameSession session = new GameSession(
                gameId,
                playerA,
                playerB,
                initialState
        );

        repository.save(session);
        readyPlayers.put(gameId, ConcurrentHashMap.newKeySet());

        publisher.sendGameStart(playerA, gameId);
        publisher.sendGameStart(playerB, gameId);
        publisher.sendLifecycle(session);
    }

    @Override
    public void playerReady(String gameId, String userId) {
        GameSession session = repository.findById(gameId);
        if (session == null) return;

        if (session.getStatus() != GameSessionStatus.WAITING_FOR_PLAYERS) {
            reconnect(gameId, userId);
            return;
        }

        boolean activated = false;

        synchronized (session) {
            if (!session.containsPlayer(userId) || isTerminal(session)) return;

            long now = System.currentTimeMillis();
            session.recordHeartbeat(userId, now);
            session.markReconnected(userId);

            Set<String> readySet = readyPlayers.computeIfAbsent(
                    gameId,
                    ignored -> ConcurrentHashMap.newKeySet()
            );

            readySet.add(userId);

            if (readySet.contains(session.getPlayerA())
                    && readySet.contains(session.getPlayerB())
                    && !session.hasDisconnectedPlayers()) {
                activateSession(session, now);
                readyPlayers.remove(gameId);
                activated = true;
            }

            repository.save(session);
        }

        if (activated) {
            broadcastState(session);
        }
        publisher.sendLifecycle(session);
    }

    @Override
    public void handleMove(String gameId, Move move, String userId) {
        GameSession session = repository.findById(gameId);
        if (session == null) return;

        String winnerId;
        GameEndReason endReason;

        synchronized (session) {
            if (session.getStatus() != GameSessionStatus.ACTIVE) return;
            if (!session.containsPlayer(userId)) return;
            if (!move.playerId().equals(userId)) return;

            GameState currentState = session.getGameState();
            Long turnEndsAt = currentState.turnEndsAt();
            if (turnEndsAt != null && System.currentTimeMillis() >= turnEndsAt) return;

            GameState afterMoveState = gameEngine.applyMove(currentState, move);
            afterMoveState = gameLoop.afterMove(afterMoveState, userId, gameId);
            session.updateGameState(afterMoveState);
            repository.save(session);

            winnerId = afterMoveState.winnerId();
            endReason = move instanceof Surrender
                    ? GameEndReason.SURRENDER
                    : GameEndReason.OBJECTIVE_COMPLETED;
        }

        if (winnerId != null) {
            finishWithWinner(session, winnerId, endReason);
        } else {
            broadcastState(session);
        }
    }

    @Override
    public void heartbeat(String gameId, String userId) {
        GameSession session = repository.findById(gameId);
        if (session == null) return;

        synchronized (session) {
            if (!session.containsPlayer(userId) || isTerminal(session)) return;
            session.recordHeartbeat(userId, System.currentTimeMillis());
            repository.save(session);
        }
    }

    @Override
    public void reconnect(String gameId, String userId) {
        GameSession session = repository.findById(gameId);
        if (session == null) return;

        boolean resumed = false;
        boolean activated = false;
        boolean rejectedByDeadline = false;

        synchronized (session) {
            if (!session.containsPlayer(userId) || isTerminal(session)) return;

            long now = System.currentTimeMillis();
            Long deadline = session.getReconnectDeadline(userId);

            if (deadline != null && now >= deadline) {
                rejectedByDeadline = true;
            } else {
                session.markReconnected(userId);
                session.recordHeartbeat(userId, now);

                if (session.getStatus() == GameSessionStatus.WAITING_FOR_PLAYERS) {
                    Set<String> readySet = readyPlayers.computeIfAbsent(
                            gameId,
                            ignored -> ConcurrentHashMap.newKeySet()
                    );
                    readySet.add(userId);

                    if (readySet.contains(session.getPlayerA())
                            && readySet.contains(session.getPlayerB())
                            && !session.hasDisconnectedPlayers()) {
                        activateSession(session, now);
                        readyPlayers.remove(gameId);
                        activated = true;
                    }
                }

                if (session.getStatus() == GameSessionStatus.PAUSED_FOR_RECONNECT
                        && !session.hasDisconnectedPlayers()) {
                    activateSession(session, now);
                    resumed = true;
                }

                repository.save(session);
            }
        }

        if (rejectedByDeadline) {
            evaluateReconnectDeadlines(session, System.currentTimeMillis());
            return;
        }

        if (activated || resumed || session.getStatus() == GameSessionStatus.ACTIVE) {
            broadcastState(session);
        }
        publisher.sendLifecycle(session);
    }

    @Override
    public void markPlayerInactive(String gameId, String userId) {
        GameSession session = repository.findById(gameId);
        if (session != null) {
            markDisconnected(session, userId, System.currentTimeMillis());
        }
    }

    @Override
    public void handlePlayerDisconnected(String userId) {
        GameSession session = repository.findByPlayerId(userId);
        if (session != null) {
            markDisconnected(session, userId, System.currentTimeMillis());
        }
    }

    @Override
    public void declineReconnect(String gameId, String userId) {
        GameSession session = repository.findById(gameId);
        if (session == null || !session.containsPlayer(userId)) return;

        String winnerId = session.getOpponentId(userId);
        if (winnerId != null) {
            finishWithWinner(
                    session,
                    winnerId,
                    GameEndReason.RECONNECT_DECLINED
            );
        }
    }

    @Override
    public void sendActiveGame(String userId) {
        GameSession session = repository.findByPlayerId(userId);

        if (session == null || isTerminal(session)) {
            publisher.sendActiveGame(userId, ActiveGameResponseDTO.empty());
            return;
        }

        ActiveGameDTO game;

        synchronized (session) {
            String opponentId = session.getOpponentId(userId);
            PlayerState opponent = session.getGameState().players().get(opponentId);

            game = new ActiveGameDTO(
                    session.getGameId(),
                    session.getStatus(),
                    opponentId,
                    opponent == null ? null : opponent.nickname(),
                    session.isDisconnected(userId),
                    session.getReconnectDeadline(userId),
                    System.currentTimeMillis()
            );
        }

        publisher.sendActiveGame(userId, ActiveGameResponseDTO.of(game));
    }

    @Override
    public void processConnectionTimeouts() {
        long now = System.currentTimeMillis();
        List<GameSession> sessions = new ArrayList<>(repository.getAllGames());

        for (GameSession session : sessions) {
            if (isTerminal(session)) continue;

            if (session.getStatus() == GameSessionStatus.WAITING_FOR_PLAYERS
                    || session.getStatus() == GameSessionStatus.ACTIVE
                    || session.getStatus() == GameSessionStatus.PAUSED_FOR_RECONNECT) {
                Set<String> missedHeartbeats = new HashSet<>();

                synchronized (session) {
                    for (String playerId : session.getPlayerIds()) {
                        if (session.isDisconnected(playerId)) continue;

                        Long lastHeartbeat = session.getLastHeartbeatAt(playerId);
                        if (lastHeartbeat != null
                                && now - lastHeartbeat
                                >= presenceProperties.heartbeatTimeoutMs()) {
                            missedHeartbeats.add(playerId);
                        }
                    }
                }

                for (String playerId : missedHeartbeats) {
                    markDisconnected(session, playerId, now);
                }
            }

            evaluateReconnectDeadlines(session, now);
        }
    }

    @Override
    public void broadcastState(GameSession session) {
        GameState state;
        String playerA;
        String playerB;
        String gameId;

        synchronized (session) {
            if (repository.findById(session.getGameId()) == null) return;

            state = session.getGameState()
                    .copyWithServerTime(System.currentTimeMillis());
            session.updateGameState(state);
            repository.save(session);

            playerA = session.getPlayerA();
            playerB = session.getPlayerB();
            gameId = session.getGameId();
        }

        publisher.sendToUser(playerA, gameId, state);
        publisher.sendToUser(playerB, gameId, state);
    }

    private void markDisconnected(
            GameSession session,
            String userId,
            long now
    ) {
        boolean changed;
        boolean gamePaused = false;

        synchronized (session) {
            if (!session.containsPlayer(userId) || isTerminal(session)) return;

            changed = session.markDisconnected(
                    userId,
                    now + presenceProperties.reconnectTimeoutMs()
            );
            if (!changed) return;

            Set<String> readySet = readyPlayers.get(session.getGameId());
            if (readySet != null) {
                readySet.remove(userId);
            }

            if (session.getStatus() == GameSessionStatus.ACTIVE) {
                pauseSession(session, now);
                gamePaused = true;
            }

            repository.save(session);
        }

        if (gamePaused) {
            broadcastState(session);
        }
        publisher.sendLifecycle(session);
    }

    private void pauseSession(GameSession session, long now) {
        GameState state = session.getGameState();
        Long turnEndsAt = state.turnEndsAt();
        long timeLeft = turnEndsAt == null
                ? session.getPausedTurnTimeLeftMs()
                : Math.max(0L, turnEndsAt - now);

        session.setPausedTurnTimeLeftMs(timeLeft);
        session.updateGameState(
                state.copyWithServerTime(now)
                        .copyWithTurnEndsAt(null)
        );
        session.setStatus(GameSessionStatus.PAUSED_FOR_RECONNECT);
    }

    private void activateSession(GameSession session, long now) {
        long timeLeft = Math.max(
                MINIMUM_RESUMED_TURN_TIME_MS,
                session.getPausedTurnTimeLeftMs()
        );

        session.updateGameState(
                session.getGameState()
                        .copyWithServerTime(now)
                        .copyWithTurnEndsAt(now + timeLeft)
        );
        session.setStatus(GameSessionStatus.ACTIVE);
        session.recordHeartbeat(session.getPlayerA(), now);
        session.recordHeartbeat(session.getPlayerB(), now);
        session.invalidateTimerSync();
    }

    private void evaluateReconnectDeadlines(
            GameSession session,
            long now
    ) {
        Set<String> disconnected;
        Set<String> expired = new HashSet<>();

        synchronized (session) {
            if (isTerminal(session)) return;

            disconnected = session.getDisconnectedPlayers();
            for (String playerId : disconnected) {
                Long deadline = session.getReconnectDeadline(playerId);
                if (deadline != null && now >= deadline) {
                    expired.add(playerId);
                }
            }
        }

        if (disconnected.size() == 2) {
            if (expired.size() == 2) {
                cancelGame(session, GameEndReason.BOTH_PLAYERS_DISCONNECTED);
            }
            return;
        }

        if (disconnected.size() == 1 && expired.size() == 1) {
            String loserId = expired.iterator().next();
            String winnerId = session.getOpponentId(loserId);
            if (winnerId != null) {
                finishWithWinner(
                        session,
                        winnerId,
                        GameEndReason.RECONNECT_TIMEOUT
                );
            }
        }
    }

    private void finishWithWinner(
            GameSession session,
            String winnerId,
            GameEndReason reason
    ) {
        synchronized (session) {
            if (isTerminal(session)
                    || repository.findById(session.getGameId()) == null) {
                return;
            }

            long now = System.currentTimeMillis();
            GameState finalState = session.getGameState()
                    .copyWithPhase(GamePhase.GAME_FINISHED)
                    .copyWithWinnerId(winnerId)
                    .copyWithServerTime(now)
                    .copyWithTurnEndsAt(null);

            session.updateGameState(finalState);
            session.setStatus(GameSessionStatus.FINISHED);
            session.setEndReason(reason);
            repository.save(session);
            readyPlayers.remove(session.getGameId());
        }

        broadcastState(session);
        publisher.sendLifecycle(session);
        try {
            gameResultService.saveGameResult(session);
        } finally {
            repository.delete(session.getGameId());
        }
    }

    private void cancelGame(
            GameSession session,
            GameEndReason reason
    ) {
        synchronized (session) {
            if (isTerminal(session)
                    || repository.findById(session.getGameId()) == null) {
                return;
            }

            long now = System.currentTimeMillis();
            session.updateGameState(
                    session.getGameState()
                            .copyWithPhase(GamePhase.GAME_FINISHED)
                            .copyWithWinnerId(null)
                            .copyWithServerTime(now)
                            .copyWithTurnEndsAt(null)
            );
            session.setStatus(GameSessionStatus.CANCELLED);
            session.setEndReason(reason);
            repository.save(session);
            readyPlayers.remove(session.getGameId());
        }

        broadcastState(session);
        publisher.sendLifecycle(session);
        repository.delete(session.getGameId());
    }

    private boolean isTerminal(GameSession session) {
        return session.getStatus() == GameSessionStatus.FINISHED
                || session.getStatus() == GameSessionStatus.CANCELLED;
    }
}