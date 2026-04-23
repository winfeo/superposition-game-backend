package io.github.winfeo.superpositiongame.backend.game.model.move;

public record DoubleTapEffect(
        String playerId,
        String cardId
) implements Move {}

