package io.github.winfeo.superpositiongame.backend.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class UserHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        System.out.println("=== DETERMINE USER ===");
        System.out.println("Attributes: " + attributes);
        String userId = (String) attributes.get("userId");
        System.out.println("userId from attributes: " + userId);

        if (userId == null || userId.isBlank()) {
            userId = "anonymous-" + System.currentTimeMillis();
            System.out.println("Using fallback userId: " + userId);
        }

        final String principalName = userId;
        System.out.println("Created Principal with name: " + principalName);
        System.out.println("=== DETERMINE USER END ===");

        return () -> principalName;
    }
}
