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
  private static final String BOARD = null;
  private static final String HOLE_CARDS = "holeCards";
  private static final String PLAYER_BETS = "playerBets";
  private static final String PLAYER_BET = "playerBet";
  private static final String PLAYER_CHIPS = "playerChips";
  private static final String POTS = null;
  private static final String POT = null;
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
  
  private final ImmutableMap<String, Object> preFlopFourPlayerDealerTurnState = 
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

  private final ImmutableList<Operation> preFlopFourPlayerDealerCall =
      ImmutableList.<Operation>of(
          new Set(WHOSE_MOVE, P[1]),
          new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3], P[0])),
          new Set(PLAYER_BET+0, 600),
          new Set(PLAYER_CHIPS+0, 1400),
          new Set(POT+0, ImmutableMap.<String, Object>of(
              CHIPS, 1500,
              CURRENT_POT_BET, 600,
              PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3], P[0])))
          );
  
  private final ImmutableList<Operation> preFlopFourPlayerDealerRaise =
      ImmutableList.<Operation>of(
          new Set(WHOSE_MOVE, P[1]),
          new Set(CURRENT_BETTER, P[0]),
          new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3], P[0])),
          new Set(PLAYER_BET+0, 1200),
          new Set(PLAYER_CHIPS+0, 800),
          new Set(POT+0, ImmutableMap.<String, Object>of(
              CHIPS, 2100,
              CURRENT_POT_BET, 1200,
              PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3], P[0])))
          );

  private final ImmutableList<Operation> preFlopFourPlayerDealerAllIn =
      ImmutableList.<Operation>of(
          new Set(WHOSE_MOVE, P[1]),
          new Set(CURRENT_BETTER, P[0]),
          new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3], P[0])),
          new Set(PLAYER_BET+0, 200),
          new Set(PLAYER_CHIPS+0, 0),
          new Set(POTS, ImmutableList.of(
              ImmutableMap.<String, Object>of(
                  CHIPS, 2900,
                  CURRENT_POT_BET, 2000,
                  PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3], P[0])),
              ImmutableMap.<String, Object>of(
                  CHIPS, 0,
                  CURRENT_POT_BET, 0,
                  PLAYERS_IN_POT, ImmutableList.of())))
          );
  
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
    assertEquals(52 + 52 + 10, initialOperation.size());
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
  public void testPreFlopFourPlayerFold() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealerTurnState, 
        preFlopFourPlayerDealerFold, playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  
  @Test
  public void testPreFlopFourPlayerCall() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealerTurnState, 
        preFlopFourPlayerDealerCall, playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testPreFlopFourPlayerRaise() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealerTurnState, 
        preFlopFourPlayerDealerRaise, playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testPreFlopFourPlayerAllIn() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealerTurnState, 
        preFlopFourPlayerDealerAllIn, playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testPreFlopWrongPlayerMoves() {
    //wrong player fold
    VerifyMove verifyMove = move(p1_id, preFlopFourPlayerDealerTurnState,
        preFlopFourPlayerDealerFold, playersInfo_4_players);
    assertHacker(verifyMove);
    
    //wrong player call
    verifyMove = move(p2_id, preFlopFourPlayerDealerTurnState,
        preFlopFourPlayerDealerCall, playersInfo_4_players);
    assertHacker(verifyMove);
    
    //wrong player raise
    verifyMove = move(p3_id, preFlopFourPlayerDealerTurnState,
        preFlopFourPlayerDealerRaise, playersInfo_4_players);
    assertHacker(verifyMove);
    
    //wrong player all in
    verifyMove = move(p1_id, preFlopFourPlayerDealerTurnState,
        preFlopFourPlayerDealerAllIn, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testPreFlopWrongAmountMoves() {
    //raise by insufficient (if you have to raise at least double the bet)
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealerTurnState, 
        preFlopFourPlayerDealerRaise, playersInfo_4_players);
    assertMoveOk(verifyMove);
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
