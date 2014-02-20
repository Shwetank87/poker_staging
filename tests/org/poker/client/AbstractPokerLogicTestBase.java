package org.poker.client;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.poker.client.GameApi.Operation;
import org.poker.client.GameApi.VerifyMove;
import org.poker.client.GameApi.VerifyMoveDone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public abstract class AbstractPokerLogicTestBase {
  
  protected PokerLogic pokerLogic = new PokerLogic();
  
  protected static final String PLAYER_ID = "playerId";
  
  protected static final String[] P = {"P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8"};
  protected static final String C = "C";
  
  protected static final String PREVIOUS_MOVE = "previousMove";
  protected static final String PREVIOUS_MOVE_ALL_IN = "previousMoveAllIn";
  protected static final String NUMBER_OF_PLAYERS = "numberOfPlayers";
  protected static final String WHOSE_MOVE = "whoseMove";
  protected static final String CURRENT_BETTER = "currentBetter";
  protected static final String CURRENT_ROUND = "currentRound";
  protected static final String PLAYERS_IN_HAND = "playersInHand";
  protected static final String BOARD = "board";
  protected static final String HOLE_CARDS = "holeCards";
  protected static final String PLAYER_BETS = "playerBets";
  protected static final String PLAYER_CHIPS = "playerChips";
  protected static final String POTS = "pots";
  protected static final String CHIPS = "chips";
  protected static final String CURRENT_POT_BET = "currentPotBet";
  protected static final String PLAYERS_IN_POT = "playersInPot";
 
  protected final int p0_id = 84;
  protected final int p1_id = 85;
  protected final int p2_id = 86;
  protected final int p3_id = 87;
  
  protected final int[] playerIdArr = {p0_id, p1_id, p2_id, p3_id};
  
  protected final ImmutableMap<String, Object> p0_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p0_id);
  protected final ImmutableMap<String, Object> p1_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p1_id);
  protected final ImmutableMap<String, Object> p2_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p2_id);
  protected final ImmutableMap<String, Object> p3_info =
      ImmutableMap.<String, Object>of(PLAYER_ID, p3_id);
  
  protected final ImmutableList<Map<String, Object>> playersInfo_2_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info);
  protected final ImmutableList<Map<String, Object>> playersInfo_3_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info, p2_info);
  protected final ImmutableList<Map<String, Object>> playersInfo_4_players =
      ImmutableList.<Map<String, Object>>of(p0_info, p1_info, p2_info, p3_info);

  // dummy starting chips as they don't matter after initial move since
  // chips information is already in the state.
  protected final ImmutableMap<Integer, Integer> startingChips_2_player =
      getStartingChips(5000, 5000);
  protected final ImmutableMap<Integer, Integer> startingChips_3_player =
      getStartingChips(5000, 5000, 5000);
  protected final ImmutableMap<Integer, Integer> startingChips_4_player =
      getStartingChips(5000, 5000, 5000, 5000);
  
  // States for different scenarios in tests

  protected final ImmutableMap<String, Object> emptyState =
      ImmutableMap.<String, Object>of();
  protected final ImmutableMap<String, Object> nonEmptyState =
      ImmutableMap.<String, Object>of("K", "V");


  // Utility methods
  
  protected VerifyMove move(int lastMovePlayerId, Map<String, Object> lastState,
      List<Operation> lastMove, List<Map<String, Object>> playersInfo,
      Map<Integer, Integer> playerIdToNumberOfTokensInPot) {
    return new VerifyMove(playersInfo,
        emptyState, // we never need to check the resulting state
        lastState, lastMove, lastMovePlayerId,
        playerIdToNumberOfTokensInPot);
  }
  
  protected ImmutableMap<Integer, Integer> getStartingChips(int... chips) {
    ImmutableMap.Builder<Integer, Integer> builder = ImmutableMap.builder();
    for (int i = 0; i < chips.length; i++) {
      builder.put(playerIdArr[i], chips[i]);
    }
    return builder.build();
  }
    
  // Utility methods copied from CheatLogicTest.java in
  // https://github.com/yoav-zibin/cheat-game

  protected void assertMoveOk(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = pokerLogic.verify(verifyMove);
    assertEquals(0, verifyDone.getHackerPlayerId());
  }

  protected void assertHacker(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = pokerLogic.verify(verifyMove);
    assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
  }
}
