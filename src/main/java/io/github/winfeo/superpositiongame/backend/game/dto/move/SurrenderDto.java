package io.github.winfeo.superpositiongame.backend.game.dto.move;

public record SurrenderDto(
        String playerId
) implements MoveDto {}

