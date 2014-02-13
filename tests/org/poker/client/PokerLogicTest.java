package org.poker.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.poker.client.GameApi.EndGame;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.Set;
import org.poker.client.GameApi.VerifyMove;
import org.poker.client.GameApi.VerifyMoveDone;
import org.poker.client.GameApi.SetVisibility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@RunWith(JUnit4.class)
public class PokerLogicTest extends AbstractPokerLogicTestBase {
  
   
  /**
   * 4-way hand during PreFlop<P>
   * Blinds 100/200<P>
   * P3 bets 600<P>
   * P0 (dealer) to act
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
  
  /**
   * 3-way hand during River<P>
   * Pot before river: 3000<P>
   * P1 bets 400<P>
   * P2 calls<P>
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

  /**
   * P0 folds and the hand ends.
   * Assume P1 wins.
   */
  private final ImmutableList<Operation> riverThreePlayerDealerFolds =
      ImmutableList.<Operation>of(
          new Set(CURRENT_ROUND, BettingRound.SHOWDOWN.name()),
          new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2])),
          new Set(PLAYER_CHIPS, ImmutableList.of(2000, 1600 + 3800, 1600)),
          new EndGame(p1_id));
  
  /**
   * P0 calls and the hand ends.
   * Assume P1 wins.
   */
  private final ImmutableList<Operation> riverThreePlayerDealerCalls =
      ImmutableList.<Operation>of(
          new Set(CURRENT_ROUND, BettingRound.SHOWDOWN.name()),
          new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[0])),
          new Set(PLAYER_CHIPS, ImmutableList.of(2000 - 400, 1600 + 3800, 1600)),
          new EndGame(p1_id));
  
  /**
   * P0 folds but still wins.
   */
  private final ImmutableList<Operation> riverThreePlayerDealerFoldsAndWins =
      ImmutableList.<Operation>of(
          new Set(CURRENT_ROUND, BettingRound.SHOWDOWN.name()),
          new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2])),
          new Set(PLAYER_CHIPS, ImmutableList.of(2000, 1600 + 3800, 1600)),
          new EndGame(p0_id));
  
  /**
   * 4 way hand on Flop
   * Pre-flop pot 2000
   * P1 bets 500
   * P2, P3 call
   * P0 to act
   */
  private final ImmutableMap<String, Object> flopFourPlayerDealerTurnState =
      ImmutableMap.<String,Object>builder().
      put(NUMBER_OF_PLAYERS,4).
      put(WHOSE_MOVE,P[0]).
      put(CURRENT_BETTER,P[1]).
      put(CURRENT_ROUND, BettingRound.FLOP.name()).
      put(PLAYERS_IN_HAND, ImmutableList.of(P[1],P[2],P[3],P[0])).
      put(HOLE_CARDS, ImmutableList.of(
          ImmutableList.of(0,1), ImmutableList.of(2,3), ImmutableList.of(4,5),
          ImmutableList.of(6,7))).
      put(BOARD,ImmutableList.of(8,9,10,11,12)).
      put(PLAYER_BETS, ImmutableList.of(0,500,500,500)).
      put(PLAYER_CHIPS, ImmutableList.of(1500 , 2000, 3000 , 5000)).
      put(POTS,ImmutableList.of(ImmutableMap.<String, Object>of(
          CHIPS,3500,
          CURRENT_POT_BET, 500,
          PLAYERS_IN_POT, ImmutableList.of(P[0],P[1],P[2],P[3])
          ))).
      build();
  
  private final ImmutableList<Operation> flopFourPlayerDealerCall() {
    List<ImmutableMap<String, Object>> pots = Lists.newArrayList();
  // Main pot set bet to 0 because round changes to Turn
  pots.add(ImmutableMap.<String, Object>of(
      CHIPS, 2000 + 500 + 500 + 500 ,
      CURRENT_POT_BET, 0,
      PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3], P[0])));
  ImmutableList.Builder<Operation> listBuilder = ImmutableList.<Operation>builder();
  listBuilder.add(new Set(WHOSE_MOVE, P[1]));
  // current better will change if move is not a call.
  listBuilder.add(new Set(CURRENT_BETTER, P[1]));
  listBuilder.add(new Set(CURRENT_ROUND, BettingRound.TURN.name()));
  listBuilder.add(new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3], P[0]))).
  add(new Set(PLAYER_BETS, ImmutableList.of(0, 0,0, 0))).
  add(new Set(PLAYER_CHIPS, ImmutableList.of(1500 - 500, 2000, 3000, 5000))).
  add(new Set(POTS, pots)).
  add(new SetVisibility(C+(4*2+3)));
  return listBuilder.build();
  }

// player uncovers more than necessary cards
  private final ImmutableList<Operation> flopFourPlayerDealerWrongCardOpen() {
    List<ImmutableMap<String, Object>> pots = Lists.newArrayList();
  // Main pot set bet to 0 because round changes to Turn
  pots.add(ImmutableMap.<String, Object>of(
      CHIPS, 2000 + 500 + 500 + 500 ,
      CURRENT_POT_BET, 0,
      PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3], P[0])));
  ImmutableList.Builder<Operation> listBuilder = ImmutableList.<Operation>builder();
  listBuilder.add(new Set(WHOSE_MOVE, P[1]));
  // current better will change if move is not a call.
  listBuilder.add(new Set(CURRENT_BETTER, P[1]));
  listBuilder.add(new Set(CURRENT_ROUND, BettingRound.TURN.name()));
  listBuilder.add(new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3], P[0]))).
  add(new Set(PLAYER_BETS, ImmutableList.of(0, 0,0, 0))).
  add(new Set(PLAYER_CHIPS, ImmutableList.of(1500 - 500, 2000, 3000, 5000))).
  add(new Set(POTS, pots)).
  add(new SetVisibility(C+(4*2+3))).
  add(new SetVisibility(C+(4*2+4)));
  return listBuilder.build();
  }
      
  
  


  /*
   * Tests
   */
 
  
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
  
  @Test
  public void testEndGameAfterLastPlayerFolds() {
    // Last player folds and the hand ends
    VerifyMove verifyMove = move(p0_id, riverThreePlayerDealersTurnState, 
        riverThreePlayerDealerFolds, playersInfo_3_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testEndGameAfterLastPlayerCalls() {
    // Last player calls and the hand ends
    VerifyMove verifyMove = move(p0_id, riverThreePlayerDealersTurnState, 
        riverThreePlayerDealerCalls, playersInfo_3_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testEndGameWithWrongPlayerVictory() {
    // Last player folds and the hand ends
    VerifyMove verifyMove = move(p0_id, riverThreePlayerDealersTurnState, 
        riverThreePlayerDealerFoldsAndWins, playersInfo_3_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testRoundProgressToTurnfromFlop() {
    VerifyMove verifyMove = move(p0_id, flopFourPlayerDealerTurnState,
        flopFourPlayerDealerCall(), playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testRoundProgressToTurnWrongCardUncover() {
    VerifyMove verifyMove = move(p0_id, flopFourPlayerDealerTurnState,
        flopFourPlayerDealerWrongCardOpen(), playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  
  
}
