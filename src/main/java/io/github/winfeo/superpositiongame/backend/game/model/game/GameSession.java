package io.github.winfeo.superpositiongame.backend.game.model.game;

public class GameSession {
    private final String gameId;
    private final String playerId_A;
    private final String playerId_B;
    private GameState gameState;

    public GameSession(
            String gameId,
            String playerId_A,
            String playerId_B,
            GameState gameState
    ) {
        this.gameId = gameId;
        this.playerId_A = playerId_A;
        this.playerId_B = playerId_B;
        this.gameState = gameState;
    }

    public String getGameId() { return gameId; }
    public String getPlayerA() { return playerId_A; }
    public String getPlayerB() { return playerId_B; }
    public GameState getGameState() { return gameState; }
    public void updateGameState(GameState gameState) { this.gameState = gameState; }
}
