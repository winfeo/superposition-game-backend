package io.github.winfeo.superpositiongame.backend.game.model.card;

public record CardDescription(
        CardType type,
        AxisRotation axis,
        int actionRadius,
        boolean requiredSpecialSlot,
        Boolean isForwardRotation
) {}
