package io.github.winfeo.superpositiongame.backend.controller;

import io.github.winfeo.superpositiongame.backend.game.core.service.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class GameReadyController {
    private final GameService gameService;

    public GameReadyController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/game/{gameId}/ready")
    public void playerReady(@DestinationVariable String gameId, Principal principal) {
        if (principal == null) return;
        String userId = principal.getName();
        gameService.playerReady(gameId, userId);
    }
}
