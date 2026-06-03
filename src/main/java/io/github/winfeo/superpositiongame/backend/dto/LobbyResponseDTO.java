package io.github.winfeo.superpositiongame.backend.dto;

import io.github.winfeo.superpositiongame.backend.entity.general.LobbyUser;

import java.util.Set;
import java.util.stream.Collectors;

public class LobbyResponseDTO {
    private Set<LobbyUserDTO> users;

    public LobbyResponseDTO(Set<LobbyUser> LobbyUsers) {
        this.users = LobbyUsers.stream()
                .map(user -> new LobbyUserDTO(user.getId()))
                .collect(Collectors.toSet());
    }

    public Set<LobbyUserDTO> getUsers() {
        return users;
    }
}
