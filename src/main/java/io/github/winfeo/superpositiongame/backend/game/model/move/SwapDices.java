package io.github.winfeo.superpositiongame.backend.game.model.move;

public record SwapDices(
        String playerId,
        String cardId,
        int firstSlotIndex,
        int secondSlotIndex,
        String firstSlotOwner,
        String secondSlotOwner
) implements Move { }
