package io.github.winfeo.superpositiongame.backend.listener;

import io.github.winfeo.superpositiongame.backend.service.LobbyService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;

@Component
public class DisconnectListener {
    private final LobbyService lobbyService;
    private final SessionUserRegistry registry;
    private final LobbyEventPublisher eventPublisher;

    public DisconnectListener(
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
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        System.out.println("DISCONNECT: sessionId=" + sessionId);
        String userId = registry.remove(sessionId);
        System.out.println("RESOLVED userId=" + userId);

        if (userId == null) return;
        lobbyService.removeUser(userId);
        eventPublisher.publish();
    }
}
