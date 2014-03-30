package org.poker.client;

import java.util.List;
import java.util.Map;

import org.game_api.GameApi.Container;
import org.game_api.GameApi.UpdateUI;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;


/**
 * Presenter for controlling the graphics for Poker 
 */
public class PokerPresenter {
  
  public interface View {
    /**
     * 
     * The process of making a move involves the Presenter calling following on the viewer:
     * Presenter calls {@link #makeYourMove()}} on the viewer
     * 
     * The viewer will call following methods on the presenter
     * When a move has been made : {@link #moveMade}
     * 
     * The Presenter will also call {@link #openBoardCards(List)} on the viewer 
     * when a board card is opened after a round (at Flop, Turn and River)
     * 
     * In addition to the above. Every player will make an initial buy-in move
     */
    
    void setPresenter(PokerPresenter pokerPresenter);
    
    void setViewerState(int numOfPlayers,
        int turnIndex,
        BettingRound round,
        List<Integer> playerBets,
        List<Pot> pots,
        List<Integer> playerChips,
        List<Player> playersInHand,
        List<List<Optional<Card>>> holeCards,
        List<Optional<Card>> board);
    
    void setPlayerState(int numOfPlayers,
        int myIndex,
        int turnIndex,
        BettingRound round,
        List<Integer> playerBets,
        List<Pot> pots, 
        List<Integer> playerChips,
        List<Player> playersInHand,
        List<List<Optional<Card>>> holeCards,
        List<Optional<Card>> board);
    
    void setEndGameState(int numOfPlayers,
        BettingRound round,
        List<Integer> playerBets,
        List<Pot> pots,
        List<Integer> playerChips,
        List<Player> playersInHand,
        List<List<Optional<Card>>> holeCards,
        List<Optional<Card>> board);    
    /**
     * Asks the Player to make his move 
     * The user can make his move {Bet, Fold, Call or Raise} by calling
     * {@link #moveMade}  
     */
    void makeYourMove();
    
    /**
     * Asks the Player to buy in
     * The view makes the move by calling
     * {@link #buyInDone(int)} on Presenter
     */
    void doBuyIn();
  }
  
  private final PokerLogic pokerLogic = new PokerLogic();
  private final PokerLogicHelper pokerLogicHelper = PokerLogicHelper.getInstance();
  private final View view;
  private final Container container;
  
  // It's Optional because it can also be viewer
  private Optional<Player> myPlayer;
  private PokerState pokerState;
  List<String> playerIdList;
  private Map<String, Integer> playerIdToTokensInPot;
  
  public PokerPresenter(View view, Container container) {
    this.view = view;
    this.container = container;
    view.setPresenter(this);
  }
  
  /* Updates the presenter and view with the state in updateUI   */
  public void updateUI(UpdateUI updateUI) {
    playerIdList = updateUI.getPlayerIds();
    int numOfPlayers = playerIdList.size();
    String playerId = updateUI.getYourPlayerId();
    int playerIndex = updateUI.getPlayerIndex(playerId);
    myPlayer = (playerIndex >=0 && playerIndex < numOfPlayers) ?
        Optional.of(Player.values()[playerIndex]) : Optional.<Player>absent();
    playerIdToTokensInPot = updateUI.getPlayerIdToNumberOfTokensInPot();
    
    // Check if the playerIdToTokensInPot is empty
    if (playerIdToTokensInPot.isEmpty()){
      for(String id : playerIdList) {
        playerIdToTokensInPot.put(id, 0);
      }
    }
    
    // Check if this is an initial setup move
    if (updateUI.getState().isEmpty()) {
      // Check that this is not an outside viewer
      if (myPlayer.isPresent()) {
        // If current player has not done the buy-in
        if (!buyInSuccessfullyDone()) {
          view.doBuyIn();
        }
        // Check if everyone has done a buy-In and current player is the dealer
        else if (canGameStart() && isDealer()) {
          sendInitialMove(playerIdList);
        }
      }
      return;
    }
    
    pokerState = pokerLogicHelper.gameApiStateToPokerState(updateUI.getState());
    int turnIndex = pokerState.getWhoseMove().ordinal();
    
    BettingRound round = pokerState.getCurrentRound();
    List<Integer> playerBets = pokerState.getPlayerBets();
    List<Pot> pots = pokerState.getPots();
    List<Integer> playerChips = pokerState.getPlayerChips();
    List<Player> playersInHand = pokerState.getPlayersInHand();
    List<Optional<Card>> board = cardIndexToOptionalList(pokerState.getBoard());
    List<List<Optional<Card>>> holeCardList = Lists.newArrayList();
    // Get List of hole cards
    for(List<Integer> holeCards : pokerState.getHoleCards()) {
      holeCardList.add(cardIndexToOptionalList(holeCards));
    }
    
    // Check if this is a third person viewer
    if (updateUI.isViewer()) {
      if (round == BettingRound.END_GAME) {
        view.setEndGameState(numOfPlayers, round, playerBets, pots, playerChips, playersInHand,
            holeCardList, board);
      }
      if (round != BettingRound.SHOWDOWN) {
        view.setViewerState(numOfPlayers, turnIndex, round, playerBets, pots, playerChips,
            playersInHand, holeCardList, board);
      }
      return;
    }
    // Check if this is an AI player
    if(updateUI.isAiPlayer()) {
      //TODO: to be done in a later HW
      return;
    }
    
    if (round == BettingRound.SHOWDOWN) {
      if (isMyTurn()) {
          container.sendMakeMove(pokerLogic.doEndGameMove(pokerState, playerIdList));
      }
      return;
    }
    
    if (round == BettingRound.END_GAME) {
      view.setEndGameState(numOfPlayers, round, playerBets, pots, playerChips, playersInHand,
          holeCardList, board);
      return;
    }
    
    view.setPlayerState(numOfPlayers, playerIndex, turnIndex, round, playerBets, pots, playerChips,
        playersInHand, holeCardList, board);
    
    if(isMyTurn() && round != BettingRound.END_GAME) {
      view.makeYourMove();
    }
  }

  /**
   * Checks if all players have successfully done a buy-in
   * 
   * @param playerIdToTokensInPot
   * @return
   */
  private boolean canGameStart() {
    boolean canGameStart = true;
    for(String playerId : playerIdList) {
      if(playerIdToTokensInPot.get(playerId) == 0) {
        return false;
      }
    }
    return canGameStart;
  }
  
  /**
   * Checks if current player has successfully done a buy-in.
   * 
   * @return
   */
  private boolean buyInSuccessfullyDone() {
    int myPlayerIndex = myPlayer.get().ordinal();
    return playerIdToTokensInPot.get(playerIdList.get(myPlayerIndex)) != 0;
  }
  
  /**
   * Checks if the current player is the dealer.
   * 
   * @param playerIndex
   * @return
   */
  private boolean isDealer() {
    return myPlayer.isPresent() && myPlayer.get().ordinal() == pokerLogic.DEALER_INDEX;
  }
  
  /**
   * Checks if it's the current player's turn.
   * 
   * @return
   */
  private boolean isMyTurn() {
    return myPlayer.isPresent() && myPlayer.get() == pokerState.getWhoseMove();
  }
  
  /**
   * Return List<Optional<Card>> for given List<Integer> using {@link PokerState#cards}.
   * 
   * @param cardIndexList
   * @return
   */
  private List<Optional<Card>> cardIndexToOptionalList(List<Integer> cardIndexList) {
    List<Optional<Card>> cards = Lists.newArrayList();
    for(int cardIndex : cardIndexList) {
      cards.add(pokerState.getCards().get(cardIndex));
    }
    return cards; 
  }

  /**
   * Send the buy-in move to the container.
   * 
   * @param amount
   */
  public void buyInDone(int amount) {
    String myPlayerId = playerIdList.get(myPlayer.get().ordinal());
    container.sendMakeMove(pokerLogic.getInitialBuyInMove(myPlayerId, amount, playerIdToTokensInPot));
  }
  
  /**
   * Send the initial move to the container
   * 
   * @param playerIdList
   */
  private void sendInitialMove(List<String> playerIdList) {
    container.sendMakeMove(pokerLogic.getInitialMove(playerIdList, playerIdToTokensInPot));
  }
  
  /**
   * Send the appropriate {@link PokerMove} (Fold, Check, Call, Bet or Raise} 
   * to the container.
   * The view can only call this method after presenter called {@link View#makeYourMove}.
   * 
   * @param move
   * @param additionalAmount The <B>additional</B> amount player bet in current move. 
   */
  public void moveMade(PokerMove move, int additionalAmount) {
    switch(move) {
    case FOLD:
      container.sendMakeMove(pokerLogic.doFoldMove(pokerState, playerIdList));
      break;
    case CHECK:
      container.sendMakeMove(pokerLogic.doCheckMove(pokerState, playerIdList));
      break;
    case CALL:
      container.sendMakeMove(pokerLogic.doCallMove(pokerState, playerIdList, additionalAmount));
      break;
    case BET:
      container.sendMakeMove(pokerLogic.doBetMove(pokerState, playerIdList, additionalAmount));
      break;
    case RAISE:
      container.sendMakeMove(pokerLogic.doRaiseMove(pokerState, playerIdList, additionalAmount));
      break;
    }
    return;
  }
  
}
