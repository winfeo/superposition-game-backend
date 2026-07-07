package io.github.winfeo.superpositiongame.backend.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ServerPingController {
    private final SimpMessagingTemplate messagingTemplate;

    public ServerPingController(
            SimpMessagingTemplate simpMessagingTemplate
    ) {
        this.messagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/ping")
    public void handlePing(Principal principal) {
        String userId = principal.getName();
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/pong",
                System.currentTimeMillis()
        );
    }
}
