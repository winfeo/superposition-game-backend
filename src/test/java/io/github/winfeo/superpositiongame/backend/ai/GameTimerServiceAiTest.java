package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.game.core.GameLoop;
import io.github.winfeo.superpositiongame.backend.game.core.service.GameService;
import io.github.winfeo.superpositiongame.backend.game.core.service.GameTimerPublisher;
import io.github.winfeo.superpositiongame.backend.game.core.service.GameTimerServiceImpl;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiDifficulty;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiGameSession;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameSessionStatus;
import io.github.winfeo.superpositiongame.backend.repository.memory.ActiveGameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.util.List;

import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.AI_ID;
import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.HUMAN_ID;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameTimerServiceAiTest {
    @Mock
    private ActiveGameRepository repository;
    @Mock
    private GameLoop gameLoop;
    @Mock
    private GameService gameService;
    @Mock
    private GameTimerPublisher publisher;
    @Mock
    private SimpUserRegistry userRegistry;
    @Mock
    private SimpUser humanUser;

    @Test
    void sendsAiGameTimerOnlyToHumanWebSocketUser() {
        AiGameSession session = new AiGameSession(
                "game-id",
                HUMAN_ID,
                AI_ID,
                AiTestFixtures.defaultGameState(),
                AiDifficulty.MCTS
        );
        session.setStatus(GameSessionStatus.ACTIVE);
        when(repository.getAllGames()).thenReturn(List.of(session));
        when(userRegistry.getUser(HUMAN_ID)).thenReturn(humanUser);
        when(humanUser.hasSessions()).thenReturn(true);

        new GameTimerServiceImpl(
                repository,
                gameLoop,
                gameService,
                publisher,
                userRegistry
        ).processTimers();

        verify(publisher).sendTimerUpdate(
                eq(HUMAN_ID),
                eq("game-id"),
                eq(7),
                anyLong(),
                eq(1L)
        );
        verify(userRegistry, never()).getUser(AI_ID);
    }
}
