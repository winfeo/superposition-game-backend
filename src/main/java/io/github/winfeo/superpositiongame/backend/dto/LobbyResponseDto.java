package io.github.winfeo.superpositiongame.backend.dto;

import io.github.winfeo.superpositiongame.backend.entity.general.LobbyUser;

import java.util.Set;
import java.util.stream.Collectors;

public class LobbyResponseDto { //TODO переделать
    private Set<UserDto> users;

    public LobbyResponseDto(Set<LobbyUser> LobbyUsers) {
        this.users = LobbyUsers.stream()
                .map(user -> new UserDto(user.getId()))
                .collect(Collectors.toSet());
    }

    public Set<UserDto> getUsers() {
        return users;
    }
}
