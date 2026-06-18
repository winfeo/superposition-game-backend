package io.github.winfeo.superpositiongame.backend.game.util;

import io.github.winfeo.superpositiongame.backend.game.model.card.AxisRotation;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardDescription;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;

import java.util.Map;
import java.util.Set;

public final class ArrowCompatibilityUtil {
    private ArrowCompatibilityUtil() {}

    private static final Map<AxisRotation, Set<DiceType>> AXIS_COMPATIBILITY = Map.of(
            AxisRotation.X, Set.of(
                    DiceType.ZERO,
                    DiceType.ONE,
                    DiceType.I,
                    DiceType.I_MINUS
            ),
            AxisRotation.Y, Set.of(
                    DiceType.ZERO,
                    DiceType.ONE,
                    DiceType.PLUS,
                    DiceType.MINUS
            ),
            AxisRotation.Z, Set.of(
                    DiceType.PLUS,
                    DiceType.MINUS,
                    DiceType.I,
                    DiceType.I_MINUS
            )
    );

    public static boolean isCardCompatibleWithArrow(
            CardDescription description,
            Dice dice
    ) {
        AxisRotation axis = description.axis();
        DiceType state = dice.state();
        Set<DiceType> allowed = AXIS_COMPATIBILITY.get(axis);

        return allowed != null && allowed.contains(state);
    }
}
