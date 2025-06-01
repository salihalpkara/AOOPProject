package com.aoopproject.common.action;

import com.aoopproject.framework.core.GameAction;

/**
 * A game action that allows the player to revert the last performed move.
 * This action is subject to game-specific rules and limitations regarding undo functionality.
 */
public class UndoAction implements GameAction {
    @Override
    public String getName() {
        return "UNDO_ACTION";
    }
}