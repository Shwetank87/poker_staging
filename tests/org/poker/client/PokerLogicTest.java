package org.poker.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.Set;
import org.poker.client.GameApi.VerifyMove;
import org.poker.client.GameApi.VerifyMoveDone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@RunWith(JUnit4.class)
public class PokerLogicTest {
  
  PokerLogic pokerLogic = new PokerLogic();
  
  private static final String PLAYER_ID = "playerId";
  
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
 
  
  private final int p0_id = 84;
  private final int p1_id = 85;
  private final int p2_id = 86;
  private final int p3_id = 87;
  
  private final ImmutableMap<String, Object> p0_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p0_id);
  private final ImmutableMap<String, Object> p1_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p1_id);
  private final ImmutableMap<String, Object> p2_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p2_id);
  private final ImmutableMap<String, Object> p3_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p3_id);
  
  private final ImmutableList<Map<String, Object>> playersInfo_2_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info);
  private final ImmutableList<Map<String, Object>> playersInfo_3_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info, p2_info);
  private final ImmutableList<Map<String, Object>> playersInfo_4_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info, p2_info, p3_info);

  private final ImmutableMap<String, Object> emptyState = ImmutableMap.<String, Object>of();
  private final ImmutableMap<String, Object> nonEmptyState =
      ImmutableMap.<String, Object>of("K", "V");
  
  /**
   * 4 players pre-flop
   * Small blind 100
   * Big 
   */
  private final ImmutableMap<String, Object> preFlopFourPlayerDealersTurnState = 
      ImmutableMap.<String, Object>builder().
          put(NUMBER_OF_PLAYERS, 4).
          put(WHOSE_MOVE, P[0]).
          put(CURRENT_BETTER, P[3]).
          put(CURRENT_ROUND, BettingRound.PRE_FLOP.name()).
          put(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3])).
          put(HOLE_CARDS, ImmutableList.of(
              ImmutableList.of(0, 1), ImmutableList.of(2, 3),
              ImmutableList.of(4, 5), ImmutableList.of(6, 7))).
          put(BOARD, ImmutableList.of(8, 9, 10, 11, 12)).
          put(PLAYER_BETS, ImmutableList.of(0, 100, 200, 600)).
          put(PLAYER_CHIPS, ImmutableList.of(2000, 1900, 1800, 1400)).
          put(POTS, ImmutableList.of(ImmutableMap.<String, Object>of(
              CHIPS, 900,
              CURRENT_POT_BET, 600,
              PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3])))).
          build();

  private final ImmutableList<Operation> preFlopFourPlayerDealerFold =
      ImmutableList.<Operation>of(new Set(WHOSE_MOVE, P[1]));

  private ImmutableList<Operation> getPreFlopFourPlayerDealerRaise(int raiseByAmount) {
    List<ImmutableMap<String, Object>> pots = Lists.newArrayList();
    // Main pot
    pots.add(ImmutableMap.<String, Object>of(
        CHIPS, 900 + 600 + raiseByAmount,
        CURRENT_POT_BET, 600 + raiseByAmount,
        PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3], P[0])));
    // If bet amount is more than chips, its an all-in move and new "side pot" is created.
    // Ideally bet cannot be more than chips, but we allow it for negative tests.
    if(600 + raiseByAmount >= 2000) {
      pots.add(ImmutableMap.<String, Object>of(
          CHIPS, 0,
          CURRENT_POT_BET, 0,
          PLAYERS_IN_POT, ImmutableList.of()));
    }
    
    ImmutableList.Builder<Operation> listBuilder = ImmutableList.<Operation>builder();
    listBuilder.add(new Set(WHOSE_MOVE, P[1]));
    // current better will change if move is not a call.
    if(raiseByAmount > 0) listBuilder.add(new Set(CURRENT_BETTER, P[0]));
    listBuilder.add(new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3], P[0]))).
    add(new Set(PLAYER_BETS, ImmutableList.of(600 + raiseByAmount, 100, 200, 600))).
    add(new Set(PLAYER_CHIPS, ImmutableList.of(2000 - 600 - raiseByAmount, 1900, 1800, 1400))).
    add(new Set(POTS, pots));
    return listBuilder.build();
  }
  
  //TODO: complete end game test!!
  /**
   *
   * Pot before river: 3000
   * P1 bets 400
   * P2 calls
   * P0 to act
   */
  private final ImmutableMap<String, Object> riverThreePlayerDealersTurnState = 
      ImmutableMap.<String, Object>builder().
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
              PLAYERS_IN_POT, ImmutableList.of(P[0], P[1], P[2])))).
          build();

  private VerifyMove move(int lastMovePlayerId, Map<String, Object> lastState,
      List<Operation> lastMove, List<Map<String, Object>> playersInfo) {
    return new VerifyMove(p0_id, playersInfo,
        // we never need to check the resulting state
        emptyState,
        lastState, lastMove, lastMovePlayerId);
  }

  private List<Operation> getInitialOperations(int[] playerIds, int[] startingChips) {
    return pokerLogic.getInitialMove(playerIds, startingChips);
  }

  @Test
  public void testInitialOperationSize() {
    List<Operation> initialOperation = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id},
        new int[]{1000, 2000, 1000});
    // 10 Set operations
    // 1 Shuffle operation
    // 52 Set operations for cards
    // 52 SetVisibility operations for cards
    assertEquals(10 + 1 + 52 + 52, initialOperation.size());
  }
  
  @Test
  public void testInitialMoveWithTwoPlayers() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id},
        new int[]{2000, 2000});
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_2_players);
    VerifyMoveDone verifyMoveDone = pokerLogic.verify(verifyMove);
    assertEquals(0, verifyMoveDone.getHackerPlayerId());
  }
  
  @Test
  public void testInitialMoveWithFourPlayers() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id, p3_id},
        new int[]{2000, 2000, 2000, 2000});
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testInitialMoveByWrongPlayer() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id, p3_id},
        new int[]{2000, 2000, 2000, 2000});
    VerifyMove verifyMove = move(p1_id, emptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveFromNonEmptyState() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id, p3_id},
        new int[]{2000, 2000, 2000, 2000});
    VerifyMove verifyMove = move(p0_id, nonEmptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveWithExtraOperation() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id, p3_id},
        new int[]{2000, 2000, 2000, 2000});
    initialOperations.add(new Set(BOARD, ImmutableList.of()));
    VerifyMove verifyMove = move(p0_id, nonEmptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testPreFlopFold() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState, 
        preFlopFourPlayerDealerFold, playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  
  @Test
  public void testPreFlopCall() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState, 
        getPreFlopFourPlayerDealerRaise(0), playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testPreFlopRaise() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState, 
        getPreFlopFourPlayerDealerRaise(600), playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testPreFlopAllIn() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState, 
        getPreFlopFourPlayerDealerRaise(1400), playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testPreFlopMoveByWrongPlayer() {
    VerifyMove verifyMove = null;
    
    //wrong player fold
    verifyMove = move(p1_id, preFlopFourPlayerDealersTurnState,
        preFlopFourPlayerDealerFold, playersInfo_4_players);
    assertHacker(verifyMove);
    
    //wrong player call
    verifyMove = move(p2_id, preFlopFourPlayerDealersTurnState,
        getPreFlopFourPlayerDealerRaise(0), playersInfo_4_players);
    assertHacker(verifyMove);
    
    //wrong player raise
    verifyMove = move(p3_id, preFlopFourPlayerDealersTurnState,
        getPreFlopFourPlayerDealerRaise(600), playersInfo_4_players);
    assertHacker(verifyMove);
    
    //wrong player all in
    verifyMove = move(p1_id, preFlopFourPlayerDealersTurnState,
        getPreFlopFourPlayerDealerRaise(1400), playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testPreFlopRaiseByWrongAmount() {
    // Raise done by insufficient amount (you have to raise to least double the existing bet)
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState, 
        getPreFlopFourPlayerDealerRaise(400), playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testPreFlopRaiseByExcessAmount() {
    // Raise to more chips than player has
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState, 
        getPreFlopFourPlayerDealerRaise(2000), playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  
  
  /*
   * Utility methods copied from
   * https://github.com/yoav-zibin/cheat-game/blob/master/eclipse/tests/org/cheat/client/CheatLogicTest.java
   */
  
  private void assertMoveOk(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = pokerLogic.verify(verifyMove);
    assertEquals(0, verifyDone.getHackerPlayerId());
  }

  private void assertHacker(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = pokerLogic.verify(verifyMove);
    assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
  }
  
}
