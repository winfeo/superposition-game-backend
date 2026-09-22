package io.github.winfeo.superpositiongame.backend.game.effect.effect;

import io.github.winfeo.superpositiongame.backend.game.effect.CardEffect;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NoiseEffect implements CardEffect {
    @Override
    public GameState apply(GameState state, Card card, int targetSlotIndex, String playerId) {
        PlayerState player = state.players().get(playerId);
        if (player == null) return state;

        List<SlotState> slots = new ArrayList<>(player.slots());
        SlotState slot = slots.get(targetSlotIndex);
        if (slot.appliedCards().isEmpty()) return state;

        Card cancelledCard = slot.appliedCards().get(slot.appliedCards().size() - 1);
        List<Card> previousCards = new ArrayList<>(
                slot.appliedCards().subList(0, slot.appliedCards().size() - 1)
        );
        Dice previousDice = slot.dice().copyWithState(
                calculatePreviousState(slot, cancelledCard)
        );
        boolean isFrozen = cancelledCard.type() == CardType.MEASUREMENT
                ? false
                : slot.isFrozen();

        SlotState updatedSlot = new SlotState(
                slot.index(),
                slot.ownerId(),
                slot.initialDice(),
                previousDice,
                previousCards,
                isFrozen
        );

        slots.set(targetSlotIndex, updatedSlot);
        PlayerState updatedPlayer = player.copyWithSlots(slots);
        Map<String, PlayerState> updatedPlayers = new HashMap<>(state.players());
        updatedPlayers.put(playerId, updatedPlayer);

        return state.copyWithPlayers(updatedPlayers);
    }

    @Override
    public boolean supports(CardType type) {
        return type == CardType.QUANTUM_NOISE;
    }

    private DiceType calculatePreviousState(SlotState slot, Card cancelledCard) {
        DiceType currentState = slot.dice().state();

        return switch (cancelledCard.type()) {
            case PAULI_X -> rollbackPauliX(currentState);
            case PAULI_Y -> rollbackPauliY(currentState);
            case PAULI_Z -> rollbackPauliZ(currentState);
            case HADAMARD -> rollbackHadamard(currentState);
            case PHASE_FORWARD -> rollbackPhase(currentState, false);
            case PHASE_BACKWARD -> rollbackPhase(currentState, true);
            case MEASUREMENT -> currentState;
            default -> currentState;
        };
    }

    private DiceType rollbackPauliX(DiceType state) {
        return switch (state) {
            case ZERO -> DiceType.ONE;
            case ONE -> DiceType.ZERO;
            case I -> DiceType.I_MINUS;
            case I_MINUS -> DiceType.I;
            default -> state;
        };
    }

    private DiceType rollbackPauliY(DiceType state) {
        return switch (state) {
            case ZERO -> DiceType.ONE;
            case ONE -> DiceType.ZERO;
            case PLUS -> DiceType.MINUS;
            case MINUS -> DiceType.PLUS;
            default -> state;
        };
    }

    private DiceType rollbackPauliZ(DiceType state) {
        return switch (state) {
            case PLUS -> DiceType.MINUS;
            case MINUS -> DiceType.PLUS;
            case I -> DiceType.I_MINUS;
            case I_MINUS -> DiceType.I;
            default -> state;
        };
    }

    private DiceType rollbackHadamard(DiceType state) {
        return switch (state) {
            case ZERO -> DiceType.PLUS;
            case ONE -> DiceType.MINUS;
            case PLUS -> DiceType.ZERO;
            case MINUS -> DiceType.ONE;
            case I -> DiceType.I_MINUS;
            case I_MINUS -> DiceType.I;
        };
    }

    private DiceType rollbackPhase(DiceType state, boolean forward) {
        return switch (state) {
            case PLUS -> forward ? DiceType.I : DiceType.I_MINUS;
            case MINUS -> forward ? DiceType.I_MINUS : DiceType.I;
            case I -> forward ? DiceType.MINUS : DiceType.PLUS;
            case I_MINUS -> forward ? DiceType.PLUS : DiceType.MINUS;
            default -> state;
        };
    }
}
