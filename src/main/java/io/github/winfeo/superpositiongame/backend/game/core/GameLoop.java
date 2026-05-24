package io.github.winfeo.superpositiongame.backend.game.core;

import io.github.winfeo.superpositiongame.backend.game.core.service.GameEventPublisher;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceState;
import io.github.winfeo.superpositiongame.backend.game.model.game.GamePhase;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotState;
import io.github.winfeo.superpositiongame.backend.util.CardGenerator;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class GameLoop {
    private final GameEventPublisher publisher;
    private final CardGenerator cardGenerator;
    private static final long TIMER_TURN_DURATION_MS = 45_000;

    public GameLoop (
            GameEventPublisher publisher,
            CardGenerator cardGenerator
    ) { //TODO убрать. Пока так, чтобы на клиенте обновлялось состояние карт у второго игрока
        this.publisher = publisher;
        this.cardGenerator = cardGenerator;
    }
    public GameState startGame(String playerA_id, String playerB_id) {
        //TODO случайно выбирать, кто первый ходит
        PlayerState playerA = new PlayerState(playerA_id);
        PlayerState playerB = new PlayerState(playerB_id);

        GameState state = GameState.initial(playerA, playerB);
        state = initSlots(state);
        state = dealCards(state);
//        state = dealDices(state);
        state = startTurn(state, playerA_id);

        return state;
    }

    private GameState dealCards(GameState state) {
        Map<String, PlayerState> players = new HashMap<>(state.players());

        for (var entry: players.entrySet()) {
            PlayerState player = entry.getValue();
            List<Card> hand = new ArrayList<>(player.hand());
            int missing = 6 - hand.size();
            for (int i = 0; i < missing; i++) {
                hand.add(cardGenerator.generateRandomCard());
            }

            players.put(player.id(), player.copyWithHand(hand));
        }

        return state.copyWithPlayers(players);
    }

    private GameState dealDices(GameState state) {
        Map<String, PlayerState> players = new HashMap<>(state.players());

        for (var entry: players.entrySet()) {
            PlayerState player = entry.getValue();

            List<SlotState> slots = new ArrayList<>(player.slots());
            List<SlotState> updatedSlots = new ArrayList<>();

            for (SlotState slot: slots) {
                Dice randomDice = generateRandomDice();
                SlotState updated = new SlotState(
                        slot.index(),
                        slot.ownerId(),
                        randomDice
                );
                updatedSlots.add(updated);
            }

            players.put(
                    player.id(),
                    player.copyWithSlots(updatedSlots)
            );
        }

        return state.copyWithPlayers(players);
    }

//    Card generateRandomCard() { //TODO переделать
//        //TODO сделать просто пул карт для тестирования?
//        CardType[] types = {
//                CardType.PAULI_Y,
//                CardType.PAULI_X,
//                CardType.PAULI_Z,
////                CardType.PAULI_X_3,
////                CardType.PAULI_Y_3,
////                CardType.PAULI_Z_3,
////                CardType.HADAMARD,
////                CardType.HADAMARD_3,
////                CardType.PHASE_FORWARD,
////                CardType.PHASE_BACKWARD,
////                CardType.ROTATE_X,
////                CardType.ROTATE_Y,
////                CardType.ROTATE_Z,
////                CardType.KRONECKER_MULTIPLICATION,
////                CardType.IDENTITY,
////                CardType.SWAP,
////                CardType.MEASUREMENT,
//                CardType.RESHUFFLE
//        };
////        CardType[] types = CardType.values();
//
//        int randomNumber = ThreadLocalRandom.current().nextInt(types.length);
//        CardType type = types[randomNumber];
//
//        String randomId = UUID.randomUUID().toString();
//        return new Card(randomId, type);
////        return new Card(randomId, CardType.SWAP);
//    }

    private Dice generateRandomDice() {
        DiceState[] values = DiceState.values();
        int randomNumber = ThreadLocalRandom.current().nextInt(values.length);
        DiceState randomState = values[randomNumber];
        DiceState requiredState = DiceState.PLUS; //TODO передлать, пока +

        return new Dice(
                UUID.randomUUID().toString(),
                randomState,
                requiredState
        );
    }

    private GameState initSlots(GameState state) {
        Map<String, PlayerState> players = new HashMap<>(state.players());

        for (var entry : players.entrySet()) {
            PlayerState player = entry.getValue();

            List<SlotState> slots = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                slots.add(new SlotState(
                        i,
                        player.id(),
                        generateRandomDice()
                ));
            }

            players.put(
                    player.id(),
                    player.copyWithSlots(slots)
            );
        }

        return state.copyWithPlayers(players);
    }

    public GameState startTurn(GameState state, String playerId) {
        long now = System.currentTimeMillis(); //Текущее время сервера
        long turnEndsAt = now + TIMER_TURN_DURATION_MS; //Старт таймера

        return state
                .copyWithCurrentPlayerId(playerId)
                .copyWithPhase(GamePhase.MOVE_START)
                .copyWithServerTime(now)
                .copyWithTurnEndsAt(turnEndsAt);
    }

    private GameState endTurn(GameState state) {
        String next = getNextPlayer(state);
        PlayerState nextPlayer = state.players().get(next).copyWithRemainingMoves(1);

        Map<String, PlayerState> updated = new HashMap<>(state.players());
        updated.put(next, nextPlayer);

        int newTurnNumber = state.turnNumber() + 1;

        long now = System.currentTimeMillis();
        long turnEndsAt = now + TIMER_TURN_DURATION_MS;

        return state
                .copyWithPlayers(updated)
                .copyWithTurnNumber(newTurnNumber)
                .copyWithCurrentPlayerId(next)
                .copyWithPhase(GamePhase.MOVE_START)
                .copyWithServerTime(now)
                .copyWithTurnEndsAt(turnEndsAt);
    }

    public GameState afterMove(
            GameState state,
            String playerId,
            String gameId
    ) {
        String winnerId = findWinnerId(state);
        if (winnerId != null) {
            return state
                    .copyWithPhase(GamePhase.GAME_FINISHED) //TODO заканчивать игру на сервере, доделать
                    .copyWithWinnerId(winnerId);
        }

        PlayerState playerState = state.players().get(playerId);
        int remainingMoves = playerState.remainingMoves() - 1;
        PlayerState updatedPlayer = playerState.copyWithRemainingMoves(remainingMoves);

        Map<String, PlayerState> updatedPlayers = new HashMap<>(state.players());
        updatedPlayers.put(playerId, updatedPlayer);

        GameState updatedState = state.copyWithPlayers(updatedPlayers);

        if (remainingMoves > 0) {
            return updatedState;
        }

        GameState clearedState = new GameState( //сбрасываем активный ряд (Kron Multi)
                updatedState.phase(),
                updatedState.currentPlayerId(),
                updatedState.players(),
                updatedState.turnNumber(),
                null,
                null,
                updatedState.serverTime(),
                updatedState.turnEndsAt()
        );

        GameState afterTurn = endTurn(clearedState);

        if (afterTurn.turnNumber() % 2 == 0) {
            publisher.sendToUser( //TODO убрать
                    playerId,
                    gameId,
                    afterTurn
            );

            afterTurn = dealCards(afterTurn);
        }

        return afterTurn;
    }

    private String findWinnerId(GameState state) {
        for (Map.Entry<String, PlayerState> entry: state.players().entrySet()) {
            String playerId = entry.getKey();
            PlayerState player = entry.getValue();

            boolean isWinner = player.slots()
                    .stream()
                    .allMatch(slot ->
                            slot.dice().state() == slot.dice().requiredState()
                    );

            if (isWinner) return playerId;
        }

        return null;
    }

    private String getNextPlayer(GameState state) {
        List<String> ids = new ArrayList<>(state.players().keySet());
        int idx = ids.indexOf(state.currentPlayerId());
        return ids.get((idx + 1) % ids.size());
    }

    public GameState forceEndTurn(GameState state) { //если таймер закончился
        GameState clearedState = new GameState(
                state.phase(),
                state.currentPlayerId(),
                state.players(),
                state.turnNumber(),
                null,
                state.winnerId(),
                state.serverTime(),
                state.turnEndsAt()
        );

        return endTurn(clearedState);
    }
}
