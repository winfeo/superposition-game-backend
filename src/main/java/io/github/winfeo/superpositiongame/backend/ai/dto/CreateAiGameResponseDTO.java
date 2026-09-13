package io.github.winfeo.superpositiongame.backend.ai.dto;

import io.github.winfeo.superpositiongame.backend.game.model.game.AiDifficulty;

public record CreateAiGameResponseDTO(
        String gameId,
        String aiPlayerId,
        AiDifficulty difficulty
) { }
