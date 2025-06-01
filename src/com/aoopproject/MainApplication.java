package com.aoopproject;

import com.aoopproject.framework.core.AbstractGameController;
import com.aoopproject.framework.core.GameFactory;
import com.aoopproject.games.samegame.SameGameFactory;
import com.aoopproject.games.sokoban.SokobanFactory;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

/**
 * The main application entry point for the AOOP Project. It provides a simple UI dialog
 * to let the user choose between available games (e.g., SameGame, Sokoban) and
 * starts the selected game using the appropriate {@link GameFactory} implementation.
 * <p>
 * All UI interactions are scheduled on the Swing Event Dispatch Thread to ensure
 * thread safety in accordance with Swing's single-threaded rule.
 */
public class MainApplication {

    /**
     * The main entry point for the application.
     * This method schedules the {@link #launchNewGame()} process to run on the
     * Swing Event Dispatch Thread to ensure thread safety for UI operations.
     *
     * @param args Command-line arguments (not used by this application).
     */
    public static void main(String[] args) {
        launchNewGame();
    }

    /**
     * Initiates a new game session.
     * <ul>
     * <li>Prompts the user to select a game (e.g., "SameGame" or "Sokoban") via a dialog.</li>
     * <li>Based on the user's choice, instantiates the appropriate {@link GameFactory}.</li>
     * <li>Delegates the game setup (including any game-specific pre-configuration like
     * difficulty settings, which are handled by the factory itself) to the factory's
     * {@code setupGame()} method.</li>
     * <li>If game setup is successful, it initializes and starts the game controller.</li>
     * <li>Handles cases where game selection or setup is cancelled by the user, or if errors occur
     * during initialization, by exiting the application gracefully.</li>
     * </ul>
     * This method can be called to restart the entire game selection and setup process.
     * All UI-related parts of the setup are executed on the Swing Event Dispatch Thread.
     */
    public static void launchNewGame() {
        System.out.println("Configuring a new game session...");
        String[] availableGames = {"SameGame", "Sokoban"};
        String chosenGame = (String) JOptionPane.showInputDialog(
                null,
                "Select a game to play:",
                "Game Launcher - AOOP Project",
                JOptionPane.PLAIN_MESSAGE,
                null,
                availableGames,
                availableGames[0]
        );

        GameFactory factory = null;
        if (chosenGame == null) {
            System.out.println("No game selected. Exiting application.");
            System.exit(0);
            return;
        }

        switch (chosenGame) {
            case "SameGame":
                factory = new SameGameFactory();
                break;
            case "Sokoban":
                factory = new SokobanFactory();
                break;
            default:
                System.out.println("Unknown game selected (" + chosenGame + "). Exiting application.");
                System.exit(0);
                return;
        }
        final GameFactory finalFactory = factory;
        SwingUtilities.invokeLater(() -> {
            AbstractGameController gameController = finalFactory.setupGame();
            if (gameController == null) {
                System.out.println("Game setup was cancelled or failed (e.g., settings dialog was closed). Exiting application.");
                System.exit(0);
                return;
            }

            try {
                gameController.initializeGame();
                gameController.startGame();
                System.out.println(chosenGame + " game started successfully.");
            } catch (Exception e) {
                System.err.println("An error occurred during " + chosenGame + " game initialization or startup:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "A critical error occurred during game startup: " + e.getMessage(),
                        "Application Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}