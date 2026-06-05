package io.github.winfeo.superpositiongame.backend.dto;

public class PlayerDTO {
    private String id;
    private String nickname;

    public PlayerDTO(
            String id,
            String nickname
    ) {
        this.id = id;
        this.nickname = nickname;
    }

    public String getId(){ return id; }

    public String getNickname() { return nickname; }
}
