package org.poker.client;

import java.util.List;

import com.google.common.base.Optional;

import org.poker.client.GameApi.Container;
import org.poker.client.GameApi.UpdateUI;

public class PokerPresenter {
  
  interface View {
    /**
     * 
     * The process of making a move involves the Presenter calling following on the viewer:
     * Presenter calls {@link #makeYourMove()}} on the viewer
     * 
     * The viewer will call following methods on the presenter
     * When a move has been made : {@link #moveMade}
     * 
     * The Presenter will also call {@link #openBoardCard(List)} on the viewer 
     * when a board card is opened after a round {Flop, Turn, River}
     * (A related question written in method comment)
     * 
     * In addition to the above. Every player will make an initial buy-in move
     */
    
    void setPresenter(PokerPresenter pokerPresenter);
    
    void setViewerState(int numOfPlayers, List<Integer> playerBets, List<Pot> pots,
        List<Integer> playerChips, List<Optional<Card>> board);
    
    void setPlayerState(int numOfPlayers, List<Integer> playerBets, List<Pot> pots, 
        List<Optional<Card>> board, List<Card> myCards);
    
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
    
    /**
     * Asks the view to open a card on the board
     * 
     * Q.) Do we really need this? 
     * I've included this because this should involve an animation on the view's part, 
     * but no action from the human player.
     * So every player's view would be notified of the round end and card opening
     */
    void openBoardCard(List<Optional<Card>> board);
  }
  
  private final PokerLogic pokerLogic = new PokerLogic();
  private final View view;
  private final Container container;
  
  private PokerState pokerState;

  public PokerPresenter(View view, Container container) {
    this.view = view;
    this.container = container;
    view.setPresenter(this);
  }
  
  /* Updates the presenter and view with the state in updateUI   */
  public void updateUI(UpdateUI updateUI) {
    List<Integer> playerIdList = updateUI.getPlayerIds();
    int playerId = updateUI.getYourPlayerId();
    int playerIndex = updateUI.getPlayerIndex(playerId);
    
    // Check if this is an initial setup move
    if (updateUI.getState().isEmpty()) {
      //check if our player has done a buy in
      if (!hasBoughtIn()) {
        view.doBuyIn();
        return;
      }
      
      // Check if everyone has done a buy-In and game ready to start
      if( hasEveryoneBoughtIn() && isDealer(playerIndex)) {
        sendInitialMove(playerIdList);
        return;
      }
    }
    
    PokerState pokerState = pokerLogic.helper.gameApiStateToPokerState(updateUI.getState());
    int numOfPlayers = pokerState.getNumberOfPlayers();
    List<Integer> playerBets = pokerState.getPlayerBets();
    List<Pot> pots = pokerState.getPots();
    List<Integer> playerChips = pokerState.getPlayerChips();
    List<Optional<Card>> board = indexToCards(pokerState.getBoard());
    // Check if this is a third person viewer
    if (updateUI.isViewer()) {
      view.setViewerState(numOfPlayers, playerBets, pots , playerChips , board );
      return;
    }
    
    //TODO
    
    
  }
  
  private boolean hasEveryoneBoughtIn() {
    // TODO Auto-generated method stub
    return false;
  }

  private boolean hasBoughtIn() {
    // TODO Auto-generated method stub
    return false;
  }

  private void sendInitialMove(List<Integer> playerIdList) {
    container.sendMakeMove(pokerLogic.getInitialMove(playerIds, startingChips));
  }

  private boolean isDealer(int playerIndex) {
    return playerIndex == pokerLogic.DEALER_INDEX;
  }
  
  private List<Optional<Card>> indexToCards(List<Optional<Integer>> list) {
    //TODO
  }

  /**
   * Makes the appropriate move {Bet, Fold, Call or Raise} according to the amount
   * The viewer can call this only after presenter called {@link #makeYourMove}
   */
  public void moveMade(int amount){
    //TODO
  }
  
  /**
   * 
   */
  // We can merge move made and buy in Done.. keeping separate for clarity
  public void buyInDone(int amount) {
    // TODO
  }

}
