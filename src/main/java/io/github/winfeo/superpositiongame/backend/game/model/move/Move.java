package io.github.winfeo.superpositiongame.backend.game.model.move;

public sealed interface Move permits
        PlayCard,
        RotateDice,
        SwapDices,
        DoubleTapEffect,
        ReshuffleCard
{
    String playerId();
}
