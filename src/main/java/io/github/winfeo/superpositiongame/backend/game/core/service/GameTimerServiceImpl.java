package io.github.winfeo.superpositiongame.backend.game.core.service;

import io.github.winfeo.superpositiongame.backend.game.core.GameLoop;
import io.github.winfeo.superpositiongame.backend.game.model.game.GamePhase;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameSession;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameSessionStatus;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.repository.memory.ActiveGameRepository;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class GameTimerServiceImpl implements GameTimerService {
    private final ActiveGameRepository repository;
    private final GameLoop gameLoop;
    private final GameService gameService;
    private final GameTimerPublisher publisher;
    private final SimpUserRegistry userRegistry;

    public GameTimerServiceImpl(
            ActiveGameRepository repository,
            GameLoop gameLoop,
            GameService gameService,
            GameTimerPublisher publisher,
            SimpUserRegistry userRegistry
    ) {
        this.repository = repository;
        this.gameLoop = gameLoop;
        this.gameService = gameService;
        this.publisher = publisher;
        this.userRegistry = userRegistry;
    }

    @Scheduled(fixedRate = 500)
    public void processTimers() {
        long now = System.currentTimeMillis();
        for (GameSession session : repository.getAllGames()) {
            boolean turnFinished = false;
            long timeLeft;

            synchronized (session) {
                if (session.getStatus() != GameSessionStatus.ACTIVE) continue;

                GameState state = session.getGameState();
                if (state.turnEndsAt() == null) continue;
                if (state.phase() == GamePhase.GAME_FINISHED) continue;
                if (state.phase() == GamePhase.GAME_SETUP) continue;

                if (now >= state.turnEndsAt()) {
                    GameState updated = gameLoop.forceEndTurn(state);
                    session.updateGameState(updated);
                    repository.save(session);
                    turnFinished = true;
                    timeLeft = 0L;
                } else {
                    timeLeft = Math.max(0, state.turnEndsAt() - now);
                }
            }

            if (turnFinished) {
                gameService.broadcastState(session);
                continue;
            }

            sendTime(
                    session.getPlayerA(),
                    session.getGameId(),
                    timeLeft,
                    now
            );

            sendTime(
                    session.getPlayerB(),
                    session.getGameId(),
                    timeLeft,
                    now
            );
        }
    }

    private void sendTime(
            String userId,
            String gameId,
            long timeLeft,
            long timestamp
    ) {
        SimpUser user = userRegistry.getUser(userId);
        if (user != null && user.hasSessions()) {
            publisher.sendTimerUpdate(
                    userId,
                    gameId,
                    timeLeft,
                    timestamp
            );
        }
    }
}