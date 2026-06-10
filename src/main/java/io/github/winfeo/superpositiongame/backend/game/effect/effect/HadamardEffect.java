package io.github.winfeo.superpositiongame.backend.game.effect.effect;

import io.github.winfeo.superpositiongame.backend.game.effect.CardEffect;
import io.github.winfeo.superpositiongame.backend.game.model.card.*;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceState;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotState;
import io.github.winfeo.superpositiongame.backend.game.util.ArrowCompatibilityUtil;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.github.winfeo.superpositiongame.backend.game.util.AddCardsUtil.addCard;

@Component
public class HadamardEffect implements CardEffect {
    @Override
    public GameState apply(GameState state, Card card, int targetSlotIndex, String playerId) {
        PlayerState player = state.players().get(playerId);
        if (player == null) return state;

        CardDescription description = CardDescriptionRepository.get(card.type());
        if (description == null) return state;

        List<SlotState> slots = new ArrayList<>(player.slots());
        System.out.println("=== HADAMARD EFFECT 3 ===");
        System.out.println("Радиус действия: " + description.actionRadius());
        List<Integer> indexes = description.actionRadius() == 3
                ? List.of(targetSlotIndex - 1, targetSlotIndex, targetSlotIndex + 1)
                : List.of(targetSlotIndex);

        for (Integer index: indexes) {
            if (index < 0 || index >= slots.size()) continue;

            SlotState slot = slots.get(index);
            SlotState updatedSlot = slot;

            if (!slot.isFrozen() /*&& ArrowCompatibilityUtil.isCardCompatibleWithArrow(description, slot.dice())*/) {
                DiceState newState = switch (slot.dice().state()) {
                    case ZERO -> DiceState.PLUS;
                    case ONE -> DiceState.MINUS;
                    case PLUS -> DiceState.ZERO;
                    case MINUS -> DiceState.ONE;
                    case I -> DiceState.I_MINUS;
                    case I_MINUS -> DiceState.I;
                };

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

//    private boolean isCardCompatibleWithArrow(
//            CardDescription description,
//            Dice dice
//    ) {
//        AxisRotation axis = description.axis();
//        DiceState state = dice.state();
//
//        Set<DiceState> allowed = Set.of(DiceState.ZERO, DiceState.ONE, DiceState.PLUS, DiceState.MINUS);
//
//        return allowed.contains(state) && axis == AxisRotation.Y;
//    }

    @Override
    public boolean supports(CardType type) {
        return type == CardType.HADAMARD || type == CardType.HADAMARD_3;
    }
}

