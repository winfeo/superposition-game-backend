package io.github.winfeo.superpositiongame.backend.game.util;

import io.github.winfeo.superpositiongame.backend.game.dto.move.MoveDto;
import io.github.winfeo.superpositiongame.backend.game.dto.move.PlayCardDto;
import io.github.winfeo.superpositiongame.backend.game.dto.move.RotateDiceDto;
import io.github.winfeo.superpositiongame.backend.game.dto.move.SwapDicesDto;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceState;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;
import io.github.winfeo.superpositiongame.backend.game.model.move.PlayCard;
import io.github.winfeo.superpositiongame.backend.game.model.move.RotateDice;
import io.github.winfeo.superpositiongame.backend.game.model.move.SwapDices;
import org.springframework.stereotype.Component;

@Component
public class MoveMapper {
    public static Move toDomain(MoveDto dto) {
        if (dto instanceof PlayCardDto d) {
            return new PlayCard(
                    d.playerId(),
                    d.cardId(),
                    d.targetSlotIndex(),
                    d.targetPlayerId()
            );
        }

        if (dto instanceof RotateDiceDto d) {
            return new RotateDice(
                    d.playerId(),
                    d.cardId(),
                    d.targetSlotIndex(),
                    DiceState.valueOf(d.newState()),
                    d.targetPlayerId()
            );
        }

        if (dto instanceof SwapDicesDto d) {
            return new SwapDices(
                    d.playerId(),
                    d.firstSlotIndex(),
                    d.secondSlotIndex()
            );
        }

        throw new IllegalArgumentException("Unknown MoveDto: " + dto.getClass());
    }
}
