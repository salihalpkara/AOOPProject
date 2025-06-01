package com.aoopproject.games.samegame;

import com.aoopproject.framework.core.GameEntity;
import java.awt.Color;

/**
 * Represents a single tile in the SameGame.
 * Each tile has a color and can be considered empty if removed.
 */
public class SameGameTile implements GameEntity {

    private final Color color;
    private boolean empty;

    /**
     * Creates a new tile with a specific color.
     * Initially, the tile is not empty.
     *
     * @param color The color of the tile. Cannot be null.
     */
    public SameGameTile(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Tile color cannot be null.");
        }
        this.color = color;
        this.empty = false;
    }

    /**
     * Gets the color of this tile.
     *
     * @return The tile's color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Marks this tile as empty (removed).
     */
    public void setEmpty() {
        this.empty = true;
    }

    @Override
    public boolean isEmpty() {
        return this.empty;
    }

    /**
     * The visual representation for a SameGameTile is its color.
     * Views can use this color to render the tile.
     * If the tile is empty, it might return null or a special placeholder.
     *
     * @return The {@link Color} of the tile, or null if empty (or a default empty color).
     */
    @Override
    public Object getVisualRepresentation() {
        return isEmpty() ? null : this.color;
    }

    @Override
    public String toString() {
        if (empty) {
            return "[ ]";
        }
        if (Color.RED.equals(color)) return "[R]";
        if (Color.GREEN.equals(color)) return "[G]";
        if (Color.BLUE.equals(color)) return "[B]";
        if (Color.YELLOW.equals(color)) return "[Y]";
        if (Color.ORANGE.equals(color)) return "[O]";
        if (Color.CYAN.equals(color)) return "[C]";
        if (Color.MAGENTA.equals(color)) return "[M]";
        return "[?]";
    }
    /**
     * Creates a copy of this tile.
     *
     * @return A new SameGameTile instance with the same color and empty state.
     */
    public SameGameTile copy() {
        SameGameTile newTile = new SameGameTile(this.color);
        if (this.empty) {
            newTile.setEmpty();
        }
        return newTile;
    }
}