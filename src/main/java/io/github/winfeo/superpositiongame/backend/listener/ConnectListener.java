package io.github.winfeo.superpositiongame.backend.listener;

import io.github.winfeo.superpositiongame.backend.entity.db.User;
import io.github.winfeo.superpositiongame.backend.exception.UserNotFoundException;
import io.github.winfeo.superpositiongame.backend.repository.UserRepository;
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
    private final UserRepository userRepository;

    public ConnectListener(
            LobbyService lobbyService,
            LobbyEventPublisher eventPublisher,
            UserRepository userRepository
    ) {
        this.lobbyService = lobbyService;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
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
        String playerId = principal != null? principal.getName(): null;
//        String playerId = accessor.getFirstNativeHeader("playerId");

        String playerNickname = null;
        try {
            Long userId = Long.parseLong(playerId);
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                playerNickname = user.getNickname();
            }
        } catch (NumberFormatException ignored) { }


        System.out.println("userId from STOMP header: " + playerId);
        System.out.println("=== CONNECT EVENT END ===");

        if (sessionId == null || playerId == null) {
            System.out.println("userId is NULL or sessionId is NULL");
            return;
        }

        System.out.println("ADDING USER: " + playerId + ", " + playerNickname);
        lobbyService.addPlayer(playerId, playerNickname);
        System.out.println("sendLobbyUpdate CALLED FROM CONNECT_LISTENER");
        eventPublisher.publish();
    }
}
