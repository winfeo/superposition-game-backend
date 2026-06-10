package io.github.winfeo.superpositiongame.backend.exception;

public class NicknameAlreadyTakenException extends RuntimeException {
    public NicknameAlreadyTakenException(String message) {
        super(message);
    }
}
