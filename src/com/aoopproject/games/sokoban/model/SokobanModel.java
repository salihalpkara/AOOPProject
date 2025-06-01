package com.aoopproject.games.sokoban.model;

import com.aoopproject.common.action.NewGameAction;
import com.aoopproject.common.action.QuitAction;
import com.aoopproject.common.action.UndoAction;
import com.aoopproject.common.model.DifficultyLevel;
import com.aoopproject.framework.core.AbstractGameModel;
import com.aoopproject.framework.core.GameAction;
import com.aoopproject.framework.core.GameEvent;
import com.aoopproject.framework.core.GameStatus;
import com.aoopproject.framework.core.Grid;
import com.aoopproject.games.sokoban.action.Direction;
import com.aoopproject.games.sokoban.action.SokobanMoveAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implements the game logic for the Sokoban puzzle game.
 * This class manages the game board (a grid of {@link SokobanTile}s), the player's position,
 * box movements, the undo history specific to Sokoban, and checks for win conditions
 * (all boxes on target locations).
 * <p>
 * It extends {@link AbstractGameModel}, relying on the superclass to handle
 * common framework actions and to manage observers, game status, score (interpreted as move count),
 * and the basic undo history stack mechanism. This class implements game-specific logic
 * within {@link #processGameSpecificAction(GameAction)}, {@link #initializeGame()},
 * {@link #undoLastMove()}, {@link #canUndo()}, {@link #isValidAction(GameAction)},
 * and {@link #checkEndGameConditions()}.
 * </p>
 */
public class SokobanModel extends AbstractGameModel {

    private int playerRow;
    private int playerCol;

    private String[] currentLevelData;
    private int totalTargets;
    private int boxesOnTargets;
    private DifficultyLevel currentDifficulty;
    private static final String[] LEVEL_EASY_DEFAULT = {
            "WWWWW",
            "W P W",
            "W B.W",
            "WWWWW"
    };
    private static final String[] LEVEL_MEDIUM_DEFAULT = {
            "WWWWW",
            "W P W",
            "W B.W",
            "W.B W",
            "WWWWW"
    };
    private static final String[] LEVEL_HARD_DEFAULT = {
            "  WWWWW ",
            "WWW   W ",
            "W.PB  W ",
            "WWW B.W ",
            "W.WWB W ",
            "W W . WW",
            "WB BBB.W",
            "W   .  W",
            "WWWWWWWW"
    };


    /**
     * Represents a snapshot of the Sokoban game's state for the undo functionality.
     * @param boardState A deep copy of the {@link Grid} of {@link SokobanTile}s.
     * @param playerRow The player's row at the time of this state.
     * @param playerCol The player's column at the time of this state.
     * @param boxesOnTargetsCount The number of boxes on target locations in this state.
     * @param score The game score (move count) in this state.
     */
    private record SokobanGameState(Grid<SokobanTile> boardState, int playerRow, int playerCol, int boxesOnTargetsCount, int score) {}

    /**
     * Constructs a SokobanModel with the specified difficulty level.
     * The actual level data (String array) will be chosen based on this difficulty
     * within the {@link #initializeGame()} method if no specific level data is pre-set.
     * @param difficulty The selected {@link DifficultyLevel}. Must not be null.
     */
    public SokobanModel(DifficultyLevel difficulty) {
        super();
        this.currentDifficulty = Objects.requireNonNull(difficulty, "DifficultyLevel cannot be null in SokobanModel constructor");
        this.currentLevelData = null;
        System.out.println("SokobanModel created for difficulty: " + this.currentDifficulty.getDisplayName());
    }

    /**
     * Default constructor, initializes with {@link DifficultyLevel#MEDIUM}.
     */
    public SokobanModel() {
        this(DifficultyLevel.MEDIUM);
    }

    /**
     * Constructs a SokobanModel with the given level data string array and associates it with a specific difficulty.
     * This constructor is useful for tests or loading specific levels directly.
     *
     * @param levelData A String array representing the level layout. If null or empty, a default for the
     * specified difficulty will be used by {@code initializeGame}.
     * @param difficulty The {@link DifficultyLevel} to associate with this game instance. Must not be null.
     */
    public SokobanModel(String[] levelData, DifficultyLevel difficulty) {
        super();
        this.currentDifficulty = Objects.requireNonNull(difficulty, "DifficultyLevel cannot be null.");
        this.currentLevelData = (levelData != null && levelData.length > 0) ? levelData : null;
        System.out.println("SokobanModel created with " +
                (this.currentLevelData != null ? "custom level data" : "difficulty-based level selection") +
                " and difficulty: " + this.currentDifficulty.getDisplayName());
    }

    /**
     * Gets the current difficulty level of the game.
     * @return The current {@link DifficultyLevel}.
     */
    public DifficultyLevel getCurrentDifficulty() {
        return currentDifficulty;
    }

    /**
     * Initializes or resets the game to the starting state of a level.
     * If {@code this.currentLevelData} was pre-set by a constructor, that level is used.
     * Otherwise, a level is chosen based on {@code this.currentDifficulty}.
     * Parses the level data, creates the game board, sets player position, counts targets,
     * resets score, sets status to PLAYING, clears undo history, and notifies observers.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void initializeGame() {
        String[] levelDataToParse;

        if (this.currentLevelData != null && this.currentLevelData.length > 0) {
            levelDataToParse = this.currentLevelData;
            System.out.println("SokobanModel.initializeGame: Using pre-set level data.");
            if (this.currentDifficulty == null) this.currentDifficulty = DifficultyLevel.MEDIUM;
        } else if (this.currentDifficulty != null) {
            System.out.println("SokobanModel.initializeGame: Choosing level based on difficulty: " + this.currentDifficulty.getDisplayName());
            switch (this.currentDifficulty) {
                case EASY:   levelDataToParse = LEVEL_EASY_DEFAULT; break;
                case HARD:   levelDataToParse = LEVEL_HARD_DEFAULT; break;
                case MEDIUM: default: levelDataToParse = LEVEL_MEDIUM_DEFAULT; break;
            }
            this.currentLevelData = levelDataToParse;
        } else {
            System.err.println("SokobanModel.initializeGame: CRITICAL - No level data and no difficulty. Using MEDIUM default level.");
            this.currentDifficulty = DifficultyLevel.MEDIUM;
            levelDataToParse = LEVEL_MEDIUM_DEFAULT;
            this.currentLevelData = levelDataToParse;
        }

        int numRows = levelDataToParse.length;
        int numCols = 0;
        if (numRows > 0) {
            for (String line : levelDataToParse) { if (line.length() > numCols) numCols = line.length(); }
        }

        Grid<SokobanTile> newBoard = new Grid<>(numRows, numCols);
        this.totalTargets = 0;
        this.boxesOnTargets = 0;
        boolean playerFound = false;

        for (int r = 0; r < numRows; r++) {
            String line = levelDataToParse[r];
            for (int c = 0; c < numCols; c++) {
                char cellChar = (c < line.length()) ? line.charAt(c) : ' ';
                SokobanBaseType base = SokobanBaseType.FLOOR;
                SokobanOccupant occupant = SokobanOccupant.NONE;
                switch (cellChar) {
                    case 'W': base = SokobanBaseType.WALL; break;
                    case ' ': base = SokobanBaseType.FLOOR; break;
                    case '.': base = SokobanBaseType.TARGET; this.totalTargets++; break;
                    case 'P': occupant = SokobanOccupant.PLAYER; this.playerRow = r; this.playerCol = c; playerFound = true; break;
                    case '@': base = SokobanBaseType.TARGET; occupant = SokobanOccupant.PLAYER; this.playerRow = r; this.playerCol = c; this.totalTargets++; playerFound = true; break;
                    case 'B': occupant = SokobanOccupant.BOX; break;
                    case '$': base = SokobanBaseType.TARGET; occupant = SokobanOccupant.BOX; this.totalTargets++; this.boxesOnTargets++; break;
                    default:  base = SokobanBaseType.FLOOR; break;
                }
                newBoard.setEntity(r, c, new SokobanTile(base, occupant));
            }
        }
        this.gameBoard = newBoard;

        if (!playerFound && numRows > 0 && numCols > 0) {
            System.err.println("CRITICAL ERROR: Player ('P' or '@') not found in Sokoban level data. Game may be unplayable.");
            setCurrentStatus(GameStatus.INITIALIZING);
            notifyObservers(new GameEvent(this, "LEVEL_LOAD_ERROR", "Player not found."));
            return;
        }

        setScore(0);
        setCurrentStatus(GameStatus.PLAYING);
        if (historyStack != null) historyStack.clear();
        notifyObservers(new GameEvent(this, "BOARD_INITIALIZED", this.gameBoard));
        notifyObservers(new GameEvent(this, "NEW_GAME_STARTED", this.currentDifficulty));
        checkEndGameConditions();
    }

    /**
     * Processes Sokoban-specific game actions, primarily player movement and box pushing.
     * This method is called by {@link AbstractGameModel#processInputAction(GameAction)}
     * when the game is PLAYING and the action is not a common framework one.
     *
     * @param action The {@link SokobanMoveAction} to process.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void processGameSpecificAction(GameAction action) {
        if (!(action instanceof SokobanMoveAction)) {
            System.err.println("SokobanModel: Received unknown game-specific action: " + action.getName());
            return;
        }

        SokobanMoveAction moveAction = (SokobanMoveAction) action;
        Grid<SokobanTile> board = (Grid<SokobanTile>) this.gameBoard;
        if (board == null) return;

        int r = this.playerRow;
        int c = this.playerCol;
        Direction dir = moveAction.getDirection();
        int dr = dir.getDeltaRow();
        int dc = dir.getDeltaColumn();

        int nextPlayerR = r + dr;
        int nextPlayerC = c + dc;

        if (!board.isValidCoordinate(nextPlayerR, nextPlayerC)) {
            notifyAndReturn("INVALID_MOVE", "Player move out of bounds."); return;
        }

        SokobanTile currentPlayerTile = board.getEntity(r, c);
        SokobanTile targetTileForPlayer = board.getEntity(nextPlayerR, nextPlayerC);
        boolean moveMade = false;

        if (targetTileForPlayer.getBaseType() == SokobanBaseType.WALL) {
            notifyAndReturn("INVALID_MOVE", "Cannot move into a wall."); return;
        }
        if (targetTileForPlayer.getOccupant() == SokobanOccupant.NONE) {
            Grid<SokobanTile> boardCopy = board.deepCopy(SokobanTile::copy);
            SokobanGameState prevState = new SokobanGameState(boardCopy, this.playerRow, this.playerCol, this.boxesOnTargets, this.getScore());
            if (historyStack != null) historyStack.push(prevState);

            currentPlayerTile.setOccupant(SokobanOccupant.NONE);
            targetTileForPlayer.setOccupant(SokobanOccupant.PLAYER);
            this.playerRow = nextPlayerR;
            this.playerCol = nextPlayerC;
            moveMade = true;

        } else if (targetTileForPlayer.getOccupant() == SokobanOccupant.BOX) {
            int nextBoxR = nextPlayerR + dr;
            int nextBoxC = nextPlayerC + dc;

            if (!board.isValidCoordinate(nextBoxR, nextBoxC)) {
                notifyAndReturn("INVALID_MOVE", "Cannot push box out of bounds."); return;
            }

            SokobanTile targetTileForBox = board.getEntity(nextBoxR, nextBoxC);
            if (targetTileForBox.getBaseType() != SokobanBaseType.WALL && targetTileForBox.getOccupant() == SokobanOccupant.NONE) {
                Grid<SokobanTile> boardCopy = board.deepCopy(SokobanTile::copy);
                SokobanGameState prevState = new SokobanGameState(boardCopy, this.playerRow, this.playerCol, this.boxesOnTargets, this.getScore());
                if (historyStack != null) historyStack.push(prevState);
                if (targetTileForPlayer.getBaseType() == SokobanBaseType.TARGET) this.boxesOnTargets--;
                if (targetTileForBox.getBaseType() == SokobanBaseType.TARGET) this.boxesOnTargets++;
                targetTileForBox.setOccupant(SokobanOccupant.BOX);
                targetTileForPlayer.setOccupant(SokobanOccupant.PLAYER);
                currentPlayerTile.setOccupant(SokobanOccupant.NONE);

                this.playerRow = nextPlayerR;
                this.playerCol = nextPlayerC;
                moveMade = true;
            } else {
                notifyAndReturn("INVALID_MOVE", "Box is blocked (wall or another box)."); return;
            }
        }

        if (moveMade) {
            setScore(this.getScore() + 1);
            notifyObservers(new GameEvent(this, "BOARD_CHANGED", this.gameBoard));
            checkEndGameConditions();
        }
    }

    /** Helper to simplify firing an event and returning, for invalid moves. */
    private void notifyAndReturn(String eventType, String payloadMessage) {
        System.out.println("SokobanModel: " + payloadMessage);
        notifyObservers(new GameEvent(this, eventType, payloadMessage));
    }


    /**
     * Undoes the last successful move by restoring the game state from history.
     */
    @Override
    public void undoLastMove() {
        if (historyStack == null || historyStack.isEmpty()) return;

        SokobanGameState prevState = (SokobanGameState) historyStack.pop();
        this.gameBoard = prevState.boardState();
        this.playerRow = prevState.playerRow();
        this.playerCol = prevState.playerCol();
        this.boxesOnTargets = prevState.boxesOnTargetsCount();
        setScore(prevState.score());
        setCurrentStatus(GameStatus.PLAYING);

        notifyObservers(new GameEvent(this, "BOARD_CHANGED", this.gameBoard));
        notifyObservers(new GameEvent(this, "UNDO_PERFORMED", null));
    }

    /**
     * Checks if an undo operation is possible.
     * @return {@code true} if undo history is not empty.
     */
    @Override
    public boolean canUndo() {
        return historyStack != null && !historyStack.isEmpty();
    }

    /**
     * Validates if a given {@link GameAction} is permissible in the current Sokoban game state.
     * Primarily checks {@link SokobanMoveAction} for validity based on game rules.
     * Other common actions (NewGame, Undo, Quit) are assumed to be handled by {@link AbstractGameModel}
     * for their basic enablement, though this method also returns true for them.
     *
     * @param action The game action to validate.
     * @return {@code true} if the action is considered valid, {@code false} otherwise.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean isValidAction(GameAction action) {
        if (action instanceof NewGameAction || action instanceof QuitAction) return true;
        if (action instanceof UndoAction) return canUndo();

        if (getCurrentStatus() != GameStatus.PLAYING) return false;

        if (action instanceof SokobanMoveAction) {
            SokobanMoveAction moveAction = (SokobanMoveAction) action;
            Grid<SokobanTile> board = (Grid<SokobanTile>) this.gameBoard;
            if (board == null) return false;

            int r = this.playerRow;
            int c = this.playerCol;
            Direction dir = moveAction.getDirection();
            int dr = dir.getDeltaRow();
            int dc = dir.getDeltaColumn();
            int nextPlayerR = r + dr;
            int nextPlayerC = c + dc;

            if (!board.isValidCoordinate(nextPlayerR, nextPlayerC)) return false;
            SokobanTile targetTileForPlayer = board.getEntity(nextPlayerR, nextPlayerC);
            if (targetTileForPlayer.getBaseType() == SokobanBaseType.WALL) return false;

            if (targetTileForPlayer.getOccupant() == SokobanOccupant.BOX) {
                int nextBoxR = nextPlayerR + dr;
                int nextBoxC = nextPlayerC + dc;
                if (!board.isValidCoordinate(nextBoxR, nextBoxC)) return false;
                SokobanTile targetTileForBox = board.getEntity(nextBoxR, nextBoxC);
                return targetTileForBox.getBaseType() != SokobanBaseType.WALL &&
                        targetTileForBox.getOccupant() == SokobanOccupant.NONE;
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if all targets are covered by boxes to determine win condition.
     * If all targets are covered and game is PLAYING, status is set to GAME_OVER_WIN.
     */
    @Override
    protected void checkEndGameConditions() {
        if (gameBoard == null || getCurrentStatus() != GameStatus.PLAYING) {
            return;
        }
        if (this.totalTargets > 0 && this.boxesOnTargets == this.totalTargets) {
            setCurrentStatus(GameStatus.GAME_OVER_WIN);
        }
    }

    /** @return The current game board. */
    @Override
    public Object getBoardViewRepresentation() {
        return this.gameBoard;
    }

    /** @return The player's current row. */
    public int getPlayerRow() { return playerRow; }
    /** @return The player's current column. */
    public int getPlayerCol() { return playerCol; }
    /** @return The total number of targets in the current level. */
    public int getTotalTargets() { return totalTargets; }
    /** @return The current number of boxes on target locations. */
    public int getBoxesOnTargets() { return boxesOnTargets; }
}