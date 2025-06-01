package com.aoopproject.common.action;

import com.aoopproject.framework.core.GameAction;

/**
 * A generic game action representing the user's intent to quit the current game.
 */
public class QuitAction implements GameAction {
    @Override
    public String getName() {
        return "QUIT_GAME_ACTION";
    }
}