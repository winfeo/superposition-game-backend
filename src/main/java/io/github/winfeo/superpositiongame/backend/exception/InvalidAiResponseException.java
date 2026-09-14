package io.github.winfeo.superpositiongame.backend.exception;

public class InvalidAiResponseException extends RuntimeException {
    public InvalidAiResponseException(String message) {
        super(message);
    }

    public InvalidAiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
