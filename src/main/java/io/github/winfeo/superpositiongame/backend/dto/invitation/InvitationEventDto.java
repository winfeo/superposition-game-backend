package io.github.winfeo.superpositiongame.backend.dto.invitation;

public record InvitationEventDto(
    InvitationEventType type,
    InvitationDto invitation
) { }
