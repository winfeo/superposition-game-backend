package io.github.winfeo.superpositiongame.backend.service;

import io.github.winfeo.superpositiongame.backend.jwt.AuthRequestDTO;
import io.github.winfeo.superpositiongame.backend.jwt.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO authenticateAndGenerateToken(AuthRequestDTO authRequest);
}
