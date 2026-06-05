package io.github.winfeo.superpositiongame.backend.entity.general;

public class Player {
    private final String id;
    private final String nickname;

    public Player(
            String id,
            String nickname
    ) {
        this.id = id;
        this.nickname = nickname;
    }

    public String getId() {
        return id;
    }

    public String getNickname() { return nickname; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
