package io.github.winfeo.superpositiongame.backend.game.effect.effect;

import io.github.winfeo.superpositiongame.backend.game.effect.CardEffect;
import io.github.winfeo.superpositiongame.backend.game.model.card.*;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotState;
import io.github.winfeo.superpositiongame.backend.game.util.ArrowCompatibilityUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.winfeo.superpositiongame.backend.game.util.AddCardsUtil.addCard;

@Component
public class PauliEffect implements CardEffect {
    @Override
    public GameState apply(GameState state, Card card, int targetSlotIndex, String playerId) {
        System.out.println("=== APPLY EFFECT ===");
        System.out.println("Карта: " + card.type().name());
        System.out.println("Таргет игрок: " + playerId);
        System.out.println("Таргет слот: " + targetSlotIndex);

        PlayerState player = state.players().get(playerId);
        if (player == null) return state;

        CardDescription description = CardDescriptionRepository.get(card.type());
        if (description == null) return state;

        List<SlotState> slots = new ArrayList<>(player.slots());
        List<Integer> indexes = description.actionRadius() == 3
                ? List.of(targetSlotIndex - 1, targetSlotIndex, targetSlotIndex + 1)
                : List.of(targetSlotIndex);

        for (Integer index: indexes) {
            if (index < 0 || index >= slots.size()) continue;

            SlotState slot = slots.get(index);
            SlotState updatedSlot = slot;

            //Если на кубите нет соотвествующей оси (стрелки) не обновляем состояние дайса
            if (!slot.isFrozen() && ArrowCompatibilityUtil.isCardCompatibleWithArrow(description, slot.dice())) {
                DiceType newState = calculateNewState(slot.dice().state(), description.axis());
                Dice updatedDice = slot.dice().copyWithState(newState);
                updatedSlot = slot.copyWithDice(updatedDice);
            }

            if (index == targetSlotIndex) {
                List<Card> appliedCards = addCard(updatedSlot.appliedCards(), card);
                updatedSlot = updatedSlot.copyWithAppliedCards(appliedCards);
            }

            slots.set(index, updatedSlot);
        }

        PlayerState updatedPlayer = player.copyWithSlots(slots);
        Map<String, PlayerState> updatedPlayers = new HashMap<>(state.players());
        updatedPlayers.put(playerId, updatedPlayer);

        return state.copyWithPlayers(updatedPlayers);
    }

    @Override
    public boolean supports(CardType type) {
        return type == CardType.PAULI_X
                || type == CardType.PAULI_X_3
                || type == CardType.PAULI_Y
                || type == CardType.PAULI_Y_3
                || type == CardType.PAULI_Z
                || type == CardType.PAULI_Z_3;
    }

    private DiceType calculateNewState(DiceType state, AxisRotation axis) {
        return switch (axis) {
            case X -> switch (state) {
                case ZERO -> DiceType.ONE;
                case ONE -> DiceType.ZERO;
                case I -> DiceType.I_MINUS;
                case I_MINUS -> DiceType.I;
                default -> state;
            };
            case Y -> switch (state) {
                case PLUS -> DiceType.MINUS;
                case MINUS -> DiceType.PLUS;
                case ZERO -> DiceType.ONE;
                case ONE -> DiceType.ZERO;
                default -> state;
            };
            case Z -> switch (state) {
                case PLUS -> DiceType.MINUS;
                case MINUS -> DiceType.PLUS;
                case I -> DiceType.I_MINUS;
                case I_MINUS -> DiceType.I;
                default -> state;
            };
        };
    }
}
