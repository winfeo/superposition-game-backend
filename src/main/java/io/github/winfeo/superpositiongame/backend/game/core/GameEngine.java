package io.github.winfeo.superpositiongame.backend.game.core;

import io.github.winfeo.superpositiongame.backend.game.effect.CardEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.CardEffectsRepository;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardDescription;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardDescriptionRepository;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.game.*;
import io.github.winfeo.superpositiongame.backend.game.model.move.*;
import io.github.winfeo.superpositiongame.backend.game.util.ArrowCompatibilityUtil;
import io.github.winfeo.superpositiongame.backend.game.util.SameRowUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GameEngine {

    private final Map<Class<? extends Move>, MoveHandler<?>> handlers = new HashMap<>();
    private final CardEffectsRepository effectsRepository;

    public GameEngine(
            CardEffectsRepository effectsRepository
    ) {
        this.effectsRepository = effectsRepository;
        handlers.put(PlayCard.class, (MoveHandler<PlayCard>) this::handlePlayCard);
        handlers.put(RotateDice.class, (MoveHandler<RotateDice>) this::handleRotateDice);
        handlers.put(SwapDices.class, (MoveHandler<SwapDices>) this::handleSwapDices);
        handlers.put(DoubleTapEffect.class, (MoveHandler<DoubleTapEffect>) this::handleDoubleTapEffect);
    }

    public GameState applyMove(GameState state, Move move) {
        if (!state.currentPlayerId().equals(move.playerId())) {
            return state;
        }

        MoveHandler<?> handler = handlers.get(move.getClass());
        if (handler == null) return state;

        GameState newState = handle(handler, state, move);
        return newState;
    }

    @SuppressWarnings("unchecked")
    private <T extends Move> GameState handle(
            MoveHandler<T> handler,
            GameState state,
            Move move
    ) {
        return handler.handle(state, (T) move);
    }

    private GameState handlePlayCard(GameState state, PlayCard move) {
        //ищем игрока
        PlayerState player = state.players().get(move.playerId());
        if (player == null) return state;

        //ищем карту в его руке
        Card card = player.hand().stream()
                .filter(c -> c.id().equals(move.cardId()))
                .findFirst()
                .orElse(null);
        if (card == null) return state;

        //ищем эффект этой карты
        CardEffect effect = effectsRepository.getEffect(card.type());
        if (effect == null) return state;

        //ищем тарет id игрока
        String targetPlayerId = findPlayerId(state, move.targetPlayerId());

        //проверяем, что карта кладётся в тот же регистр (для Kron Multi)
        if (!SameRowUtil.isMoveAllowed(state, targetPlayerId)) {
            return state;
        }

        //обновляем состояние (применяем эффект)
        GameState stateAfterEffect = effect.apply(
                state,
                card,
                move.targetSlotIndex(),
                targetPlayerId
        );

        //фиксируем ряд, если это первый ход после Kron Multi
        stateAfterEffect = SameRowUtil.updateRowAfterMove(stateAfterEffect, targetPlayerId);

        //обновляем руку игрока
        PlayerState updatedPlayer = stateAfterEffect.players().get(move.playerId());
        List<Card> newHand = updatedPlayer.hand().stream()
                .filter(c -> !c.id().equals(move.cardId()))
                .toList();
        updatedPlayer = updatedPlayer.copyWithHand(newHand);
        Map<String, PlayerState> updatedPlayers = new HashMap<>(stateAfterEffect.players());
        updatedPlayers.put(move.playerId(), updatedPlayer);

        return stateAfterEffect.copyWithPlayers(updatedPlayers);
    }

    private GameState handleRotateDice(GameState state, RotateDice move) {
        String playerId = move.playerId();

        //ищем тарет id игрока
        String targetPlayerId = findPlayerId(state, move.targetPlayerId());

        //проверяем, что карта кладётся в тот же регистр (для Kron Multi)
        if (!SameRowUtil.isMoveAllowed(state, targetPlayerId)) {
            return state;
        }

        //ищем игрока, чей слот будем изменять
        PlayerState targetPlayer = state.players().get(targetPlayerId);
        //ищем игрока, который делает ход
        PlayerState actingPlayer = state.players().get(playerId);
        if (targetPlayer == null || actingPlayer == null) return state;

        //ищем слот, который будем изменять
        List<SlotState> slots = new ArrayList<>(targetPlayer.slots());
        SlotState slot = slots.get(move.targetSlotIndex());

        //ищем карту в его руке
        Card card = actingPlayer.hand().stream()
                .filter(c -> c.id().equals(move.cardId()))
                .findFirst()
                .orElse(null);
        if (card == null) return state;

        //проверяем можно ли обновить дайс картой
        CardDescription description = CardDescriptionRepository.get(card.type());
        boolean result = ArrowCompatibilityUtil.isCardCompatibleWithArrow(description, slot.dice());

        Dice updatedDice = slot.dice();
        if (result) {
            //обновляем состояние дайса
            updatedDice = new Dice(
                    slot.dice().id(),
                    move.newState(),
                    slot.dice().requiredState()
            );
        }

        //добавляем карту в слот
        List<Card> appliedCards = new ArrayList<>(slot.appliedCards());
        appliedCards.add(card);
        SlotState updatedSlot = slot
                .copyWithDice(updatedDice)
                .copyWithAppliedCards(appliedCards);
        slots.set(move.targetSlotIndex(), updatedSlot);

        //обновляем руку игрока
        PlayerState updatedTarget = targetPlayer.copyWithSlots(slots);
        List<Card> newHand = actingPlayer.hand().stream()
                .filter(c -> !c.id().equals(move.cardId()))
                .toList();

        PlayerState updatedActing = actingPlayer.copyWithHand(newHand);
        Map<String, PlayerState> updatedPlayers = new HashMap<>(state.players());
        if (targetPlayerId.equals(playerId)) {
            updatedPlayers.put(playerId,
                    updatedTarget.copyWithHand(updatedActing.hand()));
        } else {
            updatedPlayers.put(targetPlayerId, updatedTarget);
            updatedPlayers.put(move.playerId(), updatedActing);
        }

        //фиксируем ряд, если это первый ход после Kron Multi
        state = SameRowUtil.updateRowAfterMove(state, targetPlayerId);

        return state.copyWithPlayers(updatedPlayers);
    }

    private GameState handleSwapDices(GameState state, SwapDices move) {
        String playerId = move.playerId();
        //ищем игрока
        PlayerState player = state.players().get(playerId);
        if (player == null) return state;

        //ищем нужные слоты
        List<SlotState> slots = new ArrayList<>(player.slots());
        SlotState first = slots.get(move.firstSlotIndex());
        SlotState second = slots.get(move.secondSlotIndex());

        //меняем местами слоты
        slots.set(move.firstSlotIndex(), first.copyWithDice(second.dice()));
        slots.set(move.secondSlotIndex(), second.copyWithDice(first.dice()));

        //обновляем
        PlayerState updatedPlayer = player.copyWithSlots(slots);
        Map<String, PlayerState> updatedPlayers = new HashMap<>(state.players());
        updatedPlayers.put(playerId, updatedPlayer);

        return state.copyWithPlayers(updatedPlayers);
    }

    private GameState handleDoubleTapEffect(GameState state, DoubleTapEffect move) {
        //ищем игрока
        PlayerState player = state.players().get(move.playerId());
        if (player == null) return state;

        //ищем карту в его руке
        Card card = player.hand().stream()
                .filter(c -> c.id().equals(move.cardId()))
                .findFirst()
                .orElse(null);
        if (card == null) return state;

        //ищем эффект этой карты
        CardEffect effect = effectsRepository.getEffect(card.type());
        if (effect == null) return state;

        //обновляем состояние (применяем эффект)
        GameState stateAfterEffect = effect.apply(
                state,
                card,
                -1, //TODO переделать структуру? (не применяется ни на какой слот)
                move.playerId()
        );

        //обновляем руку игрока
        PlayerState updatedPlayer = stateAfterEffect.players().get(move.playerId());
        List<Card> newHand = updatedPlayer.hand().stream()
                .filter(c -> !c.id().equals(move.cardId()))
                .toList();
        updatedPlayer = updatedPlayer.copyWithHand(newHand);
        Map<String, PlayerState> updatedPlayers = new HashMap<>(stateAfterEffect.players());
        updatedPlayers.put(move.playerId(), updatedPlayer);

        return stateAfterEffect.copyWithPlayers(updatedPlayers);
    }

    private String findPlayerId( //TODO подумать, может быть сделать систему индексов
            GameState state,
            String targetPlayerEnum
    ) {
        if (targetPlayerEnum.equals(SlotOwner.PLAYER.name())) {
            return state.currentPlayerId();
        } else {
            return state.players().keySet()
                    .stream()
                    .filter(id -> !id.equals(state.currentPlayerId()))
                    .findFirst()
                    .orElse("");
        }
    }
}

