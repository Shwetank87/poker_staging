package org.poker.client;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.Set;
import org.poker.client.GameApi.SetVisibility;
import org.poker.client.GameApi.VerifyMove;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class PokerLogicInitialMoveTest extends AbstractPokerLogicTestBase {
  
  private List<Operation> getInitialOperations(List<Integer> playerIds,
      Map<Integer, Integer> startingChips) {
    return pokerLogic.getInitialMove(playerIds, startingChips);
  }
  
  // Tests

  @Test
  public void testInitialBuyInByPlayers() {
    // Buy-in by P0
    List<Operation> buyInOperaitions = pokerLogic.getInitialBuyInMove(p0_id, 2000);
    VerifyMove verifyMove = move(p0_id, emptyState, buyInOperaitions, playersInfo_3_players);
    assertMoveOk(verifyMove);
    
    // Buy-in by P1
    buyInOperaitions = pokerLogic.getInitialBuyInMove(p0_id, 4000);
    verifyMove = move(p0_id, emptyState, buyInOperaitions, playersInfo_3_players);
    assertMoveOk(verifyMove);
    
    // Buy-in by P2
    buyInOperaitions = pokerLogic.getInitialBuyInMove(p0_id, 1000);
    verifyMove = move(p0_id, emptyState, buyInOperaitions, playersInfo_3_players);
    assertMoveOk(verifyMove);
    
    // Buy-in by P3
    buyInOperaitions = pokerLogic.getInitialBuyInMove(p0_id, 2000);
    verifyMove = move(p0_id, emptyState, buyInOperaitions, playersInfo_3_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testInitialOperationSize() {
    List<Operation> initialOperation = getInitialOperations(
        ImmutableList.<Integer>of(p0_id, p1_id, p2_id),
        ImmutableMap.<Integer, Integer>of(p0_id, 1000, p1_id, 2000, p2_id, 1000));
    // 1 SetTurn operation 
    // 10 Set operations
    // 1 Shuffle operation
    // 52 Set operations for cards
    // 52 SetVisibility operations for cards
    assertEquals(1 + 10 + 1 + 52 + 52, initialOperation.size());
  }
  
  @Test
  public void testInitialMoveWithTwoPlayers() {
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<Integer>of(p0_id, p1_id),
        ImmutableMap.<Integer, Integer>of(p0_id, 2000, p1_id, 2000));
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_2_players);
    assertMoveOk(verifyMove);
}
  
  @Test
  public void testInitialMoveWithFourPlayers() {
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<Integer>of(p0_id, p1_id, p2_id, p3_id),
        ImmutableMap.<Integer, Integer>of(p0_id, 2000, p1_id, 2000, p2_id, 2000, p3_id, 2000));
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testInitialMoveByWrongPlayer() {
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<Integer>of(p0_id, p1_id, p2_id, p3_id),
        ImmutableMap.<Integer, Integer>of(p0_id, 2000, p1_id, 2000, p2_id, 2000, p3_id, 2000));
    VerifyMove verifyMove = move(p1_id, emptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveFromNonEmptyState() {
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<Integer>of(p0_id, p1_id, p2_id, p3_id),
        ImmutableMap.<Integer, Integer>of(p0_id, 2000, p1_id, 2000, p2_id, 2000, p3_id, 2000));
    VerifyMove verifyMove = move(p0_id, nonEmptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveWithExtraOperation() {
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<Integer>of(p0_id, p1_id, p2_id, p3_id),
        ImmutableMap.<Integer, Integer>of(p0_id, 2000, p1_id, 2000, p2_id, 2000, p3_id, 2000));
    initialOperations.add(new Set(BOARD, ImmutableList.of()));
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveWithNoShuffleOperation() {
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<Integer>of(p0_id, p1_id, p2_id, p3_id),
        ImmutableMap.<Integer, Integer>of(p0_id, 2000, p1_id, 2000, p2_id, 2000, p3_id, 2000));
    //remove the shuffle operation
    for (Iterator<Operation> it = initialOperations.iterator(); it.hasNext();) {
      if (it.next() instanceof GameApi.Shuffle) {
        it.remove();
      }
    }
    
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveWithWrongVisibility() {
    int numOfPlayers = 4;
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<Integer>of(p0_id, p1_id, p2_id, p3_id),
        ImmutableMap.<Integer, Integer>of(p0_id, 2000, p1_id, 2000, p2_id, 2000, p3_id, 2000));
    
    //remove the setVisibility operations
    for (Iterator<Operation> it = initialOperations.iterator(); it.hasNext();) {
      if (it.next() instanceof SetVisibility) {
        it.remove();
      }
    }
    
    initialOperations.add(new SetVisibility(C + 0, ImmutableList.of(p0_id)));
    initialOperations.add(new SetVisibility(C + 1, ImmutableList.of(p0_id)));
    //set visibility of other opponent cards to ALL
    for (int i = 1; i < numOfPlayers; i++) {
      initialOperations.add(new SetVisibility(C + (i * 2)));
      initialOperations.add(new SetVisibility(C + (i * 2 + 1)));
    }
    // Make remaining cards not visible to anyone
    for (int i = 2 * numOfPlayers; i < 52; i++) {
      initialOperations.add(new SetVisibility(C + i, ImmutableList.<Integer>of()));
    }
    
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }

}
