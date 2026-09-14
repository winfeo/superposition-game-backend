package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.exception.AiConfigurationException;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiDifficulty;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiGameSession;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameMode;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import org.junit.jupiter.api.Test;

import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.AI_ID;
import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.HUMAN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiGameSessionTest {
    @Test
    void storesAiGameMetadataWithoutDuplicatingPlayerIds() {
        GameState state = AiTestFixtures.defaultGameState();

        AiGameSession session = new AiGameSession(
                "game-id",
                HUMAN_ID,
                AI_ID,
                state,
                AiDifficulty.MCTS
        );

        assertThat(session.getGameMode()).isEqualTo(GameMode.AI);
        assertThat(session.getHumanPlayerId()).isEqualTo(session.getPlayerA());
        assertThat(session.getAiPlayerId()).isEqualTo(session.getPlayerB());
        assertThat(session.getDifficulty()).isEqualTo(AiDifficulty.MCTS);
        assertThat(session.isRequestInProgress()).isFalse();
    }

    @Test
    void rejectsMissingDifficultyWithRussianMessage() {
        assertThatThrownBy(() -> new AiGameSession(
                "game-id",
                HUMAN_ID,
                AI_ID,
                AiTestFixtures.defaultGameState(),
                null
        ))
                .isInstanceOf(AiConfigurationException.class)
                .hasMessage("Сложность AI должна быть указана");
    }

    @Test
    void tracksRequestAndGameStateRevision() {
        AiGameSession session = new AiGameSession(
                "game-id",
                HUMAN_ID,
                AI_ID,
                AiTestFixtures.defaultGameState(),
                AiDifficulty.LOW
        );

        session.setRequestInProgress(true);
        session.updateGameState(session.getGameState().copyWithTurnNumber(8));

        assertThat(session.isRequestInProgress()).isTrue();
        assertThat(session.getGameStateRevision()).isEqualTo(1);
    }
}
