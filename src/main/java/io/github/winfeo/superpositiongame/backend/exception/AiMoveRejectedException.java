package io.github.winfeo.superpositiongame.backend.exception;

public class AiMoveRejectedException extends RuntimeException {
    public AiMoveRejectedException(String message) {
        super(message);
    }
}
