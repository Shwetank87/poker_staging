package org.poker.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.poker.client.Card.Rank;
import org.poker.client.Card.Suit;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.Set;
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

  List<Operation> getInitialMove(int[] playerIds, int[] startingChips) {

    check(playerIds != null && startingChips != null);
    check(playerIds.length == startingChips.length);
    check(playerIds.length >= 2 && playerIds.length <= 9);

    int numberOfPlayers = playerIds.length;
    boolean isHeadsUp = (numberOfPlayers == 2);
    int smallBlindPos = isHeadsUp ? 0 : 1;
    int bigBlindPos = isHeadsUp ? 1 : 2;


    List<Operation> operations = new ArrayList<Operation>();

    operations.add(new Set(NUMBER_OF_PLAYERS, playerIds.length));

    // In heads-up match, P0(dealer) to act.
    // Otherwise, player after big blind to act
    operations.add(new Set(WHOSE_MOVE, isHeadsUp ? P[0] : P[3 % numberOfPlayers]));

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
      if (i == smallBlindPos)
        playerChipsList.add(startingChips[i] - SMALL_BLIND);
      else if (i == bigBlindPos)
        playerChipsList.add(startingChips[i] - BIG_BLIND);
      else
        playerChipsList.add(startingChips[i]);
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
      operations.add(new SetVisibility(C + (i * 2), ImmutableList.of(playerIds[i])));
      operations.add(new SetVisibility(C + (i * 2 + 1), ImmutableList.of(playerIds[i])));
    }
    // Make remaining cards not visible to anyone
    for (int i = 2 * numberOfPlayers; i < 52; i++) {
      operations.add(new SetVisibility(C + i, ImmutableList.<Integer>of()));
    }
    
    return operations;
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
