package io.github.winfeo.superpositiongame.backend.dto.invitation;

public record InvitationDTO(
        String senderId,
        String senderNickname,
        String receiverId,
        String sendTime
) { }
