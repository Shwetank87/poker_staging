package org.poker.client;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.Set;
import org.poker.client.GameApi.SetVisibility;
import org.poker.client.GameApi.VerifyMove;
import org.poker.client.GameApi.VerifyMoveDone;

import com.google.common.collect.ImmutableList;

public class PokerLogicInitialMoveTest extends AbstractPokerLogicTestBase {
  
  private List<Operation> getInitialOperations(int[] playerIds, int[] startingChips) {
    return pokerLogic.getInitialMove(playerIds, startingChips);
  }
  
  
  // Tests

  @Test
  public void testInitialOperationSize() {
    List<Operation> initialOperation = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id},
        new int[]{1000, 2000, 1000});
    // 10 Set operations
    // 1 Shuffle operation
    // 52 Set operations for cards
    // 52 SetVisibility operations for cards
    assertEquals(10 + 1 + 52 + 52, initialOperation.size());
  }
  
  @Test
  public void testInitialMoveWithTwoPlayers() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id},
        new int[]{2000, 2000});
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_2_players);
    VerifyMoveDone verifyMoveDone = pokerLogic.verify(verifyMove);
    assertEquals(0, verifyMoveDone.getHackerPlayerId());
  }
  
  @Test
  public void testInitialMoveWithFourPlayers() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id, p3_id},
        new int[]{2000, 2000, 2000, 2000});
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testInitialMoveByWrongPlayer() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id, p3_id},
        new int[]{2000, 2000, 2000, 2000});
    VerifyMove verifyMove = move(p1_id, emptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveFromNonEmptyState() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id, p3_id},
        new int[]{2000, 2000, 2000, 2000});
    VerifyMove verifyMove = move(p0_id, nonEmptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveWithExtraOperation() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id, p3_id},
        new int[]{2000, 2000, 2000, 2000});
    initialOperations.add(new Set(BOARD, ImmutableList.of()));
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveWithNoShuffleOperation() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id, p3_id},
        new int[]{2000, 2000, 2000, 2000});
    //remove the shuffle operation
    for (Iterator<Operation> it = initialOperations.iterator(); it.hasNext();)
      if (it.next() instanceof GameApi.Shuffle)
        it.remove();
    
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveWithWrongVisibility() {
    int numOfPlayers = 4;
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id, p3_id},
        new int[]{2000, 2000, 2000, 2000});
    
    //remove the setVisibility operations
    for (Iterator<Operation> it = initialOperations.iterator(); it.hasNext();)
      if (it.next() instanceof SetVisibility)
        it.remove();
    
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
