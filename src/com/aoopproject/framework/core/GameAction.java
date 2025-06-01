package com.aoopproject.framework.core;

/**
 * Represents an action performed by the player or system within the game.
 * This is a marker interface or a base for more specific action types.
 * Concrete games will define their own action classes that implement this
 * interface or extend a base GameAction class.
 * <p>
 * Examples:
 * <ul>
 * <li>{@code TileClickAction(row, col)} for SameGame</li>
 * <li>{@code SwipeAction(Direction.UP)} for 2048</li>
 * <li>{@code MovePlayerAction(Direction.LEFT)} for Sokoban</li>
 * </ul>
 * These actions are typically created by an {@link InputStrategy} and
 * processed by the {@link AbstractGameModel#processInputAction(GameAction)}.
 */
public interface GameAction {

    /**
     * A descriptive name for the action, useful for logging or debugging.
     * @return a string representing the type or name of the action.
     */
    String getName();
}