package org.poker.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.poker.client.GameApi.AttemptChangeTokens;
import org.poker.client.GameApi.EndGame;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.Set;
import org.poker.client.GameApi.SetTurn;
import org.poker.client.GameApi.SetVisibility;
import org.poker.client.GameApi.VerifyMove;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@RunWith(JUnit4.class)
public class PokerLogicTest extends AbstractPokerLogicTestBase {
  
  /**
   * P0 folds and the hand ends.<Br>
   * P0 will: <ul>
   * <li>Set betting round as SHOWDOWN</li>
   * <li>Assign next turn to himself</li>
   * <li>Set visibility of hole cards of players in hand to all</li>
   * </ul>
   */
  private final ImmutableList<Operation> riverThreePlayerDealerFolds =
      ImmutableList.<Operation>of(
          new SetTurn(p0_id),
          new Set(PREVIOUS_MOVE, PokerMove.FOLD.name()),
          new Set(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE),
          new Set(WHOSE_MOVE, P[0]),
          new Set(CURRENT_ROUND, BettingRound.SHOWDOWN.name()),
          new Set(PLAYERS_IN_HAND, ImmutableList.of(P[1], P[2])),
          new Set(PLAYER_BETS, ImmutableList.of(0,0,0)),
          new Set(POTS, ImmutableList.of(ImmutableMap.<String, Object>of(
              CHIPS, 3800,
              CURRENT_POT_BET, 0,
              PLAYERS_IN_POT, ImmutableList.of(P[1], P[2]),
              PLAYER_BETS, ImmutableList.of(0, 0, 0)))),
          new SetVisibility("C2"), new SetVisibility("C3"),
          new SetVisibility("C4"), new SetVisibility("C5"));
  
  /**
   * P0 calls and the hand ends.<Br>
   * P0 will: <ul>
   * <li>Set betting round as SHOWDOWN</li>
   * <li>Assign next turn to himself</li>
   * <li>Set visibility of hole cards of players in hand to all</li>
   * </ul>
   */
  private final ImmutableList<Operation> riverThreePlayerDealerCalls =
      ImmutableList.<Operation>of(
          new SetTurn(p0_id),
          new Set(PREVIOUS_MOVE, PokerMove.CALL.name()),
          new Set(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE),
          new Set(WHOSE_MOVE, P[0]),
          new Set(CURRENT_ROUND, BettingRound.SHOWDOWN.name()),
          new Set(PLAYER_BETS, ImmutableList.of(0, 0, 0)),
          new Set(PLAYER_CHIPS, ImmutableList.of(2000 - 400, 1600, 1600)),
          new Set(POTS, ImmutableList.of(ImmutableMap.<String, Object>of(
              CHIPS, 4200,
              CURRENT_POT_BET, 0,
              PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[0]),
              PLAYER_BETS, ImmutableList.of(0, 0, 0)))),
          new SetVisibility("C2"), new SetVisibility("C3"),
          new SetVisibility("C4"), new SetVisibility("C5"),
          new SetVisibility("C0"), new SetVisibility("C1"));
          
  
  /**
   * P0 rightly declares P2 as winner.
   */
  private final ImmutableList<Operation> showdownThreePlayerDealerMakesP2Winner =
      ImmutableList.<Operation>of(
          new AttemptChangeTokens(
              ImmutableMap.<Integer, Integer>of(p0_id, 1600, p1_id, 1600 , p2_id, 1600+ 4200),
              ImmutableMap.<Integer, Integer>of(p0_id, 0, p1_id, 0, p2_id, 0)),
          new EndGame(
              ImmutableMap.<Integer, Integer>of(
                  p0_id, 0,
                  p1_id, 0,
                  p2_id, 1)));
  
  
  private final ImmutableList<Operation> flopFourPlayerDealerCalls =
      ImmutableList.<Operation>builder().
          add(new SetTurn(p1_id)).
          add(new Set(PREVIOUS_MOVE, PokerMove.CALL.name())).
          add(new Set(PREVIOUS_MOVE_ALL_IN, Boolean.FALSE)).
          add(new Set(WHOSE_MOVE, P[1])).
          add(new Set(CURRENT_ROUND, BettingRound.TURN.name())).
          add(new Set(PLAYER_BETS, ImmutableList.of(0, 0, 0, 0))).
          add(new Set(PLAYER_CHIPS, ImmutableList.of(1500 - 500, 2000, 3000, 5000))).
          add(new Set(POTS, ImmutableList.of(
              ImmutableMap.<String, Object>of(
                  CHIPS, 3500 + 500,
                  CURRENT_POT_BET, 0,
                  PLAYERS_IN_POT, ImmutableList.of(P[1], P[2], P[3], P[0]),
                  PLAYER_BETS, ImmutableList.of(0, 0, 0, 0))))).
          add(new SetVisibility(C + (4 * 2 + 3))).
          build();
  

  // Tests
  
  @Test
  public void testEndHandAfterLastPlayerFolds() {
    // Last player folds and the hand ends
    VerifyMove verifyMove = move(p0_id, riverThreePlayerDealersTurnState,
        riverThreePlayerDealerFolds, playersInfo_3_players, startingChips_3_player);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testEndHandAfterLastPlayerCalls() {
    // Last player calls and the hand ends
    VerifyMove verifyMove = move(p0_id, riverThreePlayerDealersTurnState,
        riverThreePlayerDealerCalls, playersInfo_3_players, startingChips_3_player);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testEndGame() {
    ImmutableMap<String, Object> state = 
        getStateWithCards(showdownThreePlayerDealersTurnState,
            showdownThreePlayerDealersTurncardList);
    // Last player folds and the hand ends
    VerifyMove verifyMove = move(p0_id, state,
        showdownThreePlayerDealerMakesP2Winner, playersInfo_3_players, startingChips_3_player);
    assertMoveOk(verifyMove);
  }
  //TODO: negative of this test will depend on how shuffle operation behaves 
  
  @Test
  public void testFlopToTurnTransition() {
    VerifyMove verifyMove = move(p0_id, flopFourPlayerDealerTurnState,
        flopFourPlayerDealerCalls, playersInfo_4_players, startingChips_4_player);
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
        flopFourPlayerWrongCardsOpened, playersInfo_4_players, startingChips_4_player);
    assertHacker(verifyMove);
  } 
}
