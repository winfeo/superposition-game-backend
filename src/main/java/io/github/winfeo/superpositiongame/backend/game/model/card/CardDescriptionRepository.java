package io.github.winfeo.superpositiongame.backend.game.model.card;

import java.util.Map;

public class CardDescriptionRepository {
    public static final Map<CardType, CardDescription> CARDS = Map.ofEntries(
            //PAULI
            Map.entry(CardType.PAULI_X, new CardDescription(
                    CardType.PAULI_X,
                    AxisRotation.X,
                    1,
                    false,
                    true
            )),

            Map.entry(CardType.PAULI_Y, new CardDescription(
                    CardType.PAULI_Y,
                    AxisRotation.Y,
                    1,
                    false,
                    true
            )),

            Map.entry(CardType.PAULI_Z, new CardDescription(
                    CardType.PAULI_Z,
                    AxisRotation.Z,
                    1,
                    false,
                    true
            )),
            Map.entry(CardType.PAULI_X_3, new CardDescription(
                    CardType.PAULI_X_3,
                    AxisRotation.X,
                    3,
                    true,
                    true
            )),

            Map.entry(CardType.PAULI_Y_3, new CardDescription(
                    CardType.PAULI_Y_3,
                    AxisRotation.Y,
                    3,
                    true,
                    true
            )),

            Map.entry(CardType.PAULI_Z_3, new CardDescription(
                    CardType.PAULI_Z_3,
                    AxisRotation.Z,
                    3,
                    true,
                    true
            )),

            //Rotate
            Map.entry(CardType.ROTATE_X, new CardDescription(
                    CardType.ROTATE_X,
                    AxisRotation.X,
                    1,
                    false,
                    true
            )),
            Map.entry(CardType.ROTATE_Y, new CardDescription(
                    CardType.ROTATE_Y,
                    AxisRotation.Y,
                    1,
                    false,
                    true
            )),
            Map.entry(CardType.ROTATE_Z, new CardDescription(
                    CardType.ROTATE_Z,
                    AxisRotation.Z,
                    1,
                    false,
                    true
            )),

            //Phase
            Map.entry(CardType.PHASE_FORWARD, new CardDescription(
                    CardType.PHASE_FORWARD,
                    AxisRotation.Z,
                    1,
                    false,
                    true
            )),
            Map.entry(CardType.PHASE_BACKWARD, new CardDescription(
                    CardType.PHASE_BACKWARD,
                    AxisRotation.Z,
                    1,
                    false,
                    false
            )),

            //Hadamard
            Map.entry(CardType.HADAMARD, new CardDescription(
                    CardType.HADAMARD,
                    AxisRotation.Y,
                    1,
                    false,
                    true
            )),
            Map.entry(CardType.HADAMARD_3, new CardDescription(
                    CardType.HADAMARD_3,
                    AxisRotation.Y,
                    3,
                    true,
                    true
            )),

            //Special
            Map.entry(CardType.SWAP, new CardDescription(
                    CardType.SWAP,
                    null,
                    2,
                    false,
                    null
            )),

            Map.entry(CardType.QUANTUM_NOISE, new CardDescription(
                    CardType.QUANTUM_NOISE,
                    null,
                    1,
                    false,
                    null
            )),

            Map.entry(CardType.KRONECKER_MULTIPLICATION, new CardDescription(
                    CardType.KRONECKER_MULTIPLICATION,
                    null,
                    1,
                    false,
                    null
            )),

            Map.entry(CardType.MEASUREMENT, new CardDescription(
                    CardType.MEASUREMENT,
                    null,
                    1,
                    false,
                    null
            )),

            Map.entry(CardType.IDENTITY, new CardDescription(
                    CardType.IDENTITY,
                    null,
                    1,
                    false,
                    null
            ))
    );

    public static CardDescription get(CardType type) {
        return CARDS.get(type);
    }
}
