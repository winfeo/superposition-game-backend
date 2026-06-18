package io.github.winfeo.superpositiongame.backend.util;

import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DiceGenerator {
    private static final DiceType[] TYPES = {
            DiceType.PLUS,
            DiceType.MINUS,
            DiceType.ZERO,
            DiceType.ONE,
            DiceType.I,
            DiceType.I_MINUS
    };

    public List<DiceType> generateRequiredStates(int count) {
        List<DiceType> requiredStates = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int requiredIndex = ThreadLocalRandom.current().nextInt(TYPES.length);
            requiredStates.add(TYPES[requiredIndex]);
        }
        return requiredStates;
    }

    public Dice generateDiceWithRequiredState(DiceType requiredState) {
        int randomIndex;

        do {
            randomIndex = ThreadLocalRandom.current().nextInt(TYPES.length);
        } while (TYPES[randomIndex] == requiredState);
        DiceType randomType = TYPES[randomIndex];
        String randomId = UUID.randomUUID().toString();
        return new Dice(randomId, randomType, requiredState);
    }

    public Dice generateRandomDice() {
        int requiredIndex = ThreadLocalRandom.current().nextInt(TYPES.length);
        DiceType requiredType = TYPES[requiredIndex];

        int randomIndex;
        do {
            randomIndex = ThreadLocalRandom.current().nextInt(TYPES.length);
        } while (randomIndex == requiredIndex);
        DiceType randomType = TYPES[randomIndex];
        String randomId = UUID.randomUUID().toString();

        return new Dice(randomId, randomType, requiredType);
    }

    public List<Dice> generateRandomDices(int count) {
        List<Dice> dices = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dices.add(generateRandomDice());
        }

        return dices;
    }
}
