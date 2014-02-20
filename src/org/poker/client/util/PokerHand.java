package org.poker.client.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.poker.client.Card;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class PokerHand {
  
  private Card[] cards;
  private Integer[] rankValues;
  private List<Integer> ranking;
  
  public PokerHand(List<Card> cards) {
    this(cards.toArray(new Card[0]));
  }
  
  public PokerHand(Card[] cards) {
    if(cards == null || cards.length != 5) {
      throw new IllegalArgumentException("Expected: 5 cards. Passed: " + 
          (cards == null? cards : cards.length));
    }
    this.cards = new Card[5];
    this.rankValues = new Integer[5];
    for (int i = 0; i < 5; i++) {
      // Create copies so this object is immutable
      this.cards[i] = new Card(cards[i].getSuit(), cards[i].getRank());
      this.rankValues[i] = cards[i].getRank().ordinal() + 2;
    }
    sortRankValues();
  }
  
  private void sortRankValues() {
    Arrays.sort(rankValues, Collections.reverseOrder());
    if(rankValues.equals(new Integer[]{14, 5, 4, 3, 2})) {
      rankValues = new Integer[]{5, 4, 3, 2, 1};
    }
  }
  
  public int compareRanking(PokerHand other) {
    List<Integer> otherHandRanking = other.getRanking();
    List<Integer> thisHandRanking = this.getRanking();
    
    for(int i=0; i<thisHandRanking.size() && i<otherHandRanking.size(); i++) {
      if(thisHandRanking.get(i) < otherHandRanking.get(i)) {
        return -1;
      }
      else if(thisHandRanking.get(i) > otherHandRanking.get(i)) {
        return 1;
      }
    }
    return 0;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for(int i=0; i<5; i++) {
      Card card = cards[i];
      sb.append(card.getRank().getFirstLetter()).append(card.getSuit().getFirstLetterLowerCase());
      sb.append(i == 4 ? "" : ", ");
    }
    sb.append("]");
    return sb.toString();
  }
  
  /**
   * Lazily calculates the integer list representing the standing of
   * this poker hand.<P>
   * Starting number:
   * <ul>
   * <li>8 - Straight Flush</li>
   * <li>7 - Four of a kind</li>
   * <li>6 - Full house</li>
   * <li>5 - Flush</li>
   * <li>4 - Straight</li>
   * <li>3 - Three of a kind</li>
   * <li>2 - Two Pair</li>
   * <li>1 - Pair</li>
   * <li>0 - High Card</li>
   * </ul>
   * 
   * @return
   */
  public List<Integer> getRanking() {
    
    if(ranking != null) {
      return ranking;
    }
    ranking = Lists.newArrayList();
    
    if(straight() && flush()) {
      ranking = ImmutableList.<Integer>of(8, rankValues[0]);
    }
    else if(kind(4) != -1) {
      ranking = ImmutableList.<Integer>of(7, kind(4), kind(1));
    }
    else if(kind(3) != -1 && kind(2) != -1) {
      ranking = ImmutableList.<Integer>of(6, kind(3), kind(2));
    }
    else if(flush()) {
      ranking = ImmutableList.<Integer>builder().
          add(5).
          addAll(Arrays.asList(rankValues)).
          build();
    }
    else if(straight()) {
      ranking = ImmutableList.<Integer>of(4, rankValues[0]);
    }
    else if(kind(3) != -1) {
      ranking = ImmutableList.<Integer>builder().
          add(3).
          add(kind(3)).
          addAll(Arrays.asList(rankValues)).
          build();
    }
    else if(twoPair() != null) {
      ranking = ImmutableList.<Integer>builder().
          add(2).
          addAll(Arrays.asList(twoPair())).
          addAll(Arrays.asList(rankValues)).
          build();
    }
    else if(kind(2) != -1){
      ranking = ImmutableList.<Integer>builder().
          add(1).
          add(kind(2)).
          addAll(Arrays.asList(rankValues)).
          build();
    }
    else {
      ranking = ImmutableList.<Integer>builder().
          add(0).
          addAll(Arrays.asList(rankValues)).
          build();
    }
    return ranking;
  }
  
  private boolean flush() {
    for (int i = 0; i < 4; i++) {
      if(cards[i].getSuit() != cards[i + 1].getSuit()) {
        return false;
      }
    }
    return true;
  }
  
  private boolean straight() {
    return rankValues[0] - rankValues[4] == 4;
  }
  
  private int kind(int n) {
    return kind(n, rankValues);
  }
  
  private int kind(int n, Integer[] rankValues) {
    for (int i = 0; i < 5; i++) {
      int count = 1;
      for (int j = i + 1; j < 5; j++) {
        if(rankValues[j] == rankValues[i]) {
          count++;
        }
      }
      if(count == n) {
        return rankValues[i];
      }
    }
    return -1;
  }
  
  private Integer[] twoPair() {
    int pair = kind(2);
    int lowPair = kind(2,
        new Integer[]{rankValues[4], rankValues[3], rankValues[2], rankValues[1], rankValues[0]});
    if(pair != -1 && lowPair != pair) {
      return new Integer[]{pair, lowPair};
    }
    else {
      return null;
    }
  }
  
}
