package io.github.winfeo.superpositiongame.backend.game.dto.move;

public record SwapDicesDto(
        String playerId,
        int firstSlotIndex,
        int secondSlotIndex,
        String firstSlotOwner,
        String secondSlotOwner
) implements MoveDto {}

