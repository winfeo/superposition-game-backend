package io.github.winfeo.superpositiongame.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;

import java.util.List;
import java.util.Map;

public record AiChooseActionRequestDTO(
        String model,
        String aiPlayerId,
        String opponentPlayerId,
        Map<String, List<DiceType>> dice,
        Map<String, List<DiceType>> targets,
        List<AiCardDTO> aiHand,
        List<AiCardDTO> opponentHand,
        List<AiCardDTO> deck,
        List<AiCardDTO> discard,
        @JsonProperty("protected")
        Map<String, List<Boolean>> protectedSlots,
        int moveNumber,
        int maxMoves,
        int remainingMoves,
        String currentPlayerId,
        boolean finished
) { }
