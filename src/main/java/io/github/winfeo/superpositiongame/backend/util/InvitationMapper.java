package io.github.winfeo.superpositiongame.backend.util;

import io.github.winfeo.superpositiongame.backend.dto.invitation.InvitationDTO;
import io.github.winfeo.superpositiongame.backend.entity.general.Invitation;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;

@UtilityClass
public class InvitationMapper {
    public static Invitation convertToDomain(InvitationDTO dto) {
        return new Invitation(
                dto.senderId(),
                dto.senderNickname(),
                dto.receiverId(),
                LocalDateTime.now().toString()
        );
    }

    public static InvitationDTO convertToDto(Invitation invitation) {
        return new InvitationDTO(
                invitation.senderId(),
                invitation.senderNickname(),
                invitation.receiverId(),
                invitation.sendTime()
        );
    }
}
