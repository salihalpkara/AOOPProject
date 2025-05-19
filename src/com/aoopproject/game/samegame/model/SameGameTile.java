package com.aoopproject.game.samegame.model;

import com.aoopproject.game.framework.model.GameTile;

/**
 * Represents a single tile in the SameGame, extending the generic GameTile.
 */
public class SameGameTile implements GameTile {
    private final TileColor color;

    /**
     * Constructs a SameGame tile with a specific color.
     *
     * @param color The color of the tile.
     */
    public SameGameTile(TileColor color) {
        this.color = color;
    }

    /**
     * Returns the color of the SameGame tile.
     *
     * @return The color of the tile.
     */
    @Override
    public Object getTileType() {
        return this.color;
    }

    /**
     * Returns the color specifically as a TileColor enum.
     *
     * @return The tile color.
     */
    public TileColor getColor() {
        return this.color;
    }

    // Optional: Override equals() and hashCode() if needed for comparisons based on color
    // Optional: Add toString() for debugging
}