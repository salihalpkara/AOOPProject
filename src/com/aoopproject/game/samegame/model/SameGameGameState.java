package com.aoopproject.game.samegame.model;

import com.aoopproject.game.framework.model.GameTile;
import com.aoopproject.game.framework.model.GameState;

/**
 * Represents the specific state of a SameGame game instance.
 * Implements the generic GameState interface.
 * Designed to be immutable.
 */
public class SameGameGameState implements GameState {
    private final SameGameTile[][] grid;
    private final int score;
    private final boolean isGameOver;
    private final int rows;
    private final int cols;

    /**
     * Constructs a new SameGame game state.
     *
     * @param grid The current state of the game grid. A defensive copy is made.
     * @param score The current score.
     * @param isGameOver The game over status.
     */
    public SameGameGameState(SameGameTile[][] grid, int score, boolean isGameOver) {
        if (grid == null) {
            throw new IllegalArgumentException("Grid cannot be null");
        }
        this.rows = grid.length;
        this.cols = (rows > 0) ? grid[0].length : 0;

        // Create a defensive copy of the grid to ensure immutability
        this.grid = new SameGameTile[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, this.grid[i], 0, cols);
        }

        this.score = score;
        this.isGameOver = isGameOver;
    }

    /**
     * Returns a copy of the current game grid.
     *
     * @return A 2D array of SameGameTile.
     */
    @Override
    public GameTile[][] getGrid() {
        // Return a copy to maintain immutability
        GameTile[][] gridCopy = new GameTile[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(this.grid[i], 0, gridCopy[i], 0, cols);
        }
        return gridCopy;
    }

    /**
     * Returns the SameGame specific grid.
     * @return A 2D array of SameGameTile.
     */
    public SameGameTile[][] getSameGameGrid() {
        // Return a copy to maintain immutability
        SameGameTile[][] gridCopy = new SameGameTile[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(this.grid[i], 0, gridCopy[i], 0, cols);
        }
        return gridCopy;
    }


    /**
     * Returns the current score.
     *
     * @return The score.
     */
    @Override
    public int getScore() {
        return score;
    }

    /**
     * Checks if the game is over.
     *
     * @return true if the game is over, false otherwise.
     */
    @Override
    public boolean isGameOver() {
        return isGameOver;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    // Optional: Override equals(), hashCode(), toString()
}