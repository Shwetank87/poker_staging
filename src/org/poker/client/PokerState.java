package org.poker.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class PokerState {
  
  /**
   * Type of move made by last player
   */
  private PokerMove previousMove;
  
  private boolean previousMoveAllIn;

  /**
   * Number of players in the game.
   * Can be between 2 and 9
   */
  private int numberOfPlayers;

  private Player whoseMove;
  
  /**
   * The player to make the last bet.<P>
   * At beginning of PreFlop, its the big blind and
   * in case of other rounds, its first player to act.
   */
  private Player currentBetter;

  private BettingRound currentRound;

  private ImmutableList<Optional<Card>> cards;

  private ImmutableList<Player> playersInHand;
  
  /**
   * List of hole cards belonging to each player List of bets made by each
   * player List of chips held by each player
   */
  private ImmutableList<ImmutableList<Optional<Integer>>> holeCards;

  /**
   * 5 community cards
   */
  private ImmutableList<Optional<Integer>> board;

  private ImmutableList<Integer> playerBets;
  
  private ImmutableList<Integer> playerChips;
  
  private ImmutableList<Pot> pots;

  
  public PokerState(PokerMove previousMove, boolean previousMoveAllIn, int numberOfPlayers, Player whoseMove,
      Player currentBetter, BettingRound currentRound,
      ImmutableList<Optional<Card>> cards,
      ImmutableList<Optional<Integer>> board,
      ImmutableList<Player> playersInHand,
      ImmutableList<ImmutableList<Optional<Integer>>> holeCards,
      ImmutableList<Integer> playerBets, ImmutableList<Integer> playerChips,
      ImmutableList<Pot> pots) {
    super();
    this.previousMove = previousMove;
    this.previousMoveAllIn = previousMoveAllIn;
    this.numberOfPlayers = numberOfPlayers;
    this.whoseMove = whoseMove;
    this.currentBetter = currentBetter;
    this.currentRound = currentRound;
    this.cards = cards;
    this.playersInHand = playersInHand;
    this.holeCards = holeCards;
    this.board = board;
    this.playerBets = playerBets;
    this.playerChips = playerChips;
    this.pots = pots;
  }
  
  public PokerMove getPreviousMove() {
    return previousMove;
  }

  public boolean isPreviousMoveAllIn() {
    return previousMoveAllIn;
  }

  public int getNumberOfPlayers() {
    return numberOfPlayers;
  }


  public Player getWhoseMove() {
    return whoseMove;
  }


  public Player getCurrentBetter() {
    return currentBetter;
  }


  public BettingRound getCurrentRound() {
    return currentRound;
  }


  public ImmutableList<Optional<Card>> getCards() {
    return cards;
  }


  public ImmutableList<Player> getPlayersInHand() {
    return playersInHand;
  }
  

  public ImmutableList<ImmutableList<Optional<Integer>>> getHoleCards() {
    return holeCards;
  }


  public ImmutableList<Optional<Integer>> getBoard() {
    return board;
  }


  public ImmutableList<Integer> getPlayerBets() {
    return playerBets;
  }


  public ImmutableList<Integer> getPlayerChips() {
    return playerChips;
  }


  public ImmutableList<Pot> getPots() {
    return pots;
  }

}
