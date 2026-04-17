package io.github.winfeo.superpositiongame.backend.game.model.game;

import java.util.Map;

public record GameState(
        GamePhase phase,
        String currentPlayerId,
        Map<String, PlayerState> players,
        int turnNumber,
        String activeSlotsRow //TODO id владельца слотов? подумать как переделать
) {
    public static GameState initial(
            PlayerState playerA,
            PlayerState playerB
    ) {
        return new GameState(
                GamePhase.GAME_SETUP,
                playerA.id(),
                Map.of(
                        playerA.id(), playerA,
                        playerB.id(), playerB
                ),
                0,
                null
        );
    }

    public GameState copyWithPlayers(Map<String, PlayerState> players) {
        return new GameState(
                this.phase,
                this.currentPlayerId,
                players,
                this.turnNumber,
                this.activeSlotsRow
        );
    }

    public GameState copyWithPhase(GamePhase phase) {
        return new GameState(
                phase,
                this.currentPlayerId,
                this.players,
                this.turnNumber,
                this.activeSlotsRow
        );
    }

    public GameState copyWithCurrentPlayerId(String currentPlayerId) {
        return new GameState(
                this.phase,
                currentPlayerId,
                this.players,
                this.turnNumber,
                this.activeSlotsRow
        );
    }

    public GameState copyWithTurnNumber(int turnNumber) {
        return new GameState(
                this.phase,
                this.currentPlayerId,
                this.players,
                turnNumber,
                this.activeSlotsRow
        );
    }
}
