package com.aoopproject.games.samegame;

import com.aoopproject.framework.core.AbstractGameModel;
import com.aoopproject.framework.core.GameAction;
import com.aoopproject.framework.core.GameEvent;
import com.aoopproject.framework.core.GameStatus;
import com.aoopproject.framework.core.Grid;
import com.aoopproject.common.action.QuitAction;
import com.aoopproject.common.action.UndoAction;
import com.aoopproject.common.action.NewGameAction;
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
 * This class manages the grid of {@link SameGameTile}s, processes player selections,
 * calculates scores, handles undo functionality, suggests moves, and checks for game over conditions.
 * The game's difficulty (rows, columns, number of colors) is determined by a {@link DifficultyLevel}
 * set at construction. It extends {@link AbstractGameModel}.
 */
public class SameGameModel extends AbstractGameModel {

    /** Minimum number of connected tiles of the same color required to remove them. */
    private static final int MIN_TILES_TO_REMOVE = 2;

    private final List<Color> availableColors;
    private final Random random = new Random();
    private DifficultyLevel currentDifficulty;

    /**
     * Constructs a SameGameModel with the specified difficulty level.
     *
     * @param difficulty The {@link DifficultyLevel} defining the game's parameters
     * (rows, columns, number of colors). Must not be null.
     * @throws IllegalArgumentException if difficulty is null.
     */
    public SameGameModel(DifficultyLevel difficulty) {
        super();
        if (difficulty == null) {
            throw new IllegalArgumentException("DifficultyLevel cannot be null.");
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
     * This is used if no specific difficulty is provided, for example,
     * if the difficulty selection is cancelled by the user and a fallback is needed.
     */
    public SameGameModel() {
        this(DifficultyLevel.MEDIUM);
        System.out.println("SameGameModel created with default difficulty: MEDIUM");
    }


    /**
     * Gets the current difficulty level of the game.
     * @return The current {@link DifficultyLevel}.
     */
    public DifficultyLevel getCurrentDifficulty() {
        return currentDifficulty;
    }

    /**
     * Returns the maximum number of distinct colors supported by the game's predefined palette.
     * This can be used by UI elements to inform the user about color limits when setting difficulty.
     * @return The maximum number of supported colors.
     */
    public static int getMaxSupportedColors() {
        return PredefinedColors.PALETTE.size();
    }

    /**
     * Initializes the game board based on the {@code currentDifficulty} settings.
     * It creates a new grid, fills it with random colored tiles, resets the score,
     * sets the game status to PLAYING, and clears the undo history.
     * Notifies observers with "BOARD_INITIALIZED", "SCORE_UPDATED", and "NEW_GAME_STARTED" events.
     * The "NEW_GAME_STARTED" event includes the current difficulty as its payload.
     */
    @Override
    public void initializeGame() {
        if (this.currentDifficulty == null) {
            System.err.println("CRITICAL: Difficulty not set for SameGameModel prior to initializeGame. Forcing MEDIUM.");
            this.currentDifficulty = DifficultyLevel.MEDIUM;
        }

        int rows = this.currentDifficulty.getRows();
        int cols = this.currentDifficulty.getCols();
        int colorsCount = Math.min(this.currentDifficulty.getNumColors(), this.availableColors.size());

        this.gameBoard = new Grid<>(rows, cols);
        this.score = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (colorsCount <= 0) {
                    ((Grid<SameGameTile>) this.gameBoard).setEntity(r, c, new SameGameTile(Color.GRAY));
                } else {
                    Color randomColor = availableColors.get(random.nextInt(colorsCount));
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
     * Sets the game board to a predefined grid for testing purposes.
     * Also updates the model's internal row/col count and current difficulty to match the test board,
     * resets score, status, and undo history.
     *
     * @param testBoard The grid to set as the current game board.
     * @param testDifficulty The {@link DifficultyLevel} associated with this test board configuration.
     * @param initialStatus The status to set the game to (e.g., PLAYING).
     */
    protected void setTestGameBoard(Grid<SameGameTile> testBoard, DifficultyLevel testDifficulty, GameStatus initialStatus) {
        this.gameBoard = testBoard;
        this.currentDifficulty = testDifficulty;

        this.score = 0;
        this.setCurrentStatus(initialStatus);
        if (this.historyStack != null) {
            this.historyStack.clear();
        }
        notifyObservers(new GameEvent(this, "BOARD_CONFIGURED_FOR_TEST", this.gameBoard));
    }

    /**
     * Processes a given game action. Handles {@link NewGameAction}, {@link UndoAction},
     * {@link QuitAction}, {@link HintRequestAction}, and {@link SameGameSelectAction}.
     * For {@link SameGameSelectAction}, it checks for valid moves, updates the board,
     * score, and undo history, and fires appropriate events.
     *
     * @param action The {@link GameAction} to process.
     */
    @Override
    public void processInputAction(GameAction action) {
        if (action instanceof NewGameAction) {
            initializeGame();
            return;
        }
        if (action instanceof UndoAction) {
            if (canUndo()) {
                undoLastMove();
            } else {
                System.out.println("Cannot undo: No history available.");
                notifyObservers(new GameEvent(this, "UNDO_FAILED", "No history."));
            }
            return;
        }
        if (action instanceof QuitAction) {
            setCurrentStatus(GameStatus.GAME_ENDED_USER_QUIT);
            notifyObservers(new GameEvent(this, "GAME_ENDED_BY_USER_QUIT_EVENT", null));
            System.out.println("Game quit by user.");
            return;
        }
        if (action instanceof HintRequestAction) {
            if (getCurrentStatus() == GameStatus.PLAYING) {
                List<SameGameTilePosition> suggestionGroup = suggestMove();
                if (suggestionGroup != null && !suggestionGroup.isEmpty()) {
                    notifyObservers(new GameEvent(this, "MOVE_SUGGESTION_AVAILABLE", suggestionGroup));
                } else {
                    notifyObservers(new GameEvent(this, "NO_SUGGESTION_AVAILABLE", "No valid moves to suggest."));
                }
            }
            return;
        }
        if (getCurrentStatus() != GameStatus.PLAYING) {
            System.out.println("Action ignored: Game not in PLAYING state. Current state: " + getCurrentStatus() + " Action: " + action.getName());
            return;
        }

        if (action instanceof SameGameSelectAction) {
            SameGameSelectAction selectAction = (SameGameSelectAction) action;
            int r = selectAction.getRow();
            int c = selectAction.getColumn();

            if (gameBoard == null || !gameBoard.isValidCoordinate(r, c)) {
                System.err.println("Invalid coordinates for selection: " + r + "," + c);
                return;
            }

            SameGameTile selectedTile = ((Grid<SameGameTile>) this.gameBoard).getEntity(r, c);
            if (selectedTile == null || selectedTile.isEmpty()) {
                System.err.println("Selected tile is empty or invalid at: " + r + "," + c);
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
                System.out.println("Not enough connected tiles to remove at (" + r + "," + c + "). Found: " + connectedTiles.size());
                notifyObservers(new GameEvent(this, "INVALID_SELECTION", "Not enough connected tiles to remove."));
            }
            return;
        }
        System.err.println("Unknown action type received or unhandled in PLAYING state: " + action.getName());
    }

    /**
     * Undoes the last move by restoring the game board and score from the history stack.
     * Sets the game status to PLAYING and notifies observers.
     */
    @Override
    public void undoLastMove() {
        if (!canUndo() || historyStack == null || historyStack.isEmpty()) {
            return;
        }
        GameState previousState = (GameState) historyStack.pop();

        this.gameBoard = previousState.boardState();
        setScore(previousState.scoreState());
        setCurrentStatus(GameStatus.PLAYING);

        notifyObservers(new GameEvent(this, "BOARD_CHANGED", this.gameBoard));
        notifyObservers(new GameEvent(this, "UNDO_PERFORMED", null));
        checkEndGameConditions();
        System.out.println("Last move undone. Current score: " + this.score);
    }

    /**
     * Checks if an undo operation can be performed (i.e., if there's history).
     * @return {@code true} if undo is possible, {@code false} otherwise.
     */
    @Override
    public boolean canUndo() {
        return historyStack != null && !historyStack.isEmpty();
    }

    /**
     * Validates if a given {@link GameAction} is permissible in the current game state.
     * @param action The game action to validate.
     * @return {@code true} if the action is valid, {@code false} otherwise.
     */
    @Override
    public boolean isValidAction(GameAction action) {
        if (action instanceof NewGameAction) return true;
        if (action instanceof UndoAction) return canUndo();
        if (action instanceof QuitAction) return true;

        GameStatus currentStatus = getCurrentStatus();
        if (action instanceof HintRequestAction) return currentStatus == GameStatus.PLAYING;

        if (currentStatus != GameStatus.PLAYING) return false;

        if (!(action instanceof SameGameSelectAction)) return false;

        SameGameSelectAction selectAction = (SameGameSelectAction) action;
        int r = selectAction.getRow();
        int c = selectAction.getColumn();

        if (gameBoard == null || !gameBoard.isValidCoordinate(r, c)) return false;

        SameGameTile selectedTile = ((Grid<SameGameTile>) this.gameBoard).getEntity(r, c);
        if (selectedTile == null || selectedTile.isEmpty()) return false;

        return findConnectedTiles(r, c).size() >= MIN_TILES_TO_REMOVE;
    }

    /**
     * Checks for end-of-game conditions (win or loss) based on available moves.
     * Updates the game status accordingly and notifies observers.
     */
    @Override
    protected void checkEndGameConditions() {
        if (gameBoard == null || getCurrentStatus() == GameStatus.GAME_OVER_WIN || getCurrentStatus() == GameStatus.GAME_OVER_LOSE || getCurrentStatus() == GameStatus.GAME_ENDED_USER_QUIT) {
            return;
        }

        boolean moveAvailable = false;
        for (int r = 0; r < gameBoard.getRows(); r++) {
            for (int c = 0; c < gameBoard.getColumns(); c++) {
                SameGameTile tile = ((Grid<SameGameTile>) this.gameBoard).getEntity(r, c);
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
            boolean boardEmpty = true;
            for (int r = 0; r < gameBoard.getRows(); r++) {
                for (int c = 0; c < gameBoard.getColumns(); c++) {
                    SameGameTile tile = ((Grid<SameGameTile>) this.gameBoard).getEntity(r, c);
                    if (tile != null && !tile.isEmpty()) {
                        boardEmpty = false;
                        break;
                    }
                }
                if (!boardEmpty) break;
            }
            if (boardEmpty) {
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
     * would yield the highest immediate score.
     *
     * @return A List of {@link SameGameTilePosition} representing the tiles in the suggested group.
     * Returns an empty list if no valid moves are available or game is not in PLAYING state.
     */
    public List<SameGameTilePosition> suggestMove() {
        if (getCurrentStatus() != GameStatus.PLAYING || gameBoard == null) {
            return Collections.emptyList();
        }

        List<SameGameTilePosition> bestGroupToClick = Collections.emptyList();
        int maxScoreForMove = -1;

        Grid<SameGameTile> board = (Grid<SameGameTile>) this.gameBoard;
        Set<SameGameTilePosition> processedGroupStarters = new HashSet<>();

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                SameGameTilePosition currentStartPos = new SameGameTilePosition(r, c);
                if (processedGroupStarters.contains(currentStartPos)) {
                    continue;
                }

                SameGameTile currentTile = board.getEntity(r, c);
                if (currentTile != null && !currentTile.isEmpty()) {
                    List<SameGameTilePosition> connectedTiles = findConnectedTiles(r, c);

                    if (connectedTiles.size() >= MIN_TILES_TO_REMOVE) {
                        processedGroupStarters.addAll(connectedTiles);

                        int currentMoveScore = calculatePoints(connectedTiles.size());
                        if (currentMoveScore > maxScoreForMove) {
                            maxScoreForMove = currentMoveScore;
                            bestGroupToClick = new ArrayList<>(connectedTiles);
                        }
                    }
                }
            }
        }
        return bestGroupToClick;
    }

    /**
     * Finds all tiles connected to the tile at (startRow, startCol) that are of the same color.
     * Uses a Depth First Search (DFS)-like approach.
     *
     * @param startRow The starting row of the tile to check.
     * @param startCol The starting column of the tile to check.
     * @return A list of {@link SameGameTilePosition} objects representing the connected tiles.
     */
    private List<SameGameTilePosition> findConnectedTiles(int startRow, int startCol) {
        List<SameGameTilePosition> connected = new ArrayList<>();
        Set<SameGameTilePosition> visited = new HashSet<>();
        Grid<SameGameTile> board = (Grid<SameGameTile>)this.gameBoard;
        SameGameTile startTile = board.getEntity(startRow, startCol);

        if (startTile == null || startTile.isEmpty()) {
            return connected;
        }
        Color targetColor = startTile.getColor();

        List<SameGameTilePosition> stack = new ArrayList<>();
        stack.add(new SameGameTilePosition(startRow, startCol));
        visited.add(new SameGameTilePosition(startRow, startCol));

        while(!stack.isEmpty()){
            SameGameTilePosition currentPos = stack.remove(stack.size() -1);
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
     * @param tilesToRemove A list of {@link SameGameTilePosition} indicating which tiles to remove.
     */
    private void removeTiles(List<SameGameTilePosition> tilesToRemove) {
        Grid<SameGameTile> board = (Grid<SameGameTile>) this.gameBoard;
        for (SameGameTilePosition pos : tilesToRemove) {
            SameGameTile tile = board.getEntity(pos.row, pos.col);
            if (tile != null) {
                tile.setEmpty();
            }
        }
    }

    /**
     * Applies gravity to the tiles. Tiles above empty spaces fall down.
     * Uses {@code gameBoard.getRows()} and {@code gameBoard.getColumns()} for dimensions.
     */
    private void applyGravity() {
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
     * Compacts columns by shifting non-empty columns to the left.
     * Uses {@code gameBoard.getRows()} and {@code gameBoard.getColumns()} for dimensions.
     */
    private void compactColumns() {
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
     * Formula: {@code numRemoved * (numRemoved - 1)} if {@code numRemoved >= MIN_TILES_TO_REMOVE}.
     * @param numRemoved The number of tiles removed.
     * @return The calculated score.
     */
    private int calculatePoints(int numRemoved) {
        if (numRemoved >= MIN_TILES_TO_REMOVE) {
            return numRemoved * (numRemoved - 1);
        }
        return 0;
    }

    /**
     * Provides the game board as the view representation.
     * @return The current {@link Grid} of {@link SameGameTile}s.
     */
    @Override
    public Object getBoardViewRepresentation() {
        return this.gameBoard;
    }

    /**
     * Helper inner class to represent a tile's position (row and column) on the grid.
     * Used in algorithms like {@link #findConnectedTiles(int, int)} and for move suggestions.
     * This class is public static to be accessible as a type in event payloads by views.
     */
    public static class SameGameTilePosition {
        /** The row index of the tile. */
        public final int row;
        /** The column index of the tile. */
        public final int col;

        /**
         * Constructs a new SameGameTilePosition.
         * @param r The row index.
         * @param c The column index.
         */
        public SameGameTilePosition(int r, int c) { this.row = r; this.col = c;}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SameGameTilePosition that = (SameGameTilePosition) o;
            return row == that.row && col == that.col;
        }
        @Override
        public int hashCode() { return java.util.Objects.hash(row, col); }
        @Override
        public String toString() { return "Pos(" + row + "," + col + ")";}
    }

    /**
     * Helper inner class to define a palette of colors for the game tiles
     * and a placeholder color for empty slots (used internally during gravity/compaction).
     */
    private static class PredefinedColors {
        /** The list of available colors for game tiles. */
        public static final List<Color> PALETTE = List.of(
                Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
                Color.ORANGE, Color.CYAN, Color.MAGENTA, Color.PINK
        );
        /** A placeholder color for tiles that are marked as empty, primarily for internal logic. */
        public static final Color EMPTY_SLOT_COLOR = Color.DARK_GRAY;
    }
}