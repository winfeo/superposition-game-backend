package io.github.winfeo.superpositiongame.backend.game.core.service;

import io.github.winfeo.superpositiongame.backend.entity.db.Game;
import io.github.winfeo.superpositiongame.backend.entity.db.GamePlayer;
import io.github.winfeo.superpositiongame.backend.entity.db.User;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameSession;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.repository.GamePlayerRepository;
import io.github.winfeo.superpositiongame.backend.repository.GameRepository;
import io.github.winfeo.superpositiongame.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GameResultServiceImpl implements GameResultService {
    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void saveGameResult(GameSession session) {
        GameState finalState = session.getGameState();
        String winnerIdStr = finalState.winnerId();

        Long winnerId = null;
        if (winnerIdStr != null && !winnerIdStr.startsWith("guest-")) {
            try { winnerId = Long.parseLong(winnerIdStr); }
            catch (NumberFormatException ignored) {}
        }

        int totalMoves = finalState.turnNumber();

        Game game = new Game();
        if (winnerId != null) {
            game.setWinner(userRepository.findById(winnerId).orElse(null));
        }
        game.setTotalMoves(totalMoves);
        game = gameRepository.save(game);

        for (String playerIdStr: List.of(session.getPlayerA(), session.getPlayerB())) {
            if (playerIdStr.startsWith("guest-")) { continue; }

            Long userId;
            try { userId = Long.parseLong(playerIdStr); }
            catch (NumberFormatException e) { continue; }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;

            boolean isWinner = userId.equals(winnerId);
            int ratingChange = calculateRatingChange(isWinner);

            GamePlayer player = new GamePlayer();
            player.setGame(game);
            player.setUser(user);
            player.setRatingChange(ratingChange);
            gamePlayerRepository.save(player);

            user.setGamesPlayed(user.getGamesPlayed() + 1);
            if (isWinner) { user.setWinsAmount(user.getWinsAmount() + 1); }

            int newRating = user.getRatingPoints() + ratingChange;
            if (newRating < 0) { newRating = 0; }
            user.setRatingPoints(newRating);
            userRepository.save(user);
        }
    }

    private int calculateRatingChange(boolean isWinner) {
        int change = ThreadLocalRandom.current().nextInt(15, 41);
        return isWinner? change: -change;
    }
}