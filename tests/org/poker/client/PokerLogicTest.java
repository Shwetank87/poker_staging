package org.poker.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.VerifyMove;
import org.poker.client.GameApi.VerifyMoveDone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@RunWith(JUnit4.class)
public class PokerLogicTest {
  
  PokerLogic pokerLogic = new PokerLogic();
  
  private static final String PLAYER_ID = "playerId";
  
  private final int p0_id = 84;
  private final int p1_id = 85;
  private final int p2_id = 86;
  private final int p3_id = 87;
  
  private final ImmutableMap<String, Object> p0_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p0_id);
  private final ImmutableMap<String, Object> p1_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p1_id);
  private final ImmutableMap<String, Object> p2_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p2_id);
  private final ImmutableMap<String, Object> p3_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p3_id);
  
  private final ImmutableList<Map<String, Object>> playersInfo_2_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info);
  private final ImmutableList<Map<String, Object>> playersInfo_3_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info, p2_info);
  private final ImmutableList<Map<String, Object>> playersInfo_4_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info, p2_info, p3_info);

  private final ImmutableMap<String, Object> emptyState = ImmutableMap.<String, Object>of();
  
  
  private VerifyMove move(int lastMovePlayerId, Map<String, Object> lastState,
      List<Operation> lastMove, List<Map<String, Object>> playersInfo) {
    return new VerifyMove(p0_id, playersInfo,
    // in poker we never need to check the resulting state (the server makes it,
    // and the game doesn't have any hidden decisions such in Battleships)
        emptyState, lastState, lastMove, lastMovePlayerId);
  }

  private List<Operation> getInitialOperations(int[] playerIds, int[] startingChips) {
    return pokerLogic.getInitialMove(playerIds, startingChips);
  }

  @Test
  public void testInitialMoveTwoPlayers() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id},
        new int[]{2000, 2000});

    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_2_players);
    VerifyMoveDone verifyMoveDone = pokerLogic.verify(verifyMove);
    assertEquals(0, verifyMoveDone.getHackerPlayerId());
  }
  
  @Test
  public void testInitialMoveFourPlayers() {
    List<Operation> initialOperations = getInitialOperations(
        new int[]{p0_id, p1_id, p2_id, p3_id},
        new int[]{2000, 2000, 2000, 2000});

    VerifyMove verifyMove = move(p0_id, emptyState, initialOperations, playersInfo_4_players);
    VerifyMoveDone verifyMoveDone = pokerLogic.verify(verifyMove);
    assertEquals(0, verifyMoveDone.getHackerPlayerId());
  }

}
