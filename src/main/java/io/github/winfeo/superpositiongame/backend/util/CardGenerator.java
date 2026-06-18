package io.github.winfeo.superpositiongame.backend.util;

import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class CardGenerator {
    private static final CardType[] TYPES = {
            CardType.PAULI_X,
            CardType.PAULI_Y,
            CardType.PAULI_Z,
            CardType.PAULI_X_3,
            CardType.PAULI_Y_3,
            CardType.PAULI_Z_3,
            CardType.HADAMARD,
            CardType.HADAMARD_3,
            CardType.PHASE_FORWARD,
            CardType.PHASE_BACKWARD,
            CardType.ROTATE_X,
            CardType.ROTATE_Y,
            CardType.ROTATE_Z,
            CardType.KRONECKER_MULTIPLICATION,
            CardType.IDENTITY,
            CardType.SWAP,
            CardType.MEASUREMENT,
//            CardType.QUANTUM_NOISE, //TODO доделать
//            CardType.QUANTUM_NOISE, //TODO доделать
//            CardType.QUANTUM_LUCKY, //TODO доделать
            CardType.RESHUFFLE
    };

    public Card generateRandomCard() {
        int randomIndex = ThreadLocalRandom.current().nextInt(TYPES.length);
        CardType type = TYPES[randomIndex];
        String randomId = UUID.randomUUID().toString();

        return new Card(randomId, type);
    }

    public List<Card> generateRandomCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(generateRandomCard());
        }

        return cards;
    }
}
