package com.aoopproject.framework.core;

/**
 * Defines the contract for a game view in the MVC/Observer pattern.
 * A GameView is responsible for presenting the state of an {@link AbstractGameModel}
 * to the user. It implements {@link GameObserver} to receive updates
 * when the model changes.
 * <p>
 * Concrete views (e.g., Swing-based GUI, console output) will implement
 * this interface.
 */
public interface GameView extends GameObserver {

    /**
     * Initializes the view components and prepares it for display.
     * This method might be called by a controller or a game factory.
     * It can also be used to register the view with a model, though
     * explicit registration via {@code model.addObserver(this)} is also common.
     *
     * @param model The game model that this view will represent.
     * The view may store a reference to the model if needed for polling,
     * but primarily relies on {@link #onGameEvent(GameEvent)} for updates.
     */
    void initialize(AbstractGameModel model);

    /**
     * Makes the view visible or active. For a GUI, this might mean
     * calling {@code setVisible(true)} on a frame. For a console view,
     * it might trigger an initial rendering.
     */
    void showView();

    /**
     * Displays a generic message to the user via this view.
     * This could be used for status updates, error messages, or game notifications.
     *
     * @param message The message string to display.
     */
    void displayMessage(String message);

    /**
     * Cleans up any resources used by the view (e.g., closing windows,
     * releasing graphics resources).
     * Called when the view is no longer needed.
     */
    void dispose();
}