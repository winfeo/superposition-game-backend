package io.github.winfeo.superpositiongame.backend.game.dto;

import io.github.winfeo.superpositiongame.backend.game.model.game.GameSessionStatus;

public record ActiveGameDTO(
        String gameId,
        GameSessionStatus status,
        String opponentId,
        String opponentNickname,
        boolean currentPlayerDisconnected,
        Long reconnectDeadline,
        long serverTime
) { }
