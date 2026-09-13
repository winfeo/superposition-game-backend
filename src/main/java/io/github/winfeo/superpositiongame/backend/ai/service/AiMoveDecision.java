package io.github.winfeo.superpositiongame.backend.ai.service;

import io.github.winfeo.superpositiongame.backend.game.model.move.Move;

public record AiMoveDecision(
        Move move,
        boolean fallback,
        String reason
) {
    public static AiMoveDecision remote(Move move) {
        return new AiMoveDecision(move, false, null);
    }

    public static AiMoveDecision fallback(Move move, String reason) {
        return new AiMoveDecision(move, true, reason);
    }
}
