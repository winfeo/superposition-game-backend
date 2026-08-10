package io.github.winfeo.superpositiongame.backend.game.core.service;

import io.github.winfeo.superpositiongame.backend.game.core.GameLoop;
import io.github.winfeo.superpositiongame.backend.game.model.game.*;
import io.github.winfeo.superpositiongame.backend.repository.memory.ActiveGameRepository;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class GameTimerServiceImpl implements GameTimerService {
    private static final long TIMER_PROCESS_INTERVAL_MS = 100L;
    private static final long TIMER_SYNC_INTERVAL_MS = 500L;

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

    @Scheduled(fixedRate = TIMER_PROCESS_INTERVAL_MS)
    public void processTimers() {
        for (GameSession session: repository.getAllGames()) {
            long now = System.currentTimeMillis();
            boolean turnFinished = false;
            TimerSnapshot timerSnapshot = null;

            synchronized (session) {
                if (session.getStatus() != GameSessionStatus.ACTIVE) continue;

                GameState state = session.getGameState();
                if (state.turnEndsAt() == null) continue;
                if (state.phase() == GamePhase.GAME_FINISHED) continue;
                if (state.phase() == GamePhase.GAME_SETUP) continue;

                if (now >= state.turnEndsAt()) {
                    state = gameLoop.forceEndTurn(state);
                    session.updateGameState(state);
                    repository.save(session);
                    turnFinished = true;
                }

                if (session.shouldPublishTimer(now, TIMER_SYNC_INTERVAL_MS)) {
                    long timeLeft = Math.max(0L, state.turnEndsAt() - now);
                    long revision = session.markTimerPublished(now);

                    timerSnapshot = new TimerSnapshot(
                            state.turnNumber(),
                            timeLeft,
                            revision
                    );
                }
            }

            if (turnFinished) gameService.broadcastState(session);

            if (timerSnapshot != null) {
                sendTime(
                        session.getPlayerA(),
                        session.getGameId(),
                        timerSnapshot
                );

                sendTime(
                        session.getPlayerB(),
                        session.getGameId(),
                        timerSnapshot
                );
            }
        }
    }

    private void sendTime(
            String userId,
            String gameId,
            TimerSnapshot snapshot
    ) {
        SimpUser user = userRegistry.getUser(userId);
        if (user != null && user.hasSessions()) {
            publisher.sendTimerUpdate(
                    userId,
                    gameId,
                    snapshot.turnNumber(),
                    snapshot.timeLeftMs(),
                    snapshot.revision()
            );
        }
    }
}