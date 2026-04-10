package io.github.winfeo.superpositiongame.backend.listener;

import io.github.winfeo.superpositiongame.backend.service.LobbyService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.HashMap;
import java.util.Set;

@Component
public class ConnectListener {
    private final LobbyService lobbyService;
    private final SessionUserRegistry registry;
    private final LobbyEventPublisher eventPublisher;

    public ConnectListener(
            LobbyService lobbyService,
            SimpMessagingTemplate messagingTemplate,
            SessionUserRegistry registry,
            LobbyEventPublisher eventPublisher
    ) {
        this.lobbyService = lobbyService;
        this.registry = registry;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        System.out.println("CONNECT EVENT FIRED");
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        System.out.println("ALL HEADERS: " + accessor.toNativeHeaderMap());
        String sessionId = accessor.getSessionId();
        String userId = accessor.getFirstNativeHeader("userId");
        System.out.println("CONNECT: sessionId=" + sessionId + ", userId=" + userId);

        if (sessionId == null || userId == null) {
            System.out.println("userId is NULL or sessionId is NULL");
            return;
        }

        registry.put(sessionId, userId);
        System.out.println("ADDING USER: " + userId);
        lobbyService.addUser(userId);
        eventPublisher.publish();
    }
}
