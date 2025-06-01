package com.aoopproject.common.action;

import com.aoopproject.framework.core.GameAction;

/**
 * A game action representing the user's intent to start a new game.
 */
public class NewGameAction implements GameAction {
    @Override
    public String getName() {
        return "NEW_GAME_ACTION";
    }
}