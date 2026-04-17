package io.github.winfeo.superpositiongame.backend.game.model.move;

public record SwapDices(
        String playerId,
        int firstSlotIndex,
        int secondSlotIndex
) implements Move { }
