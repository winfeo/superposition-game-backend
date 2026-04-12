package io.github.winfeo.superpositiongame.backend.dto.invitation;

public record InvitationDto(
        String senderId,
        String receiverId,
        String sendTime
) { }
