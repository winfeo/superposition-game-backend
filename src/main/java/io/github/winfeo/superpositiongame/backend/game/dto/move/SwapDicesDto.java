package io.github.winfeo.superpositiongame.backend.game.dto.move;

public record SwapDicesDto(
        String playerId,
        int firstSlotIndex,
        int secondSlotIndex
) implements MoveDto {}

