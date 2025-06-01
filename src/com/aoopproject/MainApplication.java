package com.aoopproject;

import com.aoopproject.framework.core.AbstractGameController;
import com.aoopproject.framework.core.GameFactory;
import com.aoopproject.games.samegame.SameGameFactory;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

/**
 * Main application class for launching games developed under the AOOP Project framework.
 * This class is responsible for initiating the game setup process, including allowing
 * the user to select a game (currently defaults to SameGame) and then delegating
 * the game-specific setup (like difficulty settings) to the corresponding {@link GameFactory}.
 */
public class MainApplication {

    /**
     * The main entry point for the application.
     * It schedules the launch of a new game session on the Event Dispatch Thread.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        launchNewGame();
    }

    /**
     * Initializes and starts a new game session.
     * This method determines which game to play (currently hardcoded to SameGame but designed
     * for future expansion to include other games like 2048). It then instantiates the
     * appropriate {@link GameFactory}. The factory is responsible for handling any game-specific
     * pre-setup, such as prompting for difficulty settings, before creating and wiring up
     * the model, view(s), controller, and input strategy.
     * <p>
     * If the game setup is cancelled (e.g., user closes the difficulty settings dialog via the factory),
     * the application will exit. All UI operations are ensured to run on the Swing Event Dispatch Thread.
     * This method can be called to restart the game after a previous session has ended.
     * </p>
     */
    public static void launchNewGame() {
        System.out.println("Configuring a new game session...");
        String chosenGame = "SameGame";
        GameFactory factory = null;

        if ("SameGame".equals(chosenGame)) {
            factory = new SameGameFactory();
        } else if ("2048".equals(chosenGame)) {
            System.out.println("Error: 2048 game is not yet implemented. Exiting application.");
            System.exit(0);
            return;
        } else {
            System.out.println("Unsupported game selected or selection cancelled. Exiting application.");
            System.exit(0);
            return;
        }

        final GameFactory finalFactory = factory;

        SwingUtilities.invokeLater(() -> {
            AbstractGameController gameController = finalFactory.setupGame();
            if (gameController == null) {
                System.out.println("Game setup was cancelled (e.g., settings dialog was closed). Exiting application.");
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
                        "A critical error occurred: " + e.getMessage(),
                        "Application Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}