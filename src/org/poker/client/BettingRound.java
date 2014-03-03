package org.poker.client;


public enum BettingRound {
  
  PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN, END_GAME;
  
  private static final BettingRound[] VALUES = values();
  
  public BettingRound getNextRound() {
    if(this == VALUES[VALUES.length - 1]) {
      return null;
    }
    return VALUES[this.ordinal() + 1];
  }
  
}
