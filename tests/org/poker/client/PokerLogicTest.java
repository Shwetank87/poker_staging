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
   * 3-way hand during River<Br>
   * Pot amount before River: 3000<Br>
   * P1 bets 400<Br>
   * P2 calls<Br>
   * P0 to act
   */
  private final ImmutableMap<String, Object> riverThreePlayerDealersTurnState = 
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
   * 3-way hand in ShowDown state<Br>
   * Pot amount before River: 4200<Br>
   * P0 has the turn and needs to mark EndGame<br>
   * Assume that P1 has the best hand.
   */
  private final ImmutableMap<String, Object> showdownThreePlayerDealersTurnState = 
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
  
  private ImmutableList<String> cardList = ImmutableList.of(
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
          
  private ImmutableMap<String, Object> getStateWithCards(ImmutableMap<String, Object> state,
      ImmutableList<String> cards) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    builder.putAll(state);
    for(int i = 0 ; i <52 ; i++) {
      builder.put(C+i, cards.get(i));
    }
    return builder.build();
  }
  
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

  
  /**
   * 4 way hand on Flop<Br>
   * Pot amount before Flop: 2000<Br>
   * P1 bets 500<Br>
   * P2, P3 call<Br>
   * P0 to act
   */
  private final ImmutableMap<String, Object> flopFourPlayerDealerTurnState =
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
        getStateWithCards(showdownThreePlayerDealersTurnState, cardList);
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
