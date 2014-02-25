package org.poker.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.poker.client.Card.Rank;
import org.poker.client.Card.Suit;
import org.poker.client.GameApi.AttemptChangeTokens;
import org.poker.client.GameApi.EndGame;
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

public class PokerLogic extends AbstractPokerLogicBase {

  private PokerLogicHelper helper = PokerLogicHelper.getInstance();

  public VerifyMoveDone verify(VerifyMove verifyMove) {
    try {
      checkMoveIsLegal(verifyMove);
      return new VerifyMoveDone();
    } catch (Exception e) {
      //e.printStackTrace();
      return new VerifyMoveDone(verifyMove.getLastMovePlayerId(), e.getMessage());
    }
  }
  
  private void checkMoveIsLegal(VerifyMove verifyMove) {
    // Checking the operations are as expected.
    List<Operation> expectedOperations = getExpectedOperations(verifyMove);
    List<Operation> lastMove = verifyMove.getLastMove();
    check(expectedOperations.equals(lastMove), expectedOperations, lastMove);
    // We use SetTurn, so we don't need to check that the correct player did the move.
    // However, we do need to check the first non-buyIn move is done by P0 (and then in the
    // first MakeMove we'll send SetTurn which will guarantee the correct player send MakeMove).
    if (verifyMove.getLastState().isEmpty()) {
      if(!(lastMove.get(0) instanceof AttemptChangeTokens)) {
        check(verifyMove.getLastMovePlayerId() == verifyMove.getPlayerIds().get(0), "Wrong player");
      }
    }
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
    
    PokerState lastState = helper.gameApiStateToPokerState(lastApiState);
    
    boolean isEndGame = false;
    for(Operation operation : lastMove) {
      if(operation instanceof EndGame) {
        isEndGame = true;
      }
    }
    if(isEndGame) {
      return doEndGameMove(lastState, playerIds);
    }
    
    // First operation will be SetTurn and second operation will be Set(PreviuosMove)
    PokerMove previousMove = PokerMove.valueOf((String)((Set)lastMove.get(1)).getValue());
    int playerIndex = lastState.getWhoseMove().ordinal();
    
    if (previousMove == PokerMove.FOLD) {
      return doFoldMove(lastState, playerIds);
    }
    else if (previousMove == PokerMove.CHECK) {
      return doCheckMove(lastState, playerIds);
    }
    else if (previousMove == PokerMove.CALL) {
      int additionalAmount = lastState.getPlayerChips().get(playerIndex) -
          ((List<Integer>) getSetOperationVal(PLAYER_CHIPS, lastMove)).get(playerIndex);
      return doCallMove(lastState, playerIds, additionalAmount);
    }
    else if (previousMove == PokerMove.BET) {
      int betAmount = 
          ((List<Integer>) getSetOperationVal(PLAYER_BETS, lastMove)).get(playerIndex);
      return doBetMove(lastState, playerIds, betAmount);
    }
    else if (previousMove == PokerMove.RAISE) {
      int existingBetAmount = lastState.getPlayerBets().get(playerIndex);
      int newBetAmount =
          ((List<Integer>) getSetOperationVal(PLAYER_BETS, lastMove)).get(playerIndex);
      return doRaiseMove(lastState, playerIds, newBetAmount - existingBetAmount);
    }
    
    //All possible states have been checked
    throw new IllegalStateException("No Expected move can be found");
  }
  
  /**
   * Returns operation list for end game scenario.<Br>
   * Divides the money in all the pots amongst the 
   * winners of those respective pots.
   * 
   * @param lastState
   * @param playerIds
   * @return
   */
  List<Operation> doEndGameMove(PokerState lastState, List<Integer> playerIds) {
    
    List<List<Integer>> winnersForEachPot = helper.getWinners(lastState, playerIds);
    
    List<Integer> winnings = Lists.newArrayList();
    winnings.addAll(lastState.getPlayerChips());
    
    ImmutableMap.Builder<Integer, Integer> endGameMapBuilder = ImmutableMap.builder();
    
    for (int potIndex = 0; potIndex < winnersForEachPot.size(); potIndex++) {
      List<Integer> winnersForPot = winnersForEachPot.get(potIndex);
      int potAmount = lastState.getPots().get(potIndex).getChips();
      int numberOfWinners = winnersForPot.size();
      for(int winnerIndex = 0; winnerIndex < numberOfWinners; winnerIndex++) {
        int winningShare;
        if(winnerIndex == numberOfWinners - 1) {
          //handle remaining change
          winningShare = potAmount - (numberOfWinners - 1) * (potAmount / numberOfWinners);
        }
        else {
          winningShare = potAmount / numberOfWinners;
        }
        int winnerPlayerIndex = playerIds.indexOf(winnersForPot.get(winnerIndex));
        winnings.set(winnerPlayerIndex, winnings.get(winnerPlayerIndex) + winningShare);
      }
    }
    
    for (int i = 0; i < playerIds.size(); i++) {
      if(winnersForEachPot.get(winnersForEachPot.size() - 1).contains(playerIds.get(i))) {
        endGameMapBuilder.put(playerIds.get(i), 1);
      }
      else {
        endGameMapBuilder.put(playerIds.get(i), 0);
      }
    }
    
    ImmutableMap.Builder<Integer, Integer> playerIdToTokensBuilder = ImmutableMap.builder();
    ImmutableMap.Builder<Integer, Integer> playerIdToPotTokensBuilder = ImmutableMap.builder();
    for(int i = 0; i < winnings.size(); i++) {
      playerIdToTokensBuilder.put(playerIds.get(i), winnings.get(i));
      playerIdToPotTokensBuilder.put(playerIds.get(i), 0);
    }
    return ImmutableList.<Operation>of(
        new AttemptChangeTokens(
            playerIdToTokensBuilder.build(),
            playerIdToPotTokensBuilder.build()),
        new EndGame(endGameMapBuilder.build()));
  }

  /**
   * Generates List of Operation objects for performing
   * a Fold move by the current player.
   * 
   * @param lastState
   * @param playerIds
   * @return
   */
  List<Operation> doFoldMove(PokerState lastState, List<Integer> playerIds) {
    
    if (isNewRoundStarting(lastState, PokerMove.FOLD)) {
      return doNewRoundAfterFoldMove(lastState, playerIds);
    }
    
    List<Operation> operations = Lists.newArrayList();
    
    int nextTurnIndex = getNextTurnIndex(lastState);
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.FOLD.name()));
    
    operations.add(new Set(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE));
    
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    // Remove player from PLAYERS_IN_HAND
    List<String> playersInHand = helper.getApiPlayerList(lastState.getPlayersInHand());
    List<String> newPlayersInHand = removeFromList(playersInHand, lastState.getWhoseMove().name());
    operations.add(new Set(PLAYERS_IN_HAND, newPlayersInHand));
    
    // Remove player from all pots
    List<Pot> pots = lastState.getPots();
    List<Map<String, Object>> newPots = Lists.newArrayList();
    boolean isNewPotsRequired = false;
    for(Pot pot : pots) {
      List<String> playersInPot = helper.getApiPlayerList(pot.getPlayersInPot());
      List<String> newPlayersInPot = removeFromList(playersInPot, lastState.getWhoseMove().name());
      if(playersInPot.size() != newPlayersInPot.size()) {
        isNewPotsRequired = true;
      }
      newPots.add(ImmutableMap.<String, Object>of(
          CHIPS, pot.getChips(),
          CURRENT_POT_BET, pot.getCurrentPotBet(),
          PLAYERS_IN_POT, newPlayersInPot,
          PLAYER_BETS, pot.getPlayerBets()));
    }
    if(isNewPotsRequired) {
      operations.add(new Set(POTS, newPots));
    }
    
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
    
    if (isNewRoundStarting(lastState, PokerMove.CHECK)) {
      return doNewRoundAfterCheckMove(lastState , playerIds);
    }
    
    List<Operation> operations = Lists.newArrayList();
    
    int nextTurnIndex = getNextTurnIndex(lastState);
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.CHECK.name()));
    
    operations.add(new Set(PREVIOUS_MOVE_ALL_IN, new Boolean(false)));
    
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    return operations;
  }
  
  /**
   * Generates List of Operation objects for performing
   * a Call move by the current player.
   * 
   * @param lastState
   * @return
   */
  List<Operation> doCallMove(PokerState lastState, List<Integer> playerIds, int additionalAmount) {
    
    if (isNewRoundStarting(lastState, PokerMove.CALL)) {
      return doNewRoundAfterCallMove(lastState, playerIds, additionalAmount);
    }
    
    int playerIndex = lastState.getWhoseMove().ordinal();
    int currentBetAmount = lastState.getPlayerBets().get(playerIndex);
    int requiredBetAmount = calculateLastRequiredBet(lastState);
    int currentPlayerChips = lastState.getPlayerChips().get(playerIndex);
    boolean isAllIn = (currentPlayerChips == additionalAmount);
    
    if(isAllIn) {
      check(requiredBetAmount >= currentBetAmount + additionalAmount);
    }
    else {
      check(requiredBetAmount == currentBetAmount + additionalAmount);
    }
    
    List<Operation> operations = Lists.newArrayList();
    
    int nextTurnIndex = getNextTurnIndex(lastState);
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.CALL.name()));
    
    operations.add(new Set(PREVIOUS_MOVE_ALL_IN, Boolean.valueOf(isAllIn)));
    
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    // PLAYERS_IN_HAND should already contain this player
    
    List<Integer> newPlayerBets = addOrReplaceInList(lastState.getPlayerBets(),
        Integer.valueOf(currentBetAmount + additionalAmount), playerIndex);
    operations.add(new Set(PLAYER_BETS, newPlayerBets));
    
    List<Integer> newPlayerChips = addOrReplaceInList(lastState.getPlayerChips(),
        Integer.valueOf(currentPlayerChips - additionalAmount), playerIndex);
    operations.add(new Set(PLAYER_CHIPS, newPlayerChips));
    
    // Add call amount to all the pots
    List<Pot> pots = lastState.getPots();
    List<Map<String, Object>> newPots = Lists.newArrayList();
    if(isAllIn) {
      newPots.addAll(splitPotsForPartialCall(lastState, additionalAmount, false));
    }
    else {
      for(Pot pot : pots) {
        int existingBet = lastState.getPlayerBets().get(playerIndex);
        List<String> playersInPot = helper.getApiPlayerList(pot.getPlayersInPot());
        List<String> newPlayersInPot = addToList(playersInPot, lastState.getWhoseMove().name());
        List<Integer> playerPotBets = pot.getPlayerBets();
        List<Integer> newPlayerPotBets = addOrReplaceInList(playerPotBets,
            Integer.valueOf(pot.getCurrentPotBet()), playerIndex);
        newPots.add(ImmutableMap.<String, Object>of(
            CHIPS, pot.getChips() + pot.getCurrentPotBet() - existingBet,
            CURRENT_POT_BET, pot.getCurrentPotBet(),
            PLAYERS_IN_POT, newPlayersInPot,
            PLAYER_BETS, newPlayerPotBets));
      }
    }
    operations.add(new Set(POTS, newPots));
    
    return operations;
  }
  
  /**
   * Splits the pot for all-in call move, and resets the pots
   * for a new round if required.
   * 
   * @param lastState
   * @param additionalAmount
   * @param isNewRoundStarting
   * @return
   */
  private List<Map<String, Object>> splitPotsForPartialCall(PokerState lastState, 
      int additionalAmount, boolean isNewRoundStarting) {
    
    List<Map<String, Object>> pots = splitPotsForPartialCallHelper(lastState, additionalAmount);
    
    if(isNewRoundStarting){
      List<Map<String, Object>> newPots = Lists.newArrayList();
      for( Map<String, Object> pot : pots){
        Map<String, Object> newPot = ImmutableMap.<String, Object>of(
            CHIPS, pot.get(CHIPS),
            CURRENT_POT_BET, 0,
            PLAYERS_IN_POT, pot.get(PLAYERS_IN_POT),
            PLAYER_BETS, createNewList(lastState.getNumberOfPlayers(), 0)
            );
       newPots.add(newPot); 
      }
      return newPots;
    }
    
    return pots;
  }
  
  /**
   * Since player is going all in during call move, 
   * one of the pots need to be split into 2 pots.
   * 
   * @param lastState
   * @param additionalAmount
   * @return
   */
  private List<Map<String, Object>> splitPotsForPartialCallHelper(PokerState lastState,
      int additionalAmount) {
    check(calculateLastRequiredBet(lastState) >= additionalAmount, "This is not an all-in call.");
    int playerIndex = lastState.getWhoseMove().ordinal();
    List<Pot> pots = lastState.getPots();
    List<Map<String, Object>> newPots = Lists.newArrayList();
    int remainingAmount = additionalAmount;
    boolean potSplitDone = false;
    for(int i=0; i<pots.size(); i++) {
      Pot pot = pots.get(i);
      List<String> playersInPot = helper.getApiPlayerList(pot.getPlayersInPot());
      int requiredPotBet = pot.getCurrentPotBet();
      int existingPotBet = pot.getPlayerBets().get(playerIndex);
      int remainingPotBet = requiredPotBet - existingPotBet;
      
      if(!potSplitDone && remainingAmount <= remainingPotBet) {
        //Need to split this pot.
        int requiredPotBet1 = existingPotBet + remainingAmount;
        int requiredPotBet2 = requiredPotBet - (remainingAmount + existingPotBet);
        List<Integer> newPlayerBets1 = Lists.newArrayList();
        List<Integer> newPlayerBets2 = Lists.newArrayList();
        int chips1 = 0;
        int chips2 = 0;
        List<Integer> playerBets = addOrReplaceInList(
            pot.getPlayerBets(), Integer.valueOf(existingPotBet + remainingAmount), playerIndex);
        for(int playerBet : playerBets) {
          int playerBet1 = playerBet > requiredPotBet1 ? requiredPotBet1 : playerBet;
          int playerBet2 = playerBet > requiredPotBet1 ? (playerBet - requiredPotBet) : 0;
          newPlayerBets1.add(playerBet1);
          newPlayerBets2.add(playerBet2);
          chips1 += playerBet1;
          chips2 += playerBet2;
        }
        check(pot.getChips() == chips1 + chips2, "Invalid pot split.");
        newPots.add(ImmutableMap.<String, Object>of(
            CHIPS, chips1,
            CURRENT_POT_BET, requiredPotBet1,
            PLAYERS_IN_POT, playersInPot,
            PLAYER_BETS, newPlayerBets1));
        newPots.add(ImmutableMap.<String, Object>of(
            CHIPS, chips2,
            CURRENT_POT_BET, requiredPotBet2,
            PLAYERS_IN_POT, removeFromList(playersInPot, P[playerIndex]),
            PLAYER_BETS, newPlayerBets2));
      }
      else {
        if(potSplitDone) {
          newPots.add(ImmutableMap.<String, Object>of(
              CHIPS, pot.getChips(),
              CURRENT_POT_BET, pot.getCurrentPotBet(),
              PLAYERS_IN_POT, removeFromList(playersInPot, P[playerIndex]),
              PLAYER_BETS, pot.getPlayerBets()));
        }
        else {
          newPots.add(ImmutableMap.<String, Object>of(
              CHIPS, pot.getChips() + remainingPotBet,
              CURRENT_POT_BET, requiredPotBet,
              PLAYERS_IN_POT, playersInPot,
              PLAYER_BETS, addOrReplaceInList(pot.getPlayerBets(),
                  Integer.valueOf(requiredPotBet), playerIndex)));
          remainingAmount -= remainingPotBet;
        }
      }
    }
    
    return newPots;
  }

  /**
   * Generates List of Operation objects for performing
   * a Bet move by the current player.
   * 
   * @param lastState
   * @param playerIds
   * @param betAmount
   * @return
   */
  List<Operation> doBetMove(PokerState lastState, List<Integer> playerIds, int betAmount) {
    
    // In Bet move existing bet should be zero, otherwise it'll be a raise
    check(calculateLastRequiredBet(lastState) == 0, "Bet Move: Non-zero existing bet");
    // Bet cannot be made in PreFlop round
    check(lastState.getCurrentRound() != BettingRound.PRE_FLOP);
    
    int nextTurnIndex = getNextTurnIndex(lastState);
    int playerIndex = lastState.getWhoseMove().ordinal();
    int currentPlayerChips = lastState.getPlayerChips().get(playerIndex);
    boolean isAllIn = (currentPlayerChips == betAmount);
    
    List<Operation> operations = Lists.newArrayList();
    
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.BET.name()));
    
    operations.add(new Set(PREVIOUS_MOVE_ALL_IN, Boolean.valueOf(isAllIn)));
    
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    operations.add(new Set(CURRENT_BETTER, P[playerIndex]));
    
    // Set player bets
    ImmutableList<Integer> newPlayerBets = addOrReplaceInList(lastState.getPlayerBets(),
        betAmount, playerIndex);
    operations.add(new Set(PLAYER_BETS, newPlayerBets));
    
    // Set player chips
    ImmutableList<Integer> newPlayerChips = addOrReplaceInList(lastState.getPlayerChips(),
        lastState.getPlayerChips().get(playerIndex) - betAmount, playerIndex);
    operations.add(new Set(PLAYER_CHIPS, newPlayerChips));
    
    // Bet amount needs to be added to last pot only
    List<Pot> pots = lastState.getPots();
    List<Map<String, Object>> newPots = Lists.newArrayList();
    for (int i = 0; i < pots.size() - 1; i++) {
      newPots.add(getApiPot(pots.get(i)));
    }
    Pot finalPot = pots.get(pots.size() - 1);
    List<String> playersInFinalPot = helper.getApiPlayerList(finalPot.getPlayersInPot());
    List<Integer> playerBetsInFinalPot = finalPot.getPlayerBets();
    List<Integer> newPlayerBetsInFinalPot = addOrReplaceInList(playerBetsInFinalPot,
          Integer.valueOf(betAmount), playerIndex);
    newPots.add(ImmutableMap.<String, Object>of(
        CHIPS, finalPot.getChips() + betAmount,
        CURRENT_POT_BET, betAmount,
        PLAYERS_IN_POT, addToList(playersInFinalPot, P[playerIndex]),
        PLAYER_BETS, newPlayerBetsInFinalPot));
    if (isAllIn) {
      Map<String, Object> finalApiPot = newPots.get(newPots.size() - 1);
      newPots.add(ImmutableMap.<String, Object>of(
          CHIPS, 0,
          CURRENT_POT_BET, 0,
          PLAYERS_IN_POT, removeFromList(
              (List<String>)finalApiPot.get("PLAYERS_IN_POT"), P[playerIndex]),
          PLAYER_BETS, createNewList(playerIds.size(), Integer.valueOf(0))));
    }

    return operations;
  }
  
  /**
   * Generates List of Operation objects for performing
   * a Raise move by the current player.
   * 
   * @param lastState
   * @param playerIds
   * @param additionalAmount
   * @return
   */
  List<Operation> doRaiseMove(PokerState lastState, List<Integer> playerIds,
      int additionalAmount) {
    
    int playerIndex = lastState.getWhoseMove().ordinal();
    int totalRequiredBet = calculateLastRequiredBet(lastState);
    int existingPlayerBet = lastState.getPlayerBets().get(playerIndex);
    int playerChips = lastState.getPlayerChips().get(playerIndex);
    
    int raiseByAmount = existingPlayerBet + additionalAmount - totalRequiredBet; 
    check(existingPlayerBet + additionalAmount <= playerChips,
        "Cannot raise more than existing chips.");
    boolean isAllIn = ((existingPlayerBet + additionalAmount) == playerChips);
    check(isAllIn || raiseByAmount >= totalRequiredBet,
        "Raise must be atleast twice existing bet.");
    
    // In Raise move existing bet should be non-zero, otherwise it'll be a bet
    check(totalRequiredBet != 0, "Raise Move: Zero existing bet");
    
    List<Operation> operations = Lists.newArrayList();
    
    int nextTurnIndex = getNextTurnIndex(lastState);
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.RAISE.name()));
    
    // Determine if this move is ALL In
    operations.add(new Set(PREVIOUS_MOVE_ALL_IN, Boolean.valueOf(isAllIn)));
    
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    operations.add(new Set(CURRENT_BETTER, P[playerIndex]));
    
    // Set player bets
    ImmutableList<Integer> newPlayerBets = addOrReplaceInList(lastState.getPlayerBets(),
        existingPlayerBet + additionalAmount, playerIndex);
    operations.add(new Set(PLAYER_BETS, newPlayerBets));
    
    // Set player chips
    ImmutableList<Integer> newPlayerChips = addOrReplaceInList(lastState.getPlayerChips(),
        playerChips - additionalAmount, playerIndex);
    operations.add(new Set(PLAYER_CHIPS, newPlayerChips));
    
    // requiredBet amount should be distributed amongst all the pots
    // raiseByAmount should be added to final pot
    // if its all-in move, create new final pot
    List<Pot> pots = lastState.getPots();
    List<Map<String, Object>> newPots = Lists.newArrayList();
    for (int i = 0; i < pots.size(); i++) {
      if(i != pots.size() - 1) {
        Pot pot = pots.get(i);
        int requiredPotBet = pot.getCurrentPotBet();
        int existingPotBet = pot.getPlayerBets().get(playerIndex);
        List<Integer> playerBetsInPot = pot.getPlayerBets();
        List<Integer> newPlayerBetsInPot = addOrReplaceInList(playerBetsInPot,
            Integer.valueOf(requiredPotBet), playerIndex);
        newPots.add(ImmutableMap.<String, Object>of(
            CHIPS, pot.getChips() + (requiredPotBet - existingPotBet),
            CURRENT_POT_BET, pot.getCurrentPotBet(),
            PLAYERS_IN_POT, addToList(helper.getApiPlayerList(pot.getPlayersInPot()), 
                P[playerIndex]),
            PLAYER_BETS, newPlayerBetsInPot));
      }
      else {
        Pot finalPot = pots.get(i);
        int requiredPotBet = finalPot.getCurrentPotBet();
        int existingPotBet = finalPot.getPlayerBets().get(playerIndex);
        List<Integer> playerBetsInFinalPot = finalPot.getPlayerBets();
        List<Integer> newPlayerBetsInFinalPot = addOrReplaceInList(playerBetsInFinalPot,
            Integer.valueOf(requiredPotBet + raiseByAmount), playerIndex);
        newPots.add(ImmutableMap.<String, Object>of(
            CHIPS, finalPot.getChips() + (requiredPotBet - existingPotBet) + raiseByAmount,
            CURRENT_POT_BET, requiredPotBet + raiseByAmount,
            PLAYERS_IN_POT, addToList(helper.getApiPlayerList(finalPot.getPlayersInPot()), P[playerIndex]),
            PLAYER_BETS, newPlayerBetsInFinalPot));
      }
      if(isAllIn) {
        Map<String, Object> finalPot = newPots.get(newPots.size() - 1);
        newPots.add(ImmutableMap.<String, Object>of(
            CHIPS, 0,
            CURRENT_POT_BET, 0,
            PLAYERS_IN_POT, removeFromList(
                (List<String>)finalPot.get(PLAYERS_IN_POT), P[playerIndex]),
            PLAYER_BETS, createNewList(playerIds.size(), Integer.valueOf(0))));
      }
    }
    operations.add(new Set(POTS, newPots));
    
    return operations;
  }
 
  /**
   * Returns true if the game ends after this move because all other 
   * players are all-in. 
   * 
   * @param lastState
   * @param move
   * @return
   */
  private boolean isGameOverAfterMove(PokerState lastState, PokerMove move) {
    // TODO fix next turn in new round methods
    if(move == PokerMove.BET || move == PokerMove.RAISE) {
      //Game cannot get over after a bet or a raise
      return false;
    }
    if(lastState.getCurrentRound() == BettingRound.RIVER) {
      //Method doNewRoundAfterXyzMove() will handle this scenario
      return false;
    }
    List<Player> playersInHand = lastState.getPlayersInHand();
    for(Player player : playersInHand) {
      if(player == lastState.getWhoseMove()) {
        continue;
      }
      if(lastState.getPlayerChips().get(player.ordinal()) == 0) {
        // This player is all-in, move on.
        continue;
      }
      // Found another player who's not all-in
      return false;
    }
    // All other players were all-in, so the game is over
    return true;
  }
  
  /**
   * Generates List of Operation objects for performing
   * a Fold move by the current player, which will result
   * in a new round.
   * 
   * @param lastState
   * @param playerIds
   * @return
   */
  private List<Operation> doNewRoundAfterFoldMove(PokerState lastState, List<Integer> playerIds) {
    
    int numOfPlayers = lastState.getNumberOfPlayers();
    int playerIndex = lastState.getWhoseMove().ordinal();
    BettingRound nextRound = lastState.getCurrentRound().getNextRound();
    boolean noPlayersLeft = isGameOverAfterMove(lastState, PokerMove.FOLD);
    boolean isGameEnding = (nextRound == BettingRound.SHOWDOWN);
    //If game is ending, next turn will be set to the same player
    int nextTurnIndex = (isGameEnding || noPlayersLeft) ? playerIndex : getNewRoundNextTurnIndex(
        lastState, PokerMove.FOLD);
    if(noPlayersLeft) {
      nextRound = BettingRound.SHOWDOWN;
    }
    
    List<Operation> operations = new ArrayList<Operation>();
    
    // SetTurn
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    // Set previous move
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.FOLD.name()));
    
    // Set previous move All In to false
    operations.add(new Set(PREVIOUS_MOVE_ALL_IN, false));
    
    // Set next turn
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    // Increment the current round
    operations.add(new Set(CURRENT_ROUND, nextRound.name()));
    
    // Remove the current player from players in Hand
    List<Player> newPlayerInHand = removeFromList(lastState.getPlayersInHand(), 
        Player.values()[playerIndex]);
    operations.add(new Set(PLAYERS_IN_HAND, helper.getApiPlayerList(newPlayerInHand)));
    
    // Set PlayerBets to 0
    List<Integer> newPlayerBets = createNewList(numOfPlayers, 0);
    operations.add(new Set(PLAYER_BETS, newPlayerBets));
    
    // Remove player from pots and set pot bets to 0
    List<Pot> pots = lastState.getPots();
    List<Map<String, Object>> newPots = Lists.newArrayList();
    for(Pot pot : pots) {
      List<String> playersInPot = helper.getApiPlayerList(pot.getPlayersInPot());
      List<String> newPlayersInPot = removeFromList(playersInPot, lastState.getWhoseMove().name());
      newPots.add(ImmutableMap.<String, Object>of(
          CHIPS, pot.getChips(),
          CURRENT_POT_BET, 0,
          PLAYERS_IN_POT, newPlayersInPot,
          PLAYER_BETS, createNewList(numOfPlayers, 0)));
    }
    
    operations.add(new Set(POTS, newPots));
    
    if(isGameEnding || noPlayersLeft){
      operations.addAll(makeHoleCardsVisible(lastState, PokerMove.FOLD));
    }
    if (!isGameEnding) {
      int numberOfPlayers = lastState.getNumberOfPlayers();
      BettingRound currentRound = lastState.getCurrentRound();
      if(noPlayersLeft) {
        // Open all the remaining cards
        operations.addAll(openAllCommunityCards(currentRound, numberOfPlayers));
      }
      else {
        // Open next board card(s)
        operations.addAll(openNextCommunityCards(currentRound, numberOfPlayers));
      }
    }
    
    return operations;
  }
  
  /**
   * Generates List of Operation objects for performing
   * a Check move by the current player, which will result
   * in a new round.
   * 
   * @param lastState
   * @param playerIds
   * @return
   */
  private List<Operation> doNewRoundAfterCheckMove(PokerState lastState, List<Integer> playerIds) {
    
    BettingRound nextRound = lastState.getCurrentRound().getNextRound();
    int playerIndex = lastState.getWhoseMove().ordinal();
    boolean noPlayersLeft = isGameOverAfterMove(lastState, PokerMove.CHECK);
    boolean isGameEnding = (nextRound == BettingRound.SHOWDOWN);
    //If game is ending, next turn will be set to the same player
    int nextTurnIndex = (isGameEnding || noPlayersLeft) ? playerIndex : getNewRoundNextTurnIndex(
        lastState, PokerMove.CHECK);
    int numberOfPlayers = lastState.getNumberOfPlayers();
    if(noPlayersLeft) {
      nextRound = BettingRound.SHOWDOWN;
    }

    List<Operation> operations = Lists.newArrayList();

    //set pot bets to 0 and player bets in pot to 0
    List<Pot> pots = lastState.getPots();
    List<Map<String, Object>> newPots = Lists.newArrayList();
    for(Pot pot: pots){
      newPots.add(ImmutableMap.<String,Object>of(
          CHIPS, pot.getChips(),
          CURRENT_POT_BET, 0,
          PLAYERS_IN_POT, pot.getPlayersInPot(),
          PLAYER_BETS, createNewList(numberOfPlayers, Integer.valueOf(0))));
    }
    
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.CHECK.name()));
    
    operations.add(new Set(PREVIOUS_MOVE_ALL_IN, new Boolean(false)));
    
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    operations.add(new Set(CURRENT_ROUND, nextRound.name()));
    
    operations.add(new Set(PLAYER_BETS, createNewList(numberOfPlayers, Integer.valueOf(0))));
    
    operations.add(new Set(POTS, newPots));
    
    if(isGameEnding || noPlayersLeft){
      operations.addAll(makeHoleCardsVisible(lastState, PokerMove.CHECK));
    }
    
    if (!isGameEnding) {
      BettingRound currentRound = lastState.getCurrentRound();
      if(noPlayersLeft) {
        operations.addAll(openAllCommunityCards(currentRound, numberOfPlayers));
      }
      else {
        operations.addAll(openNextCommunityCards(currentRound, numberOfPlayers));
      }
    }
    
    return operations;
  }

  /**
   * Generates List of Operation objects for performing
   * a Call move by the current player, which will result
   * in a new round.
   * 
   * @param lastState
   * @param playerIds
   * @param additionalAmount
   * @return
   */
  private List<Operation> doNewRoundAfterCallMove(PokerState lastState, List<Integer> playerIds,
      int additionalAmount) {
    
    int numOfPlayers = lastState.getNumberOfPlayers();
    int playerIndex = lastState.getWhoseMove().ordinal();
    int currentBetAmount = lastState.getPlayerBets().get(playerIndex);
    int requiredBetAmount = calculateLastRequiredBet(lastState);
    int currentPlayerChips = lastState.getPlayerChips().get(playerIndex);
    BettingRound nextRound = lastState.getCurrentRound().getNextRound();
    boolean noPlayersLeft = isGameOverAfterMove(lastState, PokerMove.CALL);
    boolean isGameEnding = (nextRound == BettingRound.SHOWDOWN);
    //If game is ending, next turn will be set to the same player
    int nextTurnIndex = (isGameEnding || noPlayersLeft) ? playerIndex : getNewRoundNextTurnIndex(
        lastState, PokerMove.CALL);
    boolean isAllIn = (currentPlayerChips == additionalAmount);
    if(noPlayersLeft) {
      nextRound = BettingRound.SHOWDOWN;
    }
    
    // Required all in checks
    if(isAllIn) {
      check(requiredBetAmount >= currentBetAmount + additionalAmount);
    }
    else {
      check(requiredBetAmount == currentBetAmount + additionalAmount);
    }
    
    List<Operation> operations = new ArrayList<Operation>();
    
    operations.add(new SetTurn(playerIds.get(nextTurnIndex)));
    
    // Set previous move
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.CALL.name()));
    
    // Set if this move is all in
    operations.add(new Set(PREVIOUS_MOVE_ALL_IN, isAllIn));
    
    // Set next turn
    operations.add(new Set(WHOSE_MOVE, P[nextTurnIndex]));
    
    // Increment the current round
    operations.add(new Set(CURRENT_ROUND, nextRound.name()));
    
    // Set PlayerBets to 0
    List<Integer> newPlayerBets = createNewList(numOfPlayers, 0);
    operations.add(new Set(PLAYER_BETS, newPlayerBets));
    
    // Set Player Chips
    List<Integer> oldPlayerChips = lastState.getPlayerChips(); 
    List<Integer> newPlayerChips = addOrReplaceInList(oldPlayerChips, 
        oldPlayerChips.get(playerIndex) - additionalAmount, playerIndex);
    operations.add(new Set(PLAYER_CHIPS, newPlayerChips));
    
    // Set Pots
    List<Pot> pots = lastState.getPots();
    List<Map<String, Object>> newPots = Lists.newArrayList();
    if(isAllIn) {
      newPots.addAll(splitPotsForPartialCall(lastState, additionalAmount, true));
    }
    else {
      for(Pot pot : pots) {
        int existingBet = lastState.getPlayerBets().get(playerIndex);
        List<String> playersInPot = helper.getApiPlayerList(pot.getPlayersInPot());
        List<String> newPlayersInPot = addToList(playersInPot, lastState.getWhoseMove().name());
        List<Integer> newPlayerPotBets = createNewList(numOfPlayers, 0);
        newPots.add(ImmutableMap.<String, Object>of(
            CHIPS, pot.getChips() + pot.getCurrentPotBet() - existingBet,
            CURRENT_POT_BET, 0,
            PLAYERS_IN_POT, newPlayersInPot,
            PLAYER_BETS, newPlayerPotBets));
      }
    }
    operations.add(new Set(POTS, newPots));
    
    if(isGameEnding || noPlayersLeft){
      operations.addAll(makeHoleCardsVisible(lastState, PokerMove.CALL));
    }
    
    if (!isGameEnding) {
      BettingRound currentRound = lastState.getCurrentRound();
      int numberOfPlayers = lastState.getNumberOfPlayers();
      if(noPlayersLeft) {
        operations.addAll(openAllCommunityCards(currentRound, numberOfPlayers));
      }
      else {
        operations.addAll(openNextCommunityCards(currentRound, numberOfPlayers));
      }
    }
    return operations;
  }

  /**
   * Returns true if a new round is going to start after given move.
   * 
   * @param lastState
   * @param previousMove
   * @return
   */
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
      break;
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

  /**
   * Generates List of Operation objects for performing
   * a buy-in move by the current player.
   * 
   * @param playerId
   * @param buyInAmount
   * @return
   */
  List<Operation> getInitialBuyInMove(int playerId, int buyInAmount) {
    return ImmutableList.<Operation>of(
        new AttemptChangeTokens(
            ImmutableMap.<Integer, Integer>of(playerId, buyInAmount*(-1)),
            ImmutableMap.<Integer, Integer>of(playerId, buyInAmount)));
  }
  
  /**
   * Generates List of Operation objects for performing
   * the initial move.
   * 
   * @param playerIds
   * @param startingChips
   * @return
   */
  List<Operation> getInitialMove(List<Integer> playerIds, Map<Integer, Integer> startingChips) {
    check(playerIds.size() >= 2 && playerIds.size() <= 9);

    int numberOfPlayers = playerIds.size();
    boolean isHeadsUp = (numberOfPlayers == 2);
    int smallBlindPos = isHeadsUp ? 0 : 1;
    int bigBlindPos = isHeadsUp ? 1 : 2;
    int utgPos = isHeadsUp ? 0 : 3 % numberOfPlayers;

    List<Operation> operations = new ArrayList<Operation>();

    // In heads-up match, P0(dealer) to act.
    // Otherwise, player after big blind to act
    operations.add(new SetTurn(playerIds.get(utgPos)));
    
    operations.add(new Set(PREVIOUS_MOVE, PokerMove.RAISE.name()));
    
    boolean bigBlindAllIn = false;
    if(startingChips.get(playerIds.get(bigBlindPos)) == BIG_BLIND) {
      bigBlindAllIn = true;
    }
    operations.add(new Set(PREVIOUS_MOVE_ALL_IN, bigBlindAllIn));
    
    //operations.add(new Set())
    operations.add(new Set(NUMBER_OF_PLAYERS, numberOfPlayers));

    // In heads-up match, P0(dealer) to act.
    // Otherwise, player after big blind to act
    operations.add(new Set(WHOSE_MOVE, P[utgPos]));
    
    // Big blind will be the current better
    operations.add(new Set(CURRENT_BETTER, P[bigBlindPos]));

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
   * Returns the Pot object in JSON based format.
   * 
   * @param pot
   * @return
   */
  private Map<String, Object> getApiPot(Pot pot) {
    return ImmutableMap.<String, Object>of(
        CHIPS, pot.getChips(),
        CURRENT_POT_BET, pot.getCurrentPotBet(),
        PLAYERS_IN_HAND, helper.getApiPlayerList(pot.getPlayersInPot()),
        PLAYER_BETS, pot.getPlayerBets());
  }
  
  /**
   * Creates a new ImmutableList of given size where all elements
   * are initialized with the given default object.
   * 
   * @param size
   * @param defaultValue
   * @return
   */
  private <T> ImmutableList<T> createNewList(int size, T defaultValue) {
    ImmutableList.Builder<T> listBuilder = ImmutableList.<T>builder();
    for (int i = 0; i < size; i++) {
      listBuilder.add(defaultValue);
    }
    return listBuilder.build();
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
  
  /**
   * Replaces an element at given index in the list;
   * If index = size of list, adds the element at the end.
   * 
   * @param list
   * @param obj
   * @param index
   * @return
   */
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
          addAll(list.subList(index+1, list.size())).build();
    }
    else {
      throw new IllegalArgumentException("Invalid index " + index);
    }
  }
    
  /**
   * Removes an element at the given index from the list.
   * 
   * @param list
   * @param obj
   * @return
   */
  private <T> ImmutableList<T> removeFromList(List<T> list, T obj) {
    int index = list.indexOf(obj);
    if(index == -1) {
      return ImmutableList.copyOf(list);
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
          addAll(list.subList(index + 1, list.size())).build();
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
      return playersInHand.get(listIndex).ordinal();
    }
    // Code should never reach here if our assumptino was correct
    throw new IllegalStateException("Next turn not found");
  }
  
  /**
   * This method gets the index of the next player to act in the new round.
   * The checking starts from the smallBlind and all-in Players are skipped. 
   * It assumes that at least one non-allIn player is in the hand.
   * 
   * @param state Last PokerState
   * @return index of next player to act
   */
  private int getNewRoundNextTurnIndex(PokerState lastState, PokerMove move) {
    
    List<Player> playersInHand;
    if(move == PokerMove.FOLD) {
      playersInHand = removeFromList(lastState.getPlayersInHand(), lastState.getWhoseMove());
    }
    else {
      playersInHand = lastState.getPlayersInHand();
    }
    int numberOfPlayers = lastState.getNumberOfPlayers();
    
    // Start from P1, which is player after the dealer
    for(int nextTurnIndex = 1; nextTurnIndex < numberOfPlayers; nextTurnIndex++) {
      if(playersInHand.contains(Player.values()[nextTurnIndex])) {
        if(lastState.getPlayerChips().get(nextTurnIndex) > 0) {
          // Found not allIn player still in the hand
          return nextTurnIndex;
        }
      }
    }
    throw new IllegalStateException("Not all-in player in hand not found.");
  }
  
  /**
   * Checks if next player to act was the big blind.
   * 
   * @param state
   * @return
   */
  private boolean isBigBlindMove(PokerState state) {
    int numberOfPlayers = state.getNumberOfPlayers();
    int bigBlindIndex = numberOfPlayers > 2 ? 2 : 1;
    return state.getWhoseMove().ordinal() == bigBlindIndex;
  }
  
  private int getBigBlindAmount() {
    return BIG_BLIND;
  }
  
  /**
   * Returns list of SetVisibility operations to open all the
   * remaining community cards
   * 
   * @param state
   * @return
   */
  private List<Operation> openAllCommunityCards(BettingRound currentRound, int numberOfPlayers) {
    ImmutableList.Builder<Operation> builder = ImmutableList.builder();
    switch(currentRound) {
    case PRE_FLOP:
      builder.addAll(openNextCommunityCards(BettingRound.PRE_FLOP, numberOfPlayers));
    case FLOP:
      builder.addAll(openNextCommunityCards(BettingRound.FLOP, numberOfPlayers));
    case TURN:
      builder.addAll(openNextCommunityCards(BettingRound.TURN, numberOfPlayers));
    default:
      break;
    }
    return builder.build();
  }
  
  /**
   * Returns list of SetVisibility operations to open the required
   * community cards to start a new round.
   * 
   * @param state
   * @return
   */
  private List<Operation> openNextCommunityCards(BettingRound currentRound, int numberOfPlayers) {
    BettingRound newRound = currentRound.getNextRound();
    switch(newRound) {
    case FLOP:
      return ImmutableList.<Operation>of(
          new SetVisibility(C + (numberOfPlayers * 2)),
          new SetVisibility(C + (numberOfPlayers * 2 + 1)),
          new SetVisibility(C + (numberOfPlayers * 2 + 2)));
    case TURN:
      return ImmutableList.<Operation>of(
          new SetVisibility(C + (numberOfPlayers * 2 + 3)));
    case RIVER:
      return ImmutableList.<Operation>of(
          new SetVisibility(C + (numberOfPlayers * 2 + 4)));
    case SHOWDOWN:
      return ImmutableList.<Operation>of();
    default:
      throw new IllegalStateException("Invalid round reached.");
    }
  }
  
  /**
   * Returns list of SetVisibility operations to make the hole cards 
   * of all the players still in the hand visible.
   * 
   * @param lastState
   * @param check
   * @return
   */
  private List<Operation> makeHoleCardsVisible(PokerState lastState, PokerMove move) {
    check(move != PokerMove.BET, "Game cannot end with a Bet move");
    check(move != PokerMove.RAISE, "Game cannot end with a Raise move");
    
    List<Player> playersInHand;
    if(move == PokerMove.FOLD) {
      playersInHand = removeFromList(lastState.getPlayersInHand(), lastState.getWhoseMove());
    }
    else {
      playersInHand = lastState.getPlayersInHand();
    }
    
    ImmutableList.Builder<Operation> builder = ImmutableList.<Operation>builder();
    for(Player player : playersInHand) {
      int index = player.ordinal();
      builder.add(new SetVisibility(C + (index * 2)));
      builder.add(new SetVisibility(C + (index * 2 + 1)));
    }
    return builder.build();
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
  
  // Following utility methods have been copied from CheatLogic.java
  // in project https://github.com/yoav-zibin/cheat-game
  
  private List<Integer> getIndicesInRange(int fromInclusive, int toInclusive) {
    List<Integer> keys = Lists.newArrayList();
    for (int i = fromInclusive; i <= toInclusive; i++) {
      keys.add(i);
    }
    return keys;
  }

  private List<String> getCardsInRange(int fromInclusive, int toInclusive) {
    List<String> keys = Lists.newArrayList();
    for (int i = fromInclusive; i <= toInclusive; i++) {
      keys.add(C + i);
    }
    return keys;
  }

  private String cardIdToString(int cardId) {
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
