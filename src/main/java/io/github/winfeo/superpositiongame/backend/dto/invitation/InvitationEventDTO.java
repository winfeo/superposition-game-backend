package io.github.winfeo.superpositiongame.backend.dto.invitation;

public record InvitationEventDTO(
    InvitationEventType type,
    InvitationDTO invitation
) { }
