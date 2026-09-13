package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.ai.service.AiMoveDecision;
import io.github.winfeo.superpositiongame.backend.ai.service.AiTurnService;
import io.github.winfeo.superpositiongame.backend.config.GamePresenceProperties;
import io.github.winfeo.superpositiongame.backend.game.core.GameEngine;
import io.github.winfeo.superpositiongame.backend.game.core.GameLoop;
import io.github.winfeo.superpositiongame.backend.game.core.service.GameEventPublisher;
import io.github.winfeo.superpositiongame.backend.game.core.service.GameResultService;
import io.github.winfeo.superpositiongame.backend.game.core.service.GameServiceImpl;
import io.github.winfeo.superpositiongame.backend.game.model.game.*;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;
import io.github.winfeo.superpositiongame.backend.game.model.move.Surrender;
import io.github.winfeo.superpositiongame.backend.repository.memory.ActiveGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.AI_ID;
import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.HUMAN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplAiTest {
    @Mock
    private GameResultService gameResultService;
    @Mock
    private ActiveGameRepository repository;
    @Mock
    private GameEngine gameEngine;
    @Mock
    private GameEventPublisher publisher;
    @Mock
    private GameLoop gameLoop;
    @Mock
    private AiTurnService aiTurnService;

    private GameServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GameServiceImpl(
                gameResultService,
                repository,
                gameEngine,
                publisher,
                gameLoop,
                new GamePresenceProperties(5_000, 60_000),
                aiTurnService
        );
    }

    @Test
    void createsAiSessionAndStartsAfterOnlyHumanBecomesReady() {
        GameState initialState = AiTestFixtures.defaultGameState()
                .copyWithCurrentPlayerId(HUMAN_ID);
        when(repository.findByPlayerId(HUMAN_ID)).thenReturn(null);
        when(gameLoop.startAiGame(eq(HUMAN_ID), anyString()))
                .thenReturn(initialState);

        Optional<AiGameSession> created = service.createAiGame(
                HUMAN_ID,
                AiDifficulty.MCTS
        );

        assertThat(created).isPresent();
        AiGameSession session = created.orElseThrow();
        assertThat(session.getHumanPlayerId()).isEqualTo(HUMAN_ID);
        assertThat(session.getAiPlayerId()).startsWith("ai-");
        assertThat(session.getGameMode()).isEqualTo(GameMode.AI);
        assertThat(session.getDifficulty()).isEqualTo(AiDifficulty.MCTS);

        when(repository.findById(session.getGameId())).thenReturn(session);
        service.playerReady(session.getGameId(), HUMAN_ID);

        assertThat(session.getStatus()).isEqualTo(GameSessionStatus.ACTIVE);
        verify(publisher).sendGameStart(HUMAN_ID, session.getGameId());
        verify(publisher, never()).sendGameStart(session.getAiPlayerId(), session.getGameId());
        verify(aiTurnService, never()).chooseMove(anyString(), any(), anyString(), any());
    }

    @Test
    void sendsStateOnlyToHumanAndDoesNotStartDuplicateAiRequest() {
        AiGameSession session = activeAiSession(AiTestFixtures.defaultGameState());
        CompletableFuture<AiMoveDecision> pending = new CompletableFuture<>();
        when(repository.findById(session.getGameId())).thenReturn(session);
        when(aiTurnService.chooseMove(
                eq(session.getGameId()),
                any(GameState.class),
                eq(AI_ID),
                eq(AiDifficulty.MCTS)
        )).thenReturn(pending);

        service.broadcastState(session);
        service.broadcastState(session);

        verify(publisher, times(2)).sendToUser(
                eq(HUMAN_ID),
                eq(session.getGameId()),
                any(GameState.class)
        );
        verify(publisher, never()).sendToUser(
                eq(AI_ID),
                anyString(),
                any(GameState.class)
        );
        verify(aiTurnService, times(1)).chooseMove(
                eq(session.getGameId()),
                any(GameState.class),
                eq(AI_ID),
                eq(AiDifficulty.MCTS)
        );
        assertThat(session.isRequestInProgress()).isTrue();
    }

    @Test
    void ignoresAiResponseWhenGameStateChangedDuringRequest() {
        AiGameSession session = activeAiSession(AiTestFixtures.defaultGameState());
        CompletableFuture<AiMoveDecision> pending = new CompletableFuture<>();
        when(repository.findById(session.getGameId())).thenReturn(session);
        when(aiTurnService.chooseMove(anyString(), any(), anyString(), any()))
                .thenReturn(pending);

        service.broadcastState(session);
        session.updateGameState(session.getGameState().copyWithTurnNumber(8));
        pending.complete(AiMoveDecision.remote(new Surrender(AI_ID)));

        assertThat(session.isRequestInProgress()).isFalse();
        verifyNoInteractions(gameEngine);
    }

    @Test
    void appliesValidAiMoveAndDoesNotSaveAiGameResult() {
        AiGameSession session = activeAiSession(AiTestFixtures.defaultGameState());
        Move move = new Surrender(AI_ID);
        GameState applied = session.getGameState().copyWithWinnerId(HUMAN_ID);
        GameState finished = applied.copyWithPhase(GamePhase.GAME_FINISHED);

        when(repository.findById(session.getGameId())).thenReturn(session);
        when(aiTurnService.chooseMove(anyString(), any(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(AiMoveDecision.remote(move)));
        when(gameEngine.applyMove(any(GameState.class), eq(move)))
                .thenReturn(Optional.of(applied));
        when(gameLoop.afterMove(applied, AI_ID, session.getGameId()))
                .thenReturn(finished);

        service.broadcastState(session);

        assertThat(session.getStatus()).isEqualTo(GameSessionStatus.FINISHED);
        assertThat(session.getGameState().winnerId()).isEqualTo(HUMAN_ID);
        verify(repository).delete(session.getGameId());
        verifyNoInteractions(gameResultService);
    }

    private AiGameSession activeAiSession(GameState state) {
        AiGameSession session = new AiGameSession(
                "game-id",
                HUMAN_ID,
                AI_ID,
                state,
                AiDifficulty.MCTS
        );
        session.setStatus(GameSessionStatus.ACTIVE);
        return session;
    }
}
