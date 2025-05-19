package com.aoopproject.game.samegame.model;

import com.aoopproject.game.framework.model.GameModel;
import com.aoopproject.game.framework.model.GameTile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack; // For group finding

/**
 * Represents the concrete game model for SameGame, extending the framework's GameModel.
 * Contains the game logic for generating the board, handling clicks, removing tiles,
 * collapsing the grid, calculating score, and checking game over conditions.
 */
public class SameGameModel extends GameModel {
    private int rows;
    private int cols;
    private int numberOfColors;
    private Random random;

    // The actual mutable grid state used internally by the model
    private SameGameTile[][] internalGrid;
    private int currentScore;


    /**
     * Constructs a new SameGame model with default settings.
     * Use initializeGame to set dimensions and colors.
     */
    public SameGameModel() {
        this.random = new Random();
        // Initial state is null until initializeGame is called
        this.currentState = null;
        this.currentScore = 0;
    }

    /**
     * Initializes or resets the SameGame board.
     *
     * @param rows The number of rows for the grid.
     * @param cols The number of columns for the grid.
     * @param numberOfColors The number of different tile colors to use.
     */
    public void initializeGame(int rows, int cols, int numberOfColors) {
        if (rows <= 0 || cols <= 0 || numberOfColors <= 0 || numberOfColors > TileColor.values().length) {
            throw new IllegalArgumentException("Invalid dimensions or number of colors.");
        }
        this.rows = rows;
        this.cols = cols;
        this.numberOfColors = numberOfColors;
        this.internalGrid = new SameGameTile[rows][cols];
        this.currentScore = 0;
        this.random = new Random(); // Reset random seed potentially for consistent testing if needed

        fillBoard(); // Populate the board with random tiles

        // Create the initial game state and notify observers
        updateGameStateAndNotify();
    }

    @Override
    public void initializeGame() {
        // Default initialization if no parameters are given
        initializeGame(15, 10, 5); // Example: 15 rows, 10 columns, 5 colors
    }


    /**
     * Fills the internal grid with random tiles based on the configured number of colors.
     */
    private void fillBoard() {
        TileColor[] colors = TileColor.values();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                TileColor randomColor = colors[random.nextInt(numberOfColors)];
                internalGrid[r][c] = new SameGameTile(randomColor);
            }
        }
    }

    /**
     * Handles a click event at the specified row and column.
     * Finds the connected group of tiles, removes them, updates the grid,
     * calculates score, and checks for game over.
     *
     * @param row The row index of the clicked tile.
     * @param col The column index of the clicked tile.
     */
    public void handleTileClick(int row, int col) {
        // Basic validation
        if (row < 0 || row >= rows || col < 0 || col >= cols || internalGrid[row][col] == null) {
            // Ignore clicks outside bounds or on empty cells
            return;
        }

        SameGameTile clickedTile = internalGrid[row][col];
        if (clickedTile == null) return; // Should be caught by validation, but double check

        // 1. Find the connected group
        List<int[]> connectedGroup = findConnectedGroup(row, col, clickedTile.getColor());

        // 2. Check if the group is large enough to be removed
        if (connectedGroup.size() >= 2) {
            // 3. Remove tiles
            removeTiles(connectedGroup);

            // 4. Collapse grid (shift tiles down, remove empty columns)
            collapseGrid();

            // 5. Calculate and update score
            updateScore(connectedGroup.size());

            // 6. Update game state and notify observers
            updateGameStateAndNotify();
        } else {
            // Optionally notify observers that the click didn't result in a change
            // or provide feedback that the group was too small.
            // For now, do nothing if group < 2.
        }
    }

    /**
     * Finds all connected tiles of the same color starting from a given tile.
     * Uses a Breadth-First Search (BFS) or Depth-First Search (DFS) algorithm.
     *
     * @param startRow The starting row for the search.
     * @param startCol The starting column for the search.
     * @param targetColor The color to search for in the connected group.
     * @return A list of [row, col] arrays representing the coordinates of the connected group.
     */
    private List<int[]> findConnectedGroup(int startRow, int startCol, TileColor targetColor) {
        List<int[]> group = new ArrayList<>();
        if (internalGrid[startRow][startCol] == null || internalGrid[startRow][startCol].getColor() != targetColor) {
            return group; // No tile or wrong color at start
        }

        boolean[][] visited = new boolean[rows][cols];
        Stack<int[]> stack = new Stack<>(); // Using Stack for DFS

        stack.push(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;
        group.add(new int[]{startRow, startCol});

        int[] dr = {-1, 1, 0, 0}; // Up, Down
        int[] dc = {0, 0, -1, 1}; // Left, Right

        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int r = current[0];
            int c = current[1];

            // Check neighbors
            for (int i = 0; i < 4; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];

                // Check bounds, not visited, and same color
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc] &&
                        internalGrid[nr][nc] != null && internalGrid[nr][nc].getColor() == targetColor) {

                    visited[nr][nc] = true;
                    stack.push(new int[]{nr, nc});
                    group.add(new int[]{nr, nc});
                }
            }
        }

        return group;
    }

    /**
     * Removes the tiles at the given coordinates from the internal grid.
     * Sets the corresponding grid cells to null.
     * @param tilesToRemove A list of [row, col] coordinates to remove.
     */
    private void removeTiles(List<int[]> tilesToRemove) {
        for (int[] pos : tilesToRemove) {
            internalGrid[pos[0]][pos[1]] = null;
        }
    }

    /**
     * Collapses the grid after tiles have been removed.
     * Tiles fall down to fill empty spaces, and empty columns are removed by shifting remaining columns left.
     */
    private void collapseGrid() {
        // 1. Collapse tiles downwards in each column
        for (int c = 0; c < cols; c++) {
            int emptyRow = rows - 1; // Start checking from bottom
            for (int r = rows - 1; r >= 0; r--) {
                if (internalGrid[r][c] != null) {
                    // If tile is not null and is above an empty spot, move it down
                    if (r != emptyRow) {
                        internalGrid[emptyRow][c] = internalGrid[r][c];
                        internalGrid[r][c] = null;
                    }
                    emptyRow--; // Move the empty spot marker up
                }
            }
        }

        // 2. Remove empty columns (shift columns left)
        int emptyCol = 0;
        for (int c = 0; c < cols; c++) {
            boolean isColumnEmpty = true;
            for(int r = 0; r < rows; r++) {
                if (internalGrid[r][c] != null) {
                    isColumnEmpty = false;
                    break;
                }
            }

            if (!isColumnEmpty) {
                // If column is not empty and is not in its final position, shift it left
                if (c != emptyCol) {
                    for(int r = 0; r < rows; r++) {
                        internalGrid[r][emptyCol] = internalGrid[r][c];
                        internalGrid[r][c] = null; // Clear the old column
                    }
                }
                emptyCol++; // Move the empty column marker right
            }
        }
        // After shifting, any columns from emptyCol to cols-1 are now truly empty.
        // No need to explicitly set them to null as shifting handles it,
        // but their content doesn't matter for the game state from emptyCol onwards.
    }


    /**
     * Updates the score based on the number of tiles removed in a group.
     * SameGame scoring rule: n * (n - 1) where n is the number of tiles.
     * @param groupSize The number of tiles removed in the group.
     */
    private void updateScore(int groupSize) {
        if (groupSize >= 2) {
            this.currentScore += groupSize * (groupSize - 1);
        }
    }

    /**
     * Checks if the game is over. The game is over if there are no more
     * groups of 2 or more adjacent tiles of the same color.
     *
     * @return true if the game is over, false otherwise.
     */
    public boolean isGameOver() {
        // Check every tile. If it's not null, try to find a connected group.
        // If any group found has size >= 2, the game is NOT over.
        boolean[][] visited = new boolean[rows][cols]; // Need a fresh visited array for check
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (internalGrid[r][c] != null && !visited[r][c]) {
                    // Found an unvisited tile, find its potential group
                    List<int[]> potentialGroup = findConnectedGroupCheck(r, c, internalGrid[r][c].getColor(), visited);
                    if (potentialGroup.size() >= 2) {
                        return false; // Found a valid move, game is not over
                    }
                }
            }
        }
        // If the loop finishes without finding any valid group, the game is over.
        return true;
    }

    /**
     * Helper method to find connected group during game over check.
     * It marks visited tiles to avoid redundant checks across the board.
     * Similar to findConnectedGroup but designed to work with a persistent visited array
     * across the whole board check.
     *
     * @param startRow The starting row.
     * @param startCol The starting column.
     * @param targetColor The color to look for.
     * @param visited A 2D boolean array tracking visited cells across the board check.
     * @return A list of coordinates in the group.
     */
    private List<int[]> findConnectedGroupCheck(int startRow, int startCol, TileColor targetColor, boolean[][] visited) {
        List<int[]> group = new ArrayList<>();
        if (startRow < 0 || startRow >= rows || startCol < 0 || startCol >= cols ||
                internalGrid[startRow][startCol] == null || internalGrid[startRow][startCol].getColor() != targetColor || visited[startRow][startCol]) {
            return group; // Invalid start, already visited, or wrong color
        }

        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;
        group.add(new int[]{startRow, startCol});

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int r = current[0];
            int c = current[1];

            for (int i = 0; i < 4; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];

                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc] &&
                        internalGrid[nr][nc] != null && internalGrid[nr][nc].getColor() == targetColor) {

                    visited[nr][nc] = true;
                    stack.push(new int[]{nr, nc});
                    group.add(new int[]{nr, nc});
                }
            }
        }
        return group;
    }


    /**
     * Creates a new immutable GameState object from the current internal state
     * and notifies all registered observers.
     */
    private void updateGameStateAndNotify() {
        boolean gameOver = isGameOver(); // Check game over status BEFORE creating state
        SameGameGameState newState = new SameGameGameState(this.internalGrid, this.currentScore, gameOver);
        // Call the protected method from the abstract base class GameModel
        notifyObservers(newState);

        // Optional: Handle final game over actions (e.g., show final score)
        if (gameOver) {
            System.out.println("Game Over! Final Score: " + this.currentScore); // For console view/debug
            // Future: Trigger high score saving, end game view etc.
        }
    }

    // Getter for board dimensions if needed by views (optional, state has grid)
    public int getRows() { return rows; }
    public int getCols() { return cols; }

}