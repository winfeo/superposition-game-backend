package io.github.winfeo.superpositiongame.backend.dto;

import io.github.winfeo.superpositiongame.backend.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public class LobbyResponseDto { //TODO переделать
    private Set<UserDto> users;

    public LobbyResponseDto(Set<User> users) {
        this.users = users.stream()
                .map(user -> new UserDto(user.getId()))
                .collect(Collectors.toSet());
    }

    public Set<UserDto> getUsers() {
        return users;
    }
}
