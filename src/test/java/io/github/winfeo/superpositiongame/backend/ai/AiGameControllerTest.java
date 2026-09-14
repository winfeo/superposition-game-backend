package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.ai.controller.AiGameController;
import io.github.winfeo.superpositiongame.backend.ai.util.AiPlayerIdResolver;
import io.github.winfeo.superpositiongame.backend.entity.db.AuthData;
import io.github.winfeo.superpositiongame.backend.entity.db.User;
import io.github.winfeo.superpositiongame.backend.game.core.service.GameService;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiDifficulty;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiGameSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.AI_ID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AiGameControllerTest {
    private static final String REGISTERED_ID = "42";
    private static final String GUEST_ID = "guest-123e4567-e89b-12d3-a456-426614174000";

    @Mock
    private GameService gameService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AiGameController(gameService, new AiPlayerIdResolver()))
                .build();
    }

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/games/ai"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(gameService);
    }

    @Test
    void createsMctsGameByDefault() throws Exception {
        AiGameSession session = session(AiDifficulty.MCTS, REGISTERED_ID);
        when(gameService.createAiGame(REGISTERED_ID, AiDifficulty.MCTS))
                .thenReturn(Optional.of(session));

        mockMvc.perform(post("/api/games/ai")
                        .principal(authenticatedPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("game-id"))
                .andExpect(jsonPath("$.aiPlayerId").value(AI_ID))
                .andExpect(jsonPath("$.difficulty").value("MCTS"));
    }

    @Test
    void createsGameWithRequestedDifficulty() throws Exception {
        AiGameSession session = session(AiDifficulty.LOW, REGISTERED_ID);
        when(gameService.createAiGame(REGISTERED_ID, AiDifficulty.LOW))
                .thenReturn(Optional.of(session));

        mockMvc.perform(post("/api/games/ai")
                        .principal(authenticatedPrincipal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficulty\":\"LOW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty").value("LOW"));
    }

    @Test
    void returnsConflictWhenPlayerAlreadyHasGame() throws Exception {
        when(gameService.createAiGame(REGISTERED_ID, AiDifficulty.MCTS))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/games/ai")
                        .principal(authenticatedPrincipal()))
                .andExpect(status().isConflict());
    }

    @Test
    void createsGameForGuestUsingExistingGuestId() throws Exception {
        AiGameSession session = session(AiDifficulty.LOW, GUEST_ID);
        when(gameService.createAiGame(GUEST_ID, AiDifficulty.LOW))
                .thenReturn(Optional.of(session));

        mockMvc.perform(post("/api/games/ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficulty\":\"LOW\",\"guestId\":\"" + GUEST_ID + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("game-id"))
                .andExpect(jsonPath("$.difficulty").value("LOW"));
    }

    @Test
    void rejectsInvalidGuestId() throws Exception {
        mockMvc.perform(post("/api/games/ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guestId\":\"guest-invalid\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameService);
    }

    @Test
    void authenticatedUserCannotChooseGuestIdInsteadOfOwnId() throws Exception {
        AiGameSession session = session(AiDifficulty.MCTS, REGISTERED_ID);
        when(gameService.createAiGame(REGISTERED_ID, AiDifficulty.MCTS))
                .thenReturn(Optional.of(session));

        mockMvc.perform(post("/api/games/ai")
                        .principal(authenticatedPrincipal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guestId\":\"" + GUEST_ID + "\"}"))
                .andExpect(status().isOk());

        verify(gameService).createAiGame(REGISTERED_ID, AiDifficulty.MCTS);
        verify(gameService, never()).createAiGame(eq(GUEST_ID), any());
    }

    @Test
    void rejectsUnknownDifficulty() throws Exception {
        mockMvc.perform(post("/api/games/ai")
                        .principal(authenticatedPrincipal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficulty\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameService);
    }

    private UsernamePasswordAuthenticationToken authenticatedPrincipal() {
        User user = new User();
        user.setId(Long.parseLong(REGISTERED_ID));
        AuthData authData = new AuthData();
        authData.setEmail("player@example.com");
        user.setAuthData(authData);
        return new UsernamePasswordAuthenticationToken(user, null, List.of());
    }

    private AiGameSession session(AiDifficulty difficulty, String humanPlayerId) {
        return new AiGameSession(
                "game-id",
                humanPlayerId,
                AI_ID,
                AiTestFixtures.defaultGameState(),
                difficulty
        );
    }
}
