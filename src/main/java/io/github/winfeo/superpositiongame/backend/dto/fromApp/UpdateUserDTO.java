package io.github.winfeo.superpositiongame.backend.dto.fromApp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateUserDTO {
    private long id;
    private String nickname;
    private String email;
}
