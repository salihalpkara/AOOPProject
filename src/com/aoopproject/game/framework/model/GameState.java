package com.aoopproject.game.framework.model;

/**
 * Interface representing the current state of a game.
 * Implementations should provide access to the game's grid, score, and game status.
 * State objects passed to observers should ideally be immutable.
 */
public interface GameState {
    /**
     * Returns the game grid. The grid contains GameTile objects.
     *
     * @return A 2D array of GameTile representing the game board.
     */
    GameTile[][] getGrid();

    /**
     * Returns the current score of the game.
     *
     * @return The current score.
     */
    int getScore();

    /**
     * Checks if the game is currently over.
     *
     * @return true if the game is over, false otherwise.
     */
    boolean isGameOver();

    // Add other common state properties as needed later (e.g., difficulty level)
}