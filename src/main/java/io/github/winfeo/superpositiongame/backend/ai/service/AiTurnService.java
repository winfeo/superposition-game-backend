package io.github.winfeo.superpositiongame.backend.ai.service;

import io.github.winfeo.superpositiongame.backend.ai.client.AiApiClient;
import io.github.winfeo.superpositiongame.backend.ai.config.AiApiProperties;
import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionRequestDTO;
import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionResponseDTO;
import io.github.winfeo.superpositiongame.backend.ai.util.AiMoveMapper;
import io.github.winfeo.superpositiongame.backend.ai.util.AiStateMapper;
import io.github.winfeo.superpositiongame.backend.exception.AiMoveRejectedException;
import io.github.winfeo.superpositiongame.backend.exception.InvalidAiResponseException;
import io.github.winfeo.superpositiongame.backend.game.core.GameEngine;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiDifficulty;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AiTurnService {
    private static final Logger log = LoggerFactory.getLogger(AiTurnService.class);

    private final AiApiClient apiClient;
    private final AiStateMapper stateMapper;
    private final AiMoveMapper moveMapper;
    private final FallbackMoveSelector fallbackMoveSelector;
    private final GameEngine gameEngine;
    private final TaskExecutor taskExecutor;
    private final AiApiProperties properties;

    public AiTurnService(
            AiApiClient apiClient,
            AiStateMapper stateMapper,
            AiMoveMapper moveMapper,
            FallbackMoveSelector fallbackMoveSelector,
            GameEngine gameEngine,
            @Qualifier("aiTaskExecutor") TaskExecutor taskExecutor,
            AiApiProperties properties
    ) {
        this.apiClient = apiClient;
        this.stateMapper = stateMapper;
        this.moveMapper = moveMapper;
        this.fallbackMoveSelector = fallbackMoveSelector;
        this.gameEngine = gameEngine;
        this.taskExecutor = taskExecutor;
        this.properties = properties;
    }

    public CompletableFuture<AiMoveDecision> chooseMove(
            String gameId,
            GameState state,
            String aiPlayerId,
            AiDifficulty difficulty
    ) {
        CompletableFuture<AiMoveDecision> remoteDecision =
                CompletableFuture.supplyAsync(
                        () -> chooseRemoteMove(
                                gameId,
                                state,
                                aiPlayerId,
                                difficulty
                        ),
                        taskExecutor
        );

        return remoteDecision
                .orTimeout(
                        properties.requestTimeout().toMillis(),
                        TimeUnit.MILLISECONDS
                )
                .exceptionally(exception ->
                        fallback(
                                gameId,
                                state,
                                aiPlayerId,
                                exception
                        )
                );
    }

    private AiMoveDecision chooseRemoteMove(
            String gameId,
            GameState state,
            String aiPlayerId,
            AiDifficulty difficulty
    ) {
        AiChooseActionRequestDTO request = stateMapper.toRequest(state, aiPlayerId, difficulty);
        AiChooseActionResponseDTO response = apiClient.chooseAction(request);

        if (response == null || response.action() == null) {
            throw new InvalidAiResponseException("Ответ AI отсутствует или не содержит действие");
        }

        Move move = moveMapper.toDomain(response.action(), state, aiPlayerId);
        if (gameEngine.applyMove(state, move).isEmpty()) {
            throw new AiMoveRejectedException("Ход AI отклонён серверной проверкой");
        }

        log.info(
                "выбранный ход от AI: gameId={}, model={}, action={}, thinkingTimeMs={}",
                gameId,
                response.model(),
                move.getClass().getSimpleName(),
                response.thinkingTimeMs()
        );

        return AiMoveDecision.remote(move);
    }

    private AiMoveDecision fallback(
            String gameId,
            GameState state,
            String aiPlayerId,
            Throwable exception
    ) {
        String reason = rootMessage(exception);
        Move fallbackMove = fallbackMoveSelector.select(state, aiPlayerId).orElse(null);

        log.warn(
                "использование механизма запасного хода от AI: gameId={}, move={}, reason={}",
                gameId,
                fallbackMove == null? "FORCE_END_TURN": fallbackMove.getClass().getSimpleName(),
                reason
        );

        return AiMoveDecision.fallback(fallbackMove, reason);
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() == null
                ? current.getClass().getSimpleName()
                : current.getMessage();
    }
}
