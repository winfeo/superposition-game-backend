package io.github.winfeo.superpositiongame.backend.game.core.service;

import io.github.winfeo.superpositiongame.backend.game.core.GameEngine;
import io.github.winfeo.superpositiongame.backend.game.core.GameLoop;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameSession;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;
import io.github.winfeo.superpositiongame.backend.repository.memory.ActiveGameRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameServiceImpl implements GameService {
    private final ActiveGameRepository repository;
//    private final Map<String, GameSession> games = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> readyPlayers = new ConcurrentHashMap<>();
    private final GameEngine gameEngine;
    private final GameEventPublisher publisher;
    private final GameLoop gameLoop;

    public GameServiceImpl(
            ActiveGameRepository repository,
            GameEngine gameEngine,
            GameEventPublisher publisher,
            GameLoop gameLoop
    ) {
        this.repository = repository;
        this.publisher = publisher;
        this.gameEngine = gameEngine;
        this.gameLoop = gameLoop;
    }

    @Override
    public void createGame(String playerA, String playerB) { //TODO void&
        String gameId = UUID.randomUUID().toString();

        GameState initialState = gameLoop.startGame(playerA, playerB);
        GameSession newSession = new GameSession(
                gameId,
                playerA,
                playerB,
                initialState
        );

//        games.put(gameId, newSession);
        repository.save(newSession);

        readyPlayers.put(gameId, ConcurrentHashMap.newKeySet());

        publisher.sendGameStart(playerA, gameId);
        publisher.sendGameStart(playerB, gameId);
    }

    @Override
    public void playerReady(String gameId, String userId) {
        GameSession session = repository.findById(gameId);
        if (session == null) return;

        Set<String> readySet = readyPlayers.get(gameId);
        if (readySet == null) return;

        if (readySet.add(userId)) {
            if (readySet.size() == 2) {
                broadcastState(session);
                readyPlayers.remove(gameId);
            }
        }
    }

    @Override
    public void handleMove(String gameId, Move move, String userId) {
        GameSession session = repository.findById(gameId);

        if (session == null) return;
        if (!move.playerId().equals(userId)) return;

        GameState currentState = session.getGameState();
        GameState afterMoveState = gameEngine.applyMove(currentState, move);
        if (afterMoveState.winnerId() == null) {
            afterMoveState = gameLoop.afterMove(afterMoveState, userId, gameId);
        }

        session.updateGameState(afterMoveState);
        repository.save(session);

        broadcastState(session);
    }

    @Override
    public void broadcastState(GameSession session) {
        GameState state = session.getGameState().copyWithServerTime(System.currentTimeMillis());
        session.updateGameState(state);
        repository.save(session);

        String playerA = session.getPlayerA();
        String playerB = session.getPlayerB();
        String gameId = session.getGameId();

        publisher.sendToUser(playerA, gameId, state);
        publisher.sendToUser(playerB, gameId, state);
    }
}
