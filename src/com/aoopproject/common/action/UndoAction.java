package com.aoopproject.common.action;

import com.aoopproject.framework.core.GameAction;

/**
 * A game action representing the user's intent to undo the last move.
 */
public class UndoAction implements GameAction {
    @Override
    public String getName() {
        return "UNDO_ACTION";
    }
}