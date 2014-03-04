package org.poker.graphics;

import java.util.List;

import org.poker.client.BettingRound;
import org.poker.client.Card;
import org.poker.client.Player;
import org.poker.client.PokerLogic;
import org.poker.client.PokerMove;
import org.poker.client.PokerPresenter;
import org.poker.client.Pot;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class PokerGraphics extends Composite implements PokerPresenter.View {
  public interface PokerGraphicsUiBinder extends UiBinder<Widget, PokerGraphics> {
  }
  
  private static final boolean AUTO_BUY_IN = false;
  private static final int AUTO_BUY_IN_VALUE = 10000;
  
  @UiField
  AbsolutePanel pokerTable;
  
  @UiField
  VerticalPanel seat1;
  @UiField
  HorizontalPanel seat2;
  @UiField
  HorizontalPanel seat3;
  @UiField
  VerticalPanel seat4;
  @UiField
  VerticalPanel seat5;
  @UiField
  VerticalPanel seat6;
  @UiField
  HorizontalPanel seat7;
  @UiField
  HorizontalPanel seat8;
  @UiField
  VerticalPanel seat9;
  
  @UiField
  HorizontalPanel holeCards1;
  @UiField
  HorizontalPanel holeCards2;
  @UiField
  HorizontalPanel holeCards3;
  @UiField
  HorizontalPanel holeCards4;
  @UiField
  HorizontalPanel holeCards5;
  @UiField
  HorizontalPanel holeCards6;
  @UiField
  HorizontalPanel holeCards7;
  @UiField
  HorizontalPanel holeCards8;
  @UiField
  HorizontalPanel holeCards9;
  
  @UiField
  VerticalPanel info1;
  @UiField
  VerticalPanel info2;
  @UiField
  VerticalPanel info3;
  @UiField
  VerticalPanel info4;
  @UiField
  VerticalPanel info5;
  @UiField
  VerticalPanel info6;
  @UiField
  VerticalPanel info7;
  @UiField
  VerticalPanel info8;
  @UiField
  VerticalPanel info9;
  
  HorizontalPanel[] holeCardPanelArr;
  CellPanel[] infoPanelArr;
  
  @UiField
  HorizontalPanel communityCards;
  @UiField
  VerticalPanel potInfoPanel;
  
  @UiField
  Button btnFold;
  @UiField
  Button btnCheck;
  @UiField
  Button btnCall;
  @UiField
  Button btnBet;
  @UiField
  TextBox txtAmount;
  @UiField
  Button btnAllIn;
  
  private PokerPresenter presenter;
  private final CardImageSupplier cardImageSupplier;
  
  private int currentBet;
  private int myCurrentBet;
  private int myChips;
  
  public PokerGraphics() {
    CardImages cardImages = GWT.create(CardImages.class);
    this.cardImageSupplier = new CardImageSupplier(cardImages);
    PokerGraphicsUiBinder uiBinder = GWT.create(PokerGraphicsUiBinder.class);
    initWidget(uiBinder.createAndBindUi(this));
    holeCardPanelArr = new HorizontalPanel[] {holeCards1, holeCards2, holeCards3,
        holeCards4, holeCards5, holeCards6, holeCards7, holeCards8, holeCards9};
    infoPanelArr = new CellPanel[] {info1, info2, info3,
        info4, info5, info6, info7, info8, info9};
    pokerTable.setStyleName("pokerTablePanel");
    for (CellPanel panel : infoPanelArr) {
      panel.setStyleName("playerInfoPanel");
    }
    potInfoPanel.add(new Label("Waiting for all players to buy-in..."));
    
  }
  
  private List<Image> createCardImages(List<Optional<Card>> cards) {
    List<Image> images = Lists.newArrayList();
    for (Optional<Card> card : cards) {
      CardImage cardImage = null;
      if(card.isPresent()) {
        cardImage = CardImage.Factory.getCardImage(card.get());
      }
      else {
        cardImage = CardImage.Factory.getBackOfCardImage();
      }
      Image newImage = new Image(cardImageSupplier.getResource(cardImage));
      newImage.setStyleName("cardImage");
      images.add(newImage);
    }
    return images;
  }
  
  private void placeCards(HorizontalPanel panel, List<Image> images) {
    panel.clear();
    for (int i = 0; i < images.size(); i++) {
      FlowPanel imageContainer = new FlowPanel();
      if (images.size() == 2 && i == 0) {
        imageContainer.setStyleName("imgShortCardContainer");
      }
      else {
        imageContainer.setStyleName("imgCardContainer");
      }
      imageContainer.add(images.get(i));
      panel.add(imageContainer);
    }
  }
  
  @Override
  public void setPresenter(PokerPresenter pokerPresenter) {
    this.presenter = pokerPresenter;
  }
  
  @Override
  public void doBuyIn() {
    disableButtons();
    
    if (AUTO_BUY_IN) {
      presenter.buyInDone(AUTO_BUY_IN_VALUE);
      return;
    }
    
    new PopupEnterValue("Enter buy-in amount", new PopupEnterValue.ValueEntered() {
      @Override
      public void setValue(int value) {
        presenter.buyInDone(value);
      }
    }).center();
  }

  @Override
  public void setViewerState(int numOfPlayers, int turnIndex, BettingRound round,
      List<Integer> playerBets, List<Pot> pots, List<Integer> playerChips,
      List<Player> playersInHand, List<List<Optional<Card>>> holeCards,
      List<Optional<Card>> board) {
    
    currentBet = 0;
    myChips = 0;
    myCurrentBet = 0;
    
    for (int i = 0; i < numOfPlayers; i++) {
      placeCards(holeCardPanelArr[i], createCardImages(holeCards.get(i)));
      infoPanelArr[i].clear();
      infoPanelArr[i].add(new Label("Chips: " + playerChips.get(i)));
      infoPanelArr[i].add(new Label("Bet: " + playerBets.get(i)));
      if (!playersInHand.contains(Player.values()[i])) {
        holeCardPanelArr[i].setStyleName("foldedHoleCardPanel");
        holeCardPanelArr[i].add(new HTMLPanel("<div class=\"overlay\"></div>"));
      }
      else if (i == turnIndex) {
        holeCardPanelArr[i].setStyleName("currentTurnHoleCardPanel");
      }
      else {
        holeCardPanelArr[i].setStyleName("holeCardPanel");
      }
    }
    placeCards(communityCards, createCardImages(board));
    potInfoPanel.clear();
    for (int i = 0; i < pots.size(); i++) {
      Pot pot = pots.get(i);
      if(pot.getPlayersInPot().isEmpty()) {
        continue;
      }
      potInfoPanel.add(new Label("Pot" + (i + 1) +
          " -- Chips: " + pot.getChips() +
          " | Bet: " + pot.getCurrentPotBet()));
      currentBet += pot.getCurrentPotBet();
    }
    disableButtons();
    
    //TODO: handle remaining state
  }

  
  @Override
  public void setPlayerState(int numOfPlayers, int myIndex, int turnIndex, BettingRound round,
      List<Integer> playerBets, List<Pot> pots, List<Integer> playerChips,
      List<Player> playersInHand, List<List<Optional<Card>>> holeCards,
      List<Optional<Card>> board) {
    currentBet = 0;
    myChips = playerChips.get(myIndex);
    myCurrentBet = playerBets.get(myIndex);
    
    for (int i = 0; i < numOfPlayers; i++) {
      placeCards(holeCardPanelArr[i], createCardImages(holeCards.get(i)));
      infoPanelArr[i].clear();
      infoPanelArr[i].add(new Label("Chips: " + playerChips.get(i)));
      infoPanelArr[i].add(new Label("Bet: " + playerBets.get(i)));
      if (!playersInHand.contains(Player.values()[i])) {
        holeCardPanelArr[i].setStyleName("foldedHoleCardPanel");
        holeCardPanelArr[i].add(new HTMLPanel("<div class=\"overlay\"></div>"));
      }
      else if (i == turnIndex) {
        holeCardPanelArr[i].setStyleName("currentTurnHoleCardPanel");
      }
      else {
        holeCardPanelArr[i].setStyleName("holeCardPanel");
      }
    }
    placeCards(communityCards, createCardImages(board));
    
    potInfoPanel.clear();
    for (int i = 0; i < pots.size(); i++) {
      Pot pot = pots.get(i);
      potInfoPanel.add(new Label("Pot" + (i + 1) +
          " -- Chips: " + pot.getChips() +
          " | Bet: " + pot.getCurrentPotBet()));
      currentBet += pot.getCurrentPotBet();
    }
    disableButtons();
    //TODO: handle remaining state
  }
  
  @Override
  public void setEndGameState(int numOfPlayers, BettingRound round, List<Integer> playerBets,
      List<Pot> pots, List<Integer> playerChips, List<Player> playersInHand,
      List<List<Optional<Card>>> holeCards, List<Optional<Card>> board) {
    
    currentBet = 0;
    myChips = 0;
    myCurrentBet = 0;
    
    for (int i = 0; i < numOfPlayers; i++) {
      placeCards(holeCardPanelArr[i], createCardImages(holeCards.get(i)));
      infoPanelArr[i].clear();
      infoPanelArr[i].add(new Label("Chips: " + playerChips.get(i)));
      if (!playersInHand.contains(Player.values()[i])) {
        holeCardPanelArr[i].setStyleName("foldedHoleCardPanel");
        holeCardPanelArr[i].add(new HTMLPanel("<div class=\"overlay\"></div>"));
      }
    }
    placeCards(communityCards, createCardImages(board));
    
    potInfoPanel.clear();
    for (int i = 0; i < pots.size(); i++) {
      Pot pot = pots.get(i);
      List<Player> winners = pot.getPlayersInPot();
      StringBuilder sb = new StringBuilder();
      for (int j = 0; j < winners.size(); j++) {
        sb.append(winners.get(j));
        if (j == winners.size() - 2) {
          if (winners.size() > 1) {
            sb.append(" and ");
          }
        }
        else if (j < winners.size() - 2) {
          sb.append(", ");
        }
      }
      potInfoPanel.add(new Label("Pot" + (i + 1) +
          " -- Chips: " + pot.getChips() +
          (winners.size() > 0 ? " | Won by: " + sb.toString() : "")));
    }
    disableButtons();
  }

  @UiHandler("btnFold")
  void onClickFoldBtn(ClickEvent e) {
    //disableClicks();
    presenter.moveMade(PokerMove.FOLD, 0);
  }
  
  @UiHandler("btnCheck")
  void onClickCheckBtn(ClickEvent e) {
    //disableClicks();
    if (currentBet - myCurrentBet != 0) {
      Window.alert("Current bet is not 0");
      return;
    }
    presenter.moveMade(PokerMove.CHECK, 0);
  }
  
  @UiHandler("btnCall")
  void onClickCallBtn(ClickEvent e) {
    //disableClicks();
    int callAmount = currentBet - myCurrentBet;
    if (myChips < callAmount) {
      Window.alert("Insufficient chips to Call");
      return;
    }
    if (callAmount == 0) {
      Window.alert("Can't call 0 amount");
      return;
    }
    presenter.moveMade(PokerMove.CALL, callAmount);
  }
  
  @UiHandler("btnBet")
  void onClickBetBtn(ClickEvent e) {
    //disableClicks();
    int amount;
    try {
      amount = Integer.parseInt(txtAmount.getText());
    }
    catch (NumberFormatException ex) {
      Window.alert("Please enter a valid number");
      return;
    }
    
    if (amount > myChips) {
      Window.alert("Insufficient chips");
      return;
    }
    
    if (currentBet == 0) {
      if (amount < PokerLogic.BIG_BLIND) {
        Window.alert("Bet cannot be less than big blind (" + PokerLogic.BIG_BLIND + ")");
        return;
      }
      presenter.moveMade(PokerMove.BET, amount);
    }
    else if (myCurrentBet + amount >= 2 * currentBet) {
      presenter.moveMade(PokerMove.RAISE, amount);
    }
    else {
      Window.alert("Invalid Raise amount");
    }
  }
  
  @UiHandler("btnAllIn")
  void onClickAllInBtn(ClickEvent e) {
    int amount = myChips;
    if (currentBet == 0) {
      presenter.moveMade(PokerMove.BET, amount);
    }
    else if (amount + myCurrentBet <= currentBet) {
      presenter.moveMade(PokerMove.CALL, amount);
    }
    else {
      presenter.moveMade(PokerMove.RAISE, amount);
    }
  }
  
  @Override
  public void makeYourMove() {
    btnFold.setEnabled(true);
    btnCheck.setEnabled(currentBet - myCurrentBet == 0);
    btnCall.setEnabled(myChips >= currentBet - myCurrentBet);
    btnBet.setEnabled(true);
    txtAmount.setEnabled(true);
    btnAllIn.setEnabled(true);
  }
  
  private void disableButtons() {
    btnFold.setEnabled(false);
    btnCheck.setEnabled(false);
    btnCall.setEnabled(false);
    btnBet.setEnabled(false);
    txtAmount.setEnabled(false);
    btnAllIn.setEnabled(false); 
  }  
}
