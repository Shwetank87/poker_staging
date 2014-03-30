package org.poker.client;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.game_api.GameApi;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetVisibility;
import org.game_api.GameApi.VerifyMove;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@RunWith(JUnit4.class)
public class PokerLogicInitialMoveTest extends AbstractPokerLogicTestBase {
  
  private List<Operation> getInitialOperations(List<String> playerIds,
      Map<String, Integer> startingChips) {
    return pokerLogic.getInitialMove(playerIds, startingChips);
  }
  
  
  // Tests

  @Test
  public void testInitialBuyInByPlayers() {
    
    Map<String, Integer> startingChips;
    
    // Buy-in by P0
    startingChips = getStartingChips(2000, 0, 0, 0);
    List<Operation> buyInOperaitions = pokerLogic.getInitialBuyInMove(p0_id, 2000, startingChips);
    VerifyMove verifyMove = move(
        p0_id, emptyState, buyInOperaitions, playersInfo_3_players, startingChips);
    assertMoveOk(verifyMove);
    
    // Buy-in by P1
    startingChips = getStartingChips(2000, 4000, 0, 0);
    buyInOperaitions = pokerLogic.getInitialBuyInMove(p1_id, 4000, startingChips);
    verifyMove = move(p1_id, emptyState, buyInOperaitions, playersInfo_3_players, startingChips);
    assertMoveOk(verifyMove);
    
    // Buy-in by P2
    startingChips = getStartingChips(2000, 4000, 1000, 0);
    buyInOperaitions = pokerLogic.getInitialBuyInMove(p2_id, 1000, startingChips);
    verifyMove = move(p2_id, emptyState, buyInOperaitions, playersInfo_3_players, startingChips);
    assertMoveOk(verifyMove);
    
    // Buy-in by P3
    startingChips = getStartingChips(2000, 4000, 1000, 2000);
    buyInOperaitions = pokerLogic.getInitialBuyInMove(p3_id, 2000, startingChips);
    verifyMove = move(p3_id, emptyState, buyInOperaitions, playersInfo_3_players, startingChips);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testInitialOperationSize() {
    List<Operation> initialOperation = getInitialOperations(
        ImmutableList.<String>of(p0_id, p1_id, p2_id),
        ImmutableMap.<String, Integer>of(p0_id, 1000, p1_id, 2000, p2_id, 1000));
    // 1 SetTurn operation 
    // 10 Set operations
    // 1 Shuffle operation
    // 52 Set operations for cards
    // 52 SetVisibility operations for cards
    assertEquals(1 + 12 + 1 + 52 + 52, initialOperation.size());
  }
  
  @Test
  public void testInitialMoveWithTwoPlayers() {
    Map<String, Integer> startingChips = getStartingChips(2000, 2000);
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<String>of(p0_id, p1_id), startingChips);
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_2_players,
        startingChips);
    assertMoveOk(verifyMove);
}
  
  @Test
  public void testInitialMoveWithFourPlayers() {
    Map<String, Integer> startingChips = getStartingChips(2000, 2000, 2000, 2000);
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<String>of(p0_id, p1_id, p2_id, p3_id), startingChips);
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players,
        startingChips);
    assertMoveOk(verifyMove);
  }
  
  @Test
  public void testInitialMoveByWrongPlayer() {
    Map<String, Integer> startingChips = getStartingChips(2000, 2000, 2000, 2000);
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<String>of(p0_id, p1_id, p2_id, p3_id), startingChips);
    VerifyMove verifyMove = move(p1_id, emptyState, initialOperations, playersInfo_4_players,
        startingChips);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveFromNonEmptyState() {
    Map<String, Integer> startingChips = getStartingChips(2000, 2000, 2000, 2000);
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<String>of(p0_id, p1_id, p2_id, p3_id), startingChips);
    VerifyMove verifyMove = move(p0_id, nonEmptyState, initialOperations, playersInfo_4_players,
        startingChips);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveWithExtraOperation() {
    Map<String, Integer> startingChips = getStartingChips(2000, 2000, 2000, 2000);
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<String>of(p0_id, p1_id, p2_id, p3_id), startingChips);
    initialOperations.add(new Set(BOARD, ImmutableList.of()));
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players,
        startingChips);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveWithNoShuffleOperation() {
    Map<String, Integer> startingChips = getStartingChips(2000, 2000, 2000, 2000);
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<String>of(p0_id, p1_id, p2_id, p3_id), startingChips);
    //remove the shuffle operation
    for (Iterator<Operation> it = initialOperations.iterator(); it.hasNext();) {
      if (it.next() instanceof GameApi.Shuffle) {
        it.remove();
      }
    }
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players,
        startingChips);
    assertHacker(verifyMove);
  }
  
  @Test
  public void testInitialMoveWithWrongVisibility() {
    int numOfPlayers = 4;
    Map<String, Integer> startingChips = getStartingChips(2000, 2000, 2000, 2000);
    
    List<Operation> initialOperations = getInitialOperations(
        ImmutableList.<String>of(p0_id, p1_id, p2_id, p3_id), startingChips);
    
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
      initialOperations.add(new SetVisibility(C + i, ImmutableList.<String>of()));
    }
    
    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players,
        startingChips);
    assertHacker(verifyMove);
  }

}
