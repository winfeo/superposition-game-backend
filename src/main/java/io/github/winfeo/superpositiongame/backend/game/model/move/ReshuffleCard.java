package io.github.winfeo.superpositiongame.backend.game.model.move;

import java.util.List;

public record ReshuffleCard(
        String playerId,
        String cardId,
        List<String> cardsToChange
) implements Move { }
