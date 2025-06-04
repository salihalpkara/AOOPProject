package com.aoopproject.games.sokoban.action;

import com.aoopproject.framework.core.GameAction;

/**
 * Represents a game action where the player attempts to move in a specific direction
 * in the Sokoban game. This action encapsulates the intended {@link Direction} of movement.
 */
public record SokobanMoveAction(Direction direction) implements GameAction {

    /**
     * Constructs a new SokobanMoveAction for a given direction.
     *
     * @param direction The {@link Direction} in which the player intends to move.
     *                  Cannot be null.
     * @throws IllegalArgumentException if direction is null.
     */
    public SokobanMoveAction {
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null for SokobanMoveAction.");
        }
    }

    /**
     * Gets the direction of this move action.
     *
     * @return The {@link Direction} of the move.
     */
    @Override
    public Direction direction() {
        return direction;
    }

    /**
     * Gets the name of this action, typically used for logging or debugging.
     * It includes the direction of the move.
     *
     * @return A string representation of the action, e.g., "SOKOBAN_MOVE_UP".
     */
    @Override
    public String getName() {
        return "SOKOBAN_MOVE_" + direction.name();
    }

    @Override
    public String toString() {
        return getName();
    }
}