package io.github.winfeo.superpositiongame.backend.game.core.service;

import io.github.winfeo.superpositiongame.backend.game.model.game.TimerUpdatePacket;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class GameTimerPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public GameTimerPublisher(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendTimerUpdate(
            String userId,
            String gameId,
            long timeLeftMs,
            long serverTimestamp
    ) {
        TimerUpdatePacket packet = new TimerUpdatePacket(timeLeftMs, serverTimestamp);

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/game/" + gameId + "/timer",
                packet
        );
    }

}
