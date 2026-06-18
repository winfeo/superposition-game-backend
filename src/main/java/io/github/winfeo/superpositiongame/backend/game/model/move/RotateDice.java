package io.github.winfeo.superpositiongame.backend.game.model.move;

import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;

public record RotateDice(
        String playerId,
        String cardId,
        int targetSlotIndex,
        DiceType newState,
        String targetPlayerId
) implements Move { }
