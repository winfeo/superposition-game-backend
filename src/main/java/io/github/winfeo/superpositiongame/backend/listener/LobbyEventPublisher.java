package io.github.winfeo.superpositiongame.backend.listener;

import io.github.winfeo.superpositiongame.backend.dto.LobbyResponseDto;
import io.github.winfeo.superpositiongame.backend.entity.User;
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
        Set<User> users = lobbyService.getUsers();
        System.out.println("USERS BEING SENT: " + users.size());
        System.out.print("USER IDs: ");
        users.forEach(user -> System.out.print(user.getId() + " "));
        System.out.println();
        LobbyResponseDto dto = new LobbyResponseDto(users);
        messagingTemplate.convertAndSend("/topic/lobby", dto);
        System.out.println("SENT TO /topic/lobby");
    }
}
