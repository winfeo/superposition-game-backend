package io.github.winfeo.superpositiongame.backend.game.dto.move;

public record PlayCardDto(
        String playerId,
        String cardId,
        int targetSlotIndex,
        String targetPlayerId
) implements MoveDto {}

