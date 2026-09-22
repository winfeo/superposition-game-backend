package io.github.winfeo.superpositiongame.backend.game;

import io.github.winfeo.superpositiongame.backend.game.core.GameEngine;
import io.github.winfeo.superpositiongame.backend.game.effect.CardEffectsRepository;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.NoiseEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.QuantumLuckyEffect;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GamePhase;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotOwner;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotState;
import io.github.winfeo.superpositiongame.backend.game.model.move.PlayCard;
import io.github.winfeo.superpositiongame.backend.util.CardGenerator;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SpecialCardsGameEngineTest {
    private static final String PLAYER_ID = "player";
    private static final String OPPONENT_ID = "opponent";
    private static final Card NOISE = new Card("noise", CardType.QUANTUM_NOISE);
    private static final Card LUCKY = new Card("lucky", CardType.QUANTUM_LUCKY);

    private final GameEngine gameEngine = new GameEngine(
            new CardEffectsRepository(List.of(
                    new NoiseEffect(),
                    new QuantumLuckyEffect()
            )),
            new CardGenerator()
    );

    @Test
    void quantumNoiseCancelsLastCardAndCanReturnSlotToInitialState() {
        SlotState slot = slot(
                DiceType.ZERO,
                DiceType.ONE,
                DiceType.PLUS,
                List.of(new Card("pauli-x", CardType.PAULI_X)),
                false
        );

        GameState result = apply(state(NOISE, slot), NOISE, SlotOwner.PLAYER);
        SlotState updatedSlot = result.players().get(PLAYER_ID).slots().get(0);

        assertThat(updatedSlot.dice().state()).isEqualTo(DiceType.ZERO);
        assertThat(updatedSlot.appliedCards()).isEmpty();
        assertThat(result.players().get(PLAYER_ID).hand()).isEmpty();
    }

    @Test
    void quantumNoiseCancelsMeasurementAndUnfreezesSlot() {
        SlotState slot = slot(
                DiceType.ZERO,
                DiceType.ZERO,
                DiceType.ONE,
                List.of(new Card("measurement", CardType.MEASUREMENT)),
                true
        );

        GameState result = apply(state(NOISE, slot), NOISE, SlotOwner.PLAYER);
        SlotState updatedSlot = result.players().get(PLAYER_ID).slots().get(0);

        assertThat(updatedSlot.dice().state()).isEqualTo(DiceType.ZERO);
        assertThat(updatedSlot.appliedCards()).isEmpty();
        assertThat(updatedSlot.isFrozen()).isFalse();
    }

    @Test
    void quantumNoiseRestoresStateBeforeForwardPhaseGate() {
        SlotState slot = slot(
                DiceType.PLUS,
                DiceType.I,
                DiceType.ZERO,
                List.of(new Card("phase", CardType.PHASE_FORWARD)),
                false
        );

        GameState result = apply(state(NOISE, slot), NOISE, SlotOwner.PLAYER);

        assertThat(result.players().get(PLAYER_ID).slots().get(0).dice().state())
                .isEqualTo(DiceType.PLUS);
    }

    @Test
    void quantumNoiseCannotBePlayedOnEmptySlot() {
        SlotState slot = slot(
                DiceType.ZERO,
                DiceType.ZERO,
                DiceType.ONE,
                List.of(),
                false
        );

        assertThat(gameEngine.applyMove(
                state(NOISE, slot),
                new PlayCard(PLAYER_ID, NOISE.id(), 0, SlotOwner.PLAYER.name())
        )).isEmpty();
    }

    @Test
    void quantumNoiseCannotCancelExcludedCards() {
        List<CardType> excludedTypes = List.of(
                CardType.ROTATE_X,
                CardType.PAULI_X_3,
                CardType.HADAMARD_3,
                CardType.QUANTUM_LUCKY
        );

        for (CardType excludedType : excludedTypes) {
            SlotState slot = slot(
                    DiceType.ZERO,
                    DiceType.ONE,
                    DiceType.PLUS,
                    List.of(new Card("excluded", excludedType)),
                    false
            );

            assertThat(gameEngine.applyMove(
                    state(NOISE, slot),
                    new PlayCard(PLAYER_ID, NOISE.id(), 0, SlotOwner.PLAYER.name())
            )).as("Не должна отменяться карта %s", excludedType).isEmpty();
        }
    }

    @Test
    void quantumLuckySetsOwnSlotToRequiredStateAndRemainsInSlot() {
        SlotState slot = slot(
                DiceType.ZERO,
                DiceType.PLUS,
                DiceType.I_MINUS,
                List.of(),
                false
        );

        GameState result = apply(state(LUCKY, slot), LUCKY, SlotOwner.PLAYER);
        SlotState updatedSlot = result.players().get(PLAYER_ID).slots().get(0);

        assertThat(updatedSlot.dice().state()).isEqualTo(DiceType.I_MINUS);
        assertThat(updatedSlot.appliedCards()).containsExactly(LUCKY);
        assertThat(result.players().get(PLAYER_ID).hand()).isEmpty();
    }

    @Test
    void quantumLuckyCannotBePlayedOnOpponentSlot() {
        SlotState slot = slot(
                DiceType.ZERO,
                DiceType.PLUS,
                DiceType.I_MINUS,
                List.of(),
                false
        );

        assertThat(gameEngine.applyMove(
                state(LUCKY, slot),
                new PlayCard(PLAYER_ID, LUCKY.id(), 0, SlotOwner.OPPONENT.name())
        )).isEmpty();
    }

    @Test
    void quantumLuckyCannotBeWastedOnCompletedSlot() {
        SlotState slot = slot(
                DiceType.ZERO,
                DiceType.ONE,
                DiceType.ONE,
                List.of(),
                false
        );

        assertThat(gameEngine.applyMove(
                state(LUCKY, slot),
                new PlayCard(PLAYER_ID, LUCKY.id(), 0, SlotOwner.PLAYER.name())
        )).isEmpty();
    }

    private GameState apply(GameState state, Card card, SlotOwner target) {
        return gameEngine.applyMove(
                state,
                new PlayCard(PLAYER_ID, card.id(), 0, target.name())
        ).orElseThrow();
    }

    private GameState state(Card handCard, SlotState playerSlot) {
        PlayerState player = new PlayerState(
                PLAYER_ID,
                "Игрок",
                List.of(handCard),
                List.of(playerSlot),
                false,
                1
        );
        PlayerState opponent = new PlayerState(
                OPPONENT_ID,
                "Соперник",
                List.of(),
                List.of(slot(DiceType.ONE, DiceType.ONE, DiceType.ZERO, List.of(), false)),
                false,
                1
        );
        Map<String, PlayerState> players = new LinkedHashMap<>();
        players.put(PLAYER_ID, player);
        players.put(OPPONENT_ID, opponent);

        return new GameState(
                GamePhase.MOVE_START,
                PLAYER_ID,
                players,
                1,
                null,
                null,
                1_000L,
                60_000L
        );
    }

    private SlotState slot(
            DiceType initialState,
            DiceType currentState,
            DiceType requiredState,
            List<Card> appliedCards,
            boolean isFrozen
    ) {
        Dice initialDice = new Dice("dice", initialState, requiredState);
        Dice currentDice = new Dice("dice", currentState, requiredState);
        return new SlotState(
                0,
                PLAYER_ID,
                initialDice,
                currentDice,
                appliedCards,
                isFrozen
        );
    }
}
