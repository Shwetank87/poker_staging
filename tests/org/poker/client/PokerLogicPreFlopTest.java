package org.poker.client;

import java.util.List;

import org.junit.Test;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.Set;
import org.poker.client.GameApi.SetVisibility;
import org.poker.client.GameApi.VerifyMove;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class PokerLogicPreFlopTest extends AbstractPokerLogicTestBase {
  
  /**
   * 4-way hand during PreFlop<Br>
   * Blinds 100/200<Br>
   * P3 bets 600<Br>
   * P0 (dealer) to act
   */
  protected final ImmutableMap<String, Object> preFlopFourPlayerDealersTurnState = 
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
  
  protected final ImmutableList<Operation> preFlopFourPlayerDealerFold =
      ImmutableList.<Operation>of(new Set(WHOSE_MOVE, P[1]));
  
  protected ImmutableList<Operation> getPreFlopFourPlayerDealerRaise(int raiseByAmount) {
    List<ImmutableMap<String, Object>> pots = Lists.newArrayList();
    // Main pot
    pots.add(ImmutableMap.<String, Object>of(
        CHIPS, 900 + 600 + raiseByAmount,
        CURRENT_POT_BET, 600 + raiseByAmount,
        PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3], P[0])));
    // If bet amount is more than chips, its an all-in move and new "side pot" is created.
    // Ideally bet cannot be more than chips, but we allow it for negative tests.
    if (600 + raiseByAmount >= 2000) {
      pots.add(ImmutableMap.<String, Object>of(
          CHIPS, 0,
          CURRENT_POT_BET, 0,
          PLAYERS_IN_POT, ImmutableList.of()));
    }
    ImmutableList.Builder<Operation> listBuilder = ImmutableList.<Operation>builder();
    listBuilder.add(new Set(WHOSE_MOVE, P[1]));
    if (raiseByAmount > 0) // current better will change if move is not a call.
      listBuilder.add(new Set(CURRENT_BETTER, P[0]));
    listBuilder.add(new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3], P[0])));
    listBuilder.add(new Set(PLAYER_BETS, ImmutableList.of(600 + raiseByAmount, 100, 200, 600)));
    listBuilder.add(new Set(PLAYER_CHIPS,
        ImmutableList.of(2000 - 600 - raiseByAmount, 1900, 1800, 1400)));
    listBuilder.add(new Set(POTS, pots));
    return listBuilder.build();
  }

  
  /**
   * 4-way hand during PreFlop<Br>
   * Blinds 100/200<Br>
   * P3 to act
   */
  private final ImmutableMap<String, Object> preFlopFourPlayerFirstMoveState = 
      ImmutableMap.<String, Object>builder().
          put(NUMBER_OF_PLAYERS, 4).
          put(WHOSE_MOVE, P[3]).
          put(CURRENT_BETTER, P[2]).
          put(CURRENT_ROUND, BettingRound.PRE_FLOP.name()).
          put(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2])).
          put(HOLE_CARDS, ImmutableList.of(
              ImmutableList.of(0, 1), ImmutableList.of(2, 3),
              ImmutableList.of(4, 5), ImmutableList.of(6, 7))).
          put(BOARD, ImmutableList.of(8, 9, 10, 11, 12)).
          put(PLAYER_BETS, ImmutableList.of(0, 100, 200, 0)).
          put(PLAYER_CHIPS, ImmutableList.of(2000, 1900, 1800, 2000)).
          put(POTS, ImmutableList.of(ImmutableMap.<String, Object>of(
              CHIPS, 300,
              CURRENT_POT_BET, 200,
              PLAYERS_IN_POT, ImmutableList.of(P[1], P[2])))).
          build();
  
  
  //Tests
  
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
  public void testPreFlopMovesByWrongPlayer() {
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
    // Raise done by insufficient amount
    // (you have to raise to least double the existing bet)
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
  public void testIllegalIncreaseInChips() {
    // P0 folds and increases his chips
    ImmutableList<Operation> illegalIncrease = ImmutableList.<Operation>of(
        new Set(WHOSE_MOVE, P[1]),
        new Set(PLAYER_CHIPS, ImmutableList.of(10000, 1900, 1800, 1400)));
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState,
        illegalIncrease, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testAttemptToViewBoard() {
    // P0 folds, but marks all community cards as visible to all
    ImmutableList<Operation> attemptToviewBoard = ImmutableList.<Operation>of(
        new Set(WHOSE_MOVE, P[1]),
        new SetVisibility(C + 8, ImmutableList.of(p0_id)),
        new SetVisibility(C + 9, ImmutableList.of(p0_id)),
        new SetVisibility(C + 10, ImmutableList.of(p0_id)),
        new SetVisibility(C + 11, ImmutableList.of(p0_id)),
        new SetVisibility(C + 12, ImmutableList.of(p0_id)));
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState,
        attemptToviewBoard, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testAttemptToSkipOthersMove() {
    // P3 marks next turn as P1 instead of P0
    ImmutableList<Operation> attemptToSkipMove = ImmutableList.<Operation>of(
        new Set(WHOSE_MOVE, P[1]));
    VerifyMove verifyMove = move(p3_id, preFlopFourPlayerFirstMoveState,
        attemptToSkipMove, playersInfo_4_players);
    assertHacker(verifyMove);
  }

}
