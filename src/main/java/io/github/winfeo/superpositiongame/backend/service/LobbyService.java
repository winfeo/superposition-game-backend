package io.github.winfeo.superpositiongame.backend.service;

import io.github.winfeo.superpositiongame.backend.entity.general.LobbyUser;

import java.util.Set;

public interface LobbyService {
    public void addUser(String userId);
    public void removeUser(String userId);
    public Set<LobbyUser> getUsers();
}
