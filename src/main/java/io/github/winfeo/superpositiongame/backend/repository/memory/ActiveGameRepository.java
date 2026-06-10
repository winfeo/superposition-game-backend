package io.github.winfeo.superpositiongame.backend.repository.memory;

import io.github.winfeo.superpositiongame.backend.game.model.game.GameSession;

import java.util.Collection;

public interface ActiveGameRepository {
    void save(GameSession session);
    GameSession findById(String gameId);
    Collection<GameSession> getAllGames();
    void delete(String gameId);
}
