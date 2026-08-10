package io.github.winfeo.superpositiongame.backend.game.model.game;

public record TimerSnapshot(
        int turnNumber,
        long timeLeftMs,
        long revision
) { }
