package io.github.winfeo.superpositiongame.backend.exception.handler;

import io.github.winfeo.superpositiongame.backend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthDataEmailAlreadyTakenException.class)
    public ResponseEntity<String> handleAuthDataEmailAlreadyTakenException(AuthDataEmailAlreadyTakenException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(LeagueNotFoundException.class)
    public ResponseEntity<String> handleLeagueNotFoundException(LeagueNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NicknameAlreadyTakenException.class)
    public ResponseEntity<String> handleNicknameAlreadyTakenException(NicknameAlreadyTakenException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NicknameIsEmptyException.class)
    public ResponseEntity<String> handleNicknameIsEmptyException(NicknameIsEmptyException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserSaveFailedException.class)
    public ResponseEntity<String> handleUserSaveFailedException(UserSaveFailedException e) {
        String message = "Не удалось сохранить пользователя.";
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }
}
