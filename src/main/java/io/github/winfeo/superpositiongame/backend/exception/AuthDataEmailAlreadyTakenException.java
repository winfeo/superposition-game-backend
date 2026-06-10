package io.github.winfeo.superpositiongame.backend.exception;

public class AuthDataEmailAlreadyTakenException extends RuntimeException {
    public AuthDataEmailAlreadyTakenException(String message) {
        super(message);
    }
}
