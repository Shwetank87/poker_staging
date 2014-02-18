package org.poker.client;

import com.google.common.collect.ImmutableList;

public class Pot {
  
  private int chips;
  
  private int currentPotBet;
  
  ImmutableList<Player> playersInPot;

  public Pot(int chips, int currentPotBet, ImmutableList<Player> playersInPot) {
    super();
    this.chips = chips;
    this.currentPotBet = currentPotBet;
    this.playersInPot = playersInPot;
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
  
}
