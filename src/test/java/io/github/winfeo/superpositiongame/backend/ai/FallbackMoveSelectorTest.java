package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.ai.service.FallbackMoveSelector;
import io.github.winfeo.superpositiongame.backend.game.core.GameEngine;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.AI_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FallbackMoveSelectorTest {
    @Mock
    private GameEngine gameEngine;

    @Test
    void returnsFirstCandidateAcceptedByGameEngine() {
        GameState state = AiTestFixtures.gameState(
                List.of(new Card("play-card", CardType.HADAMARD))
        );
        when(gameEngine.applyMove(eq(state), any(Move.class)))
                .thenReturn(Optional.of(state));

        Optional<Move> result = new FallbackMoveSelector(gameEngine)
                .select(state, AI_ID);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().playerId()).isEqualTo(AI_ID);
    }

    @Test
    void continuesAfterCandidateThrowsException() {
        GameState state = AiTestFixtures.gameState(
                List.of(new Card("play-card", CardType.HADAMARD))
        );
        when(gameEngine.applyMove(eq(state), any(Move.class)))
                .thenThrow(new IllegalStateException("ошибка кандидата"))
                .thenReturn(Optional.of(state));

        Optional<Move> result = new FallbackMoveSelector(gameEngine)
                .select(state, AI_ID);

        assertThat(result).isPresent();
        verify(gameEngine, atLeast(2)).applyMove(eq(state), any(Move.class));
    }

    @Test
    void returnsEmptyWhenEveryCandidateIsRejected() {
        GameState state = AiTestFixtures.gameState(
                List.of(new Card("identity-card", CardType.IDENTITY))
        );
        when(gameEngine.applyMove(eq(state), any(Move.class)))
                .thenReturn(Optional.empty());

        Optional<Move> result = new FallbackMoveSelector(gameEngine)
                .select(state, AI_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyWhenAiPlayerIsMissing() {
        Optional<Move> result = new FallbackMoveSelector(gameEngine)
                .select(AiTestFixtures.defaultGameState(), "missing-ai");

        assertThat(result).isEmpty();
        verifyNoInteractions(gameEngine);
    }

    @Test
    void doesNotGenerateSurrenderAsFallback() {
        GameState state = AiTestFixtures.gameState(List.of());

        Optional<Move> result = new FallbackMoveSelector(gameEngine)
                .select(state, AI_ID);

        assertThat(result).isEmpty();
        verifyNoInteractions(gameEngine);
    }
}
