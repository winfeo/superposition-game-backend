package io.github.winfeo.superpositiongame.backend.game.effect.effect;

import io.github.winfeo.superpositiongame.backend.game.effect.CardEffect;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MeasurementEffect implements CardEffect {
    @Override
    public GameState apply(GameState state, Card card, int targetSlotIndex, String playerId) {
        PlayerState player = state.players().get(playerId);
        if (player == null) return state;

        List<SlotState> slots = new ArrayList<>(player.slots());
        SlotState slot = slots.get(targetSlotIndex);

        List<Card> appliedCards = new ArrayList<>(slot.appliedCards());
        appliedCards.add(card);

        SlotState updatedSlot = new SlotState(
                slot.index(),
                slot.ownerId(),
                slot.initialDice(),
                slot.dice(),
                appliedCards,
                true
        );

        slots.set(targetSlotIndex, updatedSlot);
        PlayerState updatedPlayer = player.copyWithSlots(slots);
        Map<String, PlayerState> updatedPlayers = new HashMap<>(state.players());
        updatedPlayers.put(playerId, updatedPlayer);

        return state.copyWithPlayers(updatedPlayers);
    }

    @Override
    public boolean supports(CardType type) {
        return type == CardType.MEASUREMENT;
    }
}
