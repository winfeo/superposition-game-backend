package io.github.winfeo.superpositiongame.backend.game.dto.move;

import java.util.List;

public record ReshuffleCardDto(
        String playerId,
        String cardId,
        List<String> cardsToChange
) implements MoveDto {}

