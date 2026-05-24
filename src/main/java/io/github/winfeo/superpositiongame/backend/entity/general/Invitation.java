package io.github.winfeo.superpositiongame.backend.entity.general;

public record Invitation(
        String senderId,
        String receiverId,
        String sendTime
) { }
