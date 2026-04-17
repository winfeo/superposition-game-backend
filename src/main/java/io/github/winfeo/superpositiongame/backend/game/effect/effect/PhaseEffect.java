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
public class PhaseEffect implements CardEffect {
    @Override
    public GameState apply(GameState state, Card card, int targetSlotIndex, String playerId) {
        PlayerState player = state.players().get(playerId);
        if (player == null) return state;

        CardDescription description = CardDescriptionRepository.get(card.type());
        if (description == null) return state;
        boolean forward = description.isForwardRotation();

        List<SlotState> slots = new ArrayList<>(player.slots());
        SlotState slot = slots.get(targetSlotIndex);

        Dice updatedDice = slot.dice();
        if (ArrowCompatibilityUtil.isCardCompatibleWithArrow(description, slot.dice())) {
            DiceState newState = switch (slot.dice().state()) {
                case PLUS -> forward ? DiceState.I : DiceState.I_MINUS;
                case MINUS -> forward ? DiceState.I_MINUS : DiceState.I;
                case I -> forward ? DiceState.MINUS : DiceState.PLUS;
                case I_MINUS -> forward ? DiceState.PLUS : DiceState.MINUS;
                default -> slot.dice().state();
            };

            updatedDice = slot.dice().copyWithState(newState);
        }

        SlotState updatedSlot = slot
                .copyWithDice(updatedDice)
                .copyWithAppliedCards(addCard(slot.appliedCards(), card));
        slots.set(targetSlotIndex, updatedSlot);

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
//        Set<DiceState> allowed = Set.of(DiceState.MINUS, DiceState.PLUS, DiceState.I_MINUS, DiceState.I);
//
//        return allowed.contains(state) && axis == AxisRotation.Z;
//    }

    @Override
    public boolean supports(CardType type) {
        return type == CardType.PHASE_FORWARD || type == CardType.PHASE_BACKWARD;
    }
}
