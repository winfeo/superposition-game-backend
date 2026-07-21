package io.github.winfeo.superpositiongame.backend.controller;

import io.github.winfeo.superpositiongame.backend.dto.invitation.InvitationDTO;
import io.github.winfeo.superpositiongame.backend.dto.invitation.InvitationEventDTO;
import io.github.winfeo.superpositiongame.backend.dto.invitation.InvitationEventType;
import io.github.winfeo.superpositiongame.backend.entity.general.Invitation;
import io.github.winfeo.superpositiongame.backend.game.core.service.GameService;
import io.github.winfeo.superpositiongame.backend.listener.InvitationEventPublisher;
import io.github.winfeo.superpositiongame.backend.service.InvitationService;
import io.github.winfeo.superpositiongame.backend.util.InvitationMapper;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class InvitationController {
    private final InvitationService invitationService;
    private final GameService gameService;
    private final InvitationEventPublisher publisher;

    public InvitationController(
            InvitationService invitationService,
            InvitationEventPublisher publisher,
            GameService gameService
    ) {
        this.invitationService = invitationService;
        this.publisher = publisher;
        this.gameService = gameService;
    }

    @MessageMapping("/invite")
    public void sendInvite(InvitationDTO dto) {
        Invitation invitation = InvitationMapper.convertToDomain(dto);

        invitationService.addInvitation(invitation);

        InvitationEventDTO event = new InvitationEventDTO(
                InvitationEventType.INVITE_SEND,
                InvitationMapper.convertToDto(invitation)
        );
        publisher.sendToUser(
                invitation.receiverId(),
                event
        );
    }

    @MessageMapping("/invite.reject")
    public void rejectInvite(InvitationDTO dto) {
        Invitation invitation = InvitationMapper.convertToDomain(dto);
        invitationService.removeInvitation(invitation);

        InvitationEventDTO event = new InvitationEventDTO(
                InvitationEventType.INVITE_REMOVED,
                dto
        );
        publisher.sendToUser(
                invitation.receiverId(),
                event
        );
    }

    @MessageMapping("/invite.accept")
    public void acceptInvite(InvitationDTO dto) {
        Invitation invitation = InvitationMapper.convertToDomain(dto);
        invitationService.removeInvitation(invitation);

        InvitationEventDTO event = new InvitationEventDTO(
                InvitationEventType.INVITE_ACCEPTED,
                dto
        );
        publisher.sendToUser(
                invitation.receiverId(),
                event
        );
        publisher.sendToUser(
                invitation.senderId(),
                event
        );

        //старт игры
        gameService.createGame(
                invitation.senderId(),
                invitation.receiverId()
        );
    }

    @MessageMapping("/invitations")
    public void initInvitations(Principal principal) {
        System.out.println("=== INIT INVITATIONS (MESSAGE MAPPING) ===");
        System.out.println("Principal: " + principal);

        if (principal == null) {
            System.out.println("Principal is null!");
            return;
        }

        String userId = principal.getName();
        System.out.println("Got userId from Principal: " + userId);

        List<InvitationDTO> invitations = invitationService
                .getInvitations(userId)
                .stream()
                .map(InvitationMapper::convertToDto)
                .toList();

        System.out.println("Found " + invitations.size() + " invitations for user");

        publisher.sendToUser(
                userId,
                InvitationEventDTO.initial(invitations)
        );

        System.out.println("=== INIT INVITATIONS END ===");
    }
}
