package com.aoopproject.framework.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * An abstract base class for game models in the framework.
 * It manages the game's state (like score and {@link GameStatus}),
 * a list of {@link GameObserver}s, and provides core functionalities
 * for notifying observers about {@link GameEvent}s.
 * <p>
 * Concrete game implementations (e.g., {@code SameGameModel}, {@code TwentyFortyEightModel})
 * must extend this class and implement its abstract methods to define
 * game-specific logic such as board initialization, action processing,
 * and win/loss conditions.
 */
public abstract class AbstractGameModel {

    protected Grid<? extends GameEntity> gameBoard;
    protected int score;
    protected GameStatus currentStatus;
    private final List<GameObserver> observers;
    protected final Deque<Object> historyStack;

    /**
     * Constructs an AbstractGameModel, initializing the observer list
     * and setting the initial game status to INITIALIZING.
     */
    protected AbstractGameModel() {
        this.observers = new ArrayList<>();
        this.score = 0;
        this.currentStatus = GameStatus.INITIALIZING;
        this.historyStack = new ArrayDeque<>();
    }

    /**
     * Adds a {@link GameObserver} to be notified of game events.
     * If the observer is null or already registered, it will not be added again.
     *
     * @param observer The observer to add.
     */
    public final void addObserver(GameObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /**
     * Removes a {@link GameObserver} from the list of observers.
     *
     * @param observer The observer to remove.
     */
    public final void removeObserver(GameObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Notifies all registered observers about a specific game event.
     *
     * @param event The {@link GameEvent} to propagate to observers.
     * Must not be null.
     */
    protected final void notifyObservers(GameEvent event) {
        Objects.requireNonNull(event, "GameEvent cannot be null");
        for (GameObserver observer : new ArrayList<>(observers)) {
            observer.onGameEvent(event);
        }
    }

    /**
     * Gets the current score of the game.
     *
     * @return The current score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the current score and notifies observers with a "SCORE_UPDATED" event.
     * @param newScore The new score.
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
     * Sets the current game status and notifies observers with a "STATUS_CHANGED" event.
     *
     * @param newStatus The new {@link GameStatus}. Must not be null.
     */
    protected void setCurrentStatus(GameStatus newStatus) {
        Objects.requireNonNull(newStatus, "New status cannot be null");
        if (this.currentStatus != newStatus) {
            this.currentStatus = newStatus;
            notifyObservers(new GameEvent(this, "STATUS_CHANGED", this.currentStatus));
        }
    }

    /**
     * Retrieves the current game board.
     * The board might be null if the game has not been initialized yet.
     *
     * @return The {@link Grid} representing the game board.
     */
    public Grid<? extends GameEntity> getGameBoard() {
        return gameBoard;
    }

    /**
     * Initializes the game board and resets the game state (score, status, etc.).
     * This method should set up the initial configuration of the game.
     * After initialization, the status should typically be set to PLAYING or READY_TO_START.
     * It should also notify observers that the board has changed.
     */
    public abstract void initializeGame();

    /**
     * Processes a given {@link GameAction} taken by the player or system.
     * This method contains the core logic for how an action affects the
     * game state (e.g., moving a piece, selecting a tile).
     * It should update the game board, score, and potentially the game status.
     * Observers should be notified of relevant changes (e.g., "BOARD_CHANGED", "SCORE_UPDATED").
     *
     * @param action The {@link GameAction} to process. Must not be null.
     */
    public abstract void processInputAction(GameAction action);

    /**
     * Checks if the given action is valid in the current game state.
     * This can be used by controllers or views to enable/disable input
     * or provide feedback to the player.
     *
     * @param action The {@link GameAction} to validate.
     * @return {@code true} if the action is valid, {@code false} otherwise.
     */
    public abstract boolean isValidAction(GameAction action);

    /**
     * Checks for win/loss conditions and updates the {@link GameStatus} accordingly.
     * This method is typically called after each action or game state change.
     * If the game ends, it should set the status to GAME_OVER_WIN or GAME_OVER_LOSE
     * and notify observers.
     */
    protected abstract void checkEndGameConditions();

    /**
     * Provides a representation of the game board suitable for views.
     * This allows different views to render the board without needing to
     * know the intimate details of the {@link GameEntity} objects themselves,
     * if the default {@code getVisualRepresentation()} on entities is not sufficient
     * or if a more complex view model is needed.
     * <p>
     * For many games, views can directly iterate over {@code getGameBoard().getEntity(r,c).getVisualRepresentation()}.
     * This method offers an alternative or supplementary way.
     *
     * @return An object (e.g., a 2D array of Colors, Strings, or custom view model objects)
     * representing the current state of the board for display purposes.
     * The exact type is determined by the concrete game model and its views.
     */
    public abstract Object getBoardViewRepresentation();
    /**
     * Undoes the last move made in the game.
     * Implementations should restore the game state (board, score, etc.)
     * from the history.
     */
    public abstract void undoLastMove();

    /**
     * Checks if an undo operation is currently possible.
     *
     * @return {@code true} if an undo can be performed, {@code false} otherwise.
     */
    public abstract boolean canUndo();
}