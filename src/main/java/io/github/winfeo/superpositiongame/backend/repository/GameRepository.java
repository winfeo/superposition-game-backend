package io.github.winfeo.superpositiongame.backend.repository;

import io.github.winfeo.superpositiongame.backend.game.model.game.GameSession;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;

import java.util.Collection;

public interface GameRepository {
    void save(GameSession session);
    GameSession findById(String gameId);
    Collection<GameSession> getAllGames();
}
