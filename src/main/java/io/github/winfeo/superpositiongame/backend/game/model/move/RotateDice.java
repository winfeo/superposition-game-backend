package io.github.winfeo.superpositiongame.backend.game.model.move;

import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceState;

public record RotateDice(
        String playerId,
        String cardId,
        int targetSlotIndex,
        DiceState newState,
        String targetPlayerId
) implements Move { }
