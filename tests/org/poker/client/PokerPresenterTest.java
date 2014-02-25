package org.poker.client;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.poker.client.Card.Rank;
import org.poker.client.Card.Suit;
import org.poker.client.GameApi.Container;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.SetTurn;
import org.poker.client.GameApi.UpdateUI;
import org.poker.client.PokerPresenter.View;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/** Tests for {@link PokerPresenter}.<br>
 * Test plan:<br>
 * There are several scenarios for Presenter to handle:<br>
 * 1) Initial buy-in moves<br>
 * 2) initial empty state move by dealer<br>
 * 3) Following moves by any player:<br>
 *      - Fold<br>
 *      - Check<br>
 *      - Call<br>
 *      - Bet<br>
 *      - Raise<br>
 *      - All-in<br>
 * 4) Viewer's turn<br>
 * 5) End-Game scenario
 *<P>
 * One or more of the following methods will be called in each case:<br>
 * 1) doBuyIn<br>
 * 2) buyInDone<br>
 * 3) makeYourMove<br>
 * 4) moveMade<br>
 */

@RunWith(JUnit4.class)
public class PokerPresenterTest extends AbstractPokerLogicTestBase {

  private PokerPresenter pokerPresenter;
  private final PokerLogic pokerLogic = new PokerLogic();
  private final PokerLogicHelper pokerLogicHelper = PokerLogicHelper.getInstance();
  private View mockView;
  private Container mockContainer;
  
  private final int viewerId = GameApi.VIEWER_ID;
    
  @Before
  public void runBefore() {
    mockView = Mockito.mock(View.class);
    mockContainer = Mockito.mock(Container.class);
    pokerPresenter = new PokerPresenter(mockView, mockContainer);
    verify(mockView).setPresenter(pokerPresenter);
  }
  
  @After
  public void runAfter() {
    // Check that no more interactions are remaining
    verifyNoMoreInteractions(mockView);
    verifyNoMoreInteractions(mockContainer);
  }
  
  
  // Tests
  
  @Test
  public void testBuyInForAnyPlayer() {
    pokerPresenter.updateUI(createUpdateUI(3, p1_id, 0, emptyState, getStartingChips(0, 0, 0)));
    // P1 buys-in for 5000
    pokerPresenter.buyInDone(5000);
    verify(mockView).doBuyIn();
    verify(mockContainer).sendMakeMove(pokerLogic.getInitialBuyInMove(p1_id, 5000));
  }
  
  @Test
  public void testEmptyStateForDealer() {
    pokerPresenter.updateUI(createUpdateUI(4, p0_id, 0, emptyState, startingChips_4_player));
    verify(mockContainer).sendMakeMove(pokerLogic.getInitialMove(
        playersIds_4_players, startingChips_4_player));
  }
  
  @Test
  public void testEmptyStateForNonDealerPlayer() {
    pokerPresenter.updateUI(createUpdateUI(4, p2_id, 0, emptyState, startingChips_4_player));
  }
  
  @Test
  public void testEmptyStateForViewer() {
    pokerPresenter.updateUI(createUpdateUI(4, viewerId, 0, emptyState, startingChips_4_player));
  }
  
  @Test
  public void testFlopFourPlayerFoldMove() {
    PokerState state = pokerLogicHelper.gameApiStateToPokerState(
        flopFourPlayerDealerTurnState);
    pokerPresenter.updateUI(createUpdateUI(4, p0_id, p0_id, flopFourPlayerDealerTurnState,
        startingChips_4_player));
    // P0 folds
    pokerPresenter.moveMade(PokerMove.FOLD, 0);
    verify(mockView).setPlayerState(4, 0, state.getCurrentRound(), state.getPlayerBets(), 
        state.getPots(), state.getPlayerChips(), getHoleCards(4), getAbsentCards(5));
    verify(mockView).makeYourMove();
    verify(mockContainer).sendMakeMove(pokerLogic.doFoldMove(state, playersIds_4_players));
  }

  @Test
  public void testFlopFourPlayerCallMove() {
    PokerState state = pokerLogicHelper.gameApiStateToPokerState(
        flopFourPlayerDealerTurnState);
    pokerPresenter.updateUI(createUpdateUI(4, p0_id, p0_id, flopFourPlayerDealerTurnState,
        startingChips_4_player));
    // P0 calls 500
    pokerPresenter.moveMade(PokerMove.CALL, 500);
    verify(mockView).setPlayerState(4, 0, state.getCurrentRound(), state.getPlayerBets(), 
        state.getPots(), state.getPlayerChips(), getHoleCards(4), getAbsentCards(5));
    verify(mockView).makeYourMove();
    verify(mockContainer).sendMakeMove(pokerLogic.doCallMove(state, playersIds_4_players, 500));
  }
  
  @Test
  public void testFlopFourPlayerRaiseMove() {
    PokerState state = pokerLogicHelper.gameApiStateToPokerState(
        flopFourPlayerDealerTurnState);
    pokerPresenter.updateUI(createUpdateUI(4, p0_id, p0_id, flopFourPlayerDealerTurnState,
        startingChips_4_player));
    // P0 raises to 1000 total from 500
    pokerPresenter.moveMade(PokerMove.RAISE, 1000);
    verify(mockView).setPlayerState(4, 0, state.getCurrentRound(), state.getPlayerBets(), 
        state.getPots(), state.getPlayerChips(), getHoleCards(4), getAbsentCards(5));
    verify(mockView).makeYourMove();
    verify(mockContainer).sendMakeMove(pokerLogic.doRaiseMove(state, playersIds_4_players, 1000));
  }
  
  @Test
  public void testFlopFourPlayerBetMove() {
    PokerState state = pokerLogicHelper.gameApiStateToPokerState(
        flopFourPlayerNoBetsMadeState);
    pokerPresenter.updateUI(createUpdateUI(4, p2_id, p2_id, flopFourPlayerNoBetsMadeState,
        startingChips_4_player));
    //P2 bets 500
    pokerPresenter.moveMade(PokerMove.BET, 500);
    verify(mockView).setPlayerState(4, 2, state.getCurrentRound(), state.getPlayerBets(),
        state.getPots(), state.getPlayerChips(), getHoleCards(4), getAbsentCards(5));
    verify(mockView).makeYourMove();
    verify(mockContainer).sendMakeMove(pokerLogic.doBetMove(state, playersIds_4_players, 500));
  }
  
  @Test
  public void testFlopFourPlayerCheckMove() {
    PokerState state = pokerLogicHelper.gameApiStateToPokerState(
        flopFourPlayerNoBetsMadeState);
    pokerPresenter.updateUI(createUpdateUI(4, p2_id, p2_id, flopFourPlayerNoBetsMadeState,
        startingChips_4_player));
    //P2 bets 500
    pokerPresenter.moveMade(PokerMove.CHECK, 0);
    verify(mockView).setPlayerState(4, 2, state.getCurrentRound(), state.getPlayerBets(),
        state.getPots(), state.getPlayerChips(), getHoleCards(4), getAbsentCards(5));
    verify(mockView).makeYourMove();
    verify(mockContainer).sendMakeMove(pokerLogic.doCheckMove(state, playersIds_4_players));
  }
  
  
  
  
  @Test
  public void testFlopFourPlayerOtherPlayerMove() {
    PokerState state = pokerLogicHelper.gameApiStateToPokerState(
        flopFourPlayerDealerTurnState);
    pokerPresenter.updateUI(createUpdateUI(4, p1_id, p0_id, flopFourPlayerDealerTurnState,
        startingChips_4_player));
    // P0 folds
    verify(mockView).setPlayerState(4, 1, state.getCurrentRound(), state.getPlayerBets(), 
        state.getPots(), state.getPlayerChips(), getHoleCards(4), getAbsentCards(5));
  }
  
  @Test
  public void testFlopFourPlayerViewerMove() {
    PokerState state = pokerLogicHelper.gameApiStateToPokerState(
        flopFourPlayerDealerTurnState);
    pokerPresenter.updateUI(createUpdateUI(4, viewerId, p0_id, flopFourPlayerDealerTurnState,
        startingChips_4_player));
    // P0 folds
    verify(mockView).setViewerState(4, state.getCurrentRound(), state.getPlayerBets(), 
        state.getPots(), state.getPlayerChips(), getHoleCards(4), getAbsentCards(5));
  }
  
  @Test
  public void testFlopFourPlayerAllInMove() {
    PokerState state = pokerLogicHelper.gameApiStateToPokerState(
        flopFourPlayerDealerTurnState);
    pokerPresenter.updateUI(createUpdateUI(4, p0_id, p0_id, flopFourPlayerDealerTurnState,
        startingChips_4_player));
    // P0 goes all-in
    pokerPresenter.moveMade(PokerMove.RAISE, 1500); // P0 had 1500 chips remaining
    verify(mockView).setPlayerState(4, 0, state.getCurrentRound(), state.getPlayerBets(), 
        state.getPots(), state.getPlayerChips(), getHoleCards(4), getAbsentCards(5));
    verify(mockView).makeYourMove();
    verify(mockContainer).sendMakeMove(pokerLogic.doRaiseMove(state, playersIds_4_players, 1500));
  }
  
  @Test
  public void testEndGame() {
    ImmutableMap<String, Object> apiState = getStateWithCards(
        showdownThreePlayerDealersTurnState, showdownThreePlayerDealersTurncardList);
    PokerState state = pokerLogicHelper.gameApiStateToPokerState(apiState);
    pokerPresenter.updateUI(createUpdateUI(3, p0_id, p0_id, apiState, startingChips_3_player));
    // P0 marks P1 as winner
    verify(mockView).setPlayerState(3, 0, state.getCurrentRound(), state.getPlayerBets(),
        state.getPots(), state.getPlayerChips(), getHoleCards(3, state.getCards()), 
        getOptionalCards(6, 11, state.getCards()));
    verify(mockContainer).sendMakeMove(pokerLogic.doEndGameMove(state, playersIds_3_players));
  }
  
  @Test
  public void testEndGameForOtherPlayer() {
    ImmutableMap<String, Object> apiState = getStateWithCards(
        showdownThreePlayerDealersTurnState, showdownThreePlayerDealersTurncardList);
    PokerState state = pokerLogicHelper.gameApiStateToPokerState(apiState);
    pokerPresenter.updateUI(createUpdateUI(3, p1_id, p0_id, apiState, startingChips_3_player));
    verify(mockView).setPlayerState(3, 1, state.getCurrentRound(), state.getPlayerBets(),
        state.getPots(), state.getPlayerChips(), getHoleCards(3, state.getCards()), 
        getOptionalCards(6, 11, state.getCards()));
  }
  
  @Test
  public void testEndGameForViewer() {
    ImmutableMap<String, Object> apiState = getStateWithCards(
        showdownThreePlayerDealersTurnState, showdownThreePlayerDealersTurncardList);
    PokerState state = pokerLogicHelper.gameApiStateToPokerState(apiState);
    pokerPresenter.updateUI(createUpdateUI(3, viewerId, p0_id, apiState, startingChips_3_player));
    // P0 marks P1 as winner
    verify(mockView).setViewerState(3, state.getCurrentRound(), state.getPlayerBets(),
        state.getPots(), state.getPlayerChips(), getHoleCards(3, state.getCards()), 
        getOptionalCards(6, 11, state.getCards()));
  }
  
  
  
  // Utility methods
  
  private List<List<Optional<Card>>> getHoleCards(int numOfPlayers) {
    ImmutableList.Builder<List<Optional<Card>>> builder = ImmutableList.builder();
    for(int i = 0; i < numOfPlayers; i++) {
      builder.add(getAbsentCards(2));
    }
    return builder.build();
  }
  
  private List<List<Optional<Card>>> getHoleCards(int numOfPlayers, List<Optional<Card>> cards) {
    ImmutableList.Builder<List<Optional<Card>>> builder = ImmutableList.builder();
    for(int i = 0; i < numOfPlayers; i++) {
      builder.add(ImmutableList.<Optional<Card>>of(cards.get(i * 2), cards.get(i * 2 + 1)));
    }
    return builder.build();
  }
  
  private List<Optional<Card>> getAbsentCards(int numOfCards) {
    List<Optional<Card>> cards = Lists.newArrayList();
    for (int i = 0; i < numOfCards; i++) {
      cards.add(Optional.<Card>absent());
    }
    return cards;
  }
  
  private List<Optional<Card>> getOptionalCards(int fromInclusive, int toExclusive) {
    List<Optional<Card>> cards = Lists.newArrayList();
    for (int i = fromInclusive; i < toExclusive; i++) {
      Rank rank = Rank.values()[i / 4];
      Suit suit = Suit.values()[i % 4];
      cards.add(Optional.<Card>of(new Card(suit, rank)));
    }
    return cards;
  }
  
  private List<Optional<Card>> getOptionalCards(int fromInclusive, int toExclusive, 
      List<Optional<Card>> cards) {
    List<Optional<Card>> retCards = Lists.newArrayList();
    for (int i = fromInclusive; i < toExclusive; i++) {
      retCards.add(cards.get(i));
    }
    return retCards;
  }
  
  private UpdateUI createUpdateUI(int numOfPlayers, int yourPlayerId,
      int turnOfPlayerId, Map<String, Object> state,
      Map<Integer, Integer> playerIdToTokensInPot) {
    return new UpdateUI(yourPlayerId,
        getPlayersInfo(numOfPlayers),
        state,
        emptyState, //presenter doesn't care about last state
        ImmutableList.<Operation>of(new SetTurn(turnOfPlayerId)),
        0, //presenter doesn't care about last player ID
        playerIdToTokensInPot);
  }
  
}
