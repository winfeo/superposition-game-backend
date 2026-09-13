package io.github.winfeo.superpositiongame.backend.ai.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record AiChooseActionResponseDTO(
        String model,
        JsonNode action,
        Double thinkingTimeMs,
        Integer iterations,
        Integer rootChildren,
        String neuralModel
) { }
