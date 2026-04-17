package io.github.winfeo.superpositiongame.backend.game.dto.move;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayCardDto.class, name = "PLAY_CARD"),
        @JsonSubTypes.Type(value = RotateDiceDto.class, name = "ROTATE_DICE"),
        @JsonSubTypes.Type(value = SwapDicesDto.class, name = "SWAP_DICES")
})
public interface MoveDto {
    String playerId();
}
