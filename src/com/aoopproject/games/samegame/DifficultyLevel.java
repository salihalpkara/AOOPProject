package com.aoopproject.games.samegame;

/**
 * Represents the predefined difficulty levels for SameGame.
 * Each level has associated settings for rows, columns, and number of colors.
 */
public enum DifficultyLevel {
    EASY("Easy", 8, 12, 3),
    MEDIUM("Medium", 10, 15, 3),
    HARD("Hard", 12, 20, 4);

    private final String displayName;
    private final int rows;
    private final int cols;
    private final int numColors;

    DifficultyLevel(String displayName, int rows, int cols, int numColors) {
        this.displayName = displayName;
        this.rows = rows;
        this.cols = cols;
        this.numColors = numColors;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getNumColors() {
        return numColors;
    }

    @Override
    public String toString() {
        return displayName + " (" + rows + "x" + cols + ", " + numColors + " Colors)";
    }
}