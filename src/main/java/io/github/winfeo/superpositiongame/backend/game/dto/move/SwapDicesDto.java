package io.github.winfeo.superpositiongame.backend.game.dto.move;

public record SwapDicesDto(
        String playerId,
        String cardId,
        int firstSlotIndex,
        int secondSlotIndex,
        String firstSlotOwner,
        String secondSlotOwner
) implements MoveDto {}

