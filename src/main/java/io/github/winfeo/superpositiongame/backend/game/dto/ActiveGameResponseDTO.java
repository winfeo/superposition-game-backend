package io.github.winfeo.superpositiongame.backend.game.dto;

public record ActiveGameResponseDTO(
        boolean hasActiveGame,
        ActiveGameDTO game
) {
    public static ActiveGameResponseDTO empty() {
        return new ActiveGameResponseDTO(false, null);
    }

    public static ActiveGameResponseDTO of(ActiveGameDTO game) {
        return new ActiveGameResponseDTO(true, game);
    }
}