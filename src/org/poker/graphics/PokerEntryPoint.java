package org.poker.graphics;

import org.game_api.GameApi.ContainerConnector;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;
import org.poker.client.PokerLogic;
import org.poker.client.PokerPresenter;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;


public class PokerEntryPoint implements EntryPoint {

  //private static final int NUMBER_OF_PLAYERS = 5;
  
  ContainerConnector container;
  PokerPresenter pokerPresenter;
  
  @Override
  public void onModuleLoad() {
    
    Game game = new Game() {
      @Override
      public void sendVerifyMove(VerifyMove verifyMove) {
        container.sendVerifyMoveDone(new PokerLogic().verify(verifyMove));
      }
      @Override
      public void sendUpdateUI(UpdateUI updateUI) {
        pokerPresenter.updateUI(updateUI);
      }
    };
    container = new ContainerConnector(game);
    
    // Graphics
    PokerGraphics pokerGraphics = new PokerGraphics();
    pokerPresenter = new PokerPresenter(pokerGraphics, container);
    
    /*final ListBox playerSelect = new ListBox();
    for(int i = 0; i < NUMBER_OF_PLAYERS; i++) {
      playerSelect.addItem("Player P" + i);
    }
    playerSelect.addItem("Viewer");
    playerSelect.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int selectedIndex = playerSelect.getSelectedIndex();
        int playerId = selectedIndex < NUMBER_OF_PLAYERS ?
            container.getPlayerIds().get(selectedIndex) : GameApi.VIEWER_ID;
        container.updateUi(playerId);
      }
    });*/
    FlowPanel flowPanel = new FlowPanel();
    flowPanel.add(pokerGraphics);
    //flowPanel.add(playerSelect);
    RootPanel.get("mainDiv").add(flowPanel);
    container.sendGameReady();
  }

}
