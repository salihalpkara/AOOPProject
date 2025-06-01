package com.aoopproject.common.action;

import com.aoopproject.framework.core.GameAction;

/**
 * A game action that signals the player's intention to exit the current game session.
 * Depending on the implementation, it may prompt the user to confirm or save progress.
 */
public class QuitAction implements GameAction {
    @Override
    public String getName() {
        return "QUIT_GAME_ACTION";
    }
}