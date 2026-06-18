package io.github.winfeo.superpositiongame.backend.game.util;

import io.github.winfeo.superpositiongame.backend.game.dto.move.*;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import io.github.winfeo.superpositiongame.backend.game.model.move.*;
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
                    DiceType.valueOf(d.newState()),
                    d.targetPlayerId()
            );
        }

        if (dto instanceof SwapDicesDto d) {
            return new SwapDices(
                    d.playerId(),
                    d.cardId(),
                    d.firstSlotIndex(),
                    d.secondSlotIndex(),
                    d.firstSlotOwner(),
                    d.secondSlotOwner()
            );
        }

        if (dto instanceof DoubleTapEffectDto d) {
            return new DoubleTapEffect(
                    d.playerId(),
                    d.cardId()
            );
        }

        if(dto instanceof ReshuffleCardDto d) {
            return new ReshuffleCard(
                    d.playerId(),
                    d.cardId(),
                    d.cardsToChange()
            );
        }

        if (dto instanceof SurrenderDto d) {
            return new Surrender(
                    d.playerId()
            );
        }

        throw new IllegalArgumentException("Unknown MoveDto: " + dto.getClass());
    }
}
