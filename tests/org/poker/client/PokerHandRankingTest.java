package org.poker.client;

import java.util.List;

import org.junit.Test;
import org.poker.client.Card.Rank;
import org.poker.client.Card.Suit;
import org.poker.client.util.BestHandFinder;

import com.google.common.collect.ImmutableList;


public class PokerHandRankingTest {
  
  // temporary code to check hand rank comparisons
  //TODO: refactor as proper JUnit tests.
  public static void main(String[] args) {
    List<Card> board = ImmutableList.<Card>of(
        new Card(Suit.CLUBS, Rank.EIGHT),
        new Card(Suit.SPADES, Rank.NINE),
        new Card(Suit.CLUBS, Rank.TEN),
        new Card(Suit.DIAMONDS, Rank.JACK),
        new Card(Suit.CLUBS, Rank.KING));
    
    List<Card> holeCards1 = ImmutableList.<Card>of(
        new Card(Suit.HEARTS, Rank.QUEEN),
        new Card(Suit.CLUBS, Rank.ACE));
    
    BestHandFinder finder = new BestHandFinder(board, holeCards1);
    System.out.println(finder.find());
  }
  
}
