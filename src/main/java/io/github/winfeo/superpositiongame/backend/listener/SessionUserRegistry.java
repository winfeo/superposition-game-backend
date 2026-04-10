package io.github.winfeo.superpositiongame.backend.listener;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionUserRegistry {
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    public void put(String sessionId, String userId) {
        sessionUserMap.put(sessionId, userId);
    }

    public String remove(String sessionId) {
        return sessionUserMap.remove(sessionId);
    }

    public String get(String sessionId) {
        return sessionUserMap.get(sessionId);
    }
}
