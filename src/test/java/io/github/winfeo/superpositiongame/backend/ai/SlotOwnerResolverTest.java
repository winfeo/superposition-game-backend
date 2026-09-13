package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.game.model.game.SlotOwner;
import io.github.winfeo.superpositiongame.backend.game.util.SlotOwnerResolver;
import org.junit.jupiter.api.Test;

import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.AI_ID;
import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.HUMAN_ID;
import static org.assertj.core.api.Assertions.assertThat;

class SlotOwnerResolverTest {
    @Test
    void resolvesRelativeOwnerToRealPlayerId() {
        assertThat(SlotOwnerResolver.resolvePlayerId(
                AiTestFixtures.defaultGameState(),
                AI_ID,
                SlotOwner.PLAYER.name()
        )).contains(AI_ID);

        assertThat(SlotOwnerResolver.resolvePlayerId(
                AiTestFixtures.defaultGameState(),
                AI_ID,
                SlotOwner.OPPONENT.name()
        )).contains(HUMAN_ID);
    }

    @Test
    void resolvesRealPlayerIdToRelativeOwner() {
        assertThat(SlotOwnerResolver.resolveSlotOwner(
                AiTestFixtures.defaultGameState(),
                AI_ID,
                AI_ID
        )).contains(SlotOwner.PLAYER);

        assertThat(SlotOwnerResolver.resolveSlotOwner(
                AiTestFixtures.defaultGameState(),
                AI_ID,
                HUMAN_ID
        )).contains(SlotOwner.OPPONENT);
    }

    @Test
    void rejectsUnknownOwnersAndPlayers() {
        assertThat(SlotOwnerResolver.resolvePlayerId(
                AiTestFixtures.defaultGameState(),
                AI_ID,
                "UNKNOWN"
        )).isEmpty();

        assertThat(SlotOwnerResolver.resolveSlotOwner(
                AiTestFixtures.defaultGameState(),
                AI_ID,
                "missing-player"
        )).isEmpty();
    }
}
