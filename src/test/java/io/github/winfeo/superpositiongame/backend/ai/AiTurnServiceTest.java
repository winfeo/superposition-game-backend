package io.github.winfeo.superpositiongame.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.winfeo.superpositiongame.backend.ai.client.AiApiClient;
import io.github.winfeo.superpositiongame.backend.ai.config.AiApiProperties;
import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionRequestDTO;
import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionResponseDTO;
import io.github.winfeo.superpositiongame.backend.ai.service.AiMoveDecision;
import io.github.winfeo.superpositiongame.backend.ai.service.AiTurnService;
import io.github.winfeo.superpositiongame.backend.ai.service.FallbackMoveSelector;
import io.github.winfeo.superpositiongame.backend.ai.util.AiMoveMapper;
import io.github.winfeo.superpositiongame.backend.ai.util.AiStateMapper;
import io.github.winfeo.superpositiongame.backend.game.core.GameEngine;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiDifficulty;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;
import io.github.winfeo.superpositiongame.backend.game.model.move.Surrender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.AI_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiTurnServiceTest {
    @Mock
    private AiApiClient apiClient;
    @Mock
    private AiStateMapper stateMapper;
    @Mock
    private AiMoveMapper moveMapper;
    @Mock
    private FallbackMoveSelector fallbackMoveSelector;
    @Mock
    private GameEngine gameEngine;

    private final GameState state = AiTestFixtures.defaultGameState();
    private final Move remoteMove = new Surrender(AI_ID);
    private final Move fallbackMove = new Surrender(AI_ID);
    private AiChooseActionRequestDTO request;
    private AiChooseActionResponseDTO response;

    @BeforeEach
    void setUp() throws Exception {
        request = request();
        response = new AiChooseActionResponseDTO(
                "mcts",
                new ObjectMapper().readTree("{\"type\":\"SURRENDER\",\"playerId\":\"ai-player\"}"),
                12.5,
                100,
                5,
                null
        );
    }

    @Test
    void returnsRemoteDecisionWhenAiMoveIsValid() {
        when(stateMapper.toRequest(state, AI_ID, AiDifficulty.MCTS)).thenReturn(request);
        when(apiClient.chooseAction(request)).thenReturn(response);
        when(moveMapper.toDomain(response.action(), state, AI_ID)).thenReturn(remoteMove);
        when(gameEngine.applyMove(state, remoteMove)).thenReturn(Optional.of(state));

        AiMoveDecision decision = service(Runnable::run, Duration.ofSeconds(1))
                .chooseMove("game-id", state, AI_ID, AiDifficulty.MCTS)
                .join();

        assertThat(decision.move()).isEqualTo(remoteMove);
        assertThat(decision.fallback()).isFalse();
        assertThat(decision.reason()).isNull();
        verifyNoInteractions(fallbackMoveSelector);
    }

    @Test
    void usesFallbackWhenApiCallFails() {
        when(stateMapper.toRequest(state, AI_ID, AiDifficulty.MCTS)).thenReturn(request);
        when(apiClient.chooseAction(request))
                .thenThrow(new RuntimeException("AI-сервер недоступен"));
        when(fallbackMoveSelector.select(state, AI_ID))
                .thenReturn(Optional.of(fallbackMove));

        AiMoveDecision decision = service(Runnable::run, Duration.ofSeconds(1))
                .chooseMove("game-id", state, AI_ID, AiDifficulty.MCTS)
                .join();

        assertThat(decision.move()).isEqualTo(fallbackMove);
        assertThat(decision.fallback()).isTrue();
        assertThat(decision.reason()).isEqualTo("AI-сервер недоступен");
    }

    @Test
    void usesFallbackWhenGameEngineRejectsRemoteMove() {
        when(stateMapper.toRequest(state, AI_ID, AiDifficulty.MCTS)).thenReturn(request);
        when(apiClient.chooseAction(request)).thenReturn(response);
        when(moveMapper.toDomain(response.action(), state, AI_ID)).thenReturn(remoteMove);
        when(gameEngine.applyMove(state, remoteMove)).thenReturn(Optional.empty());
        when(fallbackMoveSelector.select(state, AI_ID))
                .thenReturn(Optional.of(fallbackMove));

        AiMoveDecision decision = service(Runnable::run, Duration.ofSeconds(1))
                .chooseMove("game-id", state, AI_ID, AiDifficulty.MCTS)
                .join();

        assertThat(decision.fallback()).isTrue();
        assertThat(decision.reason()).isEqualTo("Ход AI отклонён серверной проверкой");
    }

    @Test
    void returnsNullMoveWhenFallbackCannotFindLegalAction() {
        when(stateMapper.toRequest(state, AI_ID, AiDifficulty.MCTS)).thenReturn(request);
        when(apiClient.chooseAction(request)).thenReturn(null);
        when(fallbackMoveSelector.select(state, AI_ID)).thenReturn(Optional.empty());

        AiMoveDecision decision = service(Runnable::run, Duration.ofSeconds(1))
                .chooseMove("game-id", state, AI_ID, AiDifficulty.MCTS)
                .join();

        assertThat(decision.move()).isNull();
        assertThat(decision.fallback()).isTrue();
        assertThat(decision.reason())
                .isEqualTo("Ответ AI отсутствует или не содержит действие");
    }

    @Test
    void usesFallbackAfterOverallTimeout() throws Exception {
        TaskExecutor executorThatDoesNotStartTask = task -> { };
        when(fallbackMoveSelector.select(state, AI_ID))
                .thenReturn(Optional.of(fallbackMove));

        AiMoveDecision decision = service(
                executorThatDoesNotStartTask,
                Duration.ofMillis(20)
        )
                .chooseMove("game-id", state, AI_ID, AiDifficulty.MCTS)
                .get(1, TimeUnit.SECONDS);

        assertThat(decision.move()).isEqualTo(fallbackMove);
        assertThat(decision.fallback()).isTrue();
        assertThat(decision.reason()).isEqualTo("TimeoutException");
        verifyNoInteractions(apiClient);
    }

    private AiTurnService service(TaskExecutor executor, Duration timeout) {
        return new AiTurnService(
                apiClient,
                stateMapper,
                moveMapper,
                fallbackMoveSelector,
                gameEngine,
                executor,
                new AiApiProperties(
                        URI.create("http://ai.test"),
                        Duration.ofSeconds(1),
                        timeout
                )
        );
    }

    private AiChooseActionRequestDTO request() {
        return new AiChooseActionRequestDTO(
                "mcts",
                AI_ID,
                AiTestFixtures.HUMAN_ID,
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Map.of(),
                1,
                80,
                1,
                AI_ID,
                false
        );
    }
}
