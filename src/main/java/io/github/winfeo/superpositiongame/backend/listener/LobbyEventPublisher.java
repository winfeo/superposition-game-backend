package io.github.winfeo.superpositiongame.backend.listener;

import io.github.winfeo.superpositiongame.backend.dto.LobbyResponseDTO;
import io.github.winfeo.superpositiongame.backend.entity.general.Player;
import io.github.winfeo.superpositiongame.backend.service.LobbyService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LobbyEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;
    private final LobbyService lobbyService;

    public LobbyEventPublisher(
            SimpMessagingTemplate messagingTemplate,
            LobbyService lobbyService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.lobbyService = lobbyService;
    }

    public void publish() {
        System.out.println("sendLobbyUpdate CALLED");
        Set<Player> players = lobbyService.getPlayers();
        System.out.println("USERS BEING SENT: " + players.size());
        System.out.print("USER IDs: ");
        players.forEach(user -> System.out.print(user.getId() + " "));
        System.out.println();
        LobbyResponseDTO dto = new LobbyResponseDTO(players);
        messagingTemplate.convertAndSend("/topic/lobby", dto);
        System.out.println("SENT TO /topic/lobby");
    }
}
