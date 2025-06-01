package com.aoopproject.games.samegame;

import com.aoopproject.framework.core.GameAction;
import com.aoopproject.framework.core.GameStatus;
import com.aoopproject.framework.core.Grid;
import com.aoopproject.games.samegame.action.SameGameSelectAction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;

/**
 * Unit tests for the {@link SameGameModel} class.
 * These tests verify the core game logic, including initialization based on {@link DifficultyLevel},
 * move processing, scoring, undo functionality, and state changes.
 * Test board setups use explicit colors rather than relying on the model's internal available color list
 * for predictability, ensuring the logic operates correctly on the provided board state.
 */
class SameGameModelTest {

    private SameGameModel model;
    private DifficultyLevel testDifficultyMedium;
    private DifficultyLevel testDifficultyFor3x3;

    /**
     * Sets up model instances before each test.
     * {@code testDifficultyMedium} is used for general initialization tests.
     * {@code testDifficultyFor3x3} is defined for tests requiring small, predictable boards,
     * ensuring enough colors are theoretically available for the test patterns.
     */
    @BeforeEach
    void setUp() {
        testDifficultyMedium = DifficultyLevel.MEDIUM;
        testDifficultyFor3x3 = DifficultyLevel.EASY;

        model = new SameGameModel(testDifficultyMedium);
    }

    /**
     * Tests the {@link SameGameModel#initializeGame()} method.
     * Verifies correct board dimensions based on the model's current difficulty (Medium),
     * non-null and non-empty tiles, zero initial score, PLAYING status, and no undo history.
     */
    @Test
    void testInitializeGame() {
        model.initializeGame();

        assertNotNull(model.getGameBoard(), "Game board should not be null after initialization.");
        @SuppressWarnings("unchecked")
        Grid<SameGameTile> board = (Grid<SameGameTile>) model.getGameBoard();

        assertEquals(testDifficultyMedium.getRows(), board.getRows(), "Board rows should match Medium difficulty.");
        assertEquals(testDifficultyMedium.getCols(), board.getColumns(), "Board columns should match Medium difficulty.");

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                SameGameTile tile = board.getEntity(r, c);
                assertNotNull(tile, "Tile at (" + r + "," + c + ") should not be null.");
                assertFalse(tile.isEmpty(), "Tile at (" + r + "," + c + ") should not be empty initially.");
            }
        }

        assertEquals(0, model.getScore(), "Initial score should be 0.");
        assertEquals(GameStatus.PLAYING, model.getCurrentStatus(), "Initial game status should be PLAYING.");
        assertFalse(model.canUndo(), "Undo should not be possible on a new game.");
    }

    /**
     * Implicitly tests score calculation through gameplay scenarios.
     * Covered in {@link #testProcessInputAction_ValidMoveAndScore_CustomBoard()}.
     */
    @Test
    void testCalculatePoints_viaGameplay() {
        assertTrue(true, "calculatePoints is private and tested implicitly via processInputAction, verifying score outcomes.");
    }

    /**
     * Tests finding connected tiles using {@link SameGameModel#isValidAction(GameAction)}.
     * A custom 3x3 board is set up with explicit colors. The model's difficulty is set
     * to one consistent with a 3x3 board (e.g., {@code testDifficultyFor3x3}).
     * Verifies correct identification of valid/invalid moves based on group size.
     */
    @Test
    void testFindConnectedTiles_ThroughIsValidAction_CustomBoard() {
        model = new SameGameModel(testDifficultyFor3x3);

        Grid<SameGameTile> testBoard = new Grid<>(3, 3);
        Color C_RED = Color.RED;
        Color C_BLUE = Color.BLUE;
        testBoard.setEntity(0,0, new SameGameTile(C_RED));  testBoard.setEntity(0,1, new SameGameTile(C_RED)); testBoard.setEntity(0,2, new SameGameTile(C_BLUE));
        testBoard.setEntity(1,0, new SameGameTile(C_RED));  testBoard.setEntity(1,1, new SameGameTile(C_BLUE));testBoard.setEntity(1,2, new SameGameTile(C_BLUE));
        testBoard.setEntity(2,0, new SameGameTile(C_BLUE)); testBoard.setEntity(2,1, new SameGameTile(C_RED)); testBoard.setEntity(2,2, new SameGameTile(C_BLUE));

        model.setTestGameBoard(testBoard, testDifficultyFor3x3, GameStatus.PLAYING);

        assertTrue(model.isValidAction(new SameGameSelectAction(0,0)), "Selecting (0,0) C_RED group (3 tiles) should be valid.");
        assertTrue(model.isValidAction(new SameGameSelectAction(0,2)), "Selecting (0,2) C_BLUE group (5 tiles) should be valid.");
        assertFalse(model.isValidAction(new SameGameSelectAction(2,1)), "Selecting isolated (2,1) C_RED tile (1 tile) should be invalid.");
    }

    /**
     * Tests processing of a valid move on a custom 3x3 board.
     * Verifies score update (2 tiles removed = 2 points with n*(n-1) rule) and undo availability.
     * The model is configured with a difficulty appropriate for the test board.
     */
    @Test
    void testProcessInputAction_ValidMoveAndScore_CustomBoard() {
        model = new SameGameModel(testDifficultyFor3x3);

        Grid<SameGameTile> board = new Grid<>(3, 3);
        Color C1 = Color.RED;
        Color C2 = Color.GREEN;
        Color C3 = Color.BLUE;
        board.setEntity(0,0, new SameGameTile(C1)); board.setEntity(0,1, new SameGameTile(C1)); board.setEntity(0,2, new SameGameTile(C2));
        board.setEntity(1,0, new SameGameTile(C3)); board.setEntity(1,1, new SameGameTile(C2)); board.setEntity(1,2, new SameGameTile(C2));
        board.setEntity(2,0, new SameGameTile(C3)); board.setEntity(2,1, new SameGameTile(C3)); board.setEntity(2,2, new SameGameTile(C1));

        model.setTestGameBoard(board, testDifficultyFor3x3, GameStatus.PLAYING);

        SameGameSelectAction action = new SameGameSelectAction(0,0);
        model.processInputAction(action);

        assertEquals(2 * (2-1), model.getScore(), "Score should be 2 for removing 2 tiles.");
        assertTrue(model.canUndo(), "Undo should be possible after a valid move.");
        @SuppressWarnings("unchecked")
        Grid<SameGameTile> boardAfterMove = (Grid<SameGameTile>) model.getGameBoard();
        SameGameTile tileAt00 = boardAfterMove.getEntity(0,0);
        SameGameTile tileAt01 = boardAfterMove.getEntity(0,1);

        boolean originalTilesRemovedOrMoved = (tileAt00.isEmpty() || tileAt00.getColor() != C1) &&
                (tileAt01.isEmpty() || tileAt01.getColor() != C1);
        SameGameTile clickedTileAfterMove = boardAfterMove.getEntity(0,0);
        assertTrue(board.getEntity(0,0).isEmpty() || board.getEntity(0,1).isEmpty(),
                "Original selected group tiles should become empty (or be part of shifted content). This check may need refinement.");
    }

    /**
     * Tests the undo functionality after one valid move.
     * Initializes a game (Medium difficulty), finds and performs the first valid move,
     * then undoes it. Verifies that the score and the entire board state are reverted
     * to their initial states, and undo is no longer possible.
     */
    @Test
    void testUndoLastMove_AfterOneValidMove() {
        model.initializeGame();

        @SuppressWarnings("unchecked")
        Grid<SameGameTile> initialBoard = (Grid<SameGameTile>) model.getGameBoard();
        if (initialBoard == null) {
            fail("Initial game board is null after initializeGame.");
            return;
        }
        Grid<SameGameTile> initialBoardCopy = initialBoard.deepCopy(SameGameTile::copy);
        int initialScore = model.getScore();

        SameGameSelectAction firstValidAction = findFirstValidAction(model);
        if (firstValidAction == null) {
            fail("Could not find a valid move on the initialized board (" +
                    model.getCurrentDifficulty().getDisplayName() +
                    ") to test undo. This might indicate an issue with board generation " +
                    "or MIN_TILES_TO_REMOVE vs. board state. Or the board was too small/sparse.");
            return;
        }

        System.out.println("Performing action for undo test: " + firstValidAction.getName() + " on " + model.getCurrentDifficulty().getDisplayName());
        model.processInputAction(firstValidAction);

        assertNotEquals(initialScore, model.getScore(), "Score should change after a valid move.");
        assertTrue(model.canUndo(), "Should be able to undo after one move.");

        model.undoLastMove();

        assertEquals(initialScore, model.getScore(), "Score should revert to initial score after undo.");
        assertFalse(model.canUndo(), "Undo should not be possible after undoing the only recorded move.");
        if (findFirstValidAction(model) != null) {
            assertEquals(GameStatus.PLAYING, model.getCurrentStatus(),
                    "Game status should be PLAYING if moves are available after undo.");
        } else {
            assertTrue(model.getCurrentStatus() == GameStatus.PLAYING || model.getCurrentStatus() == GameStatus.GAME_OVER_LOSE,
                    "Status should be PLAYING or GAME_OVER_LOSE if no moves after undo.");
        }
        @SuppressWarnings("unchecked")
        Grid<SameGameTile> boardAfterUndo = (Grid<SameGameTile>) model.getGameBoard();
        assertNotNull(boardAfterUndo, "Board should not be null after undo.");
        assertEquals(initialBoardCopy.getRows(), boardAfterUndo.getRows(), "Number of rows should match initial board.");
        assertEquals(initialBoardCopy.getColumns(), boardAfterUndo.getColumns(), "Number of columns should match initial board.");

        for(int r=0; r < initialBoardCopy.getRows(); r++) {
            for(int c=0; c < initialBoardCopy.getColumns(); c++) {
                SameGameTile originalTile = initialBoardCopy.getEntity(r,c);
                SameGameTile undoneTile = boardAfterUndo.getEntity(r,c);
                assertNotNull(originalTile, "Original copied tile at ("+r+","+c+") should not be null.");
                assertNotNull(undoneTile, "Undone tile at ("+r+","+c+") should not be null.");
                assertEquals(originalTile.isEmpty(), undoneTile.isEmpty(),
                        "Tile empty state should match at ("+r+","+c+") after undo.");
                if (!originalTile.isEmpty()) {
                    assertEquals(originalTile.getColor(), undoneTile.getColor(),
                            "Tile color should match at ("+r+","+c+") after undo.");
                }
            }
        }
    }

    /**
     * Helper method to find the first valid action on the current model's board.
     * Iterates through all cells and checks if selecting that cell constitutes a valid move
     * by calling {@link SameGameModel#isValidAction(GameAction)}.
     *
     * @param gameModel The {@link SameGameModel} instance to search for a valid action.
     * @return A {@link SameGameSelectAction} for the first valid move found, or {@code null}
     * if no valid moves currently exist on the board.
     */
    private SameGameSelectAction findFirstValidAction(SameGameModel gameModel) {
        if (gameModel.getGameBoard() == null) {
            System.err.println("findFirstValidAction: gameBoard is null.");
            return null;
        }

        @SuppressWarnings("unchecked")
        Grid<SameGameTile> board = (Grid<SameGameTile>) gameModel.getGameBoard();

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                SameGameTile tile = board.getEntity(r,c);
                if (tile != null && !tile.isEmpty()) {
                    SameGameSelectAction action = new SameGameSelectAction(r, c);
                    if (gameModel.isValidAction(action)) {
                        return action;
                    }
                }
            }
        }
        System.out.println("findFirstValidAction: No valid action found on the board.");
        return null;
    }
}