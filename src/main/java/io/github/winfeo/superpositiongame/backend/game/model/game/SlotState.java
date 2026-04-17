package io.github.winfeo.superpositiongame.backend.game.model.game;

import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;

import java.util.List;

public record SlotState(
        int index,
        String ownerId,
        Dice initialDice,
        Dice dice,
        List<Card> appliedCards,
        boolean isFrozen
) {
    public SlotState(
            int index,
            String ownerId,
            Dice dice
    ) {
        this(index, ownerId, dice, dice, List.of(), false);
    }

    public SlotState copyWithDice(Dice dice) {
        return new SlotState(this.index, this.ownerId, this.initialDice, dice, this.appliedCards, this.isFrozen);
    }

    public SlotState copyWithAppliedCards(List<Card> cards) {
        return new SlotState(this.index, this.ownerId, this.initialDice, this.dice, cards, this.isFrozen);
    }
}
