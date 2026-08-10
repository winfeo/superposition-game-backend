package io.github.winfeo.superpositiongame.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "game.presence")
public record GamePresenceProperties(
        long heartbeatTimeoutMs,
        long reconnectTimeoutMs
) { }