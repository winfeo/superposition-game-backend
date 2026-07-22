package io.github.winfeo.superpositiongame.backend.game.core.service;

import io.github.winfeo.superpositiongame.backend.game.model.game.GameSession;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;

public interface GameService {
    void createGame(String playerA, String playerB);
    void handleMove(String gameId, Move move, String userId);
    void broadcastState(GameSession session);
    void playerReady(String gameId, String userId);
    void heartbeat(String gameId, String userId);
    void reconnect(String gameId, String userId);
    void markPlayerInactive(String gameId, String userId);
    void handlePlayerDisconnected(String userId);
    void declineReconnect(String gameId, String userId);
    void sendActiveGame(String userId);
    void processConnectionTimeouts();
}