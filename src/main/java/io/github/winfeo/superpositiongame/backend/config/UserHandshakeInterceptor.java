package io.github.winfeo.superpositiongame.backend.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
public class UserHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
//        List<String> userIds = request.getHeaders().get("userId");
//        if (userIds != null && !userIds.isEmpty()) {
//            attributes.put("userId", userIds.get(0));
//        }
//        return true;



//        if (request instanceof ServletServerHttpRequest servletRequest) {
//            String userId = servletRequest.getServletRequest().getParameter("userId");
//            attributes.put("userId", userId);
//        }
//        return true;

        System.out.println("=== HANDSHAKE START ===");
        System.out.println("URI: " + request.getURI());

        String userId = null;

        //TODO из HTTP-заголовка не работает?
        List<String> userIds = request.getHeaders().get("userId");
        if (userIds != null && !userIds.isEmpty()) {
            userId = userIds.get(0);
            System.out.println("Got userId from header: " + userId);
        }

        //URL-параметр
        if (userId == null) {
            String query = request.getURI().getQuery();
            System.out.println("Query params: " + query);
            if (query != null && query.contains("userId=")) {
                userId = query.split("userId=")[1].split("&")[0];
                System.out.println("Got userId from URL param: " + userId);
            }
        }

        if (userId != null) {
            attributes.put("userId", userId);
            System.out.println("Saved userId to attributes: " + userId);
        } else {
            System.out.println("No userId found in handshake!");
        }

        System.out.println("=== HANDSHAKE END ===");
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        System.out.println("After handshake, exception: " + exception);
    }
}
