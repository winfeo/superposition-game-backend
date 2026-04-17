package io.github.winfeo.superpositiongame.backend.game.effect;

import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CardEffectsRepository {
    private final List<CardEffect> effects;

    public CardEffectsRepository(List<CardEffect> effects) {
        this.effects = effects;
    }

    public CardEffect getEffect(CardType type) {
        return effects.stream()
                .filter(e -> e.supports(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Нет эффекта для карты: " + type));
    }
}
