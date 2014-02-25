package org.poker.client;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.VerifyMove;
import org.poker.client.GameApi.VerifyMoveDone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public abstract class AbstractPokerLogicTestBase {
  
  protected PokerLogic pokerLogic = new PokerLogic();
  
  protected static final String PLAYER_ID = "playerId";
  
  protected static final String[] P = {"P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8"};
  protected static final String C = "C";
  
  protected static final String PREVIOUS_MOVE = "previousMove";
  protected static final String PREVIOUS_MOVE_ALL_IN = "previousMoveAllIn";
  protected static final String NUMBER_OF_PLAYERS = "numberOfPlayers";
  protected static final String WHOSE_MOVE = "whoseMove";
  protected static final String CURRENT_BETTER = "currentBetter";
  protected static final String CURRENT_ROUND = "currentRound";
  protected static final String PLAYERS_IN_HAND = "playersInHand";
  protected static final String BOARD = "board";
  protected static final String HOLE_CARDS = "holeCards";
  protected static final String PLAYER_BETS = "playerBets";
  protected static final String PLAYER_CHIPS = "playerChips";
  protected static final String POTS = "pots";
  protected static final String CHIPS = "chips";
  protected static final String CURRENT_POT_BET = "currentPotBet";
  protected static final String PLAYERS_IN_POT = "playersInPot";
 
  protected final int p0_id = 84;
  protected final int p1_id = 85;
  protected final int p2_id = 86;
  protected final int p3_id = 87;
  
  protected final int[] playerIdArr = {p0_id, p1_id, p2_id, p3_id};
  
  protected final ImmutableMap<String, Object> p0_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p0_id);
  protected final ImmutableMap<String, Object> p1_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p1_id);
  protected final ImmutableMap<String, Object> p2_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p2_id);
  protected final ImmutableMap<String, Object> p3_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p3_id);
  
  protected final ImmutableList<Map<String, Object>> playersInfo_2_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info);
  protected final ImmutableList<Map<String, Object>> playersInfo_3_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info, p2_info);
  protected final ImmutableList<Map<String, Object>> playersInfo_4_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info, p2_info, p3_info);
  
  protected final ImmutableList<Integer> playersIds_2_players =
      ImmutableList.<Integer>of(p0_id, p1_id);
  protected final ImmutableList<Integer> playersIds_3_players =
      ImmutableList.<Integer>of(p0_id, p1_id, p2_id);
  protected final ImmutableList<Integer> playersIds_4_players =
      ImmutableList.<Integer>of(p0_id, p1_id, p2_id, p3_id);
  

  // dummy starting chips as they don't matter after initial move since
  // chips information is already in the state.
  protected final ImmutableMap<Integer, Integer> startingChips_2_player =
      getStartingChips(5000, 5000);
  protected final ImmutableMap<Integer, Integer> startingChips_3_player =
      getStartingChips(5000, 5000, 5000);
  protected final ImmutableMap<Integer, Integer> startingChips_4_player =
      getStartingChips(5000, 5000, 5000, 5000);
  
  
  // States for different scenarios in tests

  protected final ImmutableMap<String, Object> emptyState =
      ImmutableMap.<String, Object>of();
  protected final ImmutableMap<String, Object> nonEmptyState =
      ImmutableMap.<String, Object>of("K", "V");

  
  //Other interesting states
  
  /**
   * 4-way hand during PreFlop<Br>
   * Blinds 100/200<Br>
   * P3 bets 600<Br>
   * P0 (dealer) to act
   */
  protected final ImmutableMap<String, Object> preFlopFourPlayerDealersTurnState = 
      ImmutableMap.<String, Object>builder().
          put(PREVIOUS_MOVE, PokerMove.RAISE.name()).
          put(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE).
          put(NUMBER_OF_PLAYERS, 4).
          put(WHOSE_MOVE, P[0]).
          put(CURRENT_BETTER, P[3]).
          put(CURRENT_ROUND, BettingRound.PRE_FLOP.name()).
          put(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3], P[0])).
          put(HOLE_CARDS, ImmutableList.of(
              ImmutableList.of(0, 1), ImmutableList.of(2, 3),
              ImmutableList.of(4, 5), ImmutableList.of(6, 7))).
          put(BOARD, ImmutableList.of(8, 9, 10, 11, 12)).
          put(PLAYER_BETS, ImmutableList.of(0, 100, 200, 600)).
          put(PLAYER_CHIPS, ImmutableList.of(2000, 1900, 1800, 1400)).
          put(POTS, ImmutableList.of(ImmutableMap.<String, Object>of(
              CHIPS, 900,
              CURRENT_POT_BET, 600,
              PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3]),
              PLAYER_BETS, ImmutableList.of(0, 100, 200, 600)))).
          build();
  
  /**
   * 4-way hand during PreFlop<Br>
   * Blinds 100/200<Br>
   * P3 to act
   */
  protected final ImmutableMap<String, Object> preFlopFourPlayerFirstMoveState = 
      ImmutableMap.<String, Object>builder().
          put(PREVIOUS_MOVE, PokerMove.RAISE.name()).
          put(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE).
          put(NUMBER_OF_PLAYERS, 4).
          put(WHOSE_MOVE, P[3]).
          put(CURRENT_BETTER, P[2]).
          put(CURRENT_ROUND, BettingRound.PRE_FLOP.name()).
          put(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3], P[0])).
          put(HOLE_CARDS, ImmutableList.of(
              ImmutableList.of(0, 1), ImmutableList.of(2, 3),
              ImmutableList.of(4, 5), ImmutableList.of(6, 7))).
          put(BOARD, ImmutableList.of(8, 9, 10, 11, 12)).
          put(PLAYER_BETS, ImmutableList.of(0, 100, 200, 0)).
          put(PLAYER_CHIPS, ImmutableList.of(2000, 1900, 1800, 2000)).
          put(POTS, ImmutableList.of(ImmutableMap.<String, Object>of(
              CHIPS, 300,
              CURRENT_POT_BET, 200,
              PLAYERS_IN_POT, ImmutableList.of(P[1], P[2]),
              PLAYER_BETS, ImmutableList.of(0, 100, 200, 0)))).
          build();
  
  /**
   * 3-way hand during River<Br>
   * Pot amount before River: 3000<Br>
   * P1 bets 400<Br>
   * P2 calls<Br>
   * P0 to act
   */
  protected final ImmutableMap<String, Object> riverThreePlayerDealersTurnState = 
      ImmutableMap.<String, Object>builder().
          put(PREVIOUS_MOVE, PokerMove.CALL.name()).
          put(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE).
          put(NUMBER_OF_PLAYERS, 3).
          put(WHOSE_MOVE, P[0]).
          put(CURRENT_BETTER, P[1]).
          put(CURRENT_ROUND, BettingRound.RIVER.name()).
          put(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[0])).
          put(HOLE_CARDS, ImmutableList.of(
              ImmutableList.of(0, 1), ImmutableList.of(2, 3), ImmutableList.of(4, 5))).
          put(BOARD, ImmutableList.of(6, 7, 8, 9, 10)).
          put(PLAYER_BETS, ImmutableList.of(0, 400, 400)).
          put(PLAYER_CHIPS, ImmutableList.of(2000, 1600, 1600)).
          put(POTS, ImmutableList.of(ImmutableMap.<String, Object>of(
              CHIPS, 3800,
              CURRENT_POT_BET, 400,
              PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[0]),
              PLAYER_BETS, ImmutableList.of(0, 400, 400)))).
          build();


  /**
   * 3-way hand in ShowDown state<Br>
   * Pot amount before River: 4200<Br>
   * P0 has the turn and needs to mark EndGame<br>
   * Assume that P1 has the best hand.
   */
  protected final ImmutableMap<String, Object> showdownThreePlayerDealersTurnState = 
      ImmutableMap.<String, Object>builder().
          put(PREVIOUS_MOVE, PokerMove.CALL.name()).
          put(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE).
          put(NUMBER_OF_PLAYERS, 3).
          put(WHOSE_MOVE, P[0]).
          put(CURRENT_BETTER, P[1]).
          put(CURRENT_ROUND, BettingRound.SHOWDOWN.name()).
          put(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[0])).
          put(HOLE_CARDS, ImmutableList.of(
              ImmutableList.of(0, 1), ImmutableList.of(2, 3), ImmutableList.of(4, 5))).
          put(BOARD, ImmutableList.of(6, 7, 8, 9, 10)).
          put(PLAYER_BETS, ImmutableList.of(0, 0, 0)).
          put(PLAYER_CHIPS, ImmutableList.of(1600, 1600, 1600)).
          put(POTS, ImmutableList.of(ImmutableMap.<String, Object>of(
              CHIPS, 4200,
              CURRENT_POT_BET, 0,
              PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[0]),
              PLAYER_BETS, ImmutableList.of(0, 0, 0)))).
          build();
  
  protected ImmutableList<String> showdownThreePlayerDealersTurncardList = ImmutableList.of(
      "As", "Ah",//P0 cards - full house
      "Ad", "Ac",//P1 cards - full house
      "Ks", "Kh",//P2 cards - 4 of a kind (lucky guy)
      "Kd", "Kc", "Qs", "Qh", "Qd",//board
      "Qc",//remaining cards
      "Js", "Jh", "Jd", "Jc",
      "10s","10h","10d","10c",
      "9s", "9h", "9d", "9c",
      "8s", "8h", "8d", "8c",
      "7s", "7h", "7d", "7c",
      "6s", "6h", "6d", "6c",
      "5s", "5h", "5d", "5c",
      "4s", "4h", "4d", "4c",
      "3s", "3h", "3d", "3c",
      "2s", "2h", "2d", "2c");


  /**
   * 4 way hand on Flop<Br>
   * Pot amount before Flop: 2000<Br>
   * P1 bets 500<Br>
   * P2, P3 call<Br>
   * P0 to act
   */
  protected final ImmutableMap<String, Object> flopFourPlayerDealerTurnState =
      ImmutableMap.<String, Object>builder().
      put(PREVIOUS_MOVE, PokerMove.CALL.name()).
      put(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE).
      put(NUMBER_OF_PLAYERS, 4).
      put(WHOSE_MOVE, P[0]).
      put(CURRENT_BETTER, P[1]).
      put(CURRENT_ROUND, BettingRound.FLOP.name()).
      put(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3], P[0])).
      put(HOLE_CARDS, ImmutableList.of(
          ImmutableList.of(0, 1), ImmutableList.of(2, 3),
          ImmutableList.of(4, 5), ImmutableList.of(6, 7))).
      put(BOARD, ImmutableList.of(8, 9, 10, 11, 12)).
      put(PLAYER_BETS, ImmutableList.of(0, 500, 500, 500)).
      put(PLAYER_CHIPS, ImmutableList.of(1500 , 2000, 3000 , 5000)).
      put(POTS, ImmutableList.of(
          ImmutableMap.<String, Object>of(
              CHIPS, 3500,
              CURRENT_POT_BET, 500,
              PLAYERS_IN_POT, ImmutableList.of( P[1], P[2], P[3], P[0]),
              PLAYER_BETS, ImmutableList.of(0, 500, 500, 500)))).
      build();
  

  /**
   * 4 way hand on Flop<Br>
   * Pot amount before Flop: 2000<Br>
   * P1 folds<Br>
   * P2 to act<Br>
   */
  protected final ImmutableMap<String, Object> flopFourPlayerNoBetsMadeState =
      ImmutableMap.<String, Object>builder().
      put(PREVIOUS_MOVE, PokerMove.FOLD.name()).
      put(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE).
      put(NUMBER_OF_PLAYERS, 4).
      put(WHOSE_MOVE, P[2]).
      put(CURRENT_BETTER, P[1]).
      put(CURRENT_ROUND, BettingRound.FLOP.name()).
      put(PLAYERS_IN_HAND, ImmutableList.of( P[2], P[3], P[0])).
      put(HOLE_CARDS, ImmutableList.of(
          ImmutableList.of(0, 1), ImmutableList.of(2, 3),
          ImmutableList.of(4, 5), ImmutableList.of(6, 7))).
      put(BOARD, ImmutableList.of(8, 9, 10, 11, 12)).
      put(PLAYER_BETS, ImmutableList.of(0, 0, 0, 0)).
      put(PLAYER_CHIPS, ImmutableList.of(2000 , 2000, 3000 , 5000)).
      put(POTS, ImmutableList.of(
          ImmutableMap.<String, Object>of(
              CHIPS, 2000,
              CURRENT_POT_BET, 0,
              PLAYERS_IN_POT, ImmutableList.of( P[2], P[3], P[0]),
              PLAYER_BETS, ImmutableList.of(0, 0, 0, 0)))).
      build();
  
  
  // Utility methods
  
  protected ImmutableList<Map<String, Object>> getPlayersInfo(int numOfPlayers) {
    switch (numOfPlayers) {
    case 2: return playersInfo_2_players;
    case 3: return playersInfo_3_players;
    case 4: return playersInfo_4_players;
    default:
      throw new IllegalArgumentException("PlayersInfo array not available for " + numOfPlayers);
    }
  }
  
  protected VerifyMove move(int lastMovePlayerId, Map<String, Object> lastState,
      List<Operation> lastMove, List<Map<String, Object>> playersInfo,
      Map<Integer, Integer> playerIdToNumberOfTokensInPot) {
    return new VerifyMove(playersInfo,
        emptyState, // we never need to check the resulting state
        lastState, lastMove, lastMovePlayerId,
        playerIdToNumberOfTokensInPot);
  }
  
  protected ImmutableMap<Integer, Integer> getStartingChips(int... chips) {
    ImmutableMap.Builder<Integer, Integer> builder = ImmutableMap.builder();
    for (int i = 0; i < chips.length; i++) {
      builder.put(playerIdArr[i], chips[i]);
    }
    return builder.build();
  }
  
  protected ImmutableMap<String, Object> getStateWithCards(ImmutableMap<String, Object> state,
      ImmutableList<String> cards) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    builder.putAll(state);
    for(int i = 0 ; i <52 ; i++) {
      builder.put(C+i, cards.get(i));
    }
    return builder.build();
  }
    
  // Utility methods copied from CheatLogicTest.java in
  // https://github.com/yoav-zibin/cheat-game

  protected void assertMoveOk(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = pokerLogic.verify(verifyMove);
    assertEquals(0, verifyDone.getHackerPlayerId());
  }

  protected void assertHacker(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = pokerLogic.verify(verifyMove);
    assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
  }
}
