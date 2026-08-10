package io.github.winfeo.superpositiongame.backend.game.model.dice;

public record Dice(
        String id,
        DiceType state,
        DiceType requiredState
) {
    public Dice copyWithState(DiceType newState) {
        return new Dice(this.id, newState, this.requiredState);
    }
}
