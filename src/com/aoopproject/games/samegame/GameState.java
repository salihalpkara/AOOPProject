package com.aoopproject.games.samegame;

import com.aoopproject.framework.core.Grid;

/**
 * Represents a snapshot of the game's state, including the grid configuration
 * and the current score. Used for the undo functionality.
 *
 * @param boardState A deep copy of the game grid at a certain point.
 * @param scoreState The score at that point.
 */
public record GameState(Grid<SameGameTile> boardState, int scoreState) {
}