package io.github.winfeo.superpositiongame.backend.ai.controller;

import io.github.winfeo.superpositiongame.backend.ai.dto.CreateAiGameRequestDTO;
import io.github.winfeo.superpositiongame.backend.ai.dto.CreateAiGameResponseDTO;
import io.github.winfeo.superpositiongame.backend.ai.util.AiPlayerIdResolver;
import io.github.winfeo.superpositiongame.backend.game.core.service.GameService;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiDifficulty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/games/ai")
public class AiGameController {
    private final GameService gameService;
    private final AiPlayerIdResolver playerIdResolver;

    public AiGameController(GameService gameService, AiPlayerIdResolver playerIdResolver) {
        this.gameService = gameService;
        this.playerIdResolver = playerIdResolver;
    }

    @PostMapping
    public ResponseEntity<CreateAiGameResponseDTO> createGame(
            @RequestBody(required = false) CreateAiGameRequestDTO request,
            Principal principal
    ) {
        String guestId = request == null? null: request.guestId();
        Optional<String> playerId = playerIdResolver.resolve(principal, guestId);

        if (playerId.isEmpty()) {
            HttpStatus status = guestId == null? HttpStatus.UNAUTHORIZED: HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        }

        AiDifficulty difficulty = request == null || request.difficulty() == null? AiDifficulty.MCTS: request.difficulty();

        return gameService.createAiGame(playerId.get(), difficulty)
                .map(session -> ResponseEntity.ok(new CreateAiGameResponseDTO(
                        session.getGameId(),
                        session.getAiPlayerId(),
                        session.getDifficulty()
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());
    }
}
