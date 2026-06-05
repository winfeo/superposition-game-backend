package io.github.winfeo.superpositiongame.backend.service.impl;

import io.github.winfeo.superpositiongame.backend.entity.general.Player;
import io.github.winfeo.superpositiongame.backend.service.LobbyService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LobbyServiceImpl implements LobbyService {
    private final Set<Player> players = ConcurrentHashMap.newKeySet();
    @Override
    public void addPlayer(
            String playerId,
            String playerNickname
    ) {
        System.out.println("addUser: " + playerId + ", nickname: " + playerNickname);
        players.add(new Player(playerId, playerNickname));
        System.out.println("USERS NOW: " + players.size());
    }

    @Override
    public void removePlayer(String userId) {
        System.out.println("removeUser: " + userId);
        players.removeIf(user -> user.getId().equals(userId));
        System.out.println("USERS AFTER REMOVE: " + players.size());
    }

    @Override
    public Set<Player> getPlayers() {
        return players;
    }
}
