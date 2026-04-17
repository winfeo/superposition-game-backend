package io.github.winfeo.superpositiongame.backend.game.core.service;

import io.github.winfeo.superpositiongame.backend.game.dto.GameStartEventDto;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class GameEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public GameEventPublisher(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendToUser(
            String userId,
            GameState state
    ) {
        System.out.println("=== SEND TO USER GAME STATE ===");
        System.out.println("Target userId: " + userId);
        System.out.println("Event type: " + state.phase());
        System.out.println("Full destination: /user/" + userId + "/queue/game");

        try {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/game",
                    state
            );
            System.out.println("Message sent successfully");
        } catch (Exception e) {
            System.out.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== SEND TO USER GAME STATE END ===");

        System.out.println("=== GAME STATE DETAILS ===");

        state.players().forEach((playerId, player) -> {
            System.out.println("Player: " + playerId);

            //Карты в руке
            System.out.println("  Hand:");
            player.hand().forEach(card -> {
                System.out.println("    Card: " + card);
            });

            //Слоты
            System.out.println("  Slots:");
            player.slots().forEach(slot -> {
                System.out.println("    Slot index: " + slot.index());
                System.out.println("      Owner: " + slot.ownerId());

                // начальное состояние
                System.out.println("      Initial dice: " + slot.initialDice());

                // текущее состояние
                System.out.println("      Current dice: " + slot.dice());

                // карты, применённые к слоту
                System.out.println("      Applied cards:");
                slot.appliedCards().forEach(card -> {
                    System.out.println("        " + card);
                });
            });
        });

        System.out.println("=== GAME STATE DETAILS END ===");
    }

    public void sendGameStart(
            String userId,
            String gameId
    ) {
        System.out.println("=== SEND TO USER GAME ID (GAME START) ===");
        System.out.println("Target userId: " + userId);
        System.out.println("Game id: " + gameId);
        System.out.println("Full destination: /user/" + userId + "/queue/game.start");

        try {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/game.start",
                    new GameStartEventDto(gameId)
            );
            System.out.println("Message sent successfully");
        } catch (Exception e) {
            System.out.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== SEND TO USER GAME ID END (GAME START) ===");
    }
}
