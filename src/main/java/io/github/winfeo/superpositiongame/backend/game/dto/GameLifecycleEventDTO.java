package io.github.winfeo.superpositiongame.backend.game.dto;

import io.github.winfeo.superpositiongame.backend.game.model.game.GameEndReason;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameSessionStatus;

import java.util.Map;
import java.util.Set;

public record GameLifecycleEventDTO(
        String gameId,
        GameSessionStatus status,
        Set<String> disconnectedPlayerIds,
        Map<String, Long> reconnectDeadlines,
        long serverTime,
        String winnerId,
        GameEndReason endReason
) { }