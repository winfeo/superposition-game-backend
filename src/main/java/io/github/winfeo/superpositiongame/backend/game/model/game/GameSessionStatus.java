package io.github.winfeo.superpositiongame.backend.game.model.game;

public enum GameSessionStatus {
    WAITING_FOR_PLAYERS,
    ACTIVE,
    PAUSED_FOR_RECONNECT,
    FINISHED,
    CANCELLED
}
