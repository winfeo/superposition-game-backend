package io.github.winfeo.superpositiongame.backend.game.util;

import io.github.winfeo.superpositiongame.backend.game.model.card.AxisRotation;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardDescription;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceState;

import java.util.Map;
import java.util.Set;

public final class ArrowCompatibilityUtil {
    private ArrowCompatibilityUtil() {}

    private static final Map<AxisRotation, Set<DiceState>> AXIS_COMPATIBILITY = Map.of(
            AxisRotation.X, Set.of(
                    DiceState.ZERO,
                    DiceState.ONE,
                    DiceState.I,
                    DiceState.I_MINUS
            ),
            AxisRotation.Y, Set.of(
                    DiceState.ZERO,
                    DiceState.ONE,
                    DiceState.PLUS,
                    DiceState.MINUS
            ),
            AxisRotation.Z, Set.of(
                    DiceState.PLUS,
                    DiceState.MINUS,
                    DiceState.I,
                    DiceState.I_MINUS
            )
    );

    public static boolean isCardCompatibleWithArrow(
            CardDescription description,
            Dice dice
    ) {
        AxisRotation axis = description.axis();
        DiceState state = dice.state();
        Set<DiceState> allowed = AXIS_COMPATIBILITY.get(axis);

        return allowed != null && allowed.contains(state);
    }
}
