package com.aoopproject.game.framework.model;

/**
 * Interface representing a generic tile in a tile-based game.
 * Concrete games should implement this interface for their specific tile types.
 */
public interface GameTile {
    /**
     * Returns the type of the tile. This could be a color, a number, etc.,
     * depending on the specific game.
     *
     * @return An object representing the tile's type.
     */
    Object getTileType();

}