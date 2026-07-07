package io.github.winfeo.superpositiongame.backend.game.model.game;

public record TimerUpdatePacket(
        long timeLeftMs,
        long serverTimestamp
) { }