package io.github.winfeo.superpositiongame.backend.service.impl;

import io.github.winfeo.superpositiongame.backend.entity.general.LobbyUser;
import io.github.winfeo.superpositiongame.backend.service.LobbyService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LobbyServiceImpl implements LobbyService {
    private final Set<LobbyUser> LobbyUsers = ConcurrentHashMap.newKeySet();
    @Override
    public void addUser(String userId) {
        System.out.println("addUser: " + userId);
        LobbyUsers.add(new LobbyUser(userId));
        System.out.println("USERS NOW: " + LobbyUsers.size());
    }

    @Override
    public void removeUser(String userId) {
        System.out.println("removeUser: " + userId);
        LobbyUsers.removeIf(user -> user.getId().equals(userId));
        System.out.println("USERS AFTER REMOVE: " + LobbyUsers.size());
    }

    @Override
    public Set<LobbyUser> getUsers() {
        return LobbyUsers;
    }
}
