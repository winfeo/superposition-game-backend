package io.github.winfeo.superpositiongame.backend.game.model.game;

public record TimerUpdatePacket(
        int turnNumber,
        long timeLeftMs,
        long revision
) { }