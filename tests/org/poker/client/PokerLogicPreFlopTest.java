package org.poker.client;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.Set;
import org.poker.client.GameApi.SetTurn;
import org.poker.client.GameApi.SetVisibility;
import org.poker.client.GameApi.VerifyMove;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@RunWith(JUnit4.class)
public class PokerLogicPreFlopTest extends AbstractPokerLogicTestBase {
  
  
  protected final ImmutableList<Operation> preFlopFourPlayerDealerFold =
      ImmutableList.<Operation>of(
          new SetTurn(p1_id),
          new Set(PREVIOUS_MOVE, PokerMove.FOLD.name()),
          new Set(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE),
          new Set(WHOSE_MOVE, P[1]),
          new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3])));
  
  protected ImmutableList<Operation> getPreFlopFourPlayerDealerRaise(
      PokerMove move, int raiseByAmount) {
    // If bet amount is more than or equal to chips, its an all-in move.
    // Ideally bet cannot be more than chips, but we allow it for negative tests.
    boolean isAllIn = (600 + raiseByAmount >= 2000);
    List<ImmutableMap<String, Object>> pots = Lists.newArrayList();
    // Main pot
    pots.add(ImmutableMap.<String, Object>of(
        CHIPS, 900 + 600 + raiseByAmount,
        CURRENT_POT_BET, 600 + raiseByAmount,
        PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3], P[0]),
        PLAYER_BETS, ImmutableList.of(600 + raiseByAmount, 100, 200, 600)));
    // If its an all-in move, new "side pot" is created.
    if (isAllIn) {
      pots.add(ImmutableMap.<String, Object>of(
          CHIPS, 0,
          CURRENT_POT_BET, 0,
          PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3]),
          PLAYER_BETS, ImmutableList.of(0, 0, 0, 0)));
    }
    ImmutableList.Builder<Operation> listBuilder = ImmutableList.<Operation>builder();
    listBuilder.add(new SetTurn(p1_id));
    listBuilder.add(new Set(PREVIOUS_MOVE, move.name()));
    listBuilder.add(new Set(PREVIOUS_MOVE_ALL_IN, Boolean.valueOf(isAllIn)));
    listBuilder.add(new Set(WHOSE_MOVE, P[1]));
    // current better will change if move is not a call.
    if (move == PokerMove.BET || move == PokerMove.RAISE) {
      listBuilder.add(new Set(CURRENT_BETTER, P[0]));
    }
    listBuilder.add(new Set(PLAYER_BETS, ImmutableList.of(600 + raiseByAmount, 100, 200, 600)));
    listBuilder.add(new Set(PLAYER_CHIPS,
        ImmutableList.of(2000 - 600 - raiseByAmount, 1900, 1800, 1400)));
    listBuilder.add(new Set(POTS, pots));
    return listBuilder.build();
  }

  
  //Tests
  
  @Test
  public void testPreFlopFold() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState,
        preFlopFourPlayerDealerFold, playersInfo_4_players, startingChips_4_player);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testPreFlopCall() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState,
        getPreFlopFourPlayerDealerRaise(PokerMove.CALL, 0), playersInfo_4_players, 
        startingChips_4_player);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testPreFlopRaise() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState,
        getPreFlopFourPlayerDealerRaise(PokerMove.RAISE, 600), playersInfo_4_players, 
        startingChips_4_player);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testPreFlopAllIn() {
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState,
        getPreFlopFourPlayerDealerRaise(PokerMove.RAISE, 1400), playersInfo_4_players,
        startingChips_4_player);
    assertMoveOk(verifyMove);
  }
  
  //@Test - This test depends on new GameApi SetTurn functionality
  public void testPreFlopMovesByWrongPlayer() {
    VerifyMove verifyMove = null;
    
    //wrong player fold
    verifyMove = move(p1_id, preFlopFourPlayerDealersTurnState,
        preFlopFourPlayerDealerFold, playersInfo_4_players, startingChips_4_player);
    assertHacker(verifyMove);
    
    //wrong player call
    verifyMove = move(p2_id, preFlopFourPlayerDealersTurnState,
        getPreFlopFourPlayerDealerRaise(PokerMove.CALL, 0), playersInfo_4_players,
        startingChips_4_player);
    assertHacker(verifyMove);
    
    //wrong player raise
    verifyMove = move(p3_id, preFlopFourPlayerDealersTurnState,
        getPreFlopFourPlayerDealerRaise(PokerMove.RAISE, 600), playersInfo_4_players,
        startingChips_4_player);
    assertHacker(verifyMove);
    
    //wrong player all in
    verifyMove = move(p1_id, preFlopFourPlayerDealersTurnState,
        getPreFlopFourPlayerDealerRaise(PokerMove.RAISE, 1400), playersInfo_4_players,
        startingChips_4_player);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testPreFlopRaiseByWrongAmount() {
    // Raise done by insufficient amount
    // (you have to raise to least double the existing bet)
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState,
        getPreFlopFourPlayerDealerRaise(PokerMove.RAISE, 400), playersInfo_4_players,
        startingChips_4_player);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testPreFlopRaiseByExcessAmount() {
    // Raise to more chips than player has
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState,
        getPreFlopFourPlayerDealerRaise(PokerMove.RAISE, 2000), playersInfo_4_players,
        startingChips_4_player);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testIllegalIncreaseInChips() {
    // P0 folds and increases his chips
    ImmutableList<Operation> illegalIncrease = ImmutableList.<Operation>of(
        new Set(WHOSE_MOVE, P[1]),
        new Set(PREVIOUS_MOVE, PokerMove.CALL.name()),
        new Set(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE),
        new Set(PLAYER_CHIPS, ImmutableList.of(10000, 1900, 1800, 1400)));
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState,
        illegalIncrease, playersInfo_4_players, startingChips_4_player);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testAttemptToViewBoard() {
    // P0 folds, but marks all community cards as visible to all
    ImmutableList<Operation> attemptToviewBoard = ImmutableList.<Operation>of(
        new SetTurn(p1_id),
        new Set(PREVIOUS_MOVE, PokerMove.CHECK.name()),
        new Set(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE),
        new Set(WHOSE_MOVE, P[1]),
        new SetVisibility(C + 8, ImmutableList.of(p0_id)),
        new SetVisibility(C + 9, ImmutableList.of(p0_id)),
        new SetVisibility(C + 10, ImmutableList.of(p0_id)),
        new SetVisibility(C + 11, ImmutableList.of(p0_id)),
        new SetVisibility(C + 12, ImmutableList.of(p0_id)));
    VerifyMove verifyMove = move(p0_id, preFlopFourPlayerDealersTurnState,
        attemptToviewBoard, playersInfo_4_players, startingChips_4_player);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testAttemptToSkipOthersMove() {
    // P3 marks next turn as P1 instead of P0
    ImmutableList<Operation> attemptToSkipMove = ImmutableList.<Operation>of(
        new SetTurn(p1_id),
        new Set(PREVIOUS_MOVE, PokerMove.CHECK.name()),
        new Set(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE),
        new Set(WHOSE_MOVE, P[1]));
    VerifyMove verifyMove = move(p3_id, preFlopFourPlayerFirstMoveState,
        attemptToSkipMove, playersInfo_4_players, startingChips_4_player);
    assertHacker(verifyMove);
  }

}
