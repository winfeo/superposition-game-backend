package io.github.winfeo.superpositiongame.backend.dto;

public class UserDto {
    private String id;
    public UserDto(String id) {
        this.id = id;
    }

    public String getId(){
        return id;
    }
}
