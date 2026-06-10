package io.github.winfeo.superpositiongame.backend.exception;

public class UserSaveFailedException extends RuntimeException {
    public UserSaveFailedException(String message) {
        super(message);
    }
}
