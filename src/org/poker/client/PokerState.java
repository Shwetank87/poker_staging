package org.poker.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class PokerState {
  
  /*
   * Can be between 2 and 9
   */
  private int numberOfPlayers;

  private Player whoseMove;
  private Player currentBetter;
  
  private BettingRound currentRound;

  private ImmutableList<Optional<Card>> cards;

  private ImmutableList<Player> playersInHand;
  
  /*
   * List of hole cards belonging to each player 
   * List of bets made by each player
   * List of chips held by each player
   */
  private ImmutableList<ImmutableList<Optional<Integer>>> holeCards;
  private ImmutableList<Integer> playerChips;
  private ImmutableList<Integer> playerBets;
  
  
  /*
   * 5 cards on the board
   */
  private ImmutableList<Optional<Integer>> board;
  
  private ImmutableList<Pot> pots;

  public PokerState(int numberOfPlayers, Player whoseMove,
      Player currentBetter, BettingRound currentRound,
      ImmutableList<Optional<Card>> cards, ImmutableList<Player> playersInHand,
      ImmutableList<ImmutableList<Optional<Integer>>> holeCards,
      ImmutableList<Integer> playerBets,
      ImmutableList<Integer> playerChips,
      ImmutableList<Optional<Integer>> board, ImmutableList<Pot> pots) {
    super();
    this.numberOfPlayers = numberOfPlayers;
    this.whoseMove = whoseMove;
    this.currentBetter = currentBetter;
    this.currentRound = currentRound;
    this.cards = cards;
    this.playersInHand = playersInHand;
    this.holeCards = holeCards;
    this.playerBets = playerBets;
    this.playerChips = playerChips;
    this.board = board;
    this.pots = pots;
  }
  
  
}
