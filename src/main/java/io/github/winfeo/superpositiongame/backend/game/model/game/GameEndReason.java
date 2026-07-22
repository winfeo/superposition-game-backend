package io.github.winfeo.superpositiongame.backend.game.model.game;

public enum GameEndReason {
    OBJECTIVE_COMPLETED,
    SURRENDER,
    RECONNECT_DECLINED,
    RECONNECT_TIMEOUT,
    BOTH_PLAYERS_DISCONNECTED
}