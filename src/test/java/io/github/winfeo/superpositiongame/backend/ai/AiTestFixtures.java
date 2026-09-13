package io.github.winfeo.superpositiongame.backend.ai;

import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GamePhase;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AiTestFixtures {
    public static final String AI_ID = "ai-player";
    public static final String HUMAN_ID = "human-player";

    private AiTestFixtures() {
    }

    public static GameState gameState(List<Card> aiHand) {
        PlayerState ai = player(AI_ID, "AI", aiHand, 1);
        PlayerState human = player(
                HUMAN_ID,
                "Игрок",
                List.of(new Card("human-card", CardType.PAULI_X)),
                1
        );

        Map<String, PlayerState> players = new LinkedHashMap<>();
        players.put(AI_ID, ai);
        players.put(HUMAN_ID, human);

        return new GameState(
                GamePhase.MOVE_START,
                AI_ID,
                players,
                7,
                null,
                null,
                1_000L,
                System.currentTimeMillis() + 60_000L
        );
    }

    public static GameState defaultGameState() {
        return gameState(List.of(
                new Card("play-card", CardType.HADAMARD),
                new Card("rotate-card", CardType.ROTATE_X),
                new Card("swap-card", CardType.SWAP),
                new Card("reshuffle-card", CardType.RESHUFFLE),
                new Card("identity-card", CardType.IDENTITY)
        ));
    }

    public static PlayerState player(
            String id,
            String nickname,
            List<Card> hand,
            int remainingMoves
    ) {
        return new PlayerState(
                id,
                nickname,
                hand,
                slots(id),
                false,
                remainingMoves
        );
    }

    private static List<SlotState> slots(String ownerId) {
        DiceType[] states = {
                DiceType.ZERO,
                DiceType.ONE,
                DiceType.PLUS,
                DiceType.MINUS
        };
        DiceType[] targets = {
                DiceType.ONE,
                DiceType.ZERO,
                DiceType.MINUS,
                DiceType.PLUS
        };

        return java.util.stream.IntStream.range(0, 4)
                .mapToObj(index -> {
                    Dice dice = new Dice(
                            ownerId + "-dice-" + index,
                            states[index],
                            targets[index]
                    );
                    return new SlotState(
                            index,
                            ownerId,
                            dice,
                            dice,
                            List.of(),
                            index == 1
                    );
                })
                .toList();
    }
}
