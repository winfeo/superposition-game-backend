package io.github.winfeo.superpositiongame.backend.dto.toApp;

public record GameHistoryDTO (
        boolean isWinner,
        String opponentNickname,
        int totalMoves,
        int ratingChange,
        String playedAt
) {}
