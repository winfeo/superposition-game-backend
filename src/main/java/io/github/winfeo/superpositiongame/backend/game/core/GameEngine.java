package io.github.winfeo.superpositiongame.backend.game.core;

import io.github.winfeo.superpositiongame.backend.game.effect.CardEffect;
import io.github.winfeo.superpositiongame.backend.game.effect.CardEffectsRepository;
import io.github.winfeo.superpositiongame.backend.game.effect.effect.ReshuffleEffect;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardDescription;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardDescriptionRepository;
import io.github.winfeo.superpositiongame.backend.game.model.card.CardType;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.game.*;
import io.github.winfeo.superpositiongame.backend.game.model.move.*;
import io.github.winfeo.superpositiongame.backend.game.util.ArrowCompatibilityUtil;
import io.github.winfeo.superpositiongame.backend.game.util.SameRowUtil;
import io.github.winfeo.superpositiongame.backend.util.CardGenerator;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GameEngine {
    private final Map<Class<? extends Move>, MoveHandler<?>> handlers = new HashMap<>();
    private final CardEffectsRepository effectsRepository;
    private final CardGenerator cardGenerator;

    public GameEngine(
            CardEffectsRepository effectsRepository,
            CardGenerator cardGenerator
    ) {
        this.effectsRepository = effectsRepository;
        this.cardGenerator = cardGenerator;
        handlers.put(PlayCard.class, (MoveHandler<PlayCard>) this::handlePlayCard);
        handlers.put(RotateDice.class, (MoveHandler<RotateDice>) this::handleRotateDice);
        handlers.put(SwapDices.class, (MoveHandler<SwapDices>) this::handleSwapDices);
        handlers.put(DoubleTapEffect.class, (MoveHandler<DoubleTapEffect>) this::handleDoubleTapEffect);
        handlers.put(ReshuffleCard.class, (MoveHandler<ReshuffleCard>) this::handleReshuffleCard);
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

        //проверяем, что карта не в заморозке или можно применить карты
        PlayerState targetPlayer = state.players().get(targetPlayerId);
        if (targetPlayer == null) return state;

        SlotState slot = targetPlayer.slots().get(move.targetSlotIndex());

        if (slot.isFrozen() &&
                card.type() != CardType.QUANTUM_NOISE &&
                card.type() != CardType.SWAP) {
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

        //проверяем, что карта не в заморозке или можно применить карты
        if (slot.isFrozen() &&
                card.type() != CardType.QUANTUM_NOISE &&
                card.type() != CardType.SWAP) {
            return state;
        }

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

        //ищем игроков
        PlayerState player = state.players().get(playerId);
        PlayerState opponent = state.players().values().stream()
                .filter(p -> !p.id().equals(move.playerId()))
                .findFirst()
                .orElse(null);
        if (player == null || opponent == null) return state;

        //ищем нужные слоты
        List<SlotState> playerSlots = new ArrayList<>(player.slots());
        List<SlotState> opponentSlots = new ArrayList<>(opponent.slots());

        SlotState first;
        SlotState second;

        if (SlotOwner.PLAYER.name().equals(move.firstSlotOwner())) {
            first = playerSlots.get(move.firstSlotIndex());
        } else {
            first = opponentSlots.get(move.firstSlotIndex());
        }

        if (SlotOwner.PLAYER.name().equals(move.secondSlotOwner())) {
            second = playerSlots.get(move.secondSlotIndex());
        } else {
            second = opponentSlots.get(move.secondSlotIndex());
        }

//        List<SlotState> slots = new ArrayList<>(player.slots());
//        SlotState first = slots.get(move.firstSlotIndex());
//        SlotState second = slots.get(move.secondSlotIndex());

        //меняем местами слоты
//        slots.set(move.firstSlotIndex(), first.copyWithDice(second.dice()));
//        slots.set(move.secondSlotIndex(), second.copyWithDice(first.dice()));

        if (SlotOwner.PLAYER.name().equals(move.firstSlotOwner())) {
            playerSlots.set(
                    move.firstSlotIndex(),
                    playerSlots.get(move.firstSlotIndex()).copyWithDice(second.dice())
            );
        } else {
            opponentSlots.set(
                    move.firstSlotIndex(),
                    opponentSlots.get(move.firstSlotIndex()).copyWithDice(second.dice())
            );
        }

        if (SlotOwner.PLAYER.name().equals(move.secondSlotOwner())) {
            playerSlots.set(
                    move.secondSlotIndex(),
                    playerSlots.get(move.secondSlotIndex()).copyWithDice(first.dice())
            );
        } else {
            opponentSlots.set(
                    move.secondSlotIndex(),
                    opponentSlots.get(move.secondSlotIndex()).copyWithDice(first.dice())
            );
        }

        //обновляем
//        PlayerState updatedPlayer = player.copyWithSlots(slots);
        PlayerState updatedPlayer = player.copyWithSlots(playerSlots);
        PlayerState updatedOpponent = opponent.copyWithSlots(opponentSlots);

        //удаляем карту из руки
        List<Card> newHand = player.hand().stream()
                .filter(c -> !c.id().equals(move.cardId()))
                .toList();

        updatedPlayer = updatedPlayer.copyWithHand(newHand);


        Map<String, PlayerState> updatedPlayers = new HashMap<>(state.players());
        updatedPlayers.put(player.id(), updatedPlayer);
        updatedPlayers.put(opponent.id(), updatedOpponent);

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

    private GameState handleReshuffleCard(GameState state, ReshuffleCard move) {
        //ищем игрока
        PlayerState player = state.players().get(move.playerId());
        if (player == null) return state;

        //ищем карту в его руке
        Card card = player.hand().stream()
                .filter(c -> c.id().equals(move.cardId()))
                .findFirst()
                .orElse(null);

        if (card == null) return state;
        if (card.type() != CardType.RESHUFFLE) return state;

        //находим выбранные карты в руке
        List<Card> selectedCards = player.hand().stream()
                .filter(c -> move.cardsToChange().contains(c.id()))
                .toList();
        if (selectedCards.size() != move.cardsToChange().size()) return state;
        if (selectedCards.isEmpty() || selectedCards.size() > 4) return state;

        //список ID карт для удаления (Reshuffle + выбранные)
        Set<String> cardsToRemove = new HashSet<>();
        cardsToRemove.add(card.id());
        cardsToRemove.addAll(move.cardsToChange());

        //удаляем карты из руки
        List<Card> remainingHand = player.hand().stream()
                .filter(c -> !cardsToRemove.contains(c.id()))
                .toList();

        //генерируем новые карты
        int cardsToGenerate = selectedCards.size();
        List<Card> newCards = cardGenerator.generateRandomCards(cardsToGenerate);

        //обновляем руку игрока
        List<Card> updatedHand = new ArrayList<>(remainingHand);
        updatedHand.addAll(newCards);

        //обновляем игрока
        PlayerState updatedPlayer = player.copyWithHand(updatedHand);
        Map<String, PlayerState> updatedPlayers = new HashMap<>(state.players());
        updatedPlayers.put(move.playerId(), updatedPlayer);

        System.out.println("=== RESHUFFLE EFFECT ===");
        System.out.println("Player: " + move.playerId());
        System.out.println("Reshuffle card removed: " + card.id());
        System.out.println("Cards removed: " + move.cardsToChange());
        System.out.println("Cards remaining: " + remainingHand.size());
        System.out.println("Cards generated: " + newCards.size());
        System.out.println("Final hand size: " + updatedHand.size());

        return state.copyWithPlayers(updatedPlayers);
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

