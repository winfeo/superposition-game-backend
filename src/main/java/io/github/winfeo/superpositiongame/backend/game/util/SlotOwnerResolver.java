package io.github.winfeo.superpositiongame.backend.game.util;

import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotOwner;

import java.util.Optional;

public final class SlotOwnerResolver {
    private SlotOwnerResolver() { }

    public static Optional<String> resolvePlayerId(
            GameState state,
            String actingPlayerId,
            String ownerValue
    ) {
        final SlotOwner owner;

        try {
            owner = SlotOwner.valueOf(ownerValue);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Optional.empty();
        }

        if (!state.players().containsKey(actingPlayerId)) {
            return Optional.empty();
        }

        if (owner == SlotOwner.PLAYER) {
            return Optional.of(actingPlayerId);
        }

        return state.players().keySet().stream()
                .filter(id -> !id.equals(actingPlayerId))
                .findFirst();
    }

    public static Optional<SlotOwner> resolveSlotOwner(
            GameState state,
            String actingPlayerId,
            String targetPlayerId
    ) {
        if (!state.players().containsKey(actingPlayerId) || !state.players().containsKey(targetPlayerId)) {
            return Optional.empty();
        }

        return Optional.of(
                actingPlayerId.equals(targetPlayerId)
                        ? SlotOwner.PLAYER
                        : SlotOwner.OPPONENT
        );
    }
}
