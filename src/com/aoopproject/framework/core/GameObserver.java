package com.aoopproject.framework.core;

/**
 * Defines the contract for an observer that wishes to be notified
 * of changes in a {@link AbstractGameModel}.
 * Views (like {@link GameView}) or other components (e.g., a sound manager)
 * can implement this interface to react to game events.
 */
public interface GameObserver {
    /**
     * This method is called by an observable (e.g., {@link AbstractGameModel})
     * when a game event occurs that the observer might be interested in.
     *
     * @param event The {@link GameEvent} object containing details about the event.
     * Observers can inspect the event type or its payload to decide
     * how to react.
     */
    void onGameEvent(GameEvent event);
}