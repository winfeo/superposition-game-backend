package io.github.winfeo.superpositiongame.backend.service;

import io.github.winfeo.superpositiongame.backend.dto.toApp.GameHistoryDTO;

import java.util.List;

public interface GameHistoryService {
    List<GameHistoryDTO> getGameHistoryByUserId(Long userId);
}
