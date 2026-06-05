package io.github.winfeo.superpositiongame.backend.entity.general;

public class LobbyUser {
    private final String id;

    public LobbyUser(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LobbyUser)) return false;
        LobbyUser lobbyUser = (LobbyUser) o;
        return id.equals(lobbyUser.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
