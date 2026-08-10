package io.github.winfeo.superpositiongame.backend.game.effect.effect;

import io.github.winfeo.superpositiongame.backend.game.effect.CardEffect;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import org.springframework.stereotype.Component;

@Component
public class ReshuffleEffect implements CardEffect {
    @Override
    public GameState apply(GameState state, Card card, int targetSlotIndex, String playerId) {
        return state;
    }

    @Override
    public boolean supports(CardType type) {
        return type == CardType.RESHUFFLE;
    }
}

