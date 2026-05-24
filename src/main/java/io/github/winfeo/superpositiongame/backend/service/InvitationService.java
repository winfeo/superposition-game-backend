package io.github.winfeo.superpositiongame.backend.service;

import io.github.winfeo.superpositiongame.backend.entity.general.Invitation;

import java.util.Set;

public interface InvitationService {
    void addInvitation(Invitation invitation);
    Set<Invitation> getInvitations(String userId);
    void removeInvitation(Invitation invitation);
    Set<Invitation> removeAllByFromUser(String fromUserId);
    void removeAllToUser(String toUserId);
}
