package io.github.winfeo.superpositiongame.backend.controller;

import io.github.winfeo.superpositiongame.backend.dto.LobbyResponseDto;
import io.github.winfeo.superpositiongame.backend.entity.User;
import io.github.winfeo.superpositiongame.backend.listener.LobbyEventPublisher;
import io.github.winfeo.superpositiongame.backend.service.LobbyService;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.Set;


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
