package io.github.winfeo.superpositiongame.backend.entity;

import java.util.Set;

public class Lobby {
    private Set<User> users;

    public Lobby(Set<User> users) {
        this.users = users;
    }

    public Set<User> getUsers() {
        return users;
    }
}
