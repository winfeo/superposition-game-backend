package io.github.winfeo.superpositiongame.backend.dto;

import io.github.winfeo.superpositiongame.backend.entity.general.Player;

import java.util.Set;
import java.util.stream.Collectors;

public class LobbyResponseDTO {
    private Set<PlayerDTO> players;

    public LobbyResponseDTO(Set<Player> players) {
        this.players = players.stream()
                .map(player -> new PlayerDTO(
                        player.getId(),
                        player.getNickname()
                ))
                .collect(Collectors.toSet());
    }

    public Set<PlayerDTO> getPlayers() {
        return players;
    }
}
