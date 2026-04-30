package io.github.winfeo.superpositiongame.backend.game.util;

import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;

public class SameRowUtil {
    public static boolean isMoveAllowed(GameState state, String targetPlayerId) {
        String activeRow = state.activeSlotsRow();
        if (activeRow == null) return true;

        return activeRow.equals(targetPlayerId);
    }

    public static GameState updateRowAfterMove(GameState state, String targetPlayerId) {
        if (state.activeSlotsRow() != null) return state;

        return new GameState(
                state.phase(),
                state.currentPlayerId(),
                state.players(),
                state.turnNumber(),
                targetPlayerId,
                state.winnerId()
        );
    }

    public static GameState clearRow(GameState state, int remainingMoves) {
        if (remainingMoves > 0) return state;

        return new GameState(
                state.phase(),
                state.currentPlayerId(),
                state.players(),
                state.turnNumber(),
                null,
                state.winnerId()
        );
    }

}
