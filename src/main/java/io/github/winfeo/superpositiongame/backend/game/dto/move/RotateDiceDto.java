package io.github.winfeo.superpositiongame.backend.game.dto.move;

public record RotateDiceDto(
        String playerId,
        String cardId,
        int targetSlotIndex,
        String newState,
        String targetPlayerId
) implements MoveDto {}

