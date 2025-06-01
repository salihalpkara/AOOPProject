package com.aoopproject.games.sokoban.action;

/**
 * Represents the four cardinal directions for movement in a grid-based game like Sokoban.
 * Each direction has an associated delta change for row (dr) and column (dc).
 * Note on coordinate system:
 * - Increasing row index (dr > 0) typically means moving DOWN.
 * - Increasing column index (dc > 0) typically means moving RIGHT.
 */
public enum Direction {
    UP(-1, 0, "Up"),
    DOWN(1, 0, "Down"),
    LEFT(0, -1, "Left"),
    RIGHT(0, 1, "Right");

    private final int deltaRow;
    private final int deltaColumn;
    private final String displayName;

    /**
     * Constructs a Direction enum constant.
     * @param deltaRow The change in the row index when moving in this direction.
     * @param deltaColumn The change in the column index when moving in this direction.
     * @param displayName A user-friendly name for the direction.
     */
    Direction(int deltaRow, int deltaColumn, String displayName) {
        this.deltaRow = deltaRow;
        this.deltaColumn = deltaColumn;
        this.displayName = displayName;
    }

    /**
     * Gets the change in the row index associated with this direction.
     * @return The delta for the row.
     */
    public int getDeltaRow() {
        return deltaRow;
    }

    /**
     * Gets the change in the column index associated with this direction.
     * @return The delta for the column.
     */
    public int getDeltaColumn() {
        return deltaColumn;
    }

    /**
     * Gets the user-friendly display name of this direction.
     * @return The display name (e.g., "Up", "Down").
     */
    public String getDisplayName() {
        return displayName;
    }
}