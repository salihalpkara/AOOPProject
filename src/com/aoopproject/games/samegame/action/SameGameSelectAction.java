package com.aoopproject.games.samegame.action;

import com.aoopproject.framework.core.GameAction;

public class SameGameSelectAction implements GameAction {
    private final int row;
    private final int column;

    public SameGameSelectAction(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String getName() {
        return "SELECT_TILE (" + row + "," + column + ")";
    }
}