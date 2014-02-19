package org.poker.client.util;

import java.util.List;

import org.poker.client.Card;
import org.poker.client.PokerHand;

import com.google.common.collect.Lists;

public class BestHandFinder {
  
  private List<Card> cards;
  private List<Card> hand;
  private PokerHand bestHand;
  
  public BestHandFinder(List<Card> board, List<Card> holeCards) {
    if(board == null || board.size() != 5) {
      throw new IllegalArgumentException("board list invalid. Expected: 5; Got: " +
          (board == null? null : board.size()));
    }
    if(holeCards == null || holeCards.size() != 2) {
      throw new IllegalArgumentException("holeCards list invalid. Expected: 2; Got: " +
          (holeCards == null? null : holeCards.size()));
    }
    
    cards = Lists.newArrayList();
    cards.addAll(board);
    cards.addAll(holeCards);
  }
  
  public PokerHand find() {
    if(bestHand == null) {
      hand = Lists.newArrayList();
      testCombinations(0, 0);
    }
    return bestHand;
  }
  
  private void testCombinations(int depth, int num) {
    if(depth == 5) {
      PokerHand pokerHand = new PokerHand(hand);
      if(bestHand == null || pokerHand.betterThan(bestHand)) {
        bestHand = pokerHand;
      }
    }
    if(num >= cards.size()) {
      return;
    }
      
    for(int i=num; i< cards.size(); i++) {
      hand.add(cards.get(i));
      testCombinations(depth + 1, i + 1);
      hand.remove(hand.size() - 1);
    }
  }
}
