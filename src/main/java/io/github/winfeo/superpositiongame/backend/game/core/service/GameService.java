package io.github.winfeo.superpositiongame.backend.game.core.service;

import io.github.winfeo.superpositiongame.backend.game.model.game.GameSession;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;

public interface GameService {
    void createGame(String playerA, String playerB);
    void handleMove(String gameId, Move move, String userId);
    void broadcastState(GameSession session);
}
