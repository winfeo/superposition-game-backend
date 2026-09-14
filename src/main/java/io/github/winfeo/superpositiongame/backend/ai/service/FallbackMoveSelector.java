package io.github.winfeo.superpositiongame.backend.ai.service;

import io.github.winfeo.superpositiongame.backend.game.core.GameEngine;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotOwner;
import io.github.winfeo.superpositiongame.backend.game.model.move.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// локальный выбор запасного хода, если AI ничего не вернул
// выбирается первое допустимое действие (move)
@Component
public class FallbackMoveSelector {
    private final GameEngine gameEngine;

    public FallbackMoveSelector(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public Optional<Move> select(
            GameState state,
            String aiPlayerId
    ) {
        PlayerState aiPlayer = state.players().get(aiPlayerId);
        if (aiPlayer == null) return Optional.empty();

        List<Move> candidates = new ArrayList<>();
        for (Card card: aiPlayer.hand()) {
            addCandidates(candidates, aiPlayer, card);
        }
        Collections.shuffle(candidates);

        for (Move candidate: candidates) {
            try {
                if (gameEngine.applyMove(state, candidate).isPresent()) {
                    return Optional.of(candidate);
                }
            } catch (RuntimeException ignored) { }
        }

        return Optional.empty();
    }

    private void addCandidates(
            List<Move> candidates,
            PlayerState player,
            Card card
    ) {
        switch (card.type()) {
            case IDENTITY, KRONECKER_MULTIPLICATION -> candidates.add(new DoubleTapEffect(player.id(), card.id()));
            case ROTATE_X, ROTATE_Y, ROTATE_Z -> addRotateCandidates(candidates, player.id(), card.id());
            case SWAP -> addSwapCandidates(candidates, player.id(), card.id());
            case RESHUFFLE -> addReshuffleCandidates(candidates, player, card);
            case QUANTUM_LUCKY -> { }
            default -> addPlayCardCandidates(candidates, player.id(), card.id());
        }
    }

    private void addPlayCardCandidates(List<Move> candidates, String playerId, String cardId) {
        for (SlotOwner owner : SlotOwner.values()) {
            for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
                candidates.add(new PlayCard(playerId, cardId, slotIndex, owner.name()));
            }
        }
    }

    private void addRotateCandidates(List<Move> candidates, String playerId, String cardId) {
        for (SlotOwner owner : SlotOwner.values()) {
            for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
                for (DiceType newState : DiceType.values()) {
                    candidates.add(new RotateDice(
                            playerId,
                            cardId,
                            slotIndex,
                            newState,
                            owner.name()
                    ));
                }
            }
        }
    }

    private void addSwapCandidates(List<Move> candidates, String playerId, String cardId) {
        List<FallbackTarget> targets = new ArrayList<>();
        for (SlotOwner owner : SlotOwner.values()) {
            for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
                targets.add(new FallbackTarget(owner, slotIndex));
            }
        }

        for (int first = 0; first < targets.size(); first++) {
            for (int second = first + 1; second < targets.size(); second++) {
                FallbackTarget firstTarget = targets.get(first);
                FallbackTarget secondTarget = targets.get(second);
                candidates.add(new SwapDices(
                        playerId,
                        cardId,
                        firstTarget.slotIndex(),
                        secondTarget.slotIndex(),
                        firstTarget.owner().name(),
                        secondTarget.owner().name()
                ));
            }
        }
    }

    private void addReshuffleCandidates(List<Move> candidates, PlayerState player, Card reshuffle) {
        List<String> replaceableCards = player.hand().stream()
                .map(Card::id)
                .filter(id -> !id.equals(reshuffle.id()))
                .toList();

        int maximum = Math.min(4, replaceableCards.size());
        for (int count = 1; count <= maximum; count++) {
            candidates.add(new ReshuffleCard(
                    player.id(),
                    reshuffle.id(),
                    replaceableCards.subList(0, count)
            ));
        }
    }
}
