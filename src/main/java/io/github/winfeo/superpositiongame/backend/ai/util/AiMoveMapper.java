package io.github.winfeo.superpositiongame.backend.ai.util;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.winfeo.superpositiongame.backend.exception.AiStateMappingException;
import io.github.winfeo.superpositiongame.backend.exception.InvalidAiResponseException;
import io.github.winfeo.superpositiongame.backend.game.dto.move.*;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotOwner;
import io.github.winfeo.superpositiongame.backend.game.model.move.*;
import io.github.winfeo.superpositiongame.backend.game.util.SlotOwnerResolver;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AiMoveMapper {
    private static final Set<CardType> ROTATE_TYPES = Set.of(
            CardType.ROTATE_X,
            CardType.ROTATE_Y,
            CardType.ROTATE_Z
    );
    private static final Set<CardType> DOUBLE_TAP_TYPES = Set.of(
            CardType.IDENTITY,
            CardType.KRONECKER_MULTIPLICATION
    );

    private final ObjectMapper objectMapper;

    public AiMoveMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
                .addMixIn(MoveDto.class, MoveDtoWithoutTypeInfoMixin.class);
    }

    public Move toDomain(
            JsonNode action,
            GameState state,
            String aiPlayerId
    ) {
        if (action == null || !action.isObject()) {
            throw new InvalidAiResponseException("Ответ AI не содержит объекта хода");
        }

        ObjectNode payload = ((ObjectNode) action).deepCopy();
        JsonNode typeNode = payload.remove("type");

        if (typeNode == null || typeNode.asText().isBlank()) {
            throw new InvalidAiResponseException("В ответе AI отсутствует тип хода");
        }

        String type = typeNode.asText();
        return switch (type) {
            case "PLAY_CARD" -> fromPlayCard(read(payload, PlayCardDto.class), state, aiPlayerId);
            case "ROTATE_DICE" -> fromRotateDice(read(payload, RotateDiceDto.class), state, aiPlayerId);
            case "SWAP_DICES" -> fromSwapDices(read(payload, SwapDicesDto.class), state, aiPlayerId);
            case "RESHUFFLE_CARD" -> fromReshuffle(read(payload, ReshuffleCardDto.class), state, aiPlayerId);
            case "DOUBLE_TAP" -> fromDoubleTap(read(payload, DoubleTapEffectDto.class), state, aiPlayerId);
            case "SURRENDER" -> fromSurrender(read(payload, SurrenderDto.class), aiPlayerId);
            default -> throw new InvalidAiResponseException("AI вернул неизвестный тип хода: " + type);
        };
    }

    private Move fromPlayCard(PlayCardDto dto, GameState state, String aiPlayerId) {
        validateActor(dto.playerId(), aiPlayerId);
        Card card = requireCard(state, aiPlayerId, dto.cardId());

        if (DOUBLE_TAP_TYPES.contains(card.type())) {
            return new DoubleTapEffect(aiPlayerId, card.id());
        }
        if (ROTATE_TYPES.contains(card.type())
                || card.type() == CardType.SWAP
                || card.type() == CardType.RESHUFFLE) {
            throw new InvalidAiResponseException("Тип действия AI не соответствует типу карты: " + card.type());
        }

        return new PlayCard(
                aiPlayerId,
                card.id(),
                requireSlotIndex(dto.targetSlotIndex()),
                owner(state, aiPlayerId, dto.targetPlayerId()).name()
        );
    }

    private Move fromRotateDice(RotateDiceDto dto, GameState state, String aiPlayerId) {
        validateActor(dto.playerId(), aiPlayerId);
        Card card = requireCard(state, aiPlayerId, dto.cardId());
        if (!ROTATE_TYPES.contains(card.type())) {
            throw new InvalidAiResponseException("Для действия ROTATE_DICE требуется карта поворота");
        }

        final DiceType newState;
        try {
            newState = DiceType.valueOf(dto.newState());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidAiResponseException(
                    "AI вернул неизвестное состояние кубика: " + dto.newState(),
                    exception
            );
        }

        return new RotateDice(
                aiPlayerId,
                card.id(),
                requireSlotIndex(dto.targetSlotIndex()),
                newState,
                owner(state, aiPlayerId, dto.targetPlayerId()).name()
        );
    }

    private Move fromSwapDices(SwapDicesDto dto, GameState state, String aiPlayerId) {
        validateActor(dto.playerId(), aiPlayerId);
        Card card = requireCard(state, aiPlayerId, dto.cardId());
        if (card.type() != CardType.SWAP) {
            throw new InvalidAiResponseException("Для действия SWAP_DICES требуется карта SWAP");
        }

        return new SwapDices(
                aiPlayerId,
                card.id(),
                requireSlotIndex(dto.firstSlotIndex()),
                requireSlotIndex(dto.secondSlotIndex()),
                owner(state, aiPlayerId, dto.firstSlotOwner()).name(),
                owner(state, aiPlayerId, dto.secondSlotOwner()).name()
        );
    }

    private Move fromReshuffle(ReshuffleCardDto dto, GameState state, String aiPlayerId) {
        validateActor(dto.playerId(), aiPlayerId);
        Card card = requireCard(state, aiPlayerId, dto.cardId());
        if (card.type() != CardType.RESHUFFLE) {
            throw new InvalidAiResponseException("Для действия RESHUFFLE_CARD требуется карта RESHUFFLE");
        }
        if (dto.cardsToChange() == null) {
            throw new InvalidAiResponseException("В действии RESHUFFLE_CARD отсутствует список заменяемых карт");
        }

        return new ReshuffleCard(aiPlayerId, card.id(), dto.cardsToChange());
    }

    private Move fromDoubleTap(DoubleTapEffectDto dto, GameState state, String aiPlayerId) {
        validateActor(dto.playerId(), aiPlayerId);
        Card card = requireCard(state, aiPlayerId, dto.cardId());
        if (!DOUBLE_TAP_TYPES.contains(card.type())) {
            throw new InvalidAiResponseException("Действие DOUBLE_TAP не поддерживается для карты: " + card.type());
        }
        return new DoubleTapEffect(aiPlayerId, card.id());
    }

    private Move fromSurrender(SurrenderDto dto, String aiPlayerId) {
        validateActor(dto.playerId(), aiPlayerId);
        return new Surrender(aiPlayerId);
    }

    private Card requireCard(GameState state, String aiPlayerId, String cardId) {
        PlayerState aiPlayer = state.players().get(aiPlayerId);
        if (aiPlayer == null) {
            throw new AiStateMappingException("В состоянии игры отсутствует AI-игрок: " + aiPlayerId);
        }

        return aiPlayer.hand().stream()
                .filter(card -> card.id().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new InvalidAiResponseException("В руке AI отсутствует карта: " + cardId));
    }

    private SlotOwner owner(GameState state, String aiPlayerId, String targetPlayerId) {
        return SlotOwnerResolver.resolveSlotOwner(state, aiPlayerId, targetPlayerId)
                .orElseThrow(() -> new InvalidAiResponseException("AI указал неизвестного целевого игрока: " + targetPlayerId));
    }

    private int requireSlotIndex(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= 4) {
            throw new InvalidAiResponseException("AI указал недопустимый индекс слота: " + slotIndex);
        }
        return slotIndex;
    }

    private void validateActor(String playerId, String aiPlayerId) {
        if (!aiPlayerId.equals(playerId)) {
            throw new InvalidAiResponseException("Действие AI содержит неожиданный идентификатор игрока");
        }
    }

    private <T> T read(ObjectNode payload, Class<T> type) {
        try {
            return objectMapper.treeToValue(payload, type);
        } catch (JsonProcessingException exception) {
            throw new InvalidAiResponseException(
                    "Не удалось преобразовать действие AI в " + type.getSimpleName(),
                    exception
            );
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private interface MoveDtoWithoutTypeInfoMixin { }
}
