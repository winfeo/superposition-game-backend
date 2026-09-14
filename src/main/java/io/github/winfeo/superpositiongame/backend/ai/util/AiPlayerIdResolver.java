package io.github.winfeo.superpositiongame.backend.ai.util;

import io.github.winfeo.superpositiongame.backend.entity.db.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Component
public class AiPlayerIdResolver {
    public Optional<String> resolve(Principal principal, String guestId) {
        if (principal instanceof Authentication authentication && authentication.getPrincipal() instanceof User user) {
            return Optional.of(Long.toString(user.getId()));
        }

        if (!isValidGuestId(guestId)) return Optional.empty();
        return Optional.of(guestId);
    }

    private boolean isValidGuestId(String guestId) {
        if (guestId == null || !guestId.startsWith("guest-")) return false;

        String uuid = guestId.substring("guest-".length());
        try {
            return UUID.fromString(uuid).toString().equalsIgnoreCase(uuid);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
