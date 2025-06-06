package com.aoopproject.framework.core;

/**
 * Defines a strategy for obtaining user input or game actions.
 * Different implementations can provide input from various sources
 * like mouse clicks, keyboard presses, network events, or even AI.
 * <p>
 * The {@link AbstractGameController} will typically use an instance of
 * an InputStrategy to get a {@link GameAction} to pass to the
 * {@link AbstractGameModel}.
 */
public interface InputStrategy {

    /**
     * Initializes the input strategy. This can be used to set up listeners
     * or resources. For example, a Swing-based input strategy might
     * register mouse listeners to a specific component.
     *
     * @param gameView The primary game view, which might be needed for context
     * (e.g., to attach listeners to GUI components). Can be null
     * if the strategy does not depend on a view.
     */
    void initialize(GameView gameView);

    /**
     * Cleans up any resources used by the input strategy.
     * Called when the strategy is no longer needed or when the game is shutting down.
     */
    void dispose();
}