package io.github.winfeo.superpositiongame.backend.util;

import io.github.winfeo.superpositiongame.backend.dto.invitation.InvitationDto;
import io.github.winfeo.superpositiongame.backend.entity.general.Invitation;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InvitationMapper {
    public static Invitation convertToDomain(InvitationDto dto) {
        return new Invitation(
                dto.senderId(),
                dto.receiverId(),
                dto.sendTime()
        );
    }

    public static InvitationDto convertToDto(Invitation invitation) {
        return new InvitationDto(
                invitation.senderId(),
                invitation.receiverId(),
                invitation.sendTime()
        );
    }
}
