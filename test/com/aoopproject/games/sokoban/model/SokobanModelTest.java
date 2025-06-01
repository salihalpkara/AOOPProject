package com.aoopproject.games.sokoban.model;

import com.aoopproject.common.action.UndoAction;
import com.aoopproject.common.model.DifficultyLevel;
import com.aoopproject.framework.core.GameAction;
import com.aoopproject.framework.core.GameStatus;
import com.aoopproject.framework.core.Grid;
import com.aoopproject.games.sokoban.action.Direction;
import com.aoopproject.games.sokoban.action.SokobanMoveAction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link SokobanModel} class.
 * These tests verify the core game logic for Sokoban, including level initialization,
 * player movement, box pushing mechanics (valid and invalid pushes),
 * wall collisions, win condition checking, and undo functionality.
 * Each test method often sets up its own specific level configuration for isolated testing.
 */
class SokobanModelTest {

    private SokobanModel model;
    private static final String[] TEST_LEVEL_SIMPLE = {
            "WWWWW",
            "W P W",
            "W B.W",
            "WWWWW"
    };
    private static final String[] TEST_LEVEL_ONE_BOX_TO_WIN = {
            "WWWW",
            "WPBW",
            "W.WW",
            "WWWW"
    };

    /**
     * Sets up a default {@link SokobanModel} instance before each test.
     * Tests needing specific level configurations will re-initialize the model.
     */
    @BeforeEach
    void setUp() {
        model = new SokobanModel(TEST_LEVEL_SIMPLE, DifficultyLevel.EASY);
        model.initializeGame();
    }

    /**
     * Tests correct initialization from {@code TEST_LEVEL_SIMPLE} data.
     * Verifies board dimensions, initial player position, counts of targets and boxes on targets,
     * initial score (move count), game status, and that undo is not initially possible.
     */
    @Test
    void testLevelInitialization_SimpleLevel() {
        assertNotNull(model.getGameBoard(), "Game board should not be null after initialization.");
        Grid<SokobanTile> board = (Grid<SokobanTile>) model.getGameBoard();

        assertEquals(4, board.getRows(), "Board should have 4 rows for TEST_LEVEL_SIMPLE.");
        assertEquals(5, board.getColumns(), "Board should have 5 columns for TEST_LEVEL_SIMPLE.");

        assertEquals(1, model.getPlayerRow(), "Player initial row should be 1.");
        assertEquals(2, model.getPlayerCol(), "Player initial column should be 2.");

        assertEquals(SokobanOccupant.PLAYER, board.getEntity(1,2).getOccupant(), "Player occupant check.");
        assertEquals(SokobanOccupant.BOX, board.getEntity(2,2).getOccupant(), "Box occupant check.");
        assertEquals(SokobanBaseType.TARGET, board.getEntity(2,3).getBaseType(), "Target base type check.");

        assertEquals(1, model.getTotalTargets(), "Total targets should be 1 for TEST_LEVEL_SIMPLE.");
        assertEquals(0, model.getBoxesOnTargets(), "Initially 0 boxes should be on targets for TEST_LEVEL_SIMPLE.");

        assertEquals(0, model.getScore(), "Initial score (moves) should be 0.");
        assertEquals(GameStatus.PLAYING, model.getCurrentStatus(), "Initial game status should be PLAYING.");
        assertFalse(model.canUndo(), "Undo should not be possible at the start of a new game.");
    }

    /**
     * Tests player movement into an empty floor space.
     * Verifies correct update of player position, tile occupants, score, and undo availability.
     * Uses TEST_LEVEL_SIMPLE where player at (1,2) can move left to (1,1).
     */
    @Test
    void testPlayerMoveToEmptyFloor() {
        model.processGameSpecificAction(new SokobanMoveAction(Direction.LEFT));

        assertEquals(1, model.getPlayerRow(), "Player row should be 1 after moving left.");
        assertEquals(1, model.getPlayerCol(), "Player column should be 1 after moving left.");
        assertEquals(1, model.getScore(), "Score (move count) should be 1.");
        assertTrue(model.canUndo(), "Undo should be possible after a move.");

        Grid<SokobanTile> board = (Grid<SokobanTile>) model.getGameBoard();
        assertEquals(SokobanOccupant.NONE, board.getEntity(1,2).getOccupant(), "Original player position (1,2) should now be empty.");
        assertEquals(SokobanOccupant.PLAYER, board.getEntity(1,1).getOccupant(), "New player position (1,1) should now have the player.");
    }

    /**
     * Tests that the player cannot move into a wall.
     * Verifies that player position, score, and board state remain unchanged.
     * Uses TEST_LEVEL_SIMPLE where player at (1,2) attempts to move UP into wall at (0,2).
     */
    @Test
    void testPlayerCannotMoveIntoWall() {
        int initialPlayerRow = model.getPlayerRow();
        int initialPlayerCol = model.getPlayerCol();
        int initialScore = model.getScore();

        model.processGameSpecificAction(new SokobanMoveAction(Direction.UP));

        assertEquals(initialPlayerRow, model.getPlayerRow(), "Player row should not change when moving into a wall.");
        assertEquals(initialPlayerCol, model.getPlayerCol(), "Player column should not change when moving into a wall.");
        assertEquals(initialScore, model.getScore(), "Score should not change for an invalid move into a wall.");
        assertFalse(model.canUndo(), "Undo should not become available for an invalid move.");
    }

    /**
     * Tests pushing a box into an empty floor space (not a target).
     * Verifies player and box positions, tile occupants, score, undo state,
     * and that boxesOnTargets count does not change.
     */
    @Test
    void testBoxPushToEmptyFloor_Valid() {
        String[] level = {"WWWWW", "WPB W", "WWWWW"};
        model = new SokobanModel(level, DifficultyLevel.EASY);
        model.initializeGame();

        model.processGameSpecificAction(new SokobanMoveAction(Direction.RIGHT));

        assertEquals(1, model.getPlayerRow(), "Player row should be 1 (at box's old spot).");
        assertEquals(2, model.getPlayerCol(), "Player col should be 2 (at box's old spot).");
        assertEquals(1, model.getScore(), "Score should be 1.");
        assertTrue(model.canUndo(), "Undo should be possible.");

        Grid<SokobanTile> board = (Grid<SokobanTile>) model.getGameBoard();
        assertEquals(SokobanOccupant.PLAYER, board.getEntity(1,2).getOccupant(), "Player should be at (1,2).");
        assertEquals(SokobanOccupant.BOX, board.getEntity(1,3).getOccupant(), "Box should be at (1,3).");
        assertEquals(0, model.getBoxesOnTargets(), "boxesOnTargets should be 0 (box moved to floor).");
    }

    /**
     * Tests that a player cannot push a box into a wall.
     * This uses TEST_LEVEL_SIMPLE where player at (1,2) tries to push box at (2,2) DOWN into wall at (3,2).
     */
    @Test
    void testCannotPushBoxIntoWall_fromSimpleLevel() {
        int initialPlayerRow = model.getPlayerRow();
        int initialPlayerCol = model.getPlayerCol();
        int initialScore = model.getScore();
        int initialBoxesOnTargets = model.getBoxesOnTargets();
        Grid<SokobanTile> board = (Grid<SokobanTile>) model.getGameBoard();
        SokobanOccupant boxInitialOccupant = board.getEntity(2,2).getOccupant();

        model.processGameSpecificAction(new SokobanMoveAction(Direction.DOWN));

        assertEquals(initialPlayerRow, model.getPlayerRow(), "Player row should not change when push is blocked by wall.");
        assertEquals(initialPlayerCol, model.getPlayerCol(), "Player column should not change.");
        assertEquals(initialScore, model.getScore(), "Score should not change.");
        assertEquals(initialBoxesOnTargets, model.getBoxesOnTargets(), "Boxes on targets should not change.");
        assertEquals(boxInitialOccupant, board.getEntity(2,2).getOccupant(), "Box at (2,2) should remain a box.");
        assertFalse(model.canUndo(), "Undo should not be available for an invalid push.");
    }

    /**
     * Tests that a box cannot be pushed into another box.
     * Verifies player and box positions remain unchanged.
     */
    @Test
    void testCannotPushBoxIntoAnotherBox() {
        String[] levelPushToBox = {
                "WWWWWW",
                "WPBB W",
                "WWWWWW"
        };
        model = new SokobanModel(levelPushToBox, DifficultyLevel.EASY);
        model.initializeGame();

        int initialPlayerRow = model.getPlayerRow();
        int initialPlayerCol = model.getPlayerCol();

        model.processGameSpecificAction(new SokobanMoveAction(Direction.RIGHT));

        assertEquals(initialPlayerRow, model.getPlayerRow(), "Player row should not change if box push is blocked by another box.");
        assertEquals(initialPlayerCol, model.getPlayerCol(), "Player column should not change.");

        Grid<SokobanTile> finalBoard = (Grid<SokobanTile>) model.getGameBoard();
        assertEquals(SokobanOccupant.PLAYER, finalBoard.getEntity(1,1).getOccupant(), "Player should remain at (1,1).");
        assertEquals(SokobanOccupant.BOX, finalBoard.getEntity(1,2).getOccupant(), "First box should remain at (1,2).");
        assertEquals(SokobanOccupant.BOX, finalBoard.getEntity(1,3).getOccupant(), "Second box should remain at (1,3).");
        assertFalse(model.canUndo(), "Undo should not be available for an invalid push.");
    }

    /**
     * Tests pushing a box onto an empty target and achieving a win condition.
     * Uses {@code TEST_LEVEL_ONE_BOX_TO_WIN}.
     * Verifies player/box positions, score, {@code boxesOnTargets} count, and game status.
     */
    @Test
    void testBoxPushToTargetAndWin() {
        model = new SokobanModel(TEST_LEVEL_ONE_BOX_TO_WIN, DifficultyLevel.EASY);
        model.initializeGame();

        assertEquals(1, model.getTotalTargets(), "Should be 1 total target in this winnable level.");
        assertEquals(0, model.getBoxesOnTargets(), "Initially 0 boxes on targets.");
        String[] winLevel = {"PB.W"};
        model = new SokobanModel(winLevel, DifficultyLevel.EASY);
        model.initializeGame();

        assertEquals(1, model.getTotalTargets(), "Winnable level should have 1 target.");
        assertEquals(0, model.getBoxesOnTargets(), "Winnable level initially has 0 boxes on targets.");
        model.processGameSpecificAction(new SokobanMoveAction(Direction.RIGHT));

        assertEquals(0, model.getPlayerRow(), "Player row should be at box's old spot (0,1).");
        assertEquals(1, model.getPlayerCol(), "Player col should be at box's old spot (0,1).");
        assertEquals(1, model.getScore(), "Score should be 1 after push.");

        Grid<SokobanTile> board = (Grid<SokobanTile>) model.getGameBoard();
        assertEquals(SokobanOccupant.PLAYER, board.getEntity(0,1).getOccupant(), "Player should be at (0,1).");
        assertEquals(SokobanOccupant.BOX, board.getEntity(0,2).getOccupant(), "Box should be at (0,2).");
        assertEquals(SokobanBaseType.TARGET, board.getEntity(0,2).getBaseType(), "Tile (0,2) should be a target.");

        assertEquals(1, model.getBoxesOnTargets(), "boxesOnTargets should be 1 after pushing box to target.");
        assertEquals(GameStatus.GAME_OVER_WIN, model.getCurrentStatus(), "Game status should be GAME_OVER_WIN.");
        assertTrue(model.canUndo(), "Undo should be possible even after winning.");
    }

    /**
     * Tests the undo functionality for a Sokoban move where a box was pushed.
     * Makes a valid box push, then undoes it, verifying that the board state,
     * player position, box position, score, and {@code boxesOnTargets} count
     * are all reverted to their states before the push.
     */
    @Test
    void testUndoBoxPush_ToTarget() {
        String[] undoLevel = {"PB.W"};
        model = new SokobanModel(undoLevel, DifficultyLevel.EASY);
        model.initializeGame();
        Grid<SokobanTile> initialBoardCopy = ((Grid<SokobanTile>)model.getGameBoard()).deepCopy(SokobanTile::copy);
        int initialPlayerRow = model.getPlayerRow();
        int initialPlayerCol = model.getPlayerCol();
        int initialScore = model.getScore();
        int initialBoxesOnTargets = model.getBoxesOnTargets();
        model.processGameSpecificAction(new SokobanMoveAction(Direction.RIGHT));

        assertNotEquals(initialScore, model.getScore(), "Score should have changed after the push.");
        assertEquals(1, model.getBoxesOnTargets(), "One box should be on target after the push.");
        assertTrue(model.canUndo(), "Undo should be possible after the push.");
        assertEquals(GameStatus.GAME_OVER_WIN, model.getCurrentStatus(), "Game should be won after the push.");
        model.undoLastMove();
        assertEquals(initialScore, model.getScore(), "Score should revert after undo.");
        assertEquals(initialPlayerRow, model.getPlayerRow(), "Player row should revert after undo.");
        assertEquals(initialPlayerCol, model.getPlayerCol(), "Player column should revert after undo.");
        assertEquals(initialBoxesOnTargets, model.getBoxesOnTargets(), "boxesOnTargets count should revert after undo.");
        assertFalse(model.canUndo(), "Undo should not be possible after undoing the only recorded move.");
        assertEquals(GameStatus.PLAYING, model.getCurrentStatus(), "Game status should revert to PLAYING after undo from win state.");
        Grid<SokobanTile> boardAfterUndo = (Grid<SokobanTile>) model.getGameBoard();
        for(int r=0; r < initialBoardCopy.getRows(); r++) {
            for(int c=0; c < initialBoardCopy.getColumns(); c++) {
                assertEquals(initialBoardCopy.getEntity(r,c).getBaseType(), boardAfterUndo.getEntity(r,c).getBaseType(),
                        "BaseType at ("+r+","+c+") should revert.");
                assertEquals(initialBoardCopy.getEntity(r,c).getOccupant(), boardAfterUndo.getEntity(r,c).getOccupant(),
                        "Occupant at ("+r+","+c+") should revert.");
            }
        }
    }

    /**
     * Helper method to find the first valid action on the current model's board.
     * Iterates through all cells and checks if selecting that cell (which is not directly applicable
     * to Sokoban movement) or attempting a move from player's current position in any direction
     * constitutes a valid move by calling {@link SokobanModel#isValidAction(GameAction)}.
     * This method is more for games like SameGame; for Sokoban, we'd test specific moves.
     *
     * @param gameModel The {@link SokobanModel} instance to search for a valid action.
     * @return A {@link SokobanMoveAction} for the first valid move found for the player,
     * or {@code null} if no valid moves currently exist.
     */
    private SokobanMoveAction findFirstValidPlayerMove(SokobanModel gameModel) {
        if (gameModel.getGameBoard() == null || gameModel.getCurrentStatus() != GameStatus.PLAYING) {
            return null;
        }
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        for (Direction dir : directions) {
            SokobanMoveAction action = new SokobanMoveAction(dir);
            if (gameModel.isValidAction(action)) {
                return action;
            }
        }
        System.out.println("findFirstValidPlayerMove: No valid move found from player's current position.");
        return null;
    }
}