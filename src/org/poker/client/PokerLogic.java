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

  private static final String PREVIOUS_MOVE = "previousMove";
  private static final String PREVIOUS_MOVE_ALLIN = "previousMoveAllIn";
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
    
    // Handle initial move (empty last state)
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
    
    // First operation will be SetTurn and second operation will be Set(PreviuosMove)
    //TODO: except in case of end game. handle that case.
    PokerMove previousMove = PokerMove.valueOf((String)((Set)lastMove.get(1)).getValue());

    if (previousMove == PokerMove.FOLD) {
      return doFoldMove(lastState, playerIds);
    }
    else if (previousMove == PokerMove.CHECK) {
      return doCheckMove(lastState, playerIds);
    }
    else if (previousMove == PokerMove.CALL) {
      return doCallMove(lastState, playerIds);
    }
    else if (previousMove == PokerMove.BET) {
      return doBetMove(lastState, playerIds);
    }
    else if (previousMove == PokerMove.RAISE) {
      return doRaiseMove(lastState, playerIds);
    }
    
    //All possible states have been checked
    throw new IllegalStateException("No Expected move can be found");
  }
  
  /**
   * Generates List of Operation objects for performing
   * a Fold move by the current player.
   * 
   * @param lastState
   * @param playerIds
   * @return
   */
  private List<Operation> doFoldMove(PokerState lastState, List<Integer> playerIds) {
    
    if(isGameOverAfterFold(lastState)) {
      return doGameOverAfterFold(lastState);
    }
    
    if (isNewRoundStarting(lastState, PokerMove.FOLD)) {
      return doNewRoundAfterFoldMove();
    }
    
    List<Operation> operations = Lists.newArrayList();
    
    int nextTurnIndex = getNextTurnIndex(lastState);
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.FOLD.name()));
    
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    // Remove player from PLAYERS_IN_HAND
    List<String> playersInHand = getStrPlayerList(lastState.getPlayersInHand());
    List<String> newPlayersInHand = removeFromList(playersInHand, lastState.getWhoseMove().name());
    operations.add(new Set(PLAYERS_IN_HAND, newPlayersInHand));
    
    // Remove player from all pots
    List<Pot> pots = lastState.getPots();
    List<Map<String, Object>> newPots = Lists.newArrayList();
    for(Pot pot : pots) {
      List<String> playersInPot = getStrPlayerList(pot.getPlayersInPot());
      List<String> newPlayersInPot = removeFromList(playersInPot, lastState.getWhoseMove().name());
      newPots.add(ImmutableMap.<String, Object>of(
          CHIPS, pot.getChips(),
          CURRENT_POT_BET, pot.getCurrentPotBet(),
          PLAYERS_IN_POT, newPlayersInPot,
          PLAYER_BETS, pot.getPlayerBets()));
    }
    operations.add(new Set(POTS, newPots));
    
    return operations;
  }
  
  
  /**
   * Generates List of Operation objects for performing
   * a Check move by the current player.
   * 
   * @param lastState
   * @param playerIds
   * @return
   */
  List<Operation> doCheckMove(PokerState lastState, List<Integer> playerIds) {
    
    boolean isNewRoundStarting = isNewRoundStarting(lastState, PokerMove.CHECK);
    if (isNewRoundStarting) {
      return doNewRoundAfterCheckMove(lastState , playerIds);
    }
    
    List<Operation> operations = Lists.newArrayList();
    
    int nextTurnIndex = getNextTurnIndex(lastState);
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.CHECK.name()));
    
    operations.add(new Set(PREVIOUS_MOVE_ALLIN, new Boolean(false)));
    
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    return operations;
  }
  
  
  private List<Operation> doNewRoundAfterCheckMove(PokerState lastState, List<Integer> playerIds) {
    // TODO Auto-generated method stub
    List<Operation> operations = Lists.newArrayList();
    int nextTurnIndex = getNextTurnIndex(lastState);
    BettingRound nextRound = calculateNextRound(lastState.getCurrentRound());
    boolean endGameFlag = nextRound == BettingRound.SHOWDOWN ? true : false;
    
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.CHECK.name()));
    
    operations.add(new Set(PREVIOUS_MOVE_ALLIN, new Boolean(false)));
    
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    operations.add(new Set(CURRENT_ROUND, nextRound.name()));
    
    operations.addAll(setBoardVisibility(lastState,nextRound));
    
    
    
    
    return null;
  }

  private BettingRound calculateNextRound(BettingRound currentRound) {
    return BettingRound.values()[currentRound.ordinal()+1];
  }
  /**
   * Generates List of Operation objects for performing
   * a Call move by the current player.
   * 
   * @param lastState
   * @return
   */
  List<Operation> doCallMove(PokerState lastState, List<Integer> playerIds) {
    
    if(isGameOverAfterCall(lastState)) {
      return doGameOverAfterCallMove();
    }
    
    if (isNewRoundStarting(lastState, PokerMove.CALL)) {
      return doNewRoundAfterCallMove();
    }
    
    List<Operation> operations = Lists.newArrayList();
    
    int nextTurnIndex = getNextTurnIndex(lastState);
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.CALL.name()));
    
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    // PLAYERS_IN_HAND should already contain this player
    
    int playerIndex = lastState.getWhoseMove().ordinal();
    int requiredBetAmount = calculateLastRequiredBet(lastState);
    List<Integer> newPlayerBets = addOrReplaceInList(lastState.getPlayerBets(),
        Integer.valueOf(requiredBetAmount), playerIndex);
    operations.add(new Set(PLAYER_BETS, newPlayerBets));
    
    int currentBetAmount = lastState.getPlayerBets().get(playerIndex);
    int currentPlayerChips = lastState.getPlayerChips().get(playerIndex);
    List<Integer> newPlayerChips = addOrReplaceInList(lastState.getPlayerChips(),
        Integer.valueOf(currentPlayerChips - (requiredBetAmount - currentBetAmount)),
        playerIndex);
    operations.add(new Set(PLAYER_CHIPS, newPlayerChips));
    
    // Add call amount to all the pots
    List<Pot> pots = lastState.getPots();
    List<Map<String, Object>> newPots = Lists.newArrayList();
    for(Pot pot : pots) {
      List<String> playersInPot = getStrPlayerList(pot.getPlayersInPot());
      List<String> newPlayersInPot = addToList(playersInPot, lastState.getWhoseMove().name());
      List<Integer> playerPotBets = pot.getPlayerBets();
      List<Integer> newPlayerPotBets = addOrReplaceInList(playerPotBets,
          Integer.valueOf(pot.getCurrentPotBet()), playerIndex);
      newPots.add(ImmutableMap.<String, Object>of(
          CHIPS, pot.getChips(),
          CURRENT_POT_BET, pot.getCurrentPotBet(),
          PLAYERS_IN_POT, newPlayersInPot,
          PLAYER_BETS, newPlayerPotBets));
    }
    operations.add(new Set(POTS, newPots));
    
    return operations;
  }
  

  private boolean isGameOverAfterCall(PokerState lastState) {
    // TODO Auto-generated method stub
    
    return false;
  }

  //TODO: provide bet amount as a parameter
  private List<Operation> doBetMove(PokerState lastState, List<Integer> playerIds) {
    
    List<Operation> operations = Lists.newArrayList();
    
    int nextTurnIndex = getNextTurnIndex(lastState);
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.CALL.name()));
    
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    int lastPlayerIndex = lastState.getWhoseMove().ordinal(); 
    operations.add(new Set(CURRENT_BETTER, P[lastPlayerIndex]));
    
    //TODO: handle other operations
    
    return operations;
  }

  private List<Operation> doRaiseMove(PokerState lastState, List<Integer> playerIds) {
    //TODO: code this
    return null;
  }

  
  //ToDo : Discuss All-In with Rohan again
  private boolean isNewRoundStarting(PokerState lastState,
      PokerMove previousMove) {
    if (previousMove == PokerMove.BET || previousMove == PokerMove.RAISE) {
      return false;
    }
    
    // If its a big blind move in preflop and he checks, round ends
    if (lastState.getCurrentRound() == BettingRound.PRE_FLOP && 
        calculateLastRequiredBet(lastState) == getBigBlindAmount() &&
        isBigBlindMove(lastState) &&
        (previousMove == PokerMove.CHECK || previousMove == PokerMove.FOLD)) {
      return true;
    }
    
    boolean isNewRoundStarting = false;
    List<Player> playersInHand = lastState.getPlayersInHand();
    int currentBetterIndex = playersInHand.indexOf(lastState.getCurrentBetter());
    int lastPlayerListIndex = playersInHand.indexOf(lastState.getWhoseMove());
    for(int i=1; i<playersInHand.size(); i++) {
      int listIndex = (i + lastPlayerListIndex) % playersInHand.size();
      if(listIndex == currentBetterIndex) {
        // Reached the current better. The round will end unless its preflop bigblind bet.        
        isNewRoundStarting = true;
        break;
      }
      if(lastState.getPlayerChips().get(i) == 0) {
        // This player is all-in. Continue to next player
        continue;
      }
    }
    
    // Special case for pre-flop bigblind bet. The round goes on to big blind
    if (isNewRoundStarting) {
      if (lastState.getCurrentRound() == BettingRound.PRE_FLOP &&
          calculateLastRequiredBet(lastState) == getBigBlindAmount()) {
         isNewRoundStarting = false;
      }
    }
    
    return isNewRoundStarting;
  }

  private boolean isAllInHigherThanCall(PokerState lastState) {
    int whoseTurnIndex = lastState.getWhoseMove().ordinal();
    int toCallAmount = calculateLastRequiredBet(lastState) - 
        lastState.getPlayerBets().get(whoseTurnIndex);
    int lastMoveAmount = lastState.getPlayerChips().get(whoseTurnIndex);
    
    return (lastMoveAmount > toCallAmount);
  }

  public List<Operation> getInitialBuyInMove(int playerId, int buyInAmount) {
    return ImmutableList.<Operation>of(
        new AttemptChangeTokens(
            ImmutableMap.<Integer, Integer>of(playerId, buyInAmount*(-1)),
            ImmutableMap.<Integer, Integer>of(playerId, buyInAmount)));
  }
  
  public List<Operation> getInitialMove(List<Integer> playerIds, Map<Integer, Integer> startingChips) {
    check(playerIds.size() >= 2 && playerIds.size() <= 9);

    int numberOfPlayers = playerIds.size();
    boolean isHeadsUp = (numberOfPlayers == 2);
    int smallBlindPos = isHeadsUp ? 0 : 1;
    int bigBlindPos = isHeadsUp ? 1 : 2;


    List<Operation> operations = new ArrayList<Operation>();

    // In heads-up match, P0(dealer) to act.
    // Otherwise, player after big blind to act
    operations.add(new SetTurn(isHeadsUp ? playerIds.get(0) : playerIds.get(3 % numberOfPlayers)));
    
    //operations.add(new Set())
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
  
  private int calculateLastRequiredBet(PokerState lastState) {
    List<Pot> pots = lastState.getPots();
    int totalRequiredBet = 0;
    for(Pot pot : pots) {
      totalRequiredBet += pot.getCurrentPotBet();
    }
    return totalRequiredBet;
  }
  
  /**
   * If given object doesn't exist in the list, adds it to the end.
   * Otherwise returns a copy of the original list.
   * 
   * @param list List to alter
   * @param obj Object to add
   * @return Possibly altered list, definitely containing given element
   */
  private <T> ImmutableList<T> addToList(List<T> list, T obj) {
    int index = list.indexOf(obj);
    if(index == -1) {
      return addOrReplaceInList(list, obj, list.size());
    }
    return ImmutableList.<T>copyOf(list);
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
    
  private <T> ImmutableList<T> removeFromList(List<T> list, T obj) {
    int index = list.indexOf(obj);
    if(index == -1) {
      throw new IllegalArgumentException("Object not found in list");
    }
    return removeFromList(list, index);
  }
  
  private <T> ImmutableList<T> removeFromList(List<T> list, int index) {
    if(index == list.size() - 1) {
      //replace last element
      return ImmutableList.<T>builder().addAll(list.subList(0, index)).build();
    }
    else if(index >= 0 && index < list.size() - 1) {
      //replace some element (not last)
      return ImmutableList.<T>builder().
          addAll(list.subList(0, index)).
          addAll(list.subList(index+1, list.size() - 1)).build();
    }
    else {
      throw new IllegalArgumentException("Invalid index " + index);
    }
  }
  
  /**
   * This method gets the index of the next player to act.
   * It assumes that round will not end after the current move.
   * 
   * @param state Last PokerState
   * @return index of next player to act
   */
  private int getNextTurnIndex(PokerState lastState) {
    List<Player> playersInHand = lastState.getPlayersInHand();
    int lastPlayerListIndex = playersInHand.indexOf(lastState.getWhoseMove());
    for(int i=1; i<playersInHand.size(); i++) {
      int listIndex = (i + lastPlayerListIndex) % playersInHand.size();
      if(lastState.getPlayerChips().get(i) == 0) {
        // This player is all-in. Continue to next player
        continue;
      }
      return listIndex;
    }
    // Code should never reach here if our assumptino was correct
    throw new IllegalStateException("Next turn not found");
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
  
  private boolean isBigBlindMove(PokerState state) {
    int numberOfPlayers = state.getNumberOfPlayers();
    int bigBlindIndex = numberOfPlayers > 2 ? 2 : 1;
    return state.getWhoseMove().ordinal() == bigBlindIndex;
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
