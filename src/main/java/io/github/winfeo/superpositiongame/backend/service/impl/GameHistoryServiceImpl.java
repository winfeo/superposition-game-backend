package io.github.winfeo.superpositiongame.backend.service.impl;

import io.github.winfeo.superpositiongame.backend.dto.toApp.GameHistoryDTO;
import io.github.winfeo.superpositiongame.backend.entity.db.Game;
import io.github.winfeo.superpositiongame.backend.entity.db.GamePlayer;
import io.github.winfeo.superpositiongame.backend.exception.PlayerNotFoundException;
import io.github.winfeo.superpositiongame.backend.exception.UserNotFoundException;
import io.github.winfeo.superpositiongame.backend.repository.GamePlayerRepository;
import io.github.winfeo.superpositiongame.backend.repository.GameRepository;
import io.github.winfeo.superpositiongame.backend.repository.UserRepository;
import io.github.winfeo.superpositiongame.backend.service.GameHistoryService;
import io.github.winfeo.superpositiongame.backend.util.GameHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameHistoryServiceImpl implements GameHistoryService {
    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final UserRepository userRepository;
    @Transactional(readOnly = true)
    public List<GameHistoryDTO> getGameHistoryByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id: " + userId + " не найден"));

        List<Game> games = gameRepository.findAllGamesByPlayerId(userId);

        return games.stream()
                .map(game -> {
                    GamePlayer currentPlayer = gamePlayerRepository
                            .findByGameIdAndPlayerId(game.getId(), userId)
                            .orElseThrow(() -> new PlayerNotFoundException("Игрок с id: " + userId + " не найден в игре"));

                    //TODO подумать, как лучше хранить всех участников игры
                    GamePlayer opponent = gamePlayerRepository
                            .findOpponentByGameIdAndPlayerId(game.getId(), userId)
                            .orElse(null); //если просто гость - нет зареганого акка

                    return GameHistoryMapper.convertToDto(game, currentPlayer, opponent);
                })
                .collect(Collectors.toList());
    }
}
