package org.poker.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.poker.client.Card.Rank;
import org.poker.client.Card.Suit;
import org.poker.client.util.BestHandFinder;
import org.poker.client.util.PokerHand;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class PokerLogicHelper {

  private static final String PREVIOUS_MOVE = "previousMove";
  private static final String PREVIOUS_MOVE_ALL_IN = "previousMoveAllIn";
  private static final String NUMBER_OF_PLAYERS = "numberOfPlayers";
  private static final String WHOSE_MOVE = "whoseMove";
  private static final String CURRENT_BETTER = "currentBetter";
  private static final String CURRENT_ROUND = "currentRound";
  private static final String PLAYERS_IN_HAND = "playersInHand";
  private static final String BOARD = "board";
  private static final String HOLE_CARDS = "holeCards";
  private static final String PLAYER_BETS = "playerBets";
  private static final String PLAYER_CHIPS = "playerChips";
  private static final String POTS = "pots";
  private static final String CHIPS = "chips";
  private static final String CURRENT_POT_BET = "currentPotBet";
  private static final String PLAYERS_IN_POT = "playersInPot";

  private static PokerLogicHelper instance;
  
  private PokerLogicHelper() {
    super();
  }
  
  public static PokerLogicHelper getInstance() {
    if(instance == null) {
      instance = new PokerLogicHelper();
    }
    return instance;
  }
  
  /**
   * Converts JSON state object into PokerState POJO
   * 
   * @param gameApiState
   * @return
   */
  public PokerState gameApiStateToPokerState(Map<String, Object> gameApiState) {

    PokerMove previousMove = PokerMove.valueOf((String)gameApiState.get(PREVIOUS_MOVE));
    boolean previousMoveAllIn = (boolean)gameApiState.get(PREVIOUS_MOVE_ALL_IN);
    int numberOfPlayers = (int)gameApiState.get(NUMBER_OF_PLAYERS);
    Player whoseMove = Player.valueOf((String)gameApiState.get(WHOSE_MOVE));
    Player currentBetter = Player.valueOf((String)gameApiState.get(CURRENT_BETTER));
    BettingRound currentRound = BettingRound.valueOf((String)gameApiState.get(CURRENT_ROUND));

    // Get Cards
    ArrayList<Optional<Card>> cardList = new ArrayList();
    for (int i =0 ; i<52 ; i++) {
      Card card;
      String crd = (String)gameApiState.get("C"+i);
      if (crd != null) {
        Rank rank = Rank.fromFirstLetter(crd.substring(0, crd.length() - 1));
        Suit suit = Suit.fromFirstLetterLowerCase(crd.substring(crd.length() - 1));
        card = new Card(suit, rank);
      }
      else {
        card = null;
      }
      cardList.add(Optional.fromNullable(card));
    }
    ImmutableList<Optional<Card>> cards = ImmutableList.copyOf(cardList);

    // Get Board
    List<Integer> boardElements = (List<Integer>) gameApiState.get(BOARD);
    List<Optional<Integer>> boardElementsOptional = Lists.newArrayList();
    for(Integer boardElement : boardElements) {
      boardElementsOptional.add(Optional.fromNullable(boardElement));
    }
    ImmutableList<Optional<Integer>> board = ImmutableList.copyOf(boardElementsOptional);

    // Get Players in Hand
    List<String> playerInHandList = (List<String>) gameApiState.get(PLAYERS_IN_HAND);
    List<Player> temp = new ArrayList<Player>();
    for (String s : playerInHandList){
      temp.add(Player.valueOf(s));
    }
    ImmutableList<Player> playersInHand = ImmutableList.copyOf(temp);

    // Get holeCards
    List<List<Integer>> holeCardList = (List<List<Integer>>)gameApiState.get(HOLE_CARDS);
    ImmutableList.Builder<ImmutableList<Optional<Integer>>> holeCardListBuilder = 
        ImmutableList.builder();
    for(List<Integer> holeCards : holeCardList) {
      ImmutableList.Builder<Optional<Integer>> holeCardsOptionalBuilder =
          ImmutableList.builder();
      for(Integer holeCard : holeCards) {
        holeCardsOptionalBuilder.add(Optional.fromNullable(holeCard));
      }
      holeCardListBuilder.add(holeCardsOptionalBuilder.build());
    }
    ImmutableList<ImmutableList<Optional<Integer>>> holeCards = holeCardListBuilder.build();

    // Get playerBets
    List<Integer> bets = (List<Integer>)gameApiState.get(PLAYER_BETS);
    ImmutableList<Integer> playerBets = ImmutableList.copyOf(bets);

    // Get playerChips
    List<Integer> chips = (List<Integer>)gameApiState.get(PLAYER_CHIPS);
    ImmutableList<Integer> playerChips = ImmutableList.copyOf(chips);

    // Get pots
    List<Map<String,Object>> gamePots = (List<Map<String,Object>>)gameApiState.get(POTS);
    List<Pot> potlist = new ArrayList<Pot>();
    for(Map<String,Object> pot : gamePots) {
      potlist.add(new Pot(
          (int)pot.get(CHIPS),
          (int)pot.get(CURRENT_POT_BET),
          ImmutableList.copyOf(getPlayerListFromApi((List<String>)pot.get(PLAYERS_IN_POT))),
          ImmutableList.copyOf((List<Integer>) pot.get(PLAYER_BETS))));
    }

    ImmutableList<Pot> pots = ImmutableList.copyOf(potlist);

    return new PokerState(previousMove, 
        previousMoveAllIn, numberOfPlayers, 
        whoseMove, currentBetter, currentRound, 
        cards, board, playersInHand, holeCards, 
        playerBets, playerChips, pots);
  }

  /**
   * Get JSON players list from POJO player list
   * 
   * @param players
   * @return
   */
  public List<String> getApiPlayerList(List<Player> players) {
    ImmutableList.Builder<String> playerListBuilder = ImmutableList.builder();
    for(Player player : players) {
      playerListBuilder.add(player.name());
    }
    return playerListBuilder.build();
  }

  public List<Player> getPlayerListFromApi(List<String> players) {
    ImmutableList.Builder<Player> playerListBuilder = ImmutableList.builder();
    for(String player : players) {
      playerListBuilder.add(Player.valueOf(player));
    }
    return playerListBuilder.build();
  }

  /**
   * Returns the list of player IDs of players winning each pot.
   * 
   * @param lastState
   * @param playerIds
   * @return
   */
  public List<List<Integer>> getWinners(PokerState lastState, List<Integer> playerIds) {
    
    List<Player> playersInHand = lastState.getPlayersInHand();
    List<PokerHand> bestHands = Lists.newArrayList();
    // Get best poker hands for each player in pot
    for (int i = 0; i < playerIds.size(); i++) {
      if(playersInHand.contains(Player.values()[i])) {
        List<Card> board = Lists.newArrayList();
        List<Card> holeCards = Lists.newArrayList();
        for(Optional<Integer> boardCard : lastState.getBoard()) {
          board.add(lastState.getCards().get(boardCard.get()).get());
        }
        for(Optional<Integer> holeCard : lastState.getHoleCards().get(i)) {
          holeCards.add(lastState.getCards().get(holeCard.get()).get());
        }
        bestHands.add(new BestHandFinder(board, holeCards).find());
      }
      else {
        //We don't care about players that folded
        bestHands.add(null);
      }
    }
    // For each pot, find list of players that had the best hand
    // Add this list to the return list
    List<List<Integer>> winners = Lists.newArrayList();
    List<Pot> pots = lastState.getPots();
    for(Pot pot : pots) {
      List<Player> playersInPot = pot.getPlayersInPot();
      List<Integer> potWinners = Lists.newArrayList();
      PokerHand bestHandInPot = null;
      for(Player player : playersInPot) {
        PokerHand currentHand = bestHands.get(player.ordinal());
        if(bestHandInPot == null) {
          bestHandInPot = currentHand;
        }
        int comparison = currentHand.compareRanking(bestHandInPot);
        if(comparison > 0) {
          //found better hand
          bestHandInPot = currentHand;
          potWinners = Lists.newArrayList();
          potWinners.add(playerIds.get(player.ordinal()));
        }
        else if(comparison == 0) {
          //found equally good hand
          potWinners.add(playerIds.get(player.ordinal()));
        }
        else {
          //found worse hand. ignore it.
        }
      }
      winners.add(potWinners);
    }
    return winners;
  }
  
}
