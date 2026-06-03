package io.github.winfeo.superpositiongame.backend.listener;

import io.github.winfeo.superpositiongame.backend.dto.LobbyResponseDTO;
import io.github.winfeo.superpositiongame.backend.entity.general.LobbyUser;
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
        Set<LobbyUser> LobbyUsers = lobbyService.getUsers();
        System.out.println("USERS BEING SENT: " + LobbyUsers.size());
        System.out.print("USER IDs: ");
        LobbyUsers.forEach(user -> System.out.print(user.getId() + " "));
        System.out.println();
        LobbyResponseDTO dto = new LobbyResponseDTO(LobbyUsers);
        messagingTemplate.convertAndSend("/topic/lobby", dto);
        System.out.println("SENT TO /topic/lobby");
    }
}
