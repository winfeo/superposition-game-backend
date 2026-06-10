package io.github.winfeo.superpositiongame.backend.game.dto.move;

public record DoubleTapEffectDto(
        String playerId,
        String cardId
) implements MoveDto {}

