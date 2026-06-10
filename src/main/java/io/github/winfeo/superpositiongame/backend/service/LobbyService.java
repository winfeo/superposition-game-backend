package io.github.winfeo.superpositiongame.backend.service;

import io.github.winfeo.superpositiongame.backend.entity.general.Player;

import java.util.Set;

public interface LobbyService {
    public void addPlayer(String playerId, String playerNickname);
    public void removePlayer(String userId);
    public Set<Player> getPlayers();
}
