package io.github.winfeo.superpositiongame.backend.repository.memory;

import io.github.winfeo.superpositiongame.backend.game.model.game.GameSession;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//TODO удалить потом, сделать нормально
@Component
public class ActiveGameRepositoryImpl implements ActiveGameRepository {
    private final Map<String, GameSession> games = new ConcurrentHashMap<>();

    @Override
    public void save(GameSession session) {
        games.put(session.getGameId(), session);
    }

    @Override
    public GameSession findById(String gameId) {
        return games.get(gameId);
    }

    @Override
    public GameSession findByPlayerId(String playerId) {
        return games.values()
                .stream()
                .filter(session -> session.containsPlayer(playerId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<GameSession> getAllGames() {
        return games.values();
    }

    @Override
    public void delete(String gameId) { games.remove(gameId); }
}
