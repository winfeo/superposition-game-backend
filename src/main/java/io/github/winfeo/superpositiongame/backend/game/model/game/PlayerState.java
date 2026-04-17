package io.github.winfeo.superpositiongame.backend.game.model.game;

import io.github.winfeo.superpositiongame.backend.game.model.card.Card;

import java.util.List;

public record PlayerState(
        String id,
        List<Card> hand,
        List<SlotState> slots,
        boolean skipNextTurn,
        int remainingMoves
) {
    public PlayerState(String id) {
        this(id, List.of(), List.of(), false, 1);
    }

    public PlayerState copyWithHand(List<Card> hand) {
        return new PlayerState(this.id, hand, this.slots, this.skipNextTurn, this.remainingMoves);
    }

    public PlayerState copyWithSlots(List<SlotState> slots) {
        return new PlayerState(this.id, this.hand, slots, this.skipNextTurn, this.remainingMoves);
    }

    public PlayerState copyWithRemainingMoves(int remainingMoves) {
        return new PlayerState(this.id, this.hand, this.slots, this.skipNextTurn, remainingMoves);
    }
}

