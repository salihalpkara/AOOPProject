package com.aoopproject.common.action;

import com.aoopproject.framework.core.GameAction;

/**
 * Represents an in-game action where the player requests a hint to determine
 * the next optimal move. This action is typically handled by the game's logic
 * to highlight or suggest a possible next step.
 */
public class HintRequestAction implements GameAction {
    @Override
    public String getName() {
        return "HINT_REQUEST_ACTION";
    }
}