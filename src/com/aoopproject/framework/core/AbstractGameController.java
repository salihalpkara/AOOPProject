package com.aoopproject.framework.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An abstract base class for game controllers in the framework.
 * The controller orchestrates the game flow, manages the relationship
 * between the {@link AbstractGameModel} and one or more {@link GameView}s,
 * and handles user input via an {@link InputStrategy}.
 * <p>
 * Concrete game implementations will typically provide a specific controller
 * that extends this class, possibly overriding methods to customize game setup
 * or the main game loop if necessary.
 */
public abstract class AbstractGameController {

    protected AbstractGameModel gameModel;
    protected List<GameView> gameViews;
    protected InputStrategy inputStrategy;

    /**
     * Constructs an AbstractGameController.
     * Initializes the list of views. The model, views, and input strategy
     * are typically set via setters or a {@link GameFactory}.
     */
    public AbstractGameController() {
        this.gameViews = new ArrayList<>();
    }

    /**
     * Sets the game model for this controller.
     *
     * @param model The {@link AbstractGameModel} to be controlled. Must not be null.
     */
    public void setGameModel(AbstractGameModel model) {
        this.gameModel = Objects.requireNonNull(model, "Game model cannot be null.");
    }

    /**
     * Adds a game view to this controller and registers it as an observer to the model.
     *
     * @param view The {@link GameView} to add. Must not be null.
     */
    public void addGameView(GameView view) {
        Objects.requireNonNull(view, "Game view cannot be null.");
        this.gameViews.add(view);
        if (this.gameModel != null) {
            this.gameModel.addObserver(view);
            view.initialize(this.gameModel);
        }
    }

    /**
     * Removes a game view from this controller and unregisters it from the model.
     * @param view The {@link GameView} to remove.
     */
    public void removeGameView(GameView view) {
        if (view != null) {
            this.gameViews.remove(view);
            if (this.gameModel != null) {
                this.gameModel.removeObserver(view);
            }
            view.dispose();
        }
    }

    /**
     * Sets the input strategy for this controller.
     *
     * @param strategy The {@link InputStrategy} to use for obtaining game actions. Must not be null.
     */
    public void setInputStrategy(InputStrategy strategy) {
        this.inputStrategy = Objects.requireNonNull(strategy, "Input strategy cannot be null.");
        System.out.println("Input strategy set in controller: " + (strategy != null ? strategy.getClass().getSimpleName() : "null"));

    }

    /**
     * Initializes the game. This typically involves initializing the model,
     * then initializing and showing the views.
     * Subclasses might override this to add game-specific setup steps.
     */
    public void initializeGame() {
        if (gameModel == null) {
            throw new IllegalStateException("Game model has not been set.");
        }
        gameModel.initializeGame();
        for (GameView view : new ArrayList<>(gameViews)) {
            view.initialize(this.gameModel);
            this.gameModel.addObserver(view);

            view.showView();
        }
    }

    /**
     * Starts the main game loop or prepares the game for playing.
     * The base implementation here is conceptual. Concrete controllers might
     * implement a more event-driven approach rather than a blocking loop.
     * This method should be called after {@link #initializeGame()}.
     */
    public void startGame() {
        if (gameModel == null || inputStrategy == null || gameViews.isEmpty()) {
            throw new IllegalStateException("Game model, input strategy, or views have not been properly set up.");
        }
        if (gameModel.getCurrentStatus() != GameStatus.PLAYING && gameModel.getCurrentStatus() != GameStatus.READY_TO_START) {
            gameModel.setCurrentStatus(GameStatus.PLAYING);
        }
        System.out.println("Game started. Controller is ready to process input.");
    }


    /**
     * Cleans up resources when the game is over or the controller is disposed.
     * This involves disposing of the views and the input strategy.
     */
    public void dispose() {
        if (inputStrategy != null) {
            inputStrategy.dispose();
        }
        for (GameView view : new ArrayList<>(gameViews)) {
            removeGameView(view);
        }
        gameViews.clear();
    }
    public AbstractGameModel getGameModel() {
        return this.gameModel;
    }

    /**
     * Processes a game action that has been submitted externally (e.g., by a UI event).
     * This method contains the core logic for validating and processing an action,
     * and then checking for end-game conditions.
     *
     * @param action The {@link GameAction} to process.
     */
    public void submitUserAction(GameAction action) {
        if (gameModel == null) {
            System.err.println("Cannot submit action: Game model is not set in controller.");
            return;
        }
        if (action == null) {
            System.err.println("Cannot submit action: Action is null.");
            return;
        }
        gameModel.processInputAction(action);
    }

    /**
     * This method remains for input strategies that poll or block for input,
     * like a console input or an AI player that computes its next move.
     * It retrieves an action from the current input strategy and then submits it.
     */
    public void processNextActionFromStrategy() {
        if (inputStrategy == null) {
            System.err.println("Cannot process next action: Input strategy is not set.");
            return;
        }
        if (gameModel != null && gameModel.getCurrentStatus() == GameStatus.PLAYING) {
            GameAction action = inputStrategy.solicitAction();
            if (action != null) {
                submitUserAction(action);
            }
        }
    }

}