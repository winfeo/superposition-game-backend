package io.github.winfeo.superpositiongame.backend.service.impl;

import io.github.winfeo.superpositiongame.backend.entity.general.Invitation;
import io.github.winfeo.superpositiongame.backend.service.InvitationService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InvitationServiceImpl implements InvitationService {
    private final Map<String, Set<Invitation>> invitations = new ConcurrentHashMap<>();

    @Override
    public void addInvitation(Invitation invitation) {
        invitations
                .computeIfAbsent(
                        invitation.receiverId(),
                        k -> ConcurrentHashMap.newKeySet()
                )
                .add(invitation);
    }

    @Override
    public Set<Invitation> getInvitations(String userId) {
        Set<Invitation> userInvitations = invitations.get(userId);

        if (userInvitations == null) {
            return Collections.emptySet();
        }

        return Set.copyOf(userInvitations);
    }

    @Override
    public void removeInvitation(Invitation invitation) {
        Set<Invitation> list = invitations.get(invitation.receiverId());

        if (list != null) {
            list.removeIf(inv ->
                    inv.senderId().equals(invitation.senderId()) && inv.receiverId().equals(invitation.receiverId())
            );
        }
    }

    @Override
    public Set<Invitation> removeAllByFromUser(String fromUserId) {
        Set<Invitation> removedSet = new HashSet<>();

        for (Set<Invitation> set: invitations.values()) {
            Iterator<Invitation> it = set.iterator();
            while (it.hasNext()) {
                Invitation inv = it.next();
                if (inv.senderId().equals(fromUserId)) {
                    removedSet.add(inv);
                    it.remove();
                }
            }
        }

        return removedSet;
    }

    @Override
    public void removeAllToUser(String toUserId) {
        invitations.remove(toUserId);
    }
}
