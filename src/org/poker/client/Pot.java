package org.poker.client;

import com.google.common.collect.ImmutableList;

public class Pot {
  
  private int chips;
  
  private int currentPotBet;
  
  private ImmutableList<Player> playersInPot;
  
  private ImmutableList<Integer> playerBets;

  public Pot(int chips, int currentPotBet, ImmutableList<Player> playersInPot,
      ImmutableList<Integer> playerBets) {
    super();
    this.chips = chips;
    this.currentPotBet = currentPotBet;
    this.playersInPot = playersInPot;
    this.playerBets = playerBets;
  }

  public int getChips() {
    return chips;
  }

  public int getCurrentPotBet() {
    return currentPotBet;
  }

  public ImmutableList<Player> getPlayersInPot() {
    return playersInPot;
  }
  
  public ImmutableList<Integer> getPlayerBets() {
    return playerBets;
  }
  
}
