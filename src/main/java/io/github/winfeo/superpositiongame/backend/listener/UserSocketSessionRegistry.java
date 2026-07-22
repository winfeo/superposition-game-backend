package io.github.winfeo.superpositiongame.backend.listener;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSocketSessionRegistry {
    private final Map<String, String> userBySession = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> sessionsByUser = new ConcurrentHashMap<>();

    public synchronized boolean register(String sessionId, String userId) {
        userBySession.put(sessionId, userId);

        Set<String> sessions = sessionsByUser.computeIfAbsent(
                userId,
                ignored -> ConcurrentHashMap.newKeySet()
        );
        sessions.add(sessionId);

        return sessions.size() == 1;
    }

    public synchronized DisconnectedSession unregister(String sessionId) {
        String userId = userBySession.remove(sessionId);
        if (userId == null) return null;

        Set<String> sessions = sessionsByUser.get(userId);
        if (sessions == null) {
            return new DisconnectedSession(userId, true);
        }

        sessions.remove(sessionId);
        boolean lastSession = sessions.isEmpty();

        if (lastSession) {
            sessionsByUser.remove(userId, sessions);
        }

        return new DisconnectedSession(userId, lastSession);
    }

    public synchronized boolean hasSessions(String userId) {
        Set<String> sessions = sessionsByUser.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public record DisconnectedSession(
            String userId,
            boolean lastSession
    ) { }
}

