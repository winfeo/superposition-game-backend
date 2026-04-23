package io.github.winfeo.superpositiongame.backend.game.effect.effect;

import io.github.winfeo.superpositiongame.backend.game.effect.CardEffect;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MultiplicationEffect implements CardEffect {
    @Override
    public GameState apply(GameState state, Card card, int targetSlotIndex, String playerId) {
        PlayerState player = state.players().get(playerId);
        if (player == null) return state;

        PlayerState updatedPlayer = new PlayerState(
                player.id(),
                player.hand(),
                player.slots(),
                player.skipNextTurn(),
                player.remainingMoves() + 2
        );

        Map<String, PlayerState> updatedPlayers = new HashMap<>(state.players());
        updatedPlayers.put(playerId, updatedPlayer);

        System.out.println("=== HADAMARD EFFECT 3 ===");
        System.out.println("Игрок: " + updatedPlayer.id());
        System.out.println("Число ходов игрока: " + updatedPlayer.remainingMoves());
        System.out.println("=========================");


//        return state.copyWithPlayers(updatedPlayers);
        return new GameState(
                state.phase(),
                state.currentPlayerId(),
                updatedPlayers,
                state.turnNumber(),
                null
        );
    }

    @Override
    public boolean supports(CardType type) {
        return type == CardType.KRONECKER_MULTIPLICATION;
    }
}
