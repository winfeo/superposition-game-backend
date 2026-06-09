package io.github.winfeo.superpositiongame.backend.jwt;

import io.github.winfeo.superpositiongame.backend.dto.toApp.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private UserDTO user;
}
