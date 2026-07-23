package io.github.winfeo.superpositiongame.backend.game.model.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameSession {
    private final String gameId;
    private final String playerId_A;
    private final String playerId_B;
    private volatile GameState gameState;
    private volatile GameSessionStatus status;
    private volatile GameEndReason endReason;
    private volatile long pausedTurnTimeLeftMs;
    private final Map<String, Long> lastHeartbeatAt = new HashMap<>();
    private final Map<String, Long> reconnectDeadlines = new HashMap<>();
    private final Set<String> disconnectedPlayers = new HashSet<>();
    private long timerRevision;
    private long lastTimerSyncAt;
    private int lastTimerSyncTurnNumber = -1;

    public GameSession(
            String gameId,
            String playerId_A,
            String playerId_B,
            GameState gameState
    ) {
        this.gameId = gameId;
        this.playerId_A = playerId_A;
        this.playerId_B = playerId_B;
        this.gameState = gameState;
        this.status = GameSessionStatus.WAITING_FOR_PLAYERS;

        long now = System.currentTimeMillis();
        Long turnEndsAt = gameState.turnEndsAt();
        this.pausedTurnTimeLeftMs = turnEndsAt == null? 0L: Math.max(0L, turnEndsAt - now);
    }

    public String getGameId() { return gameId; }
    public String getPlayerA() { return playerId_A; }
    public String getPlayerB() { return playerId_B; }
    public GameState getGameState() { return gameState; }
    public void updateGameState(GameState gameState) { this.gameState = gameState; }

    public GameSessionStatus getStatus() { return status; }
    public void setStatus(GameSessionStatus status) { this.status = status; }

    public GameEndReason getEndReason() { return endReason; }
    public void setEndReason(GameEndReason endReason) { this.endReason = endReason; }

    public long getPausedTurnTimeLeftMs() { return pausedTurnTimeLeftMs; }
    public void setPausedTurnTimeLeftMs(long pausedTurnTimeLeftMs) {
        this.pausedTurnTimeLeftMs = Math.max(0L, pausedTurnTimeLeftMs);
    }

    public boolean containsPlayer(String playerId) {
        return playerId_A.equals(playerId) || playerId_B.equals(playerId);
    }

    public String getOpponentId(String playerId) {
        if (playerId_A.equals(playerId)) return playerId_B;
        if (playerId_B.equals(playerId)) return playerId_A;
        return null;
    }

    public Set<String> getPlayerIds() {
        return Set.of(playerId_A, playerId_B);
    }

    public synchronized void recordHeartbeat(
            String playerId,
            long timestamp
    ) {
        if (containsPlayer(playerId)) {
            lastHeartbeatAt.put(playerId, timestamp);
        }
    }

    public synchronized Long getLastHeartbeatAt(String playerId) {
        return lastHeartbeatAt.get(playerId);
    }

    public synchronized boolean markDisconnected(
            String playerId,
            long reconnectDeadline
    ) {
        if (!containsPlayer(playerId) || !disconnectedPlayers.add(playerId)) {
            return false;
        }

        reconnectDeadlines.put(playerId, reconnectDeadline);
        return true;
    }

    public synchronized boolean markReconnected(String playerId) {
        if (!containsPlayer(playerId)) return false;

        reconnectDeadlines.remove(playerId);
        return disconnectedPlayers.remove(playerId);
    }

    public synchronized boolean isDisconnected(String playerId) {
        return disconnectedPlayers.contains(playerId);
    }

    public synchronized boolean hasDisconnectedPlayers() {
        return !disconnectedPlayers.isEmpty();
    }

    public synchronized boolean areAllPlayersDisconnected() {
        return disconnectedPlayers.contains(playerId_A) && disconnectedPlayers.contains(playerId_B);
    }

    public synchronized Set<String> getDisconnectedPlayers() {
        return Set.copyOf(disconnectedPlayers);
    }

    public synchronized Long getReconnectDeadline(String playerId) {
        return reconnectDeadlines.get(playerId);
    }

    public synchronized Map<String, Long> getReconnectDeadlines() {
        return Map.copyOf(reconnectDeadlines);
    }

    public synchronized boolean shouldPublishTimer(
            long now,
            long syncIntervalMs
    ) {
        return gameState.turnNumber() != lastTimerSyncTurnNumber || now - lastTimerSyncAt >= syncIntervalMs;
    }

    public synchronized long markTimerPublished(long now) {
        lastTimerSyncAt = now;
        lastTimerSyncTurnNumber = gameState.turnNumber();
        return ++timerRevision;
    }

    public synchronized void invalidateTimerSync() {
        lastTimerSyncAt = 0L;
        lastTimerSyncTurnNumber = -1;
    }
}

