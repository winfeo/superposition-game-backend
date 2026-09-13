package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionRequestDTO;
import io.github.winfeo.superpositiongame.backend.ai.util.AiStateMapper;
import io.github.winfeo.superpositiongame.backend.exception.AiStateMappingException;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiDifficulty;
import io.github.winfeo.superpositiongame.backend.game.model.game.GamePhase;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.AI_ID;
import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.HUMAN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiStateMapperTest {
    private final AiStateMapper mapper = new AiStateMapper();

    @Test
    void mapsCompleteGameStateForMctsModel() {
        GameState state = AiTestFixtures.defaultGameState();

        AiChooseActionRequestDTO request = mapper.toRequest(
                state,
                AI_ID,
                AiDifficulty.MCTS
        );

        assertThat(request.model()).isEqualTo("mcts");
        assertThat(request.aiPlayerId()).isEqualTo(AI_ID);
        assertThat(request.opponentPlayerId()).isEqualTo(HUMAN_ID);
        assertThat(request.dice()).containsOnlyKeys(AI_ID, HUMAN_ID);
        assertThat(request.targets()).containsOnlyKeys(AI_ID, HUMAN_ID);
        assertThat(request.aiHand()).hasSize(5);
        assertThat(request.opponentHand()).hasSize(1);
        assertThat(request.deck()).isEmpty();
        assertThat(request.discard()).isEmpty();
        assertThat(request.protectedSlots().get(AI_ID))
                .containsExactly(false, true, false, false);
        assertThat(request.moveNumber()).isEqualTo(7);
        assertThat(request.maxMoves()).isEqualTo(80);
        assertThat(request.remainingMoves()).isEqualTo(1);
        assertThat(request.currentPlayerId()).isEqualTo(AI_ID);
        assertThat(request.finished()).isFalse();
    }

    @Test
    void mapsLowDifficultyToLowApiValue() {
        AiChooseActionRequestDTO request = mapper.toRequest(
                AiTestFixtures.defaultGameState(),
                AI_ID,
                AiDifficulty.LOW
        );

        assertThat(request.model()).isEqualTo("low");
    }

    @Test
    void marksFinishedGame() {
        GameState finished = AiTestFixtures.defaultGameState()
                .copyWithPhase(GamePhase.GAME_FINISHED);

        assertThat(mapper.toRequest(finished, AI_ID, AiDifficulty.MCTS).finished())
                .isTrue();
    }

    @Test
    void rejectsStateWithoutAiPlayer() {
        assertThatThrownBy(() -> mapper.toRequest(
                AiTestFixtures.defaultGameState(),
                "missing-ai",
                AiDifficulty.MCTS
        ))
                .isInstanceOf(AiStateMappingException.class)
                .hasMessage("В состоянии игры отсутствует AI-игрок: missing-ai");
    }

    @Test
    void rejectsStateWithoutOpponent() {
        GameState source = AiTestFixtures.defaultGameState();
        LinkedHashMap<String, PlayerState> players = new LinkedHashMap<>();
        players.put(AI_ID, source.players().get(AI_ID));
        GameState withoutOpponent = source.copyWithPlayers(players);

        assertThatThrownBy(() -> mapper.toRequest(
                withoutOpponent,
                AI_ID,
                AiDifficulty.MCTS
        ))
                .isInstanceOf(AiStateMappingException.class)
                .hasMessage("В состоянии игры отсутствует противник AI");
    }
}
