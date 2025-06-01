package com.aoopproject.common.action;

import com.aoopproject.framework.core.GameAction;

/**
 * Represents a request from the user to get a hint for the next best move.
 */
public class HintRequestAction implements GameAction {
    @Override
    public String getName() {
        return "HINT_REQUEST_ACTION";
    }
}