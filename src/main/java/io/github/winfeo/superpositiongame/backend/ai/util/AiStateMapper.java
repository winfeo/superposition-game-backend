package io.github.winfeo.superpositiongame.backend.ai.util;

import io.github.winfeo.superpositiongame.backend.ai.dto.AiCardDTO;
import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionRequestDTO;
import io.github.winfeo.superpositiongame.backend.exception.AiStateMappingException;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiDifficulty;
import io.github.winfeo.superpositiongame.backend.game.model.game.GamePhase;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotState;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiStateMapper {
    private static final int DEFAULT_MAX_MOVES = 80;

    public AiChooseActionRequestDTO toRequest(
            GameState state,
            String aiPlayerId,
            AiDifficulty difficulty
    ) {
        PlayerState aiPlayer = requirePlayer(state, aiPlayerId);
        PlayerState opponent = state.players().values().stream()
                .filter(player -> !player.id().equals(aiPlayerId))
                .findFirst()
                .orElseThrow(() -> new AiStateMappingException("В состоянии игры отсутствует противник AI"));

        Map<String, List<DiceType>> dice = new LinkedHashMap<>();
        dice.put(aiPlayer.id(), currentStates(aiPlayer));
        dice.put(opponent.id(), currentStates(opponent));

        Map<String, List<DiceType>> targets = new LinkedHashMap<>();
        targets.put(aiPlayer.id(), targetStates(aiPlayer));
        targets.put(opponent.id(), targetStates(opponent));

        Map<String, List<Boolean>> protectedSlots = new LinkedHashMap<>();
        protectedSlots.put(aiPlayer.id(), frozenSlots(aiPlayer));
        protectedSlots.put(opponent.id(), frozenSlots(opponent));

        return new AiChooseActionRequestDTO(
                difficulty.apiValue(),
                aiPlayer.id(),
                opponent.id(),
                dice,
                targets,
                cards(aiPlayer.hand()),
                cards(opponent.hand()),
                List.of(),
                List.of(),
                protectedSlots,
                state.turnNumber(),
                DEFAULT_MAX_MOVES,
                aiPlayer.remainingMoves(),
                state.currentPlayerId(),
                state.phase() == GamePhase.GAME_FINISHED || state.winnerId() != null
        );
    }

    private PlayerState requirePlayer(GameState state, String playerId) {
        PlayerState player = state.players().get(playerId);
        if (player == null) {
            throw new AiStateMappingException("В состоянии игры отсутствует AI-игрок: " + playerId);
        }
        return player;
    }

    private List<DiceType> currentStates(PlayerState player) {
        return player.slots().stream()
                .map(SlotState::dice)
                .map(dice -> dice.state())
                .toList();
    }

    private List<DiceType> targetStates(PlayerState player) {
        return player.slots().stream()
                .map(SlotState::dice)
                .map(dice -> dice.requiredState())
                .toList();
    }

    private List<Boolean> frozenSlots(PlayerState player) {
        return player.slots().stream()
                .map(SlotState::isFrozen)
                .toList();
    }

    private List<AiCardDTO> cards(List<Card> cards) {
        return cards.stream()
                .map(card -> new AiCardDTO(card.id(), card.type()))
                .toList();
    }
}
