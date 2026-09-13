package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.exception.AiConfigurationException;
import io.github.winfeo.superpositiongame.backend.exception.AiMoveRejectedException;
import io.github.winfeo.superpositiongame.backend.exception.AiStateMappingException;
import io.github.winfeo.superpositiongame.backend.exception.InvalidAiResponseException;
import io.github.winfeo.superpositiongame.backend.exception.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerAiTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsExternalAiErrorsToBadGateway() {
        ResponseEntity<String> invalidResponse = handler.handleAiServiceException(
                new InvalidAiResponseException("Некорректный ответ AI")
        );
        ResponseEntity<String> rejectedMove = handler.handleAiServiceException(
                new AiMoveRejectedException("Ход отклонён")
        );

        assertThat(invalidResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(invalidResponse.getBody()).isEqualTo("Некорректный ответ AI");
        assertThat(rejectedMove.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void mapsInternalAiErrorsToInternalServerError() {
        ResponseEntity<String> stateError = handler.handleAiInternalException(
                new AiStateMappingException("Ошибка состояния")
        );
        ResponseEntity<String> configError = handler.handleAiInternalException(
                new AiConfigurationException("Ошибка конфигурации")
        );

        assertThat(stateError.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(stateError.getBody()).isEqualTo("Ошибка состояния");
        assertThat(configError.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
