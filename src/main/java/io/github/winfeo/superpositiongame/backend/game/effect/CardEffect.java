package io.github.winfeo.superpositiongame.backend.game.effect;

import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;

public interface CardEffect {
    GameState apply(
            GameState state,
            Card card,
            int targetSlotIndex,
            String targetPlayerId
    );

    boolean supports(CardType type);
}
