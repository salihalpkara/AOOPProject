package com.aoopproject.framework.core;

/**
 * Represents a generic entity that can exist on the game's {@link Grid}.
 * Concrete game implementations (e.g., SameGame, 2048) will provide
 * specific classes that implement this interface to define their tiles,
 * pieces, or other interactive elements.
 * <p>
 * For example, a {@code Tile} in SameGame or a {@code NumberCell} in 2048
 * would implement this interface.
 */
public interface GameEntity {

    /**
     * Provides a representation of the entity that can be used by
     * a {@link GameView} to display it. This could be a character,
     * a color, an image path, or any other display-related information.
     * The exact nature of the returned object should be defined by
     * concrete implementations and understood by corresponding views.
     *
     * @return An object representing how this entity should be visualized.
     */
    Object getVisualRepresentation();

    /**
     * Indicates whether this entity represents an empty or non-interactive
     * space on the grid. Some games might use special "empty" entity objects
     * rather than nulls in the grid.
     *
     * @return {@code true} if the entity is considered empty or a placeholder,
     * {@code false} otherwise.
     */
    boolean isEmpty();
}