package io.github.winfeo.superpositiongame.backend.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.winfeo.superpositiongame.backend.ai.client.AiApiClient;
import io.github.winfeo.superpositiongame.backend.ai.config.AiApiConfig;
import io.github.winfeo.superpositiongame.backend.ai.config.AiApiProperties;
import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionRequestDTO;
import io.github.winfeo.superpositiongame.backend.ai.dto.AiChooseActionResponseDTO;
import io.github.winfeo.superpositiongame.backend.ai.service.AiMoveDecision;
import io.github.winfeo.superpositiongame.backend.ai.service.AiTurnService;
import io.github.winfeo.superpositiongame.backend.ai.service.FallbackMoveSelector;
import io.github.winfeo.superpositiongame.backend.ai.util.AiMoveMapper;
import io.github.winfeo.superpositiongame.backend.ai.util.AiStateMapper;
import io.github.winfeo.superpositiongame.backend.game.core.GameEngine;
import io.github.winfeo.superpositiongame.backend.game.effect.CardEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.CardEffectsRepository;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.HadamardEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.IdentityEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.MeasurementEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.MultiplicationEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.NoiseEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.QuantumLuckyEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.PauliEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.PhaseEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.ReshuffleEffect;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.game.AiDifficulty;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotState;
import io.github.winfeo.superpositiongame.backend.game.model.move.DoubleTapEffect;
import io.github.winfeo.superpositiongame.backend.game.model.move.Move;
import io.github.winfeo.superpositiongame.backend.game.model.move.PlayCard;
import io.github.winfeo.superpositiongame.backend.game.model.move.ReshuffleCard;
import io.github.winfeo.superpositiongame.backend.game.model.move.RotateDice;
import io.github.winfeo.superpositiongame.backend.game.model.move.SwapDices;
import io.github.winfeo.superpositiongame.backend.util.CardGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.AI_ID;
import static io.github.winfeo.superpositiongame.backend.ai.AiTestFixtures.HUMAN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("external-ai")
@EnabledIfSystemProperty(named = "ai.live", matches = "true")
class AiLiveIntegrationTest {
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    @ParameterizedTest(name = "реальный AI возвращает допустимый ход для сложности {0}")
    @EnumSource(AiDifficulty.class)
    void receivesAndProcessesRealAiMove(AiDifficulty difficulty) throws Exception {
        AiApiProperties properties = new AiApiProperties(
                configuredBaseUrl(),
                CONNECT_TIMEOUT,
                REQUEST_TIMEOUT
        );
        AiApiConfig config = new AiApiConfig();
        AiApiClient apiClient = new AiApiClient(config.aiRestClient(properties));
        GameEngine gameEngine = gameEngine();
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.aiTaskExecutor();

        try {
            GameState state = AiTestFixtures.defaultGameState();
            AiTurnService turnService = new AiTurnService(
                    apiClient,
                    new AiStateMapper(),
                    new AiMoveMapper(new ObjectMapper()),
                    new FallbackMoveSelector(gameEngine),
                    gameEngine,
                    executor,
                    properties
            );

            AiMoveDecision decision = turnService
                    .chooseMove("live-ai-test", state, AI_ID, difficulty)
                    .get(REQUEST_TIMEOUT.plusSeconds(2).toMillis(), TimeUnit.MILLISECONDS);

            assertThat(decision.fallback())
                    .as("Вместо ответа реального AI был использован запасной ход. Причина: %s", decision.reason())
                    .isFalse();
            assertThat(decision.move())
                    .as("AI должен вернуть ход")
                    .isNotNull();
            assertThat(gameEngine.applyMove(state, decision.move()))
                    .as("Ход реального AI должен приниматься GameEngine")
                    .isPresent();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void healthEndpointReportsAvailableAiModels() {
        HealthResponse response = restClient()
                .get()
                .uri("/health")
                .retrieve()
                .body(HealthResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("ok");
        assertThat(response.models()).contains("low", "mcts");
    }

    @Test
    void modelsEndpointReportsMctsLimitWithinServerTimeout() {
        ModelsResponse response = restClient()
                .get()
                .uri("/models")
                .retrieve()
                .body(ModelsResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.models()).contains("low", "mcts");
        assertThat(response.mctsTimeLimitSec())
                .isPositive()
                .isLessThanOrEqualTo(REQUEST_TIMEOUT.toSeconds());
    }

    @ParameterizedTest(name = "реальный AI обрабатывает карту {0} как {1}")
    @MethodSource("cardScenarios")
    void preservesCardIdAndReturnsMoveAcceptedByGameEngine(
            CardType cardType,
            Class<? extends Move> expectedMoveType
    ) {
        Card card = new Card("live-" + cardType.name().toLowerCase(), cardType);
        GameState state = AiTestFixtures.gameState(List.of(card));

        AiChooseActionResponseDTO response = chooseAction(state, AiDifficulty.LOW);
        JsonNode action = response.action();

        assertThat(response.model()).isEqualTo(AiDifficulty.LOW.apiValue());
        assertThat(action).isNotNull();
        assertThat(action.path("playerId").asText()).isEqualTo(AI_ID);
        assertThat(action.path("cardId").asText()).isEqualTo(card.id());

        Move move = moveMapper().toDomain(action, state, AI_ID);
        assertThat(move).isInstanceOf(expectedMoveType);
        assertThat(gameEngine().applyMove(state, move))
                .as("Ход для карты %s должен приниматься GameEngine", cardType)
                .isPresent();
    }

    @Test
    void reshuffleResponseCanBeProcessedByGameEngine() {
        Card reshuffle = new Card("live-reshuffle", CardType.RESHUFFLE);
        GameState state = AiTestFixtures.gameState(List.of(reshuffle));

        AiChooseActionResponseDTO response = chooseAction(state, AiDifficulty.LOW);
        Move move = moveMapper().toDomain(response.action(), state, AI_ID);

        if (move instanceof ReshuffleCard reshuffleMove) {
            List<String> handIds = state.players().get(AI_ID).hand().stream()
                    .map(Card::id)
                    .filter(id -> !id.equals(reshuffle.id()))
                    .toList();
            assertThat(reshuffleMove.cardsToChange())
                    .as("AI должен указать существующие карты из руки для замены")
                    .isNotEmpty()
                    .allMatch(handIds::contains);
        }

        assertThat(gameEngine().applyMove(state, move))
                .as("Ответ AI для RESHUFFLE должен преобразовываться в допустимый серверный ход")
                .isPresent();
    }

    @Test
    void aiDoesNotChooseProtectedSlotWhenLegalSlotExists() {
        Card card = new Card("live-protected-hadamard", CardType.HADAMARD);
        GameState state = withFirstAiSlotProtected(
                AiTestFixtures.gameState(List.of(card))
        );

        AiChooseActionResponseDTO response = chooseAction(state, AiDifficulty.LOW);
        JsonNode action = response.action();

        assertThat(action.path("type").asText()).isEqualTo("PLAY_CARD");
        String targetPlayerId = action.path("targetPlayerId").asText();
        int targetSlotIndex = action.path("targetSlotIndex").asInt(-1);
        PlayerState targetPlayer = state.players().get(targetPlayerId);

        assertThat(targetPlayer).isNotNull();
        assertThat(targetSlotIndex).isBetween(0, 3);
        assertThat(targetPlayer.slots().get(targetSlotIndex).isFrozen())
                .as("AI не должен выбирать защищённый слот")
                .isFalse();

        Move move = moveMapper().toDomain(action, state, AI_ID);
        assertThat(gameEngine().applyMove(state, move)).isPresent();
    }

    @Test
    void rejectsUnknownModel() {
        AiChooseActionRequestDTO validRequest = request(
                AiTestFixtures.defaultGameState(),
                AiDifficulty.LOW
        );
        AiChooseActionRequestDTO invalidRequest = copyRequest(
                validRequest,
                "unknown",
                validRequest.currentPlayerId()
        );

        assertThatThrownBy(() -> apiClient().chooseAction(invalidRequest))
                .isInstanceOfSatisfying(
                        RestClientResponseException.class,
                        exception -> assertThat(exception.getStatusCode().value()).isEqualTo(422)
                );
    }

    @Test
    void rejectsRequestWhenItIsNotAiTurn() {
        AiChooseActionRequestDTO validRequest = request(
                AiTestFixtures.defaultGameState(),
                AiDifficulty.LOW
        );
        AiChooseActionRequestDTO invalidRequest = copyRequest(
                validRequest,
                validRequest.model(),
                HUMAN_ID
        );

        assertThatThrownBy(() -> apiClient().chooseAction(invalidRequest))
                .isInstanceOfSatisfying(
                        RestClientResponseException.class,
                        exception -> assertThat(exception.getStatusCode().value()).isEqualTo(422)
                );
    }

    private static Stream<Arguments> cardScenarios() {
        return Stream.of(
                Arguments.of(CardType.HADAMARD, PlayCard.class),
                Arguments.of(CardType.HADAMARD_3, PlayCard.class),
                Arguments.of(CardType.PAULI_X, PlayCard.class),
                Arguments.of(CardType.PAULI_X_3, PlayCard.class),
                Arguments.of(CardType.PAULI_Y, PlayCard.class),
                Arguments.of(CardType.PAULI_Y_3, PlayCard.class),
                Arguments.of(CardType.PAULI_Z, PlayCard.class),
                Arguments.of(CardType.PAULI_Z_3, PlayCard.class),
                Arguments.of(CardType.PHASE_FORWARD, PlayCard.class),
                Arguments.of(CardType.PHASE_BACKWARD, PlayCard.class),
                Arguments.of(CardType.ROTATE_X, RotateDice.class),
                Arguments.of(CardType.ROTATE_Y, RotateDice.class),
                Arguments.of(CardType.ROTATE_Z, RotateDice.class),
                Arguments.of(CardType.SWAP, SwapDices.class),
                Arguments.of(CardType.IDENTITY, DoubleTapEffect.class),
                Arguments.of(CardType.MEASUREMENT, PlayCard.class),
                Arguments.of(CardType.KRONECKER_MULTIPLICATION, DoubleTapEffect.class)
        );
    }

    private AiChooseActionResponseDTO chooseAction(
            GameState state,
            AiDifficulty difficulty
    ) {
        AiChooseActionResponseDTO response = apiClient().chooseAction(
                request(state, difficulty)
        );
        assertThat(response).isNotNull();
        assertThat(response.action()).as("AI должен вернуть действие").isNotNull();
        return response;
    }

    private AiChooseActionRequestDTO request(
            GameState state,
            AiDifficulty difficulty
    ) {
        return new AiStateMapper().toRequest(state, AI_ID, difficulty);
    }

    private AiChooseActionRequestDTO copyRequest(
            AiChooseActionRequestDTO request,
            String model,
            String currentPlayerId
    ) {
        return new AiChooseActionRequestDTO(
                model,
                request.aiPlayerId(),
                request.opponentPlayerId(),
                request.dice(),
                request.targets(),
                request.aiHand(),
                request.opponentHand(),
                request.deck(),
                request.discard(),
                request.protectedSlots(),
                request.moveNumber(),
                request.maxMoves(),
                request.remainingMoves(),
                currentPlayerId,
                request.finished()
        );
    }

    private GameState withFirstAiSlotProtected(GameState state) {
        PlayerState aiPlayer = state.players().get(AI_ID);
        List<SlotState> slots = aiPlayer.slots().stream()
                .map(slot -> new SlotState(
                        slot.index(),
                        slot.ownerId(),
                        slot.initialDice(),
                        slot.dice(),
                        slot.appliedCards(),
                        slot.index() == 0
                ))
                .toList();

        Map<String, PlayerState> players = new LinkedHashMap<>(state.players());
        players.put(AI_ID, aiPlayer.copyWithSlots(slots));
        return state.copyWithPlayers(players);
    }

    private AiApiClient apiClient() {
        return new AiApiClient(restClient());
    }

    private RestClient restClient() {
        return new AiApiConfig().aiRestClient(properties());
    }

    private AiApiProperties properties() {
        return new AiApiProperties(
                configuredBaseUrl(),
                CONNECT_TIMEOUT,
                REQUEST_TIMEOUT
        );
    }

    private AiMoveMapper moveMapper() {
        return new AiMoveMapper(new ObjectMapper());
    }

    private URI configuredBaseUrl() {
        String configuredUrl = System.getProperty("ai.api.base-url");
        if (configuredUrl == null || configuredUrl.isBlank()) {
            configuredUrl = System.getenv("AI_API_BASE_URL");
        }

        assertThat(configuredUrl)
                .as("Для внешнего теста укажи ai.api.base-url или переменную AI_API_BASE_URL")
                .isNotBlank();
        return URI.create(configuredUrl);
    }

    private GameEngine gameEngine() {
        List<CardEffect> effects = List.of(
                new HadamardEffect(),
                new IdentityEffect(),
                new MeasurementEffect(),
                new MultiplicationEffect(),
                new NoiseEffect(),
                new QuantumLuckyEffect(),
                new PauliEffect(),
                new PhaseEffect(),
                new ReshuffleEffect()
        );
        return new GameEngine(
                new CardEffectsRepository(effects),
                new CardGenerator()
        );
    }

    private record HealthResponse(
            String status,
            List<String> models,
            @JsonProperty("neural_model") String neuralModel
    ) { }

    private record ModelsResponse(
            List<String> models,
            double mctsTimeLimitSec,
            boolean neuralLoaded
    ) { }
}
