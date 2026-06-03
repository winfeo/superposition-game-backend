package io.github.winfeo.superpositiongame.backend.dto;

public class LobbyUserDTO {
    private String id;
    public LobbyUserDTO(String id) {
        this.id = id;
    }

    public String getId(){
        return id;
    }
}
