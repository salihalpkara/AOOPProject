package com.aoopproject.common.action;

import com.aoopproject.framework.core.GameAction;

/**
 * Represents a game action triggered when the user opts to start a new game session.
 * It signals the game controller to reset the current game state and initialize
 * a fresh game instance.
 */
public class NewGameAction implements GameAction {
    @Override
    public String getName() {
        return "NEW_GAME_ACTION";
    }
}