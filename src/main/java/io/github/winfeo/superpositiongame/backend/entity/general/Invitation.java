package io.github.winfeo.superpositiongame.backend.entity.general;

public record Invitation(
        String senderId,
        String senderNickname,
        String receiverId,
        String sendTime
) { }
