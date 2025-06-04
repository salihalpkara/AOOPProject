package com.aoopproject.common.input;

import com.aoopproject.framework.core.GameAction;
import com.aoopproject.framework.core.GameView;
import com.aoopproject.framework.core.InputStrategy;
import com.aoopproject.common.action.QuitAction;
import com.aoopproject.games.samegame.action.SameGameSelectAction;

import java.util.Scanner;

/**
 * An {@link InputStrategy} that reads user input from the console.
 * This is a simple strategy primarily for text-based games or debugging.
 */
public class ConsoleInputStrategy implements InputStrategy {

    private Scanner scanner;

    public ConsoleInputStrategy() {
    }

    @Override
    public void initialize(GameView gameView) {
        this.scanner = new Scanner(System.in);
        System.out.println("ConsoleInputStrategy initialized. Enter actions via console.");
    }

    /**
     * Prompts the user to enter coordinates (e.g., "row col") for SameGame.
     * This will need to be adapted based on the specific GameAction expected by the game.
     * For now, it's a placeholder expecting row and column for a tile selection.
     *
     * @return A {@link GameAction} representing the console input.
     * Currently returns a generic placeholder or a specific action like SameGameSelectAction.
     */
    @Override
    public GameAction solicitAction() {
        System.out.print("Enter action (e.g., for SameGame 'row col' to select tile, or 'quit'): ");
        if (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if ("quit".equalsIgnoreCase(line)) {
                return new QuitAction();
            }
            String[] parts = line.split("\\s+");
            if (parts.length == 2) {
                try {
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    return new SameGameSelectAction(row, col);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input format. Please use numbers for row and column.");
                    return null;
                }
            } else {
                System.out.println("Invalid input. Expected 'row col' or 'quit'.");
                return null;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        if (scanner != null) {
            System.out.println("ConsoleInputStrategy disposed.");
        }
    }
}