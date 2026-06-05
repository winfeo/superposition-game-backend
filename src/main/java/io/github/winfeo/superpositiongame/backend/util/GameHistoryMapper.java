package io.github.winfeo.superpositiongame.backend.util;

import io.github.winfeo.superpositiongame.backend.dto.toApp.GameHistoryDTO;
import io.github.winfeo.superpositiongame.backend.entity.db.Game;
import io.github.winfeo.superpositiongame.backend.entity.db.GamePlayer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GameHistoryMapper {
    public GameHistoryDTO convertToDto(
            Game game,
            GamePlayer currentPlayer,
            GamePlayer opponent
    ) {
        boolean isWinner = game.getWinner() != null &&
                game.getWinner().getId() == currentPlayer.getUser().getId();

        String opponentNickname;
        if (opponent != null) opponentNickname = opponent.getUser().getNickname();
        else opponentNickname = "Гость";

        String playedAt = game.getPlayedAt().toString();

        return new GameHistoryDTO(
                isWinner,
                opponentNickname,
                game.getTotalMoves(),
                currentPlayer.getRatingChange(),
                playedAt
        );
    }
}