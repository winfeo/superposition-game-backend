package io.github.winfeo.superpositiongame.backend.game.core;

import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;

@FunctionalInterface
public interface MoveHandler<T extends Move> {
    GameState handle(GameState state, T move);
}

