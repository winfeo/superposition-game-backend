package io.github.winfeo.superpositiongame.backend.game.model.game;

import io.github.winfeo.superpositiongame.backend.exception.AiConfigurationException;

// Сессия игрока с AI
// playerA - человек
// playerB - AI
public final class AiGameSession extends GameSession {
    private final AiDifficulty difficulty;
    private boolean requestInProgress;

    public AiGameSession(
            String gameId,
            String humanPlayerId,
            String aiPlayerId,
            GameState gameState,
            AiDifficulty difficulty
    ) {
        super(
                gameId,
                humanPlayerId,
                aiPlayerId,
                gameState,
                GameMode.AI
        );
        if (difficulty == null) {
            throw new AiConfigurationException("Сложность AI должна быть указана");
        }
        this.difficulty = difficulty;
    }

    public String getHumanPlayerId() {
        return getPlayerA();
    }

    public String getAiPlayerId() {
        return getPlayerB();
    }

    public AiDifficulty getDifficulty() {
        return difficulty;
    }

    public synchronized boolean isRequestInProgress() {
        return requestInProgress;
    }

    public synchronized void setRequestInProgress(boolean requestInProgress) {
        this.requestInProgress = requestInProgress;
    }
}
