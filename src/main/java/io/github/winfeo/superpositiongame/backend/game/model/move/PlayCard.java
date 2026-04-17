package io.github.winfeo.superpositiongame.backend.game.model.move;

public record PlayCard(
        String playerId,
        String cardId,
        int targetSlotIndex,
        String targetPlayerId
) implements Move { }
