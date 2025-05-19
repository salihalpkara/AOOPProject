package com.aoopproject.game.framework.model;

import com.aoopproject.game.framework.view.GameObserver; // We will create GameObserver in Phase 2
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for a game model in the framework.
 * Manages game state and notifies observers of state changes.
 */
public abstract class GameModel {
    private List<GameObserver> observers = new ArrayList<>();
    protected GameState currentState; // Hold the current state

    /**
     * Adds an observer to the model.
     *
     * @param observer The observer to add.
     */
    public void addObserver(GameObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            // Optionally, notify the new observer immediately with the current state
            if (currentState != null) {
                observer.update(currentState);
            }
        }
    }

    /**
     * Removes an observer from the model.
     *
     * @param observer The observer to remove.
     */
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers about a state change.
     * Subclasses should call this method whenever the game state is updated.
     *
     * @param state The new game state.
     */
    protected void notifyObservers(GameState state) {
        this.currentState = state; // Update the model's internal state
        for (GameObserver observer : observers) {
            observer.update(state);
        }
    }

    /**
     * Initializes or resets the game to its starting state.
     * This method must be implemented by concrete game models.
     */
    public abstract void initializeGame();

    /**
     * Returns the current game state.
     *
     * @return The current state of the game.
     */
    public GameState getCurrentState() {
        return currentState;
    }

    // Concrete game models will add abstract methods here for handling
    // specific game actions (e.g., move, click, etc.)
    // Example: public abstract void performAction(Object actionDetails);
    // We will refine this based on SameGame's needs first.
}