package io.github.winfeo.superpositiongame.backend.dto.invitation;

import java.util.List;

public record InvitationEventDTO(
        InvitationEventType type,
        InvitationDTO invitation,
        List<InvitationDTO> invitations
) {
    public InvitationEventDTO(
            InvitationEventType type,
            InvitationDTO invitation
    ) {
        this(type, invitation, null);
    }

    public static InvitationEventDTO initial(
            List<InvitationDTO> invitations
    ) {
        return new InvitationEventDTO(
                InvitationEventType.INIT,
                null,
                invitations
        );
    }
}
