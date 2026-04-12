package io.github.winfeo.superpositiongame.backend.entity;

public record Invitation(
        String senderId,
        String receiverId,
        String sendTime
) { }
