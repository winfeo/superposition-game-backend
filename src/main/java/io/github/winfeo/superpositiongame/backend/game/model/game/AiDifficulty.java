package io.github.winfeo.superpositiongame.backend.game.model.game;

public enum AiDifficulty {
    LOW("low"),
    MCTS("mcts");

    private final String apiValue;

    AiDifficulty(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }
}
