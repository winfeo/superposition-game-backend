package io.github.winfeo.superpositiongame.backend.dto.toApp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private long id;
    private String league;
    private String nickname;
    private int ratingPoints = 0;
    private int winsAmount = 0;
    private int gamesPlayed = 0;
    private LocalDateTime createdAt;
}
