package com.aoopproject.games.samegame;

import com.aoopproject.common.action.NewGameAction;
import com.aoopproject.common.action.QuitAction;
import com.aoopproject.common.action.UndoAction;
import com.aoopproject.common.model.DifficultyLevel;
import com.aoopproject.framework.core.AbstractGameModel;
import com.aoopproject.framework.core.GameAction;
import com.aoopproject.framework.core.GameEvent;
import com.aoopproject.framework.core.GameStatus;
import com.aoopproject.framework.core.Grid;
import com.aoopproject.common.action.HintRequestAction;
import com.aoopproject.games.samegame.action.SameGameSelectAction;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Implements the game logic for SameGame.
 * This class is responsible for managing the grid of {@link SameGameTile}s,
 * processing game-specific player actions like tile selections and hint requests,
 * calculating scores, applying game mechanics (tile removal, gravity, column compaction),
 * and determining win/loss conditions.
 * <p>
 * It extends {@link AbstractGameModel}, relying on the superclass to handle
 * common framework actions (New Game, Undo, Quit) and to manage observers,
 * game status, score, and the undo history stack. The game's difficulty
 * (rows, columns, number of colors) is determined by a {@link DifficultyLevel}
 * set at construction.
 * </p>
 */
public class SameGameModel extends AbstractGameModel {

    /** Minimum number of connected tiles of the same color required for them to be removed. */
    private static final int MIN_TILES_TO_REMOVE = 2;

    /** List of colors available for tile generation based on the current difficulty. */
    private final List<Color> availableColors;
    /** Random number generator for selecting tile colors. */
    private final Random random = new Random();
    /** The current difficulty level of the game. */
    private DifficultyLevel currentDifficulty;

    /**
     * Represents a snapshot of the SameGame's state for the undo functionality.
     * Stores a deep copy of the game grid and the score at that point.
     * Other game-specific state relevant to undo could be added here if necessary.
     */
    private record GameState(Grid<SameGameTile> boardState, int scoreState) {}


    /**
     * Constructs a SameGameModel with the specified difficulty level.
     * Initializes the list of available colors based on the difficulty.
     *
     * @param difficulty The {@link DifficultyLevel} defining the game's parameters
     * (rows, columns, number of colors). Must not be null.
     * @throws IllegalArgumentException if difficulty is null.
     */
    public SameGameModel(DifficultyLevel difficulty) {
        super();
        if (difficulty == null) {
            throw new IllegalArgumentException("DifficultyLevel cannot be null for SameGameModel.");
        }
        this.currentDifficulty = difficulty;

        this.availableColors = new ArrayList<>();
        int colorsToUse = Math.min(this.currentDifficulty.getNumColors(), PredefinedColors.PALETTE.size());
        for (int i = 0; i < colorsToUse; i++) {
            this.availableColors.add(PredefinedColors.PALETTE.get(i));
        }
    }

    /**
     * Default constructor that initializes the game with {@link DifficultyLevel#MEDIUM}.
     * This is used if no specific difficulty is explicitly provided.
     */
    public SameGameModel() {
        this(DifficultyLevel.MEDIUM);
        System.out.println("SameGameModel created with default difficulty: MEDIUM.");
    }

    /**
     * Gets the current difficulty level of the game.
     * @return The current {@link DifficultyLevel}.
     */
    public DifficultyLevel getCurrentDifficulty() {
        return currentDifficulty;
    }

    /**
     * Initializes or resets the game to its starting state based on the {@code currentDifficulty}.
     * This involves:
     * <ul>
     * <li>Creating a new game board ({@link #gameBoard}) with dimensions from the current difficulty.</li>
     * <li>Filling the board with {@link SameGameTile}s of random colors chosen from the available palette
     * based on the number of colors specified by the current difficulty.</li>
     * <li>Resetting the score to 0.</li>
     * <li>Setting the game status to {@link GameStatus#PLAYING}.</li>
     * <li>Clearing the undo history stack.</li>
     * <li>Notifying observers with "BOARD_INITIALIZED", "SCORE_UPDATED", and "NEW_GAME_STARTED" events.
     * The "NEW_GAME_STARTED" event includes the current difficulty as its payload.</li>
     * </ul>
     */
    @Override
    @SuppressWarnings("unchecked")
    public void initializeGame() {
        if (this.currentDifficulty == null) {
            System.err.println("CRITICAL: SameGameModel - Difficulty not set prior to initializeGame. Forcing MEDIUM.");
            this.currentDifficulty = DifficultyLevel.MEDIUM;
            this.availableColors.clear();
            int colorsToUse = Math.min(this.currentDifficulty.getNumColors(), PredefinedColors.PALETTE.size());
            for (int i = 0; i < colorsToUse; i++) {
                this.availableColors.add(PredefinedColors.PALETTE.get(i));
            }
        }

        int rows = this.currentDifficulty.getRows();
        int cols = this.currentDifficulty.getCols();
        int colorsCountInUse = this.availableColors.size();


        this.gameBoard = new Grid<>(rows, cols);
        this.score = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (colorsCountInUse <= 0) {
                    ((Grid<SameGameTile>) this.gameBoard).setEntity(r, c, new SameGameTile(Color.GRAY));
                    System.err.println("Warning: No available colors for tile generation. Using GRAY.");
                } else {
                    Color randomColor = availableColors.get(random.nextInt(colorsCountInUse));
                    ((Grid<SameGameTile>) this.gameBoard).setEntity(r, c, new SameGameTile(randomColor));
                }
            }
        }
        setCurrentStatus(GameStatus.PLAYING);
        if (historyStack != null) {
            historyStack.clear();
        }
        notifyObservers(new GameEvent(this, "BOARD_INITIALIZED", this.gameBoard));
        notifyObservers(new GameEvent(this, "SCORE_UPDATED", this.score));
        notifyObservers(new GameEvent(this, "NEW_GAME_STARTED", this.currentDifficulty));
    }

    /**
     * Sets the game board to a predefined grid and difficulty for testing purposes.
     * This method is intended for use in test environments to create specific scenarios.
     * It reinitializes internal color lists based on the test difficulty, resets the score,
     * sets the game status, clears the undo history, and notifies observers.
     *
     * @param testBoard The {@link Grid} of {@link SameGameTile}s to set as the current game board.
     * @param testDifficulty The {@link DifficultyLevel} to associate with this test board configuration.
     * @param initialStatus The {@link GameStatus} to set the game to (e.g., PLAYING).
     */
    protected void setTestGameBoard(Grid<SameGameTile> testBoard, DifficultyLevel testDifficulty, GameStatus initialStatus) {
        this.gameBoard = testBoard;
        this.currentDifficulty = testDifficulty;
        this.availableColors.clear();
        int colorsToUse = Math.min(this.currentDifficulty.getNumColors(), PredefinedColors.PALETTE.size());
        for (int i = 0; i < colorsToUse; i++) {
            this.availableColors.add(PredefinedColors.PALETTE.get(i));
        }

        this.score = 0;
        this.setCurrentStatus(initialStatus);
        if (this.historyStack != null) {
            this.historyStack.clear();
        }
        notifyObservers(new GameEvent(this, "BOARD_CONFIGURED_FOR_TEST", this.gameBoard));
    }

    /**
     * Processes game-specific actions for SameGame, such as selecting a tile or requesting a hint.
     * This method is called by {@link AbstractGameModel#processInputAction(GameAction)}
     * when the game is in the {@link GameStatus#PLAYING} state and the action is not a common
     * framework action (like New Game, Undo, or Quit).
     * <p>
     * For a {@link SameGameSelectAction}:
     * <ul>
     * <li>Validates the selection (coordinates, non-empty tile).</li>
     * <li>Finds connected tiles of the same color.</li>
     * <li>If enough tiles are connected (>= {@link #MIN_TILES_TO_REMOVE}):
     * <ul>
     * <li>Saves the current state (board, score) for undo.</li>
     * <li>Removes the tiles.</li>
     * <li>Applies gravity to shift remaining tiles down.</li>
     * <li>Compacts columns to remove empty ones.</li>
     * <li>Calculates and updates the score.</li>
     * <li>Notifies observers with "BOARD_CHANGED" and "TILES_REMOVED_SUCCESS" events.</li>
     * <li>Checks for end-game conditions.</li>
     * </ul>
     * </li>
     * <li>If not enough tiles are connected, notifies observers with an "INVALID_SELECTION" event.</li>
     * </ul>
     * For a {@link HintRequestAction}:
     * <ul>
     * <li>Calls {@link #suggestMove()} to find the best immediate move.</li>
     * <li>Notifies observers with "MOVE_SUGGESTION_AVAILABLE" (with the suggested tile group)
     * or "NO_SUGGESTION_AVAILABLE" events.</li>
     * </ul>
     * </p>
     *
     * @param action The game-specific {@link GameAction} to process.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void processGameSpecificAction(GameAction action) {

        if (action instanceof HintRequestAction) {
            List<SameGameTilePosition> suggestionGroup = suggestMove();
            if (suggestionGroup != null && !suggestionGroup.isEmpty()) {
                notifyObservers(new GameEvent(this, "MOVE_SUGGESTION_AVAILABLE", suggestionGroup));
            } else {
                notifyObservers(new GameEvent(this, "NO_SUGGESTION_AVAILABLE", "No valid moves to suggest."));
            }
            return;
        }

        if (action instanceof SameGameSelectAction) {
            SameGameSelectAction selectAction = (SameGameSelectAction) action;
            int r = selectAction.row();
            int c = selectAction.column();

            if (gameBoard == null || !gameBoard.isValidCoordinate(r, c)) {
                System.err.println("SameGameModel: Invalid coordinates for selection: (" + r + "," + c + ")");
                notifyObservers(new GameEvent(this, "INVALID_SELECTION", "Invalid coordinates."));
                return;
            }

            SameGameTile selectedTile = ((Grid<SameGameTile>) this.gameBoard).getEntity(r, c);
            if (selectedTile == null || selectedTile.isEmpty()) {
                System.err.println("SameGameModel: Selected tile is empty or null at: (" + r + "," + c + ")");
                notifyObservers(new GameEvent(this, "INVALID_SELECTION", "Selected tile is empty."));
                return;
            }

            List<SameGameTilePosition> connectedTiles = findConnectedTiles(r, c);

            if (connectedTiles.size() >= MIN_TILES_TO_REMOVE) {
                Grid<SameGameTile> boardBeforeMove = ((Grid<SameGameTile>) this.gameBoard).deepCopy(SameGameTile::copy);
                int scoreBeforeMove = this.score;
                if (historyStack != null) {
                    historyStack.push(new GameState(boardBeforeMove, scoreBeforeMove));
                }

                removeTiles(connectedTiles);
                applyGravity();
                compactColumns();
                int pointsEarned = calculatePoints(connectedTiles.size());
                setScore(this.score + pointsEarned);
                notifyObservers(new GameEvent(this, "BOARD_CHANGED", this.gameBoard));
                notifyObservers(new GameEvent(this, "TILES_REMOVED_SUCCESS", connectedTiles.size()));
                checkEndGameConditions();
            } else {
                System.out.println("SameGameModel: Not enough connected tiles to remove at (" + r + "," + c + "). Found: " + connectedTiles.size());
                notifyObservers(new GameEvent(this, "INVALID_SELECTION", "Not enough connected tiles to remove."));
            }
            return;
        }
        System.err.println("SameGameModel: Unknown game-specific action type received: " + action.getName());
    }

    /**
     * Undoes the last successfully processed move by restoring the game board and score
     * from the history stack. Sets the game status back to PLAYING and notifies observers.
     * This method is called by {@link AbstractGameModel#processInputAction(GameAction)}
     * when an {@link UndoAction} is received and {@link #canUndo()} is true.
     */
    @Override
    public void undoLastMove() {
        if (historyStack == null || historyStack.isEmpty()) {
            System.err.println("SameGameModel.undoLastMove: Called when canUndo should be false or historyStack is null.");
            return;
        }

        GameState previousState = (GameState) historyStack.pop();

        this.gameBoard = previousState.boardState();
        setScore(previousState.scoreState());
        setCurrentStatus(GameStatus.PLAYING);

        notifyObservers(new GameEvent(this, "BOARD_CHANGED", this.gameBoard));
        notifyObservers(new GameEvent(this, "UNDO_PERFORMED", null));
        checkEndGameConditions();
        System.out.println("SameGameModel: Last move undone. Current score: " + this.score);
    }

    /**
     * Checks if an undo operation can currently be performed.
     * An undo is possible if the {@link AbstractGameModel#historyStack} is not null and not empty.
     *
     * @return {@code true} if an undo operation is possible, {@code false} otherwise.
     */
    @Override
    public boolean canUndo() {
        return historyStack != null && !historyStack.isEmpty();
    }

    /**
     * Validates if a given {@link GameAction} is permissible in the current SameGame state.
     * <ul>
     * <li>Common actions (NewGame, Undo, Quit) are validated by {@link AbstractGameModel} or their specific conditions.</li>
     * <li>A {@link HintRequestAction} is valid if the game is currently {@link GameStatus#PLAYING}.</li>
     * <li>A {@link SameGameSelectAction} is valid if the game is PLAYING, the selected coordinates are
     * valid, the tile at those coordinates is not empty, and selecting it would result in
     * removing at least {@link #MIN_TILES_TO_REMOVE} tiles.</li>
     * </ul>
     *
     * @param action The {@link GameAction} to validate.
     * @return {@code true} if the action is considered valid for processing, {@code false} otherwise.
     */
    @Override
    public boolean isValidAction(GameAction action) {
        if (action instanceof NewGameAction) return true;
        if (action instanceof UndoAction) return canUndo();
        if (action instanceof QuitAction) return true;

        GameStatus currentStatus = getCurrentStatus();
        if (action instanceof HintRequestAction) {
            return currentStatus == GameStatus.PLAYING;
        }

        if (currentStatus != GameStatus.PLAYING) {
            return false;
        }

        if (!(action instanceof SameGameSelectAction)) {
            return false;
        }
        SameGameSelectAction selectAction = (SameGameSelectAction) action;
        int r = selectAction.row();
        int c = selectAction.column();

        if (gameBoard == null || !gameBoard.isValidCoordinate(r, c)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        SameGameTile selectedTile = ((Grid<SameGameTile>) this.gameBoard).getEntity(r, c);
        if (selectedTile == null || selectedTile.isEmpty()) {
            return false;
        }
        return findConnectedTiles(r, c).size() >= MIN_TILES_TO_REMOVE;
    }

    /**
     * Checks for SameGame's end-of-game conditions (win or loss).
     * A loss occurs if no more valid moves (groups of {@link #MIN_TILES_TO_REMOVE} or more) can be made.
     * A win occurs if the board is completely empty (which also implies no more moves).
     * Updates the {@link #currentStatus} and notifies observers if the game ends.
     * If moves are available and status was not PLAYING (e.g., after an undo from game over),
     * it sets the status back to PLAYING.
     */
    @Override
    protected void checkEndGameConditions() {
        if (gameBoard == null ||
                getCurrentStatus() == GameStatus.GAME_OVER_WIN ||
                getCurrentStatus() == GameStatus.GAME_OVER_LOSE ||
                getCurrentStatus() == GameStatus.GAME_ENDED_USER_QUIT) {
            return;
        }

        boolean moveAvailable = false;
        Grid<SameGameTile> board = (Grid<SameGameTile>) this.gameBoard;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                SameGameTile tile = board.getEntity(r, c);
                if (tile != null && !tile.isEmpty()) {
                    if (findConnectedTiles(r, c).size() >= MIN_TILES_TO_REMOVE) {
                        moveAvailable = true;
                        break;
                    }
                }
            }
            if (moveAvailable) break;
        }

        if (!moveAvailable) {
            boolean boardCompletelyEmpty = true;
            for (int r = 0; r < board.getRows(); r++) {
                for (int c = 0; c < board.getColumns(); c++) {
                    SameGameTile tile = board.getEntity(r, c);
                    if (tile != null && !tile.isEmpty()) {
                        boardCompletelyEmpty = false;
                        break;
                    }
                }
                if (!boardCompletelyEmpty) break;
            }
            if (boardCompletelyEmpty) {
                setCurrentStatus(GameStatus.GAME_OVER_WIN);
            } else {
                setCurrentStatus(GameStatus.GAME_OVER_LOSE);
            }
        } else if (getCurrentStatus() != GameStatus.PLAYING) {
            setCurrentStatus(GameStatus.PLAYING);
        }
    }

    /**
     * Analyzes the current board state and suggests a group of tiles that, if clicked,
     * would yield the highest immediate score based on the game's scoring rules.
     *
     * @return A {@code List<SameGameTilePosition>} representing the tiles in the suggested group.
     * Returns an empty list if no valid moves are available or if the game is not in PLAYING state.
     */
    public List<SameGameTilePosition> suggestMove() {
        if (getCurrentStatus() != GameStatus.PLAYING || gameBoard == null) {
            return Collections.emptyList();
        }

        List<SameGameTilePosition> bestGroupToClick = Collections.emptyList();
        int maxScoreForMove = -1;

        Grid<SameGameTile> board = (Grid<SameGameTile>) this.gameBoard;
        Set<SameGameTilePosition> consideredStartingTiles = new HashSet<>();

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                SameGameTilePosition currentStartPos = new SameGameTilePosition(r, c);
                if (consideredStartingTiles.contains(currentStartPos)) {
                    continue;
                }

                SameGameTile currentTile = board.getEntity(r, c);
                if (currentTile != null && !currentTile.isEmpty()) {
                    List<SameGameTilePosition> connectedTiles = findConnectedTiles(r, c);

                    if (connectedTiles.size() >= MIN_TILES_TO_REMOVE) {
                        consideredStartingTiles.addAll(connectedTiles);

                        int currentMoveScore = calculatePoints(connectedTiles.size());
                        if (currentMoveScore > maxScoreForMove) {
                            maxScoreForMove = currentMoveScore;
                            bestGroupToClick = new ArrayList<>(connectedTiles);
                        } else if (currentMoveScore == maxScoreForMove && !connectedTiles.isEmpty()) {
                            if (bestGroupToClick.isEmpty() || connectedTiles.size() > bestGroupToClick.size()) {
                                bestGroupToClick = new ArrayList<>(connectedTiles);
                            }
                        }
                    }
                }
            }
        }
        return bestGroupToClick;
    }

    /**
     * Finds all tiles connected to the tile at (startRow, startCol) that are of the same color.
     * Uses a Depth First Search (DFS)-like algorithm.
     *
     * @param startRow The starting row of the tile to check.
     * @param startCol The starting column of the tile to check.
     * @return A list of {@link SameGameTilePosition} objects representing the connected tiles.
     * Returns an empty list if the starting tile is empty or invalid.
     */
    private List<SameGameTilePosition> findConnectedTiles(int startRow, int startCol) {
        List<SameGameTilePosition> connected = new ArrayList<>();
        Set<SameGameTilePosition> visited = new HashSet<>();

        @SuppressWarnings("unchecked")
        Grid<SameGameTile> board = (Grid<SameGameTile>)this.gameBoard;
        if (board == null) return connected;

        SameGameTile startTile = board.getEntity(startRow, startCol);

        if (startTile == null || startTile.isEmpty()) {
            return connected;
        }
        Color targetColor = startTile.getColor();

        List<SameGameTilePosition> stack = new ArrayList<>();
        stack.add(new SameGameTilePosition(startRow, startCol));
        visited.add(new SameGameTilePosition(startRow, startCol));

        while(!stack.isEmpty()){
            SameGameTilePosition currentPos = stack.removeLast();
            connected.add(currentPos);
            int r = currentPos.row;
            int c = currentPos.col;
            int[] dr = {-1, 1, 0, 0};
            int[] dc = {0, 0, -1, 1};

            for(int i=0; i<4; i++){
                int nr = r + dr[i];
                int nc = c + dc[i];
                SameGameTilePosition nextPos = new SameGameTilePosition(nr, nc);

                if(board.isValidCoordinate(nr, nc) && !visited.contains(nextPos)){
                    SameGameTile neighborTile = board.getEntity(nr, nc);
                    if(neighborTile != null && !neighborTile.isEmpty() && neighborTile.getColor().equals(targetColor)){
                        visited.add(nextPos);
                        stack.add(nextPos);
                    }
                }
            }
        }
        return connected;
    }

    /**
     * Marks a list of tiles as empty on the game board.
     * This is called after a valid group of tiles is identified for removal.
     *
     * @param tilesToRemove A list of {@link SameGameTilePosition} indicating which tiles to remove (mark as empty).
     */
    private void removeTiles(List<SameGameTilePosition> tilesToRemove) {
        @SuppressWarnings("unchecked")
        Grid<SameGameTile> board = (Grid<SameGameTile>) this.gameBoard;
        if (board == null) return;

        for (SameGameTilePosition pos : tilesToRemove) {
            SameGameTile tile = board.getEntity(pos.row, pos.col);
            if (tile != null) {
                tile.setEmpty();
            }
        }
    }

    /**
     * Applies gravity to the tiles on the board. After tiles are removed (marked as empty),
     * tiles above the empty spaces fall down to fill them. Vacated upper cells are
     * filled with new placeholder empty tiles.
     * This method iterates using the actual dimensions of the current {@code gameBoard}.
     * It is typically called after {@link #removeTiles(List)}.
     * This method's visibility is protected for testing purposes.
     */
    protected void applyGravity() {
        @SuppressWarnings("unchecked")
        Grid<SameGameTile> board = (Grid<SameGameTile>) this.gameBoard;
        if (board == null) return;

        int currentBoardRows = board.getRows();
        int currentBoardCols = board.getColumns();

        for (int c = 0; c < currentBoardCols; c++) {
            int emptySlotRow = currentBoardRows - 1;
            for (int r = currentBoardRows - 1; r >= 0; r--) {
                SameGameTile currentTile = board.getEntity(r, c);
                if (currentTile != null && !currentTile.isEmpty()) {
                    if (r != emptySlotRow) {
                        board.setEntity(emptySlotRow, c, currentTile);
                        SameGameTile emptyPlaceholder = new SameGameTile(PredefinedColors.EMPTY_SLOT_COLOR);
                        emptyPlaceholder.setEmpty();
                        board.setEntity(r, c, emptyPlaceholder);
                    }
                    emptySlotRow--;
                }
            }
            for (int r = emptySlotRow; r >=0; r--) {
                SameGameTile emptyPlaceholder = new SameGameTile(PredefinedColors.EMPTY_SLOT_COLOR);
                emptyPlaceholder.setEmpty();
                board.setEntity(r, c, emptyPlaceholder);
            }
        }
    }

    /**
     * Compacts columns by shifting non-empty columns to the left to fill
     * any columns that became entirely empty after tile removal and gravity.
     * Vacated columns on the right are filled with new placeholder empty tiles.
     * This method iterates using the actual dimensions of the current {@code gameBoard}.
     * It is typically called after {@link #applyGravity()}.
     * This method's visibility is protected for testing purposes.
     */
    protected void compactColumns() {
        @SuppressWarnings("unchecked")
        Grid<SameGameTile> board = (Grid<SameGameTile>) this.gameBoard;
        if (board == null) return;

        int currentBoardRows = board.getRows();
        int currentBoardCols = board.getColumns();
        int writeCol = 0;

        for (int readCol = 0; readCol < currentBoardCols; readCol++) {
            boolean columnIsEntirelyEmpty = true;
            for (int r = 0; r < currentBoardRows; r++) {
                SameGameTile tile = board.getEntity(r, readCol);
                if (tile != null && !tile.isEmpty()) {
                    columnIsEntirelyEmpty = false;
                    break;
                }
            }

            if (!columnIsEntirelyEmpty) {
                if (readCol != writeCol) {
                    for (int r = 0; r < currentBoardRows; r++) {
                        board.setEntity(r, writeCol, board.getEntity(r, readCol));
                        SameGameTile emptyPlaceholder = new SameGameTile(PredefinedColors.EMPTY_SLOT_COLOR);
                        emptyPlaceholder.setEmpty();
                        board.setEntity(r, readCol, emptyPlaceholder);
                    }
                }
                writeCol++;
            }
        }
    }

    /**
     * Calculates the score for removing a specified number of tiles.
     * The current scoring formula is {@code numRemoved * (numRemoved - 1)}
     * if at least {@link #MIN_TILES_TO_REMOVE} tiles are removed.
     *
     * @param numRemoved The number of tiles removed in one move.
     * @return The points awarded for this move. Returns 0 if fewer than {@code MIN_TILES_TO_REMOVE} tiles are specified.
     */
    private int calculatePoints(int numRemoved) {
        if (numRemoved >= MIN_TILES_TO_REMOVE) {
            return numRemoved * (numRemoved - 1);
        }
        return 0;
    }

    /**
     * Helper inner class to represent a tile's position (row and column) on the game grid.
     * This class is immutable. Used in algorithms like finding connected tiles and for move suggestions.
     * It is declared as {@code public static} to be accessible as a type in {@link GameEvent} payloads
     * by views or other observers.
     */
    public static class SameGameTilePosition {
        /** The row index of the tile (0-based). */
        public final int row;
        /** The column index of the tile (0-based). */
        public final int col;

        /**
         * Constructs a new {@code SameGameTilePosition}.
         * @param r The row index.
         * @param c The column index.
         */
        public SameGameTilePosition(int r, int c) { this.row = r; this.col = c;}

        /**
         * Compares this tile position to another object for equality.
         * Two positions are equal if they have the same row and column.
         * @param o The object to compare with.
         * @return {@code true} if the objects are equal, {@code false} otherwise.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SameGameTilePosition that = (SameGameTilePosition) o;
            return row == that.row && col == that.col;
        }

        /**
         * Generates a hash code for this tile position.
         * Based on the row and column values.
         * @return The hash code.
         */
        @Override
        public int hashCode() { return java.util.Objects.hash(row, col); }

        /**
         * Returns a string representation of this tile position.
         * @return A string in the format "Pos(row,col)".
         */
        @Override
        public String toString() { return "Pos(" + row + "," + col + ")";}
    }

    /**
     * Helper inner class to define a palette of standard colors for the game tiles
     * and a placeholder color for empty slots created during internal game logic
     * (e.g., after gravity or column compaction). This class and its fields are
     * {@code public static} to be accessible by tests or other related utility classes
     * for consistent color referencing.
     */
    public static class PredefinedColors {
        /** The list of available standard colors for game tiles. */
        public static final List<Color> PALETTE = List.of(
                Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
                Color.ORANGE, Color.CYAN, Color.MAGENTA, Color.PINK
        );
        /** * A placeholder color used internally for {@link SameGameTile}s that are marked as empty
         * (e.g., after tiles are removed and gravity applied). This helps in debugging
         * and ensures that even "empty" tiles in the grid are non-null {@code SameGameTile} objects.
         * The visual representation of such tiles should be handled by the view based on their {@code isEmpty()} state.
         */
        public static final Color EMPTY_SLOT_COLOR = Color.DARK_GRAY;
    }
}