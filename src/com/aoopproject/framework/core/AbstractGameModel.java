package com.aoopproject.framework.core;

import com.aoopproject.common.action.NewGameAction;
import com.aoopproject.common.action.QuitAction;
import com.aoopproject.common.action.UndoAction;
import com.aoopproject.common.score.ScoreEntry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * An abstract base class for game models in the AOOP Project framework.
 * It manages the fundamental aspects of a game, including:
 * <ul>
 * <li>The game board (as a {@link Grid} of {@link GameEntity}s).</li>
 * <li>The current score.</li>
 * <li>The current {@link GameStatus} (e.g., PLAYING, GAME_OVER_WIN).</li>
 * <li>A list of {@link GameObserver}s to be notified of {@link GameEvent}s.</li>
 * <li>A history stack ({@code Deque<Object>}) for implementing undo functionality.</li>
 * </ul>
 * This class provides a template for action processing by handling common framework
 * actions (New Game, Undo, Quit) in its final {@link #processInputAction(GameAction)} method
 * and delegating game-specific actions to the abstract {@link #processGameSpecificAction(GameAction)}
 * method, which must be implemented by concrete game models.
 * <p>
 * Concrete game implementations (e.g., {@code SameGameModel}, {@code SokobanModel})
 * must extend this class and implement all its abstract methods to define game-specific
 * logic for initialization, action processing, validation, win/loss conditions, undo/redo,
 * and view representations.
 * </p>
 */
public abstract class AbstractGameModel {

    /** The game board holding game entities. Must be initialized by subclasses. */
    protected Grid<? extends GameEntity> gameBoard;
    /** The current score of the game. */
    protected int score;
    /** The current status of the game (e.g., PLAYING, GAME_OVER_WIN). */
    protected GameStatus currentStatus;
    /** List of registered observers to be notified of game events. */
    private final List<GameObserver> observers;
    /** Stack to store game states for undo functionality. Subclasses manage the type of state objects. */
    protected final Deque<Object> historyStack;

    /**
     * Constructs an {@code AbstractGameModel}.
     * Initializes the list of observers, sets the initial score to 0,
     * the initial game status to {@link GameStatus#INITIALIZING},
     * and initializes the history stack for undo operations.
     */
    protected AbstractGameModel() {
        this.observers = new ArrayList<>();
        this.score = 0;
        this.currentStatus = GameStatus.INITIALIZING;
        this.historyStack = new ArrayDeque<>();
    }

    /**
     * Adds a {@link GameObserver} to the list of observers to be notified of game events.
     * The observer will not be added if it is {@code null} or already registered.
     *
     * @param observer The {@code GameObserver} to add.
     */
    public final void addObserver(GameObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /**
     * Removes a {@link GameObserver} from the list of observers.
     * If the observer is not in the list, this method does nothing.
     *
     * @param observer The {@code GameObserver} to remove.
     */
    public final void removeObserver(GameObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Notifies all registered {@link GameObserver}s about a specific {@link GameEvent}.
     * Iterates over a copy of the observer list to allow observers to unregister themselves
     * during event notification without causing a {@code ConcurrentModificationException}.
     *
     * @param event The {@link GameEvent} to propagate to observers. Must not be {@code null}.
     * @throws NullPointerException if the event is {@code null}.
     */
    protected final void notifyObservers(GameEvent event) {
        Objects.requireNonNull(event, "GameEvent cannot be null when notifying observers.");
        for (GameObserver observer : new ArrayList<>(observers)) {
            observer.onGameEvent(event);
        }
    }

    /**
     * Gets the current score of the game.
     *
     * @return The current integer score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the current score of the game.
     * If the new score is different from the current score, it updates the score
     * and notifies all registered observers with a "SCORE_UPDATED" {@link GameEvent}.
     *
     * @param newScore The new score to set.
     */
    protected void setScore(int newScore) {
        if (this.score != newScore) {
            this.score = newScore;
            notifyObservers(new GameEvent(this, "SCORE_UPDATED", this.score));
        }
    }

    /**
     * Gets the current status of the game.
     *
     * @return The current {@link GameStatus}.
     */
    public GameStatus getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Sets the current status of the game.
     * If the new status is different from the current status, it updates the status
     * and notifies all registered observers with a "STATUS_CHANGED" {@link GameEvent}.
     *
     * @param newStatus The new {@link GameStatus} to set. Must not be {@code null}.
     * @throws NullPointerException if newStatus is {@code null}.
     */
    protected void setCurrentStatus(GameStatus newStatus) {
        Objects.requireNonNull(newStatus, "GameStatus cannot be set to null.");
        if (this.currentStatus != newStatus) {
            this.currentStatus = newStatus;
            notifyObservers(new GameEvent(this, "STATUS_CHANGED", this.currentStatus));
        }
    }

    /**
     * Retrieves the current game board.
     * The board might be {@code null} if the game has not been initialized yet
     * via {@link #initializeGame()}.
     *
     * @return The {@link Grid} representing the game board, or {@code null} if not initialized.
     */
    public Grid<? extends GameEntity> getGameBoard() {
        return gameBoard;
    }

    /**
     * Called to indicate that a new high score has been officially achieved and recorded.
     * The model can then fire an appropriate event (e.g., "NEW_HIGH_SCORE_ACHIEVED")
     * for observers like a sound manager or other UI components to react to.
     *
     * @param scoreDetails Optional: Details of the high score, such as the {@link ScoreEntry} object
     * or any relevant information. Can be null if the event is just a signal.
     */
    public void newHighScoreAchieved(Object scoreDetails) {
        System.out.println("AbstractGameModel: New high score reported by view. Firing NEW_HIGH_SCORE_ACHIEVED event.");
        notifyObservers(new GameEvent(this, "NEW_HIGH_SCORE_ACHIEVED", scoreDetails));
    }

    /**
     * Central dispatcher for processing all game actions.
     * This method handles common framework-level actions such as starting a new game
     * ({@link NewGameAction}), undoing the last move ({@link UndoAction}),
     * and quitting the game ({@link QuitAction}).
     * <p>
     * For actions specific to the concrete game implementation, this method delegates
     * to the {@link #processGameSpecificAction(GameAction)} method, but only if the
     * game is currently in the {@link GameStatus#PLAYING} state.
     * </p>
     * This method is {@code final} to ensure a consistent action processing pipeline
     * across all game models.
     *
     * @param action The {@link GameAction} to process. If {@code null}, an error is logged.
     */
    public final void processInputAction(GameAction action) {
        if (action == null) {
            System.err.println("AbstractGameModel: Received a null action. Action ignored.");
            return;
        }
        if (action instanceof NewGameAction) {
            System.out.println("AbstractGameModel: Processing NewGameAction - initializing game.");
            initializeGame();
            return;
        }

        if (action instanceof UndoAction) {
            System.out.println("AbstractGameModel: Processing UndoAction.");
            if (canUndo()) {
                undoLastMove();
            } else {
                System.out.println("AbstractGameModel: Cannot undo - no history or action not permitted.");
                notifyObservers(new GameEvent(this, "UNDO_FAILED", "No history or undo not allowed."));
            }
            return;
        }

        if (action instanceof QuitAction) {
            System.out.println("AbstractGameModel: Processing QuitAction - setting status to GAME_ENDED_USER_QUIT.");
            setCurrentStatus(GameStatus.GAME_ENDED_USER_QUIT);
            notifyObservers(new GameEvent(this, "GAME_ENDED_BY_USER_QUIT_EVENT", null));
            return;
        }
        if (getCurrentStatus() != GameStatus.PLAYING) {
            System.out.println("AbstractGameModel: Action '" + action.getName() +
                    "' ignored as game is not in PLAYING state. Current status: " + getCurrentStatus());
            return;
        }
        System.out.println("AbstractGameModel: Delegating action '" + action.getName() + "' to processGameSpecificAction.");
        processGameSpecificAction(action);
    }

    /**
     * Initializes or resets the game to its starting state specific to the concrete game.
     * This typically involves setting up the game board ({@link #gameBoard}),
     * resetting the score ({@link #score}), setting the {@link #currentStatus}
     * (usually to {@link GameStatus#PLAYING}), clearing any history ({@link #historyStack}),
     * and notifying observers with appropriate events like "BOARD_INITIALIZED" and "NEW_GAME_STARTED".
     */
    public abstract void initializeGame();

    /**
     * Processes game-specific actions when the game is in the {@link GameStatus#PLAYING} state.
     * Concrete game models (e.g., {@code SameGameModel}, {@code SokobanModel}) must implement this
     * method to define how actions unique to that game (like tile selections, character movements,
     * or hint requests) affect the game state.
     * <p>
     * Implementations should:
     * <ul>
     * <li>Validate the game-specific action.</li>
     * <li>If valid, update the game state (board, score, player position, etc.).</li>
     * <li>Push previous state to {@link #historyStack} if the action is undoable.</li>
     * <li>Notify observers of relevant changes (e.g., "BOARD_CHANGED", "SCORE_UPDATED", game-specific events).</li>
     * <li>Call {@link #checkEndGameConditions()} if the action could lead to game end.</li>
     * <li>If the action is invalid by game rules (but passed initial type checks), notify observers
     * with an appropriate event (e.g., "INVALID_MOVE", "INVALID_SELECTION").</li>
     * </ul>
     * </p>
     *
     * @param action The game-specific {@link GameAction} to process.
     */
    protected abstract void processGameSpecificAction(GameAction action);


    /**
     * Checks if a given {@link GameAction} is currently valid according to the game's rules and state.
     * This method can be used by UI components to enable/disable controls, or by AI
     * to evaluate potential moves. Concrete game models must implement this to reflect
     * their specific validation logic for different actions.
     *
     * @param action The {@link GameAction} to validate.
     * @return {@code true} if the action is currently valid, {@code false} otherwise.
     */
    public abstract boolean isValidAction(GameAction action);

    /**
     * Checks for conditions that signify the end of the game (e.g., win or loss).
     * This method is typically called by concrete models after an action that changes the game state.
     * If an end-game condition is met, this method should update the {@link #currentStatus}
     * (e.g., to {@link GameStatus#GAME_OVER_WIN} or {@link GameStatus#GAME_OVER_LOSE})
     * which will, in turn, notify observers.
     */
    protected abstract void checkEndGameConditions();

    /**
     * Reverts the game state to before the last successfully processed and undoable move.
     * Concrete game models must implement this by popping a saved state from the
     * {@link #historyStack} and restoring all relevant game attributes (board, score,
     * player position, specific game counters, etc.).
     * After restoring, it should set the status (usually to {@link GameStatus#PLAYING})
     * and notify observers of changes ("BOARD_CHANGED", "UNDO_PERFORMED", etc.).
     */
    public abstract void undoLastMove();

    /**
     * Checks if an undo operation can currently be performed.
     * Typically, this means checking if the {@link #historyStack} is not empty.
     *
     * @return {@code true} if an undo operation is possible, {@code false} otherwise.
     */
    public abstract boolean canUndo();
}