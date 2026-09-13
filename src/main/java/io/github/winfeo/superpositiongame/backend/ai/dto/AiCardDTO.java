package io.github.winfeo.superpositiongame.backend.ai.dto;

import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;

public record AiCardDTO(
        String id,
        CardType type
) { }
