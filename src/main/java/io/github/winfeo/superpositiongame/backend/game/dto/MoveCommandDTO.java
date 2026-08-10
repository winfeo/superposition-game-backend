package io.github.winfeo.superpositiongame.backend.game.dto;

import io.github.winfeo.superpositiongame.backend.game.dto.move.MoveDto;

public record MoveCommandDTO(
        int expectedTurnNumber,
        MoveDto move
) {}
