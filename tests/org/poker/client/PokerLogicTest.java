package org.poker.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.poker.client.GameApi.EndGame;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.Set;
import org.poker.client.GameApi.SetVisibility;
import org.poker.client.GameApi.VerifyMove;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@RunWith(JUnit4.class)
public class PokerLogicTest extends AbstractPokerLogicTestBase {
  
  /**
   * 3-way hand during River<Br>
   * Pot amount before River: 3000<Br>
   * P1 bets 400<Br>
   * P2 calls<Br>
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
              PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[0])))).
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
   * P0 folds but declare himself winner.
   */
  private final ImmutableList<Operation> riverThreePlayerDealerFoldsAndWins =
      ImmutableList.<Operation>of(
          new Set(CURRENT_ROUND, BettingRound.SHOWDOWN.name()),
          new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2])),
          new Set(PLAYER_CHIPS, ImmutableList.of(2000, 1600 + 3800, 1600)),
          new EndGame(p0_id));
 
  
  /**
   * 4 way hand on Flop<Br>
   * Pot amount before Flop: 2000<Br>
   * P1 bets 500<Br>
   * P2, P3 call<Br>
   * P0 to act
   */
  private final ImmutableMap<String, Object> flopFourPlayerDealerTurnState =
      ImmutableMap.<String, Object>builder().
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
              PLAYERS_IN_POT, ImmutableList.of(P[0], P[1], P[2], P[3])))).
      build();
  
  private final ImmutableList<Operation> flopFourPlayerDealerCalls =
      ImmutableList.<Operation>builder().
          add(new Set(WHOSE_MOVE, P[1])).
          add(new Set(CURRENT_BETTER, P[1])). // P1 will be current better initially in new round.
          add(new Set(CURRENT_ROUND, BettingRound.TURN.name())).
          add(new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2], P[3], P[0]))).
          add(new Set(PLAYER_BETS, ImmutableList.of(0, 0, 0, 0))).
          add(new Set(PLAYER_CHIPS, ImmutableList.of(1500 - 500, 2000, 3000, 5000))).
          add(new Set(POTS, ImmutableList.of(
              ImmutableMap.<String, Object>of(
                  CHIPS, 2000 + 500 + 500 + 500,
                  CURRENT_POT_BET, 0,
                  PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3], P[0]))))).
          add(new SetVisibility(C + (4 * 2 + 3))).
          build();
  

  // Tests
  
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
  public void testFlopToTurnTransition() {
    VerifyMove verifyMove = move(p0_id, flopFourPlayerDealerTurnState,
        flopFourPlayerDealerCalls, playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testFlopToTurnTransitionWithWrongCardOpen() {
    // Player opens more board cards than necessary
    ImmutableList<Operation> flopFourPlayerWrongCardsOpened =
        ImmutableList.<Operation>builder().
            addAll(flopFourPlayerDealerCalls).
            add(new SetVisibility(C + (4 * 2 + 4))). // Set River card as open prematurely
            build();
    VerifyMove verifyMove = move(p0_id, flopFourPlayerDealerTurnState,
        flopFourPlayerWrongCardsOpened, playersInfo_4_players);
    assertHacker(verifyMove);
  } 
}
