package io.github.winfeo.superpositiongame.backend.game.core.service;

import io.github.winfeo.superpositiongame.backend.game.core.GameLoop;
import io.github.winfeo.superpositiongame.backend.game.model.game.GamePhase;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameSession;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.repository.memory.ActiveGameRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class GameTimerServiceImpl implements GameTimerService {
    private final ActiveGameRepository repository;
    private final GameLoop gameLoop;
    private final GameService gameService;

    public GameTimerServiceImpl(
            ActiveGameRepository repository,
            GameLoop gameLoop,
            GameService gameService
    ) {
        this.repository = repository;
        this.gameLoop = gameLoop;
        this.gameService = gameService;
    }

    @Scheduled(fixedRate = 1000)
    public void processTimers() {
        long now = System.currentTimeMillis();
        for (GameSession session : repository.getAllGames()) {
            GameState state = session.getGameState();
            if (state.turnEndsAt() == null) continue;
            if (state.phase() == GamePhase.GAME_FINISHED) continue;
            if (now >= state.turnEndsAt()) {
                GameState updated = gameLoop.forceEndTurn(state);
                session.updateGameState(updated);
                repository.save(session);

                gameService.broadcastState(session);
            }
        }
    }
}
