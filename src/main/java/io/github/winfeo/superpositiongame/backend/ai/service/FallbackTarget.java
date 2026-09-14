package io.github.winfeo.superpositiongame.backend.ai.service;

import io.github.winfeo.superpositiongame.backend.game.model.game.SlotOwner;

public record FallbackTarget(
        SlotOwner owner,
        int slotIndex
) { }
