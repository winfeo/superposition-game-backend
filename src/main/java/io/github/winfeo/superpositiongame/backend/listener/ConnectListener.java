package io.github.winfeo.superpositiongame.backend.listener;

import io.github.winfeo.superpositiongame.backend.service.LobbyService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.security.Principal;

@Component
public class ConnectListener {
    private final LobbyService lobbyService;
    private final LobbyEventPublisher eventPublisher;

    public ConnectListener(
            LobbyService lobbyService,
            LobbyEventPublisher eventPublisher
    ) {
        this.lobbyService = lobbyService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        System.out.println("=== CONNECT EVENT ===");
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        System.out.println("Session ID: " + accessor.getSessionId());
        System.out.println("User from Principal: " + accessor.getUser());
        System.out.println("All STOMP headers: " + accessor.toNativeHeaderMap());

        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();
        String userId = principal != null? principal.getName(): null;
//        String userId = accessor.getFirstNativeHeader("userId");

        System.out.println("userId from STOMP header: " + userId);
        System.out.println("=== CONNECT EVENT END ===");

        if (sessionId == null || userId == null) {
            System.out.println("userId is NULL or sessionId is NULL");
            return;
        }

        System.out.println("ADDING USER: " + userId);
        lobbyService.addUser(userId);
        System.out.println("sendLobbyUpdate CALLED FROM CONNECT_LISTENER");
        eventPublisher.publish();
    }
}
