package org.poker.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.poker.client.Card.Rank;
import org.poker.client.Card.Suit;
import org.poker.client.GameApi.AttemptChangeTokens;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.Set;
import org.poker.client.GameApi.SetTurn;
import org.poker.client.GameApi.SetVisibility;
import org.poker.client.GameApi.Shuffle;
import org.poker.client.GameApi.VerifyMove;
import org.poker.client.GameApi.VerifyMoveDone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class PokerLogic {

  public static final int SMALL_BLIND = 100;
  public static final int BIG_BLIND = 200;

  private static final String[] P = {"P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8"};
  private static final String C = "C";

  private static final String NUMBER_OF_PLAYERS = "numberOfPlayers";
  private static final String WHOSE_MOVE = "whoseMove";
  private static final String CURRENT_BETTER = "currentBetter";
  private static final String CURRENT_ROUND = "currentRound";
  private static final String PLAYERS_IN_HAND = "playersInHand";
  private static final String BOARD = "board";
  private static final String HOLE_CARDS = "holeCards";
  private static final String PLAYER_BETS = "playerBets";
  private static final String PLAYER_CHIPS = "playerChips";
  private static final String POTS = "pots";
  private static final String CHIPS = "chips";
  private static final String CURRENT_POT_BET = "currentPotBet";
  private static final String PLAYERS_IN_POT = "playersInPot";


  public VerifyMoveDone verify(VerifyMove verifyMove) {
    // TODO: I will implement this method in HW2
    return new VerifyMoveDone();
  }
  
  private void checkMoveIsLegal(VerifyMove verifyMove) {
    List<Operation> expectedOperations = getExpectedOperations(verifyMove);
  }
  
  private List<Operation> getExpectedOperations(VerifyMove verifyMove) {
    return getExpectedOperations(
        verifyMove.getLastState(),
        verifyMove.getLastMove(),
        verifyMove.getPlayerIds(),
        verifyMove.getLastMovePlayerId(),
        verifyMove.getPlayerIdToNumberOfTokensInPot());
  }
  
  private List<Operation> getExpectedOperations(
      Map<String, Object> lastApiState, List<Operation> lastMove, List<Integer> playerIds,
      int lastMovePlayerId, Map<Integer, Integer> playerIdToNumberOfTokensInPot) {
    if(lastApiState.isEmpty()) {
      if(lastMove.get(0) instanceof AttemptChangeTokens) {
        // Player's move was to "buy-in"
        int buyInAmount = playerIdToNumberOfTokensInPot.get(lastMovePlayerId);
        return getInitialBuyInMove(lastMovePlayerId, buyInAmount);
      }
      else {
        // Initial move performed by the dealer
        return getInitialMove(playerIds, playerIdToNumberOfTokensInPot);
      }
    }
    
    PokerState lastState = gameApiStateToPokerState(lastApiState,
        Player.values()[playerIds.indexOf(lastMovePlayerId)]);
    
    String lastMoveRound = (String) getSetOperationVal(CURRENT_ROUND, lastMove);
    boolean isNewRound = lastState.getCurrentRound().name().equals(lastMoveRound);

    int lastPlayerIndex = playerIds.indexOf(lastMovePlayerId);
    String lastPlayerStr = P[lastPlayerIndex];
    
    List<Player> lastStatePlayersInHand = lastState.getPlayersInHand();
    List<Integer> lastStatePlayerBets = lastState.getPlayerBets();
    List<Integer> lastStatePlayerChips = lastState.getPlayerChips();
    
    List<String> lastMovePlayersInHand = (List<String>)getSetOperationVal(PLAYERS_IN_HAND, lastMove);
    List<Integer> lastMovePlayerBets = (List<Integer>)getSetOperationVal(PLAYER_BETS, lastMove);
    List<Integer> lastMovePlayerChips = (List<Integer>)lastApiState.get(PLAYER_CHIPS);
    
    int lastPlayerBet = 0;
    if(lastMovePlayerBets != null) {
      lastPlayerBet =
          lastMovePlayerBets.get(lastPlayerIndex) - lastStatePlayerBets.get(lastPlayerIndex);
    }
    
    int lastRequiredBet = 0;
    if(!isNewRound) {
      lastRequiredBet = calculateLastRequiredBet(lastState);
    }
    //int lastPlayerPotAmount = playerChips.get(lastPlayerIndex);
    
    if(isNewRound) {
      // A new round has begun.
      // Possible moves are Fold, Check, Bet (can be all-in).
      if(playerFolded(lastStatePlayersInHand, lastMovePlayersInHand, lastPlayerIndex)) {
        //TODO: Player folded
      }
      else if(lastPlayerBet == 0) {
        //TODO: Player checked
      }
      else {
        //TODO: Player posted a bet
      }
    }
    else {
      // The same round has continued.
      // Possible moves are Fold, Check, Bet (can be all-in), Raise (can be all-in)
      if(playerFolded(lastStatePlayersInHand, lastMovePlayersInHand, lastPlayerIndex)) {
        //TODO: Player folded
      }
      else if(lastPlayerBet == 0) {
        //TODO: Player checked
      }
      else if(lastPlayerBet == lastRequiredBet) {
        //TODO: Player called
      }
      else if(lastRequiredBet == 0){
        //TODO: Player posted a bet
      }
      else {
        //TODO: Player raised.
      }
    }
    //TODO: check other cases
    return Lists.newArrayList();
  }
  
  private List<Operation> getInitialBuyInMove(int playerId, int buyInAmount) {
    return ImmutableList.<Operation>of(
        new AttemptChangeTokens(
            ImmutableMap.<Integer, Integer>of(playerId, buyInAmount*(-1)),
            ImmutableMap.<Integer, Integer>of(playerId, buyInAmount)));
  }
  
  List<Operation> getInitialMove(List<Integer> playerIds, Map<Integer, Integer> startingChips) {

    check(playerIds.size() >= 2 && playerIds.size() <= 9);

    int numberOfPlayers = playerIds.size();
    boolean isHeadsUp = (numberOfPlayers == 2);
    int smallBlindPos = isHeadsUp ? 0 : 1;
    int bigBlindPos = isHeadsUp ? 1 : 2;


    List<Operation> operations = new ArrayList<Operation>();

    // In heads-up match, P0(dealer) to act.
    // Otherwise, player after big blind to act
    operations.add(new SetTurn(isHeadsUp ? playerIds.get(0) : playerIds.get(3 % numberOfPlayers)));
    
    operations.add(new Set(NUMBER_OF_PLAYERS, numberOfPlayers));

    // Big blind will be the current better
    operations.add(new Set(CURRENT_BETTER, isHeadsUp ? P[1] : P[2]));

    operations.add(new Set(CURRENT_ROUND, BettingRound.PRE_FLOP.name()));

    // Sets all the 52 cards as 2c, 2d, ... As, Ah
    for (int i = 0; i < 52; i++) {
      operations.add(new Set(C + i, cardIdToString(i)));
    }

    // Initially small blind and big blind will be in the hand
    operations.add(new Set(PLAYERS_IN_HAND, ImmutableList.of(P[smallBlindPos], P[bigBlindPos])));

    // Assign hole cards C(2i) and C(2i+1) to player i
    List<List<String>> holeCardList = Lists.newArrayList();
    for (int i = 0; i < numberOfPlayers; i++) {
      // We're giving C0, C1 to P0; C2, C3 to P1, so on..
      // (though in real world first card is not dealt to dealer)
      holeCardList.add(ImmutableList.of(C + (i * 2), C + (i * 2 + 1)));
    }
    operations.add(new Set(HOLE_CARDS, holeCardList));
    
    // Assign next 5 cards as the board
    operations.add(new Set(BOARD, getIndicesInRange(numberOfPlayers * 2, numberOfPlayers * 2 + 4)));
    
    // Post small and big blinds
    List<Integer> playerBetList = Lists.newArrayList();
    for (int i = 0; i < numberOfPlayers; i++) {
      if (i == smallBlindPos) playerBetList.add(SMALL_BLIND);
      else if (i == bigBlindPos) playerBetList.add(BIG_BLIND);
      else playerBetList.add(0);
    }
    operations.add(new Set(PLAYER_BETS, playerBetList));
    
    // Assign starting chips (minus blinds)
    List<Integer> playerChipsList = Lists.newArrayList();
    for (int i = 0; i < numberOfPlayers; i++) {
      int playerId = playerIds.get(i);
      if (i == smallBlindPos)
        playerChipsList.add(startingChips.get(playerId) - SMALL_BLIND);
      else if (i == bigBlindPos)
        playerChipsList.add(startingChips.get(playerId) - BIG_BLIND);
      else
        playerChipsList.add(startingChips.get(playerId));
    }
    operations.add(new Set(PLAYER_CHIPS, playerChipsList));
    
    // Create the main pot with small and big blind already in it
    Map<String, Object> mainPot = ImmutableMap.<String, Object>of(
        CHIPS, SMALL_BLIND + BIG_BLIND, 
        CURRENT_POT_BET, BIG_BLIND,
        PLAYERS_IN_POT, ImmutableList.of(P[smallBlindPos], P[bigBlindPos]));
    operations.add(new Set(POTS, ImmutableList.of(mainPot)));
    
    // shuffle the cards
    operations.add(new Shuffle(getCardsInRange(0, 51)));
    
    // Make hole cards visible to players holding them
    for (int i = 0; i < numberOfPlayers; i++) {
      operations.add(new SetVisibility(C + (i * 2), ImmutableList.of(playerIds.get(i))));
      operations.add(new SetVisibility(C + (i * 2 + 1), ImmutableList.of(playerIds.get(i))));
    }
    // Make remaining cards not visible to anyone
    for (int i = 2 * numberOfPlayers; i < 52; i++) {
      operations.add(new SetVisibility(C + i, ImmutableList.<Integer>of()));
    }
    
    return operations;
  }
  
  
  List<Operation> doCheckMove(PokerState lastState) {
    List<Operation> operations = Lists.newArrayList();
    int currentBetterIndex = lastState.getCurrentBetter().ordinal();
    int nextPlayerIndex = calculateNextPlayerIndex(lastState, currentBetterIndex);
    if(nextPlayerIndex != -1) {
      operations.add(new Set(WHOSE_MOVE, P[nextPlayerIndex]));
      if(!lastState.getPlayersInHand().contains(lastState.getWhoseMove())) {
        List<Player> newPlayersInHand = ImmutableList.<Player>builder()
            .addAll(ImmutableList.copyOf(lastState.getPlayersInHand()))
            .add(lastState.getWhoseMove())
            .build();
        operations.add(new Set(PLAYERS_IN_HAND, newPlayersInHand));
      }
      return operations;
    }
    else {
      // TODO: Start a new round
      return null;
    }
  }
  
  List<Operation> doCallMove(PokerState state) {
    List<Operation> operations = Lists.newArrayList();
    int playerIndex = state.getWhoseMove().ordinal();
    int currentBetterIndex = state.getCurrentBetter().ordinal();
    int currentBetAmount = state.getPlayerBets().get(playerIndex);
    int currentPlayerChips = state.getPlayerChips().get(playerIndex);
    int requiredBetAmount = calculateLastRequiredBet(state);
    int nextPlayerIndex = calculateNextPlayerIndex(state, currentBetterIndex);
    if(nextPlayerIndex != -1) {
      operations.add(new Set(WHOSE_MOVE, P[nextPlayerIndex]));
      if(!state.getPlayersInHand().contains(state.getWhoseMove())) {
        List<Player> newPlayersInHand = addOrReplaceInList(state.getPlayersInHand(),
            state.getWhoseMove(), state.getPlayersInHand().size());
        operations.add(new Set(PLAYERS_IN_HAND, newPlayersInHand));
      }
      List<Integer> newPlayerBets = addOrReplaceInList(state.getPlayerBets(),
          Integer.valueOf(requiredBetAmount), playerIndex);
      operations.add(new Set(PLAYER_BETS, newPlayerBets));
      List<Integer> newPlayerChips = addOrReplaceInList(state.getPlayerChips(),
          Integer.valueOf(currentPlayerChips - (requiredBetAmount - currentBetAmount)),
          playerIndex);
      operations.add(new Set(PLAYER_CHIPS, newPlayerChips));
      List<Map<String, Object>> newPots = addChipsInPots(state.getPots(), state.getWhoseMove(),
          requiredBetAmount - currentBetAmount);
      operations.add(new Set(POTS, newPots));
    }
    else {
      // TODO: Start a new round
    }
    //TODO: fix
    return null;
  }
  
  private boolean playerFolded(List<Player> statePlayers, List<String> movePlayers,
      int playerIndex) {
    if(movePlayers == null) {
      // Player didn't update list of players in hand
      if(statePlayers.contains(Player.values()[playerIndex])) {
        return false;
      }
      else {
        return true;
      }
    }
    else {
      // Players updated list of players in hand
      if(movePlayers.contains(P[playerIndex])) {
        return false;
      }
      else {
        return true;
      }
    }
  }
  
  private int calculateLastRequiredBet(PokerState lastState) {
    List<Pot> pots = lastState.getPots();
    int totalRequiredBet = 0;
    for(Pot pot : pots) {
      totalRequiredBet += pot.getCurrentPotBet();
    }
    return totalRequiredBet;
  }
  
  private <T> ImmutableList<T> addOrReplaceInList(List<T> list, T obj, int index) {
    if(index == list.size()) {
      //append is required
      return ImmutableList.<T>builder().addAll(list).add(obj).build();
    }
    else if(index == list.size() - 1) {
      //replace last element
      return ImmutableList.<T>builder().addAll(list.subList(0, index)).add(obj).build();
    }
    else if(index >= 0 && index < list.size() - 1) {
      //replace some element (not last)
      return ImmutableList.<T>builder().
          addAll(list.subList(0, index)).add(obj).
          addAll(list.subList(index+1, list.size() - 1)).build();
    }
    else {
      throw new IllegalArgumentException("Invalid index " + index);
    }
  }
  
  //TODO: Add current better cross checking
  private int calculateNextPlayerIndex(PokerState lastState, int currentBetterIndex) {
    if(lastState.getCurrentRound() == BettingRound.PRE_FLOP) {
      return calculateNextPlayerIndexPreFlop(lastState, currentBetterIndex);
    }
    List<Player> playersInHand = lastState.getPlayersInHand();
    int lastPlayerListIndex = playersInHand.indexOf(lastState.getWhoseMove());
    for(int i=1; i<playersInHand.size(); i++) {
      int listIndex = (i + lastPlayerListIndex) % playersInHand.size();
      if(listIndex == currentBetterIndex) {
        // Reached the current better. The round will end.
        return -1;
      }
      if(lastState.getPlayerChips().get(i) == 0) {
        // This player is all-in. Continue to next player
        continue;
      }
      return listIndex;
    }
    return -1;
  }
  
  private int calculateNextPlayerIndexPreFlop(PokerState lastState, int currentBetterIndex) {
    int numberOfPlayers = lastState.getNumberOfPlayers();
    int lastPlayerIndex = lastState.getWhoseMove().ordinal();
    for(int i = 1; i < numberOfPlayers; i++) {
      int index = (i + lastPlayerIndex) % numberOfPlayers;
      if(index == currentBetterIndex) {
        if(index == getBigBlindIndex(lastState) &&
            lastState.getPlayerBets().get(index) == getBigBlindAmount()) {
          return index;    
        }
        else {
          return -1;
        }
      }
      if(lastState.getFoldedPlayers().contains(Player.values()[index])) {
        // This player already folded. Continue to next player.
        continue;
      }
      if(lastState.getPlayerChips().get(index) == 0) {
        // This player is all-in. Continue to next player.
        continue;
      }
      return index;
    }
    return -1;
  }
  
  private List<Map<String, Object>> addChipsInPots(List<Pot> pots, Player player, int chips) {
    List<Map<String, Object>> newPots = Lists.newArrayList();
    int i = pots.size() - 1;
    while(i >= 0 && chips > 0) {
      Pot pot = pots.get(i);
      int potBet = pot.getCurrentPotBet();
      int chipsToAdd;
      if(chips >= potBet) {
        chipsToAdd = potBet;
        chips -= potBet;
      }
      else {
        chipsToAdd = chips;
        chips = 0;
      }
      newPots.add(0, ImmutableMap.<String, Object>of(
          CHIPS, pot.getChips() + chipsToAdd,
          CURRENT_POT_BET, pot.getCurrentPotBet(),
          PLAYERS_IN_POT, getStrPlayerList(pot.getPlayersInPot())));
    }
    for(; i >= 0; i--) {
      Pot pot = pots.get(i);
      newPots.add(0, ImmutableMap.<String, Object>of(
          CHIPS, pot.getChips(),
          CURRENT_POT_BET, pot.getCurrentPotBet(),
          PLAYERS_IN_POT, getStrPlayerList(pot.getPlayersInPot())));
    }
    return newPots;
  }
  
  private List<String> getStrPlayerList(List<Player> players) {
    ImmutableList.Builder<String> playerListBuilder = ImmutableList.builder();
    for(Player player : players) {
      playerListBuilder.add(player.name());
    }
    return playerListBuilder.build();
  }
  
  private int getBigBlindIndex(PokerState state) {
    int numberOfPlayers = state.getNumberOfPlayers();
    return numberOfPlayers > 2 ? 2 : 1;
  }
  
  private int getBigBlindAmount() {
    return BIG_BLIND;
  }
  
  /**
   * Finds a Set operation containing the given key and returns its value.
   * Default return value is null. In case multiple Set exist with same key,
   * value from first one is returned.
   * 
   * @param key
   * @param operations
   * @return Object from first Set containing given key; null otherwise.
   */
  private Object getSetOperationVal(String key, List<Operation> operations) {
    Object value = null;
    if(operations != null && key != null) {
      for(Operation operation : operations) {
        if(operation instanceof Set && key.equals(((Set) operation).getKey())) {
          value = ((Set) operation).getValue();
          break;
        }
      }
    }
    return value;
  }
  
  private PokerState gameApiStateToPokerState(Map<String, Object> gameApiState,
      Player whoseMove) {
    //TODO
    return null;
  }
  
  // Following utility methods have been copied from CheatLogic.java
  // in project https://github.com/yoav-zibin/cheat-game
  
  List<Integer> getIndicesInRange(int fromInclusive, int toInclusive) {
    List<Integer> keys = Lists.newArrayList();
    for (int i = fromInclusive; i <= toInclusive; i++) {
      keys.add(i);
    }
    return keys;
  }

  List<String> getCardsInRange(int fromInclusive, int toInclusive) {
    List<String> keys = Lists.newArrayList();
    for (int i = fromInclusive; i <= toInclusive; i++) {
      keys.add(C + i);
    }
    return keys;
  }

  String cardIdToString(int cardId) {
    checkArgument(cardId >= 0 && cardId < 52);
    int rank = (cardId / 4);
    String rankString = Rank.values()[rank].getFirstLetter();
    int suit = cardId % 4;
    String suitString = Suit.values()[suit].getFirstLetterLowerCase();
    return rankString + suitString;
  }
  
  private void check(boolean val, Object... debugArguments) {
    if (!val) {
      throw new RuntimeException("We have a hacker! debugArguments="
          + Arrays.toString(debugArguments));
    }
  }
}
