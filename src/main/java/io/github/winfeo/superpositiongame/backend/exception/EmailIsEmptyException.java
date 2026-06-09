package io.github.winfeo.superpositiongame.backend.exception;

public class EmailIsEmptyException extends RuntimeException {
    public EmailIsEmptyException(String message) {
        super(message);
    }
}
