package io.github.winfeo.superpositiongame.backend.game.effect.effect;

import io.github.winfeo.superpositiongame.backend.game.effect.CardEffect;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
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

        List<Card> previousCards = slot.appliedCards()
                .subList(0, slot.appliedCards().size() - 1);

        Dice previousDice = calculatePreviousDiceState(
                slot.initialDice(),
                previousCards
        );

        SlotState updatedSlot = slot
                .copyWithAppliedCards(previousCards)
                .copyWithDice(previousDice);

        slots.set(targetSlotIndex, updatedSlot);
        PlayerState updatedPlayer = player.copyWithSlots(slots);
        Map<String, PlayerState> updatedPlayers = new HashMap<>(state.players());
        updatedPlayers.put(playerId, updatedPlayer);

        return state.copyWithPlayers(updatedPlayers);
    }

    @Override
    public boolean supports(CardType type) {
        return false;
    }

    private Dice calculatePreviousDiceState(Dice initialDice, List<Card> appliedCards) {
        // TODO
        return initialDice;
    }
}
