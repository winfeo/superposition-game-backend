package io.github.winfeo.superpositiongame.backend.listener;

import io.github.winfeo.superpositiongame.backend.dto.invitation.InvitationDTO;
import io.github.winfeo.superpositiongame.backend.dto.invitation.InvitationEventDTO;
import io.github.winfeo.superpositiongame.backend.dto.invitation.InvitationEventType;
import io.github.winfeo.superpositiongame.backend.entity.general.Invitation;
import io.github.winfeo.superpositiongame.backend.service.InvitationService;
import io.github.winfeo.superpositiongame.backend.service.LobbyService;
import io.github.winfeo.superpositiongame.backend.util.InvitationMapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Set;

@Component
public class DisconnectListener {
    private final LobbyService lobbyService;
    private final InvitationService invitationService;
    private final LobbyEventPublisher lobbyPublisher;
    private final InvitationEventPublisher invitationPublisher;

    public DisconnectListener(
            LobbyService lobbyService,
            InvitationService invitationService,
            LobbyEventPublisher lobbyPublisher,
            InvitationEventPublisher invitationPublisher
    ) {
        this.lobbyService = lobbyService;
        this.invitationService = invitationService;
        this.lobbyPublisher = lobbyPublisher;
        this.invitationPublisher = invitationPublisher;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        System.out.println("DISCONNECT: sessionId=" + sessionId);
        Principal principal = event.getUser();
        String userId = principal != null? principal.getName(): null;
//        String userId = registry.remove(sessionId);
        System.out.println("RESOLVED userId=" + userId);

        if (userId == null) return;
        Set<Invitation> removedInvites = invitationService.removeAllByFromUser(userId);
        for (Invitation inv: removedInvites) {
            InvitationDTO dto = InvitationMapper.convertToDto(inv);
            invitationPublisher.sendToUser(
                    inv.receiverId(),
                    new InvitationEventDTO(
                            InvitationEventType.INVITE_REMOVED,
                            dto
                    )
            );
        }

//        Set<InvitationDto> toUserInvites = invitationService.getInvitations(userId);
//        for (var inv: toUserInvites) {
//            invitationPublisher.sendToUser(
//                    inv.receiverId(),
//                    new InvitationEventDto(InvitationType.INVITE_REMOVED, inv)
//            );
//        }

        invitationService.removeAllToUser(userId);
        lobbyService.removePlayer(userId);
        lobbyPublisher.publish();
    }
}
