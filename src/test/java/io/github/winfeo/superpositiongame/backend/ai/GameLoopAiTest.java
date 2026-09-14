package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.entity.db.User;
import io.github.winfeo.superpositiongame.backend.game.core.GameLoop;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.repository.UserRepository;
import io.github.winfeo.superpositiongame.backend.util.CardGenerator;
import io.github.winfeo.superpositiongame.backend.util.DiceGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameLoopAiTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardGenerator cardGenerator;
    @Mock
    private DiceGenerator diceGenerator;

    @Test
    void createsAiPlayerWithoutDatabaseAccountAndUsesSharedGameSetup() {
        User human = new User();
        human.setNickname("Игрок");
        when(userRepository.findById(42L)).thenReturn(Optional.of(human));
        when(diceGenerator.generateRequiredStates(4)).thenReturn(List.of(
                DiceType.ZERO,
                DiceType.ONE,
                DiceType.PLUS,
                DiceType.MINUS
        ));
        AtomicInteger diceCounter = new AtomicInteger();
        when(diceGenerator.generateDiceWithRequiredState(any(DiceType.class)))
                .thenAnswer(invocation -> new Dice(
                        "dice-" + diceCounter.incrementAndGet(),
                        DiceType.I,
                        invocation.getArgument(0)
                ));
        AtomicInteger cardCounter = new AtomicInteger();
        when(cardGenerator.generateRandomCard())
                .thenAnswer(invocation -> new Card(
                        "card-" + cardCounter.incrementAndGet(),
                        CardType.HADAMARD
                ));

        GameState state = new GameLoop(
                userRepository,
                cardGenerator,
                diceGenerator
        ).startAiGame("42", "ai-generated-id");

        assertThat(state.currentPlayerId()).isEqualTo("42");
        assertThat(state.players().get("42").nickname()).isEqualTo("Игрок");
        assertThat(state.players().get("ai-generated-id").nickname()).isEqualTo("AI");
        assertThat(state.players().values())
                .allSatisfy(player -> {
                    assertThat(player.hand()).hasSize(6);
                    assertThat(player.slots()).hasSize(4);
                });
        verify(userRepository).findById(42L);
        verifyNoMoreInteractions(userRepository);
    }
}
