package io.github.winfeo.superpositiongame.backend.controller;

import io.github.winfeo.superpositiongame.backend.dto.toApp.GameHistoryDTO;
import io.github.winfeo.superpositiongame.backend.service.GameHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class GameHistoryController {
    private final GameHistoryService gameHistoryService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<GameHistoryDTO>> getGameHistoryByUserId(@PathVariable Long userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gameHistoryService.getGameHistoryByUserId(userId));
    }
}
