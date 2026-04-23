package io.github.winfeo.superpositiongame.backend.game.dto.move;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.winfeo.superpositiongame.backend.game.model.move.DoubleTapEffect;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayCardDto.class, name = "PLAY_CARD"),
        @JsonSubTypes.Type(value = RotateDiceDto.class, name = "ROTATE_DICE"),
        @JsonSubTypes.Type(value = SwapDicesDto.class, name = "SWAP_DICES"),
        @JsonSubTypes.Type(value = DoubleTapEffectDto.class, name = "DOUBLE_TAP")
})
public interface MoveDto {
    String playerId();
}
