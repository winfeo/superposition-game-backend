package io.github.winfeo.superpositiongame.backend.controller;

import io.github.winfeo.superpositiongame.backend.listener.LobbyEventPublisher;
import io.github.winfeo.superpositiongame.backend.service.LobbyService;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;


@Controller
public class LobbyController {
    private final LobbyEventPublisher eventPublisher;

    public LobbyController(
            LobbyService lobbyService,
            LobbyEventPublisher eventPublisher
    ) {
        this.eventPublisher = eventPublisher;
    }

    @MessageMapping("/lobby")
    public void getLobbyUsers() {
        eventPublisher.publish();
    }
}
