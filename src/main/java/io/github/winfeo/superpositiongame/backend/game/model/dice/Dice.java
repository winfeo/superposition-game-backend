package io.github.winfeo.superpositiongame.backend.game.model.dice;

public record Dice(
        String id,
        DiceState state,
        DiceState requiredState
) {
    public Dice copyWithState(DiceState newState) {
        return new Dice(this.id, newState, this.requiredState);
    }
}
