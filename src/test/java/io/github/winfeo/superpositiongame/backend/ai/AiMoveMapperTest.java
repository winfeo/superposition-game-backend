package io.github.winfeo.superpositiongame.backend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.winfeo.superpositiongame.backend.ai.util.AiMoveMapper;
import io.github.winfeo.superpositiongame.backend.exception.InvalidAiResponseException;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotOwner;
import io.github.winfeo.superpositiongame.backend.game.model.move.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.AI_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiMoveMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiMoveMapper mapper = new AiMoveMapper(objectMapper);
    private final GameState state = AiTestFixtures.defaultGameState();

    @Test
    void mapsPlayCardAndResolvesOpponent() throws Exception {
        Move move = mapper.toDomain(action("""
                {
                  "type": "PLAY_CARD",
                  "playerId": "ai-player",
                  "cardId": "play-card",
                  "targetSlotIndex": 2,
                  "targetPlayerId": "human-player"
                }
                """), state, AI_ID);

        assertThat(move).isEqualTo(new PlayCard(
                AI_ID,
                "play-card",
                2,
                SlotOwner.OPPONENT.name()
        ));
    }

    @Test
    void mapsRotateDiceAndResolvesOwnSlots() throws Exception {
        Move move = mapper.toDomain(action("""
                {
                  "type": "ROTATE_DICE",
                  "playerId": "ai-player",
                  "cardId": "rotate-card",
                  "targetSlotIndex": 0,
                  "newState": "PLUS",
                  "targetPlayerId": "ai-player"
                }
                """), state, AI_ID);

        assertThat(move).isEqualTo(new RotateDice(
                AI_ID,
                "rotate-card",
                0,
                DiceType.PLUS,
                SlotOwner.PLAYER.name()
        ));
    }

    @Test
    void mapsSwapDiceOwnersFromPlayerIds() throws Exception {
        Move move = mapper.toDomain(action("""
                {
                  "type": "SWAP_DICES",
                  "playerId": "ai-player",
                  "cardId": "swap-card",
                  "firstSlotIndex": 0,
                  "secondSlotIndex": 3,
                  "firstSlotOwner": "ai-player",
                  "secondSlotOwner": "human-player"
                }
                """), state, AI_ID);

        assertThat(move).isEqualTo(new SwapDices(
                AI_ID,
                "swap-card",
                0,
                3,
                SlotOwner.PLAYER.name(),
                SlotOwner.OPPONENT.name()
        ));
    }

    @Test
    void mapsReshuffle() throws Exception {
        Move move = mapper.toDomain(action("""
                {
                  "type": "RESHUFFLE_CARD",
                  "playerId": "ai-player",
                  "cardId": "reshuffle-card",
                  "cardsToChange": ["play-card", "rotate-card"]
                }
                """), state, AI_ID);

        assertThat(move).isEqualTo(new ReshuffleCard(
                AI_ID,
                "reshuffle-card",
                List.of("play-card", "rotate-card")
        ));
    }

    @Test
    void mapsIdentityPlayCardToDoubleTapEffect() throws Exception {
        Move move = mapper.toDomain(action("""
                {
                  "type": "PLAY_CARD",
                  "playerId": "ai-player",
                  "cardId": "identity-card"
                }
                """), state, AI_ID);

        assertThat(move).isEqualTo(new DoubleTapEffect(AI_ID, "identity-card"));
    }

    @Test
    void mapsSurrender() throws Exception {
        Move move = mapper.toDomain(action("""
                {
                  "type": "SURRENDER",
                  "playerId": "ai-player"
                }
                """), state, AI_ID);

        assertThat(move).isEqualTo(new Surrender(AI_ID));
    }

    @Test
    void rejectsUnknownActionType() throws Exception {
        assertThatThrownBy(() -> mapper.toDomain(action("""
                {
                  "type": "UNKNOWN",
                  "playerId": "ai-player"
                }
                """), state, AI_ID))
                .isInstanceOf(InvalidAiResponseException.class)
                .hasMessage("AI вернул неизвестный тип хода: UNKNOWN");
    }

    @Test
    void rejectsMissingCard() throws Exception {
        assertThatThrownBy(() -> mapper.toDomain(action("""
                {
                  "type": "PLAY_CARD",
                  "playerId": "ai-player",
                  "cardId": "missing-card",
                  "targetSlotIndex": 0,
                  "targetPlayerId": "human-player"
                }
                """), state, AI_ID))
                .isInstanceOf(InvalidAiResponseException.class)
                .hasMessage("В руке AI отсутствует карта: missing-card");
    }

    @Test
    void rejectsUnexpectedActor() throws Exception {
        assertThatThrownBy(() -> mapper.toDomain(action("""
                {
                  "type": "SURRENDER",
                  "playerId": "human-player"
                }
                """), state, AI_ID))
                .isInstanceOf(InvalidAiResponseException.class)
                .hasMessage("Действие AI содержит неожиданный идентификатор игрока");
    }

    @Test
    void rejectsUnknownDiceState() throws Exception {
        assertThatThrownBy(() -> mapper.toDomain(action("""
                {
                  "type": "ROTATE_DICE",
                  "playerId": "ai-player",
                  "cardId": "rotate-card",
                  "targetSlotIndex": 0,
                  "newState": "UNKNOWN",
                  "targetPlayerId": "ai-player"
                }
                """), state, AI_ID))
                .isInstanceOf(InvalidAiResponseException.class)
                .hasMessage("AI вернул неизвестное состояние кубика: UNKNOWN");
    }

    @Test
    void rejectsOutOfRangeSlot() throws Exception {
        assertThatThrownBy(() -> mapper.toDomain(action("""
                {
                  "type": "PLAY_CARD",
                  "playerId": "ai-player",
                  "cardId": "play-card",
                  "targetSlotIndex": 4,
                  "targetPlayerId": "human-player"
                }
                """), state, AI_ID))
                .isInstanceOf(InvalidAiResponseException.class)
                .hasMessage("AI указал недопустимый индекс слота: 4");
    }

    private JsonNode action(String json) throws Exception {
        return objectMapper.readTree(json);
    }
}
