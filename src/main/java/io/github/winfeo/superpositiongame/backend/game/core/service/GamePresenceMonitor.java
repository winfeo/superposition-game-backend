package io.github.winfeo.superpositiongame.backend.game.core.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GamePresenceMonitor {
    private final GameService gameService;

    public GamePresenceMonitor(GameService gameService) {
        this.gameService = gameService;
    }

    @Scheduled(fixedRateString = "${game.presence.monitor-interval-ms:1000}")
    public void checkConnections() {
        gameService.processConnectionTimeouts();
    }
}
