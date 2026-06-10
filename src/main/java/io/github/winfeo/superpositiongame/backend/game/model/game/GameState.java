package io.github.winfeo.superpositiongame.backend.game.model.game;

import java.util.Map;

public record GameState(
        GamePhase phase,
        String currentPlayerId,
        Map<String, PlayerState> players,
        int turnNumber,
        String activeSlotsRow, //TODO id владельца слотов? подумать как переделать
        String winnerId, //TODO передавать отдельно победителя? Сделать энам с результатами игры?
        Long serverTime,
        Long turnEndsAt //Время на ход
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
                1,
                null,
                null,
                null,
                null
        );
    }

    public GameState copyWithPlayers(Map<String, PlayerState> players) {
        return new GameState(
                this.phase,
                this.currentPlayerId,
                players,
                this.turnNumber,
                this.activeSlotsRow,
                this.winnerId,
                this.serverTime,
                this.turnEndsAt
        );
    }

    public GameState copyWithPhase(GamePhase phase) {
        return new GameState(
                phase,
                this.currentPlayerId,
                this.players,
                this.turnNumber,
                this.activeSlotsRow,
                this.winnerId,
                this.serverTime,
                this.turnEndsAt
        );
    }

    public GameState copyWithCurrentPlayerId(String currentPlayerId) {
        return new GameState(
                this.phase,
                currentPlayerId,
                this.players,
                this.turnNumber,
                this.activeSlotsRow,
                this.winnerId,
                this.serverTime,
                this.turnEndsAt
        );
    }

    public GameState copyWithTurnNumber(int turnNumber) {
        return new GameState(
                this.phase,
                this.currentPlayerId,
                this.players,
                turnNumber,
                this.activeSlotsRow,
                this.winnerId,
                this.serverTime,
                this.turnEndsAt
        );
    }

    public GameState copyWithWinnerId(String winnerId) {
        return new GameState(
                this.phase,
                this.currentPlayerId,
                this.players,
                this.turnNumber,
                this.activeSlotsRow,
                winnerId,
                this.serverTime,
                this.turnEndsAt
        );
    }

    public GameState copyWithTurnEndsAt(Long turnEndsAt) {
        return new GameState(
                this.phase,
                this.currentPlayerId,
                this.players,
                this.turnNumber,
                this.activeSlotsRow,
                this.winnerId,
                this.serverTime,
                turnEndsAt
        );
    }

    public GameState copyWithServerTime(Long serverTime) {
        return new GameState(
                this.phase,
                this.currentPlayerId,
                this.players,
                this.turnNumber,
                this.activeSlotsRow,
                this.winnerId,
                serverTime,
                this.turnEndsAt
        );
    }
}
