package io.github.winfeo.superpositiongame.backend.game.util;

import io.github.winfeo.superpositiongame.backend.game.model.card.Card;

import java.util.ArrayList;
import java.util.List;

public final class AddCardsUtil {
    public static List<Card> addCard(List<Card> list, Card card) {
        List<Card> newList = new ArrayList<>(list);
        newList.add(card);
        return newList;
    }
}
