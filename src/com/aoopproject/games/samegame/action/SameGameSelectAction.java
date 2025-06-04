package com.aoopproject.games.samegame.action;

import com.aoopproject.framework.core.GameAction;

public record SameGameSelectAction(int row, int column) implements GameAction {

    @Override
    public String getName() {
        return "SELECT_TILE (" + row + "," + column + ")";
    }
}