package io.github.winfeo.superpositiongame.backend.game.core.service;

import io.github.winfeo.superpositiongame.backend.game.model.game.GameSession;

public interface GameResultService {
    void saveGameResult(GameSession session);
}
