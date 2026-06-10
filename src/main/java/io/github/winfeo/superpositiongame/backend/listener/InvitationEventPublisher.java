package io.github.winfeo.superpositiongame.backend.listener;

import io.github.winfeo.superpositiongame.backend.dto.invitation.InvitationEventDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class InvitationEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public InvitationEventPublisher(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendToUser(String userId, InvitationEventDTO event) {
        System.out.println("=== SEND TO USER ===");
        System.out.println("Target userId: " + userId);
        System.out.println("Event type: " + event.type());
        System.out.println("Full destination: /user/" + userId + "/queue/invitations");

        try {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/invitations",
                    event
            );
            System.out.println("Message sent successfully");
        } catch (Exception e) {
            System.out.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== SEND TO USER END ===");
    }
}
