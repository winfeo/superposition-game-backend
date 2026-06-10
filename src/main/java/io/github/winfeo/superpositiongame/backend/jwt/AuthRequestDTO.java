package io.github.winfeo.superpositiongame.backend.jwt;

import lombok.Data;

@Data
public class AuthRequestDTO {
    private String email;
    private String password;
}
