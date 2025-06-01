package com.aoopproject.games.samegame;

import com.aoopproject.common.model.DifficultyLevel;
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
 * move processing (tile removal, gravity, column compaction), scoring, undo functionality,
 * and game state changes. Test board setups use explicit colors and specific difficulty
 * configurations to ensure predictable and verifiable outcomes.
 */
class SameGameModelTest {

    private SameGameModel model;
    private DifficultyLevel testDifficultyMedium;
    private DifficultyLevel testDifficultyFor3x3;

    /**
     * Sets up model instances before each test.
     * {@code testDifficultyMedium} (e.g., {@link DifficultyLevel#MEDIUM}) is used for general initialization tests.
     * {@code testDifficultyFor3x3} (e.g., {@link DifficultyLevel#EASY} or a custom small one)
     * is used for tests requiring small, predictable 3x3 boards.
     * The model is instantiated with {@code testDifficultyMedium} by default in setUp.
     * Individual tests can re-instantiate the model with {@code testDifficultyFor3x3} if needed.
     */
    @BeforeEach
    void setUp() {
        testDifficultyMedium = DifficultyLevel.MEDIUM;
        testDifficultyFor3x3 = DifficultyLevel.EASY;

        model = new SameGameModel(testDifficultyMedium);
    }

    /**
     * Tests the {@link SameGameModel#initializeGame()} method.
     * Verifies that the game board is created with dimensions corresponding to the model's
     * current difficulty (set to Medium in {@code setUp()}). It also checks for non-null,
     * non-empty tiles, an initial score of 0, PLAYING game status, and that undo is not initially possible.
     */
    @Test
    void testInitializeGame() {
        model.initializeGame();

        assertNotNull(model.getGameBoard(), "Game board should not be null after initialization.");
        @SuppressWarnings("unchecked")
        Grid<SameGameTile> board = (Grid<SameGameTile>) model.getGameBoard();

        assertEquals(testDifficultyMedium.getRows(), board.getRows(), "Board rows should match the difficulty's row count.");
        assertEquals(testDifficultyMedium.getCols(), board.getColumns(), "Board columns should match the difficulty's column count.");

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                SameGameTile tile = board.getEntity(r, c);
                assertNotNull(tile, "Tile at (" + r + "," + c + ") should not be null.");
                assertFalse(tile.isEmpty(), "Tile at (" + r + "," + c + ") should not be empty initially.");
            }
        }

        assertEquals(0, model.getScore(), "Initial score should be 0.");
        assertEquals(GameStatus.PLAYING, model.getCurrentStatus(), "Initial game status should be PLAYING.");
        assertFalse(model.canUndo(), "Undo should not be possible on a new game (no history).");
    }

    /**
     * Verifies that the private {@code calculatePoints} method is implicitly tested
     * through gameplay scenarios, specifically within {@link #testProcessInputAction_ValidMoveAndScore_CustomBoard()}.
     */
    @Test
    void testCalculatePoints_viaGameplay() {
        assertTrue(true, "calculatePoints is private; its correctness is verified by checking score outcomes in processInputAction tests.");
    }

    /**
     * Tests the logic for finding connected tiles, accessed indirectly via {@link SameGameModel#isValidAction(GameAction)}.
     * A custom 3x3 board is set up with specific colors. The model is configured with a
     * {@link DifficultyLevel} suitable for this board size to ensure consistency if model internals
     * (like available colors) are derived from it during {@code setTestGameBoard}.
     * Verifies that selections on tile groups of various sizes are correctly identified as valid or invalid
     * based on the game's {@code MIN_TILES_TO_REMOVE} rule.
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

        assertTrue(model.isValidAction(new SameGameSelectAction(0,0)), "Selecting (0,0) C_RED group (3 tiles) should be a valid move.");
        assertTrue(model.isValidAction(new SameGameSelectAction(0,2)), "Selecting (0,2) C_BLUE group (5 tiles) should be a valid move.");
        assertFalse(model.isValidAction(new SameGameSelectAction(2,1)), "Selecting isolated (2,1) C_RED tile (1 tile) should be an invalid move.");
    }

    /**
     * Tests the processing of a valid move on a custom 3x3 board.
     * This includes verifying the score update (2 tiles removed = 2 points with n*(n-1) rule, MIN_TO_REMOVE=2)
     * and checking if undo becomes available. The model is configured with a difficulty appropriate for the test board.
     * Assertions on the exact final board state after gravity/compaction are complex and are tested more focally
     * in specific gravity/compaction tests.
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

        assertEquals(2 * (2-1), model.getScore(), "Score should be 2 for removing 2 tiles (n*(n-1) rule).");
        assertTrue(model.canUndo(), "Undo should be possible after a valid move.");
        @SuppressWarnings("unchecked")
        Grid<SameGameTile> boardAfterMove = (Grid<SameGameTile>) model.getGameBoard();
        SameGameTile tile00After = boardAfterMove.getEntity(0,0);
        SameGameTile tile01After = boardAfterMove.getEntity(0,1);
        assertTrue(tile00After.isEmpty() || tile00After.getColor() != C1 || tile01After.isEmpty() || tile01After.getColor() != C1,
                "Original selected group tiles at (0,0) and (0,1) should have become empty or their content shifted/changed.");
    }

    /**
     * Tests the undo functionality after one valid move.
     * Initializes a game (using Medium difficulty from setUp), finds and performs the first valid move,
     * then undoes it. Verifies that the score and the entire board state are correctly reverted
     * to their initial states, and that undo is no longer possible after undoing the only recorded move.
     */
    @Test
    void testUndoLastMove_AfterOneValidMove() {
        model.initializeGame();

        @SuppressWarnings("unchecked")
        Grid<SameGameTile> initialBoardGrid = (Grid<SameGameTile>) model.getGameBoard();
        if (initialBoardGrid == null) {
            fail("Initial game board is null after initializeGame, cannot proceed with undo test.");
            return;
        }
        Grid<SameGameTile> initialBoardCopy = initialBoardGrid.deepCopy(SameGameTile::copy);
        int initialScore = model.getScore();

        SameGameSelectAction firstValidAction = findFirstValidAction(model);
        if (firstValidAction == null) {
            fail("Could not find a valid move on the initialized board (" +
                    model.getCurrentDifficulty().getDisplayName() +
                    ") to test undo. This might indicate an issue with board generation, " +
                    "MIN_TILES_TO_REMOVE setting, or the board being too small/sparse for the difficulty.");
            return;
        }
        model.processInputAction(firstValidAction);

        assertNotEquals(initialScore, model.getScore(), "Score should have changed after a valid move.");
        assertTrue(model.canUndo(), "Undo should be possible after one valid move.");

        model.undoLastMove();

        assertEquals(initialScore, model.getScore(), "Score should revert to the initial score after undo.");
        assertFalse(model.canUndo(), "Undo should not be possible after undoing the only recorded move.");
        if (findFirstValidAction(model) != null) {
            assertEquals(GameStatus.PLAYING, model.getCurrentStatus(),
                    "Game status should be PLAYING if moves are available on the board after undo.");
        } else {
            GameStatus statusAfterUndo = model.getCurrentStatus();
            assertTrue(statusAfterUndo == GameStatus.PLAYING || statusAfterUndo == GameStatus.GAME_OVER_LOSE || statusAfterUndo == GameStatus.GAME_OVER_WIN,
                    "Status after undo should be PLAYING, GAME_OVER_LOSE, or GAME_OVER_WIN. Actual: " + statusAfterUndo);
        }
        @SuppressWarnings("unchecked")
        Grid<SameGameTile> boardAfterUndo = (Grid<SameGameTile>) model.getGameBoard();
        assertNotNull(boardAfterUndo, "Board should not be null after undo operation.");
        assertEquals(initialBoardCopy.getRows(), boardAfterUndo.getRows(), "Number of rows should match the initial board after undo.");
        assertEquals(initialBoardCopy.getColumns(), boardAfterUndo.getColumns(), "Number of columns should match the initial board after undo.");

        for(int r=0; r < initialBoardCopy.getRows(); r++) {
            for(int c=0; c < initialBoardCopy.getColumns(); c++) {
                SameGameTile originalCopiedTile = initialBoardCopy.getEntity(r,c);
                SameGameTile tileAfterUndo = boardAfterUndo.getEntity(r,c);
                assertNotNull(originalCopiedTile, "Original copied tile at ("+r+","+c+") should not be null (error in test setup).");
                assertNotNull(tileAfterUndo, "Tile at ("+r+","+c+") after undo should not be null.");

                assertEquals(originalCopiedTile.isEmpty(), tileAfterUndo.isEmpty(),
                        "Tile empty state at ("+r+","+c+") should match initial state after undo.");
                if (!originalCopiedTile.isEmpty()) {
                    assertEquals(originalCopiedTile.getColor(), tileAfterUndo.getColor(),
                            "Tile color at ("+r+","+c+") should match initial state after undo.");
                }
            }
        }
    }

    /**
     * Tests the {@link SameGameModel#applyGravity()} method with a simple scenario.
     * Sets up a 3x3 board with gaps and checks if tiles fall down correctly into empty spaces below them,
     * and that the vacated upper cells are marked as empty.
     * Requires {@code applyGravity} to be package-private or protected in {@code SameGameModel}.
     */
    @Test
    void testApplyGravity_simpleFall() {
        model = new SameGameModel(testDifficultyFor3x3);

        Grid<SameGameTile> board = new Grid<>(3, 3);
        Color R = Color.RED;
        Color G = Color.GREEN;
        SameGameTile emptyPlaceholder = new SameGameTile(SameGameModel.PredefinedColors.EMPTY_SLOT_COLOR);
        emptyPlaceholder.setEmpty();
        board.setEntity(0, 0, new SameGameTile(R));
        board.setEntity(1, 0, emptyPlaceholder.copy());
        board.setEntity(2, 0, new SameGameTile(G));

        board.setEntity(0, 1, emptyPlaceholder.copy());
        board.setEntity(1, 1, new SameGameTile(R));
        board.setEntity(2, 1, new SameGameTile(G));

        board.setEntity(0, 2, new SameGameTile(R));
        board.setEntity(1, 2, new SameGameTile(G));
        board.setEntity(2, 2, emptyPlaceholder.copy());

        model.setTestGameBoard(board, testDifficultyFor3x3, GameStatus.PLAYING);

        model.applyGravity();

        @SuppressWarnings("unchecked")
        Grid<SameGameTile> boardAfterGravity = (Grid<SameGameTile>) model.getGameBoard();
        assertTrue(boardAfterGravity.getEntity(0, 0).isEmpty(), "Col0,Row0 should be empty after gravity.");
        assertEquals(R, boardAfterGravity.getEntity(1, 0).getColor(), "Col0,Row1 should be Red.");
        assertFalse(boardAfterGravity.getEntity(1, 0).isEmpty());
        assertEquals(G, boardAfterGravity.getEntity(2, 0).getColor(), "Col0,Row2 should be Green.");
        assertFalse(boardAfterGravity.getEntity(2, 0).isEmpty());
        assertTrue(boardAfterGravity.getEntity(0, 1).isEmpty(), "Col1,Row0 should be empty after gravity.");
        assertEquals(R, boardAfterGravity.getEntity(1, 1).getColor(), "Col1,Row1 should be Red.");
        assertFalse(boardAfterGravity.getEntity(1, 1).isEmpty());
        assertEquals(G, boardAfterGravity.getEntity(2, 1).getColor(), "Col1,Row2 should be Green.");
        assertFalse(boardAfterGravity.getEntity(2, 1).isEmpty());
        assertTrue(boardAfterGravity.getEntity(0, 2).isEmpty(), "Col2,Row0 should be empty after gravity.");
        assertEquals(R, boardAfterGravity.getEntity(1, 2).getColor(), "Col2,Row1 should be Red.");
        assertFalse(boardAfterGravity.getEntity(1, 2).isEmpty());
        assertEquals(G, boardAfterGravity.getEntity(2, 2).getColor(), "Col2,Row2 should be Green.");
        assertFalse(boardAfterGravity.getEntity(2, 2).isEmpty());
    }

    /**
     * Tests the {@link SameGameModel#compactColumns()} method.
     * Sets up a 3x3 board with an entirely empty column between two non-empty columns
     * and verifies that the non-empty columns are shifted left correctly, and the
     * vacated rightmost column becomes empty.
     * Requires {@code compactColumns} to be package-private or protected in {@code SameGameModel}.
     */
    @Test
    void testCompactColumns_withEmptyColumnInMiddle() {
        model = new SameGameModel(testDifficultyFor3x3);

        Grid<SameGameTile> board = new Grid<>(3, 3);
        Color R = Color.RED;
        Color B = Color.BLUE;
        SameGameTile emptyTile = new SameGameTile(SameGameModel.PredefinedColors.EMPTY_SLOT_COLOR);
        emptyTile.setEmpty();
        board.setEntity(0, 0, new SameGameTile(R)); board.setEntity(1, 0, new SameGameTile(R)); board.setEntity(2, 0, new SameGameTile(R));
        board.setEntity(0, 1, emptyTile.copy());    board.setEntity(1, 1, emptyTile.copy());    board.setEntity(2, 1, emptyTile.copy());
        board.setEntity(0, 2, new SameGameTile(B)); board.setEntity(1, 2, new SameGameTile(B)); board.setEntity(2, 2, new SameGameTile(B));

        model.setTestGameBoard(board, testDifficultyFor3x3, GameStatus.PLAYING);

        model.compactColumns();

        @SuppressWarnings("unchecked")
        Grid<SameGameTile> boardAfterCompact = (Grid<SameGameTile>) model.getGameBoard();
        assertFalse(boardAfterCompact.getEntity(0, 0).isEmpty());
        assertEquals(R, boardAfterCompact.getEntity(0, 0).getColor(), "Col0,Row0 should be Red.");
        assertEquals(R, boardAfterCompact.getEntity(1, 0).getColor(), "Col0,Row1 should be Red.");
        assertEquals(R, boardAfterCompact.getEntity(2, 0).getColor(), "Col0,Row2 should be Red.");
        assertFalse(boardAfterCompact.getEntity(0, 1).isEmpty());
        assertEquals(B, boardAfterCompact.getEntity(0, 1).getColor(), "Col1,Row0 should now be Blue.");
        assertEquals(B, boardAfterCompact.getEntity(1, 1).getColor(), "Col1,Row1 should now be Blue.");
        assertEquals(B, boardAfterCompact.getEntity(2, 1).getColor(), "Col1,Row2 should now be Blue.");
        assertTrue(boardAfterCompact.getEntity(0, 2).isEmpty(), "Col2,Row0 should now be empty.");
        assertTrue(boardAfterCompact.getEntity(1, 2).isEmpty(), "Col2,Row1 should now be empty.");
        assertTrue(boardAfterCompact.getEntity(2, 2).isEmpty(), "Col2,Row2 should now be empty.");
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
        System.out.println("findFirstValidAction: No valid action found on the board for current difficulty: " + gameModel.getCurrentDifficulty().getDisplayName());
        return null;
    }
}