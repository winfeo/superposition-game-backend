package io.github.winfeo.superpositiongame.backend.service.impl;

import io.github.winfeo.superpositiongame.backend.entity.User;
import io.github.winfeo.superpositiongame.backend.service.LobbyService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LobbyServiceImpl implements LobbyService {
    private final Set<User> users = ConcurrentHashMap.newKeySet();
    @Override
    public void addUser(String userId) {
        System.out.println("addUser: " + userId);
        users.add(new User(userId));
        System.out.println("USERS NOW: " + users.size());
    }

    @Override
    public void removeUser(String userId) {
        System.out.println("removeUser: " + userId);
        users.removeIf(user -> user.getId().equals(userId));
        System.out.println("USERS AFTER REMOVE: " + users.size());
    }

    @Override
    public Set<User> getUsers() {
        return users;
    }
}
