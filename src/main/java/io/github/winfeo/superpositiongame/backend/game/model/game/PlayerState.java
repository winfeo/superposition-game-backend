package io.github.winfeo.superpositiongame.backend.game.model.game;

import io.github.winfeo.superpositiongame.backend.game.model.card.Card;

import java.util.List;

//TODO добавить DTO модель
public record PlayerState(
        String id,
        String nickname,
        List<Card> hand,
        List<SlotState> slots,
        boolean skipNextTurn,
        int remainingMoves
) {
    public PlayerState(
            String id,
            String nickname
    ) {
        this(id, nickname, List.of(), List.of(), false, 1);
    }

    public PlayerState copyWithHand(List<Card> hand) {
        return new PlayerState(this.id, this.nickname, hand, this.slots, this.skipNextTurn, this.remainingMoves);
    }

    public PlayerState copyWithSlots(List<SlotState> slots) {
        return new PlayerState(this.id, this.nickname, this.hand, slots, this.skipNextTurn, this.remainingMoves);
    }

    public PlayerState copyWithRemainingMoves(int remainingMoves) {
        return new PlayerState(this.id, this.nickname, this.hand, this.slots, this.skipNextTurn, remainingMoves);
    }
}

