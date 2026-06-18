package io.github.winfeo.superpositiongame.backend.game.core;

import io.github.winfeo.superpositiongame.backend.entity.db.User;
import io.github.winfeo.superpositiongame.backend.game.core.service.GameEventPublisher;
import io.github.winfeo.superpositiongame.backend.game.model.card.Card;
import io.github.winfeo.superpositiongame.backend.game.model.dice.Dice;
import io.github.winfeo.superpositiongame.backend.game.model.dice.DiceType;
import io.github.winfeo.superpositiongame.backend.game.model.game.GamePhase;
import io.github.winfeo.superpositiongame.backend.game.model.game.GameState;
import io.github.winfeo.superpositiongame.backend.game.model.game.PlayerState;
import io.github.winfeo.superpositiongame.backend.game.model.game.SlotState;
import io.github.winfeo.superpositiongame.backend.repository.UserRepository;
import io.github.winfeo.superpositiongame.backend.util.CardGenerator;
import io.github.winfeo.superpositiongame.backend.util.DiceGenerator;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class GameLoop {
    private final UserRepository userRepository;
    private final GameEventPublisher publisher;
    private final CardGenerator cardGenerator;
    private final DiceGenerator diceGenerator;
    private static final long TIMER_TURN_DURATION_MS = 45_000;

    public GameLoop (
            UserRepository userRepository,
            GameEventPublisher publisher,
            CardGenerator cardGenerator,
            DiceGenerator diceGenerator
    ) { //TODO убрать. Пока так, чтобы на клиенте обновлялось состояние карт у второго игрока
        this.userRepository = userRepository;
        this.publisher = publisher;
        this.cardGenerator = cardGenerator;
        this.diceGenerator = diceGenerator;
    }
    public GameState startGame(String playerA_id, String playerB_id) {
        //TODO случайно выбирать, кто первый ходит

        String playerNicknameA = null;
        try {
            Long userId = Long.parseLong(playerA_id);
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                playerNicknameA = user.getNickname();
            }
        } catch (NumberFormatException ignored) { }

        String playerNicknameB = null;
        try {
            Long userId = Long.parseLong(playerB_id);
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                playerNicknameB = user.getNickname();
            }
        } catch (NumberFormatException ignored) { }

        PlayerState playerA = new PlayerState(playerA_id, playerNicknameA);
        PlayerState playerB = new PlayerState(playerB_id, playerNicknameB);

        GameState state = GameState.initial(playerA, playerB);

        //общие требуемые состояния для слотов
        List<DiceType> requiredStates = diceGenerator.generateRequiredStates(4);
        state = initSlots(state, requiredStates);
        state = dealCards(state);
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

    private GameState initSlots(GameState state, List<DiceType> requiredStates) {
        Map<String, PlayerState> players = new HashMap<>(state.players());

        for (var entry : players.entrySet()) {
            PlayerState player = entry.getValue();
            List<SlotState> slots = new ArrayList<>();

            for (int i = 0; i < requiredStates.size(); i++) {
                Dice dice = diceGenerator.generateDiceWithRequiredState(requiredStates.get(i));
                slots.add(new SlotState(
                        i,
                        player.id(),
                        dice
                ));
            }

            players.put(
                    player.id(),
                    player.copyWithSlots(slots)
            );
        }

        return state.copyWithPlayers(players);
    }

    private String getUserNickname(String playerId) {
        try {
            Long userId = Long.parseLong(playerId);
            User user = userRepository.findById(userId).orElse(null);
            return user!= null? user.getNickname(): null;
        } catch (NumberFormatException ignored) {
            return null;
        }
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
        if (state.winnerId() != null) {
            return state.copyWithPhase(GamePhase.GAME_FINISHED);
        }

        String winnerId = findWinnerId(state);
        if (winnerId != null) {
            return state
                    .copyWithPhase(GamePhase.GAME_FINISHED)
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
