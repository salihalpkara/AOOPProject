package com.aoopproject.games.samegame;

import com.aoopproject.framework.core.AbstractGameModel;
import com.aoopproject.framework.core.GameEvent;
import com.aoopproject.framework.core.GameView;
import com.aoopproject.framework.core.Grid;

/**
 * A console-based view for the SameGame.
 * It renders the game board and score to the standard output.
 */
public class SameGameViewConsole implements GameView {

    private AbstractGameModel model;

    private boolean viewInitialized = false;

    @Override
    public void initialize(AbstractGameModel model) {
        if (viewInitialized) {
            return;
        }
        this.model = model;
        System.out.println("SameGame Console View Initialized.");
        this.viewInitialized = true;
    }

    @Override
    public void onGameEvent(GameEvent event) {
        System.out.println("ConsoleView received event: " + event.getType() + " - Payload: " + event.getPayload());
        switch (event.getType()) {
            case "BOARD_INITIALIZED":
            case "BOARD_CHANGED":
            case "SCORE_UPDATED":
            case "STATUS_CHANGED":
                render();
                break;
            case "INVALID_SELECTION":
                if (event.getPayload() instanceof String) {
                    displayMessage((String) event.getPayload());
                }
                break;
        }

        if (model.getCurrentStatus().toString().startsWith("GAME_OVER")) {
            displayMessage("Game Over! Final Score: " + model.getScore());
            if(model.getCurrentStatus() == com.aoopproject.framework.core.GameStatus.GAME_OVER_WIN) {
                displayMessage("Congratulations, you cleared the board!");
            } else if (model.getCurrentStatus() == com.aoopproject.framework.core.GameStatus.GAME_OVER_LOSE) {
                displayMessage("No more moves possible.");
            }
        }
    }

    private void render() {
        if (model == null || model.getGameBoard() == null) {
            System.out.println("Model or game board not yet available for rendering.");
            return;
        }

        Grid<SameGameTile> board = (Grid<SameGameTile>) model.getGameBoard();
        System.out.print("   ");
        for (int c = 0; c < board.getColumns(); c++) {
            System.out.printf("%-3s", c);
        }
        System.out.println();
        System.out.print("  +");
        for (int c = 0; c < board.getColumns(); c++) {
            System.out.print("---");
        }
        System.out.println("+");
        for (int r = 0; r < board.getRows(); r++) {
            System.out.printf("%2d| ", r);
            for (int c = 0; c < board.getColumns(); c++) {
                SameGameTile tile = board.getEntity(r, c);
                if (tile != null) {
                    System.out.print(tile);
                } else {
                    System.out.print("[ ]");
                }
            }
            System.out.println(" |");
        }
        System.out.print("  +");
        for (int c = 0; c < board.getColumns(); c++) {
            System.out.print("---");
        }
        System.out.println("+");
        System.out.println("Score: " + model.getScore());
        System.out.println("Status: " + model.getCurrentStatus());
        System.out.println("------------------------------------");
    }

    @Override
    public void showView() {
        System.out.println("Console View is now active.");
        render();
    }

    @Override
    public void displayMessage(String message) {
        System.out.println("MESSAGE: " + message);
    }

    @Override
    public void dispose() {
        System.out.println("Console View disposed.");
    }
}