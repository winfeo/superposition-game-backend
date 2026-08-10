package io.github.winfeo.superpositiongame.backend.game.core;

import io.github.winfeo.superpositiongame.backend.game.core.service.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class GamePresenceController {
    private final GameService gameService;

    public GamePresenceController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/game/active")
    public void getActiveGame(Principal principal) {
        if (principal == null) return;
        gameService.sendActiveGame(principal.getName());
    }

    @MessageMapping("/game/{gameId}/heartbeat")
    public void heartbeat(
            @DestinationVariable String gameId,
            Principal principal
    ) {
        if (principal == null) return;
        gameService.heartbeat(gameId, principal.getName());
    }

    @MessageMapping("/game/{gameId}/reconnect")
    public void reconnect(
            @DestinationVariable String gameId,
            Principal principal
    ) {
        if (principal == null) return;
        gameService.reconnect(gameId, principal.getName());
    }

    @MessageMapping("/game/{gameId}/inactive")
    public void markInactive(
            @DestinationVariable String gameId,
            Principal principal
    ) {
        if (principal == null) return;
        gameService.markPlayerInactive(gameId, principal.getName());
    }

    @MessageMapping("/game/{gameId}/reconnect.decline")
    public void declineReconnect(
            @DestinationVariable String gameId,
            Principal principal
    ) {
        if (principal == null) return;
        gameService.declineReconnect(gameId, principal.getName());
    }
}