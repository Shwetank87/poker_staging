package org.poker.client;

import com.google.common.base.Objects;
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
  
  @Override
  public boolean equals(Object obj) {
    if(obj == this) return true;
    if(obj == null) return false;
    
    if(obj instanceof Pot) {
      Pot other = (Pot)obj;
      return chips == other.chips &&
          currentPotBet == other.currentPotBet &&
          Objects.equal(playersInPot, other.playersInPot) &&
          Objects.equal(playerBets, other.playerBets);
    }
    return false;
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
