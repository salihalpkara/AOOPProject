package com.aoopproject.games.sokoban.model;

import com.aoopproject.framework.core.GameEntity;

/**
 * Represents a single tile on the Sokoban game board.
 * It implements {@link GameEntity} and holds information about its base type (wall, floor, target)
 * and any occupant (player, box, or none). It determines its visual representation based on this state,
 * loading images from the "/sokoban/images/" resource path.
 */
public class SokobanTile implements GameEntity {

    private final SokobanBaseType baseType;
    private SokobanOccupant occupant;
    public static final String IMG_WALL = "/sokoban/images/wall.png";
    public static final String IMG_FLOOR = "/sokoban/images/blank.png";
    public static final String IMG_TARGET = "/sokoban/images/blankmarked.png";
    public static final String IMG_PLAYER = "/sokoban/images/player.png";
    public static final String IMG_BOX_ON_FLOOR = "/sokoban/images/crate.png";
    public static final String IMG_BOX_ON_TARGET = "/sokoban/images/cratemarked.png";

    /**
     * Constructs a SokobanTile with a specific base type and occupant.
     * @param baseType The static base type of the tile (WALL, FLOOR, TARGET).
     * @param occupant The initial occupant of the tile (PLAYER, BOX, NONE).
     */
    public SokobanTile(SokobanBaseType baseType, SokobanOccupant occupant) {
        this.baseType = baseType;
        this.occupant = occupant;
    }

    /**
     * Gets the base type of this tile.
     * @return The {@link SokobanBaseType}.
     */
    public SokobanBaseType getBaseType() {
        return baseType;
    }

    /**
     * Gets the current occupant of this tile.
     * @return The {@link SokobanOccupant}.
     */
    public SokobanOccupant getOccupant() {
        return occupant;
    }

    /**
     * Sets the occupant of this tile. Used when entities move.
     * Prevents placing occupants on WALL tiles after construction.
     * @param occupant The new occupant.
     */
    public void setOccupant(SokobanOccupant occupant) {
        if (this.baseType == SokobanBaseType.WALL && occupant != SokobanOccupant.NONE) {
            System.err.println("Warning: Attempted to place an occupant on a WALL tile.");
            return;
        }
        this.occupant = occupant;
    }

    /**
     * Determines the visual representation (image path string) of this tile
     * based on its base type and current occupant.
     *
     * @return A string representing the path to the image resource.
     */
    @Override
    public Object getVisualRepresentation() {
        if (baseType == SokobanBaseType.WALL) {
            return IMG_WALL;
        }
        if (occupant == SokobanOccupant.PLAYER) {
            return IMG_PLAYER;
        }
        if (occupant == SokobanOccupant.BOX) {
            return (baseType == SokobanBaseType.TARGET) ? IMG_BOX_ON_TARGET : IMG_BOX_ON_FLOOR;
        }
        if (baseType == SokobanBaseType.TARGET) {
            return IMG_TARGET;
        }
        return IMG_FLOOR;
    }

    /**
     * A tile is considered "empty" in terms of its base structure if it's not a wall.
     * Actual movability onto the tile depends on occupants and is handled by game logic
     * using methods like {@link #isPlayerWalkable()} or {@link #isBoxPushTarget()}.
     *
     * @return {@code true} if the tile's base type is not WALL, {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        return baseType != SokobanBaseType.WALL;
    }

    /**
     * Checks if a box can be pushed onto this tile.
     * A box can be pushed onto a FLOOR or a TARGET tile if it's currently not occupied by another box or player.
     * @return true if a box can be pushed here, false otherwise.
     */
    public boolean isBoxPushTarget() {
        return (baseType == SokobanBaseType.FLOOR || baseType == SokobanBaseType.TARGET) &&
                occupant == SokobanOccupant.NONE;
    }

    /**
     * Checks if the player can walk onto this tile.
     * Player can walk onto a FLOOR or a TARGET tile if it's not currently occupied by a BOX.
     * (Player occupying a tile means the player *is* there, so this check is for moving *to* a tile).
     * @return true if the player can walk here, false otherwise.
     */
    public boolean isPlayerWalkable() {
        return (baseType == SokobanBaseType.FLOOR || baseType == SokobanBaseType.TARGET) &&
                occupant != SokobanOccupant.BOX;
    }

    /**
     * Creates a copy of this SokobanTile with the same base type and current occupant.
     * Useful for state saving mechanisms like an undo history or for predictive checks.
     * @return A new {@code SokobanTile} instance with the same properties.
     */
    public SokobanTile copy() {
        return new SokobanTile(this.baseType, this.occupant);
    }

    /**
     * Provides a character representation for console view or debugging.
     * 'W': Wall, ' ': Floor, '.': Target,
     * 'P': Player on Floor, '@': Player on Target (Player image covers target),
     * 'B': Box on Floor, 'X': Box on Target.
     * @return Character representing the tile state.
     */
    @Override
    public String toString() {
        if (baseType == SokobanBaseType.WALL) return "W";
        if (occupant == SokobanOccupant.PLAYER) return (baseType == SokobanBaseType.TARGET) ? "@" : "P";
        if (occupant == SokobanOccupant.BOX) return (baseType == SokobanBaseType.TARGET) ? "X" : "B";
        if (baseType == SokobanBaseType.TARGET) return ".";
        return " ";
    }
}