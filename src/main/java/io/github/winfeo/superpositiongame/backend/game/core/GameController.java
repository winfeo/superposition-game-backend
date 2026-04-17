package io.github.winfeo.superpositiongame.backend.game.core;

import io.github.winfeo.superpositiongame.backend.game.core.service.GameService;
import io.github.winfeo.superpositiongame.backend.game.dto.move.MoveDto;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;
import io.github.winfeo.superpositiongame.backend.game.util.MoveMapper;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class GameController {
    private final GameService service;

    public GameController(
            GameService service
    ) {
        this.service = service;
    }
    @MessageMapping("/game/{gameId}/move")
    public void handleMove(
            @DestinationVariable String gameId,
            MoveDto moveDto,
            Principal principal
    ) {
        String userId = principal.getName();
        Move move = MoveMapper.toDomain(moveDto);
        service.handleMove(gameId, move, userId);
    }
}
