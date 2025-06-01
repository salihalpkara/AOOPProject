package com.aoopproject.games.sokoban.view;

import com.aoopproject.MainApplication;
import com.aoopproject.framework.core.AbstractGameModel;
import com.aoopproject.framework.core.GameEvent;
import com.aoopproject.framework.core.GameStatus;
import com.aoopproject.framework.core.GameView;
import com.aoopproject.framework.core.Grid;
import com.aoopproject.common.action.NewGameAction;
import com.aoopproject.common.action.UndoAction;
import com.aoopproject.games.sokoban.model.SokobanModel;
import com.aoopproject.games.sokoban.model.SokobanTile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A Swing-based graphical view for the Sokoban game.
 * This class implements {@link GameView} and is responsible for rendering the
 * Sokoban game board using images for different game entities (player, box, wall, etc.).
 * It also displays the current score (move count) and provides UI elements like
 * Undo and Restart Level buttons.
 */
public class SokobanViewSwing implements GameView {

    private SokobanModel model;
    private JFrame frame;
    private GamePanel gamePanel;
    private JLabel scoreLabel;
    private JLabel statusLabel;
    private JButton undoButton;
    private JButton restartButton;

    private boolean viewInitialized = false;
    private static final int TILE_SIZE = 32;
    private static final int PADDING = 10;

    private boolean gameOverDialogShown = false;

    /**
     * Provides access to the main JFrame of this view.
     * This can be used by an input strategy (e.g., KeyboardInputStrategy)
     * to attach key listeners to the frame.
     *
     * @return The main {@link JFrame} of the game.
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Initializes the Swing view components for Sokoban.
     *
     * @param model The game model, expected to be an instance of {@link SokobanModel}.
     */
    @Override
    public void initialize(AbstractGameModel model) {
        if (!(model instanceof SokobanModel)) {
            throw new IllegalArgumentException("SokobanViewSwing requires a SokobanModel instance.");
        }
        if (viewInitialized && this.model == model) {
            return;
        }
        this.model = (SokobanModel) model;

        frame = new JFrame("Sokoban - AOOP Project");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));

        scoreLabel = new JLabel("Moves: 0", SwingConstants.CENTER);
        statusLabel = new JLabel("Status: INITIALIZING", SwingConstants.CENTER);

        undoButton = new JButton("Undo");
        undoButton.setFocusable(false);
        undoButton.setEnabled(false);
        undoButton.addActionListener(e -> {
            if (this.model != null) this.model.processInputAction(new UndoAction());
        });

        restartButton = new JButton("Restart Level");
        restartButton.setFocusable(false);
        restartButton.addActionListener(e -> {
            if (this.model != null) this.model.processInputAction(new NewGameAction());
        });
        int initialRows = 10;
        int initialCols = 10;
        if (this.model.getGameBoard() != null) {
            initialRows = this.model.getGameBoard().getRows();
            initialCols = this.model.getGameBoard().getColumns();
        }

        gamePanel = new GamePanel(initialRows, initialCols);
        frame.add(gamePanel, BorderLayout.CENTER);

        JPanel topInfoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        topInfoPanel.add(scoreLabel);
        topInfoPanel.add(statusLabel);
        frame.add(topInfoPanel, BorderLayout.NORTH);

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        bottomButtonPanel.add(undoButton);
        bottomButtonPanel.add(restartButton);
        frame.add(bottomButtonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setMinimumSize(frame.getPreferredSize());
        frame.setLocationRelativeTo(null);
        frame.setFocusable(true);
        frame.requestFocusInWindow();

        System.out.println("Sokoban Swing View Initialized.");
        this.viewInitialized = true;
        updateUIState();
    }

    /**
     * Handles game events from the model, ensuring UI updates run on the EDT.
     * @param event The {@link GameEvent}.
     */
    @Override
    public void onGameEvent(GameEvent event) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> handleGameEvent(event));
        } else {
            handleGameEvent(event);
        }
    }

    /**
     * Processes game events on the EDT to update the UI.
     * @param event The {@link GameEvent} to handle.
     */
    private void handleGameEvent(GameEvent event) {
        if (frame == null || model == null) return;

        final GameStatus currentModelStatus = model.getCurrentStatus();
        final String eventType = event.getType();

        if ("STATUS_CHANGED".equals(eventType) || "NEW_GAME_STARTED".equals(eventType)) {
            if (model.getCurrentStatus() == GameStatus.PLAYING) {
                gameOverDialogShown = false;
            }
        }
        updateUIState();

        switch (eventType) {
            case "BOARD_INITIALIZED":
                if (model.getGameBoard() != null && gamePanel != null) {
                    gamePanel.setGridDimensions(model.getGameBoard().getRows(), model.getGameBoard().getColumns());
                    frame.pack();
                }
            case "BOARD_CHANGED":
            case "UNDO_PERFORMED":
                if (gamePanel != null) gamePanel.repaint();
                break;
            case "INVALID_MOVE":
                if (event.getPayload() instanceof String) {
                    System.out.println("SokobanView: Invalid move - " + event.getPayload());
                }
                break;
            case "UNDO_FAILED":
                if (event.getPayload() instanceof String && frame.isVisible()) {
                    JOptionPane.showMessageDialog(frame, (String) event.getPayload(), "Undo Failed", JOptionPane.WARNING_MESSAGE);
                }
                break;
        }

        if (currentModelStatus.toString().startsWith("GAME_OVER") && !gameOverDialogShown) {
            gameOverDialogShown = true;
            String endMessage = "Level Complete! Moves: " + model.getScore();
            if (currentModelStatus == GameStatus.GAME_OVER_WIN) {
                endMessage = "Congratulations! Level Solved! Moves: " + model.getScore();
            } else if (currentModelStatus == GameStatus.GAME_ENDED_USER_QUIT) {
                endMessage = "Game Quit. Moves: " + model.getScore();
            }

            final String finalEndMessage = endMessage;
            SwingUtilities.invokeLater(() -> {
                if (!frame.isVisible()) return;
                JOptionPane.showMessageDialog(frame, finalEndMessage, "Sokoban", JOptionPane.INFORMATION_MESSAGE);
                GameStatus statusAfterDialog = model.getCurrentStatus();
                if (statusAfterDialog != GameStatus.GAME_ENDED_USER_QUIT && statusAfterDialog == GameStatus.GAME_OVER_WIN) {
                    int choice = JOptionPane.showConfirmDialog(frame,
                            "Play another game session?",
                            "Game Over",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        System.out.println("User chose to play a new game.");
                        frame.dispose();
                        MainApplication.launchNewGame();
                    } else {
                        frame.dispose();
                        System.exit(0);
                    }
                } else if (statusAfterDialog == GameStatus.GAME_ENDED_USER_QUIT) {
                    frame.dispose();
                }
            });
        }
    }

    /**
     * Updates UI elements like score and status labels, and button states.
     */
    private void updateUIState() {
        if (model != null) {
            scoreLabel.setText("Moves: " + model.getScore());
            statusLabel.setText("Status: " + model.getCurrentStatus());
            boolean isPlaying = model.getCurrentStatus() == GameStatus.PLAYING;
            undoButton.setEnabled(model.canUndo() && isPlaying);
            restartButton.setEnabled(true);
        } else {
            scoreLabel.setText("Moves: N/A");
            statusLabel.setText("Status: N/A");
            if(undoButton != null) undoButton.setEnabled(false);
            if(restartButton != null) restartButton.setEnabled(false);
        }
    }

    /** Makes the game window visible. */
    @Override
    public void showView() {
        if (frame != null) {
            if (this.model != null && this.model.getGameBoard() != null && gamePanel != null) {
                int currentRows = this.model.getGameBoard().getRows();
                int currentCols = this.model.getGameBoard().getColumns();
                if (gamePanel.numRows != currentRows || gamePanel.numCols != currentCols) {
                    gamePanel.setGridDimensions(currentRows, currentCols);
                    frame.pack();
                }
            }
            frame.setVisible(true);
            updateUIState();
            if(gamePanel != null) {
                gamePanel.loadImages();
                gamePanel.repaint();
            }
            frame.requestFocusInWindow();
        }
    }

    /** Hides the game window. */
    @Override
    public void hideView() { if (frame != null) frame.setVisible(false); }

    /** Displays a generic message dialog. @param message The message to display. */
    @Override
    public void displayMessage(String message) { /* ... (SameGameViewSwing'deki gibi) ... */ }

    /** Disposes of the main game JFrame. */
    @Override
    public void dispose() { /* ... (SameGameViewSwing'deki gibi) ... */ }

    /**
     * Inner class for drawing the Sokoban game board using images.
     */
    public class GamePanel extends JPanel {
        private int numRows;
        private int numCols;
        private Map<String, Image> imageCache;

        /**
         * Constructs a GamePanel with initial dimensions.
         * @param rows Initial number of rows.
         * @param cols Initial number of columns.
         */
        public GamePanel(int rows, int cols) {
            setGridDimensions(rows, cols);
            this.imageCache = new HashMap<>();
        }



        /**
         * Loads all necessary Sokoban images into the cache if not already loaded.
         * This method should be called before the first paint operation that needs the images.
         */
        public void loadImages() {
            if (!imageCache.isEmpty()) return;

            String[] imagePaths = {
                    SokobanTile.IMG_WALL, SokobanTile.IMG_FLOOR, SokobanTile.IMG_TARGET,
                    SokobanTile.IMG_PLAYER, SokobanTile.IMG_BOX_ON_FLOOR, SokobanTile.IMG_BOX_ON_TARGET
            };
            for (String path : imagePaths) {
                if (!imageCache.containsKey(path)) {
                    try {
                        InputStream imgStream = SokobanViewSwing.class.getResourceAsStream(path);
                        if (imgStream == null) {
                            System.err.println("Failed to load image resource: " + path);
                            continue;
                        }
                        imageCache.put(path, ImageIO.read(imgStream));
                        System.out.println("Loaded image: " + path);
                    } catch (IOException e) {
                        System.err.println("Error loading image " + path + ": " + e.getMessage());
                    }
                }
            }
        }


        /** Sets grid dimensions and preferred size. */
        public void setGridDimensions(int rows, int cols) {
            this.numRows = rows; this.numCols = cols;
            setPreferredSize(new Dimension(cols * TILE_SIZE + 2 * PADDING, rows * TILE_SIZE + 2 * PADDING));
            revalidate(); repaint();
        }

        /** Paints the Sokoban board. */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (model == null || model.getGameBoard() == null || imageCache == null) {
                g.drawString("Sokoban board not available or images not loaded.", PADDING, PADDING + 15);
                return;
            }
            if (imageCache.isEmpty() && numRows > 0) {
                System.out.println("paintComponent: Image cache is empty. Attempting to load images.");
                loadImages();
                if(imageCache.isEmpty()){
                    g.drawString("Failed to load images. Cannot render board.", PADDING, PADDING + 15);
                    return;
                }
            }


            Graphics2D g2d = (Graphics2D) g.create();
            try {
                Grid<SokobanTile> board = (Grid<SokobanTile>) model.getGameBoard();
                for (int r = 0; r < this.numRows; r++) {
                    for (int c = 0; c < this.numCols; c++) {
                        SokobanTile tile = board.getEntity(r, c);
                        int x = PADDING + c * TILE_SIZE;
                        int y = PADDING + r * TILE_SIZE;

                        if (tile != null) {
                            String imagePath = (String) tile.getVisualRepresentation();
                            Image img = imageCache.get(imagePath);
                            if (img != null) {
                                g2d.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, this);
                            } else {
                                g2d.setColor(Color.MAGENTA);
                                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                                g2d.setColor(Color.BLACK);
                                g2d.drawString("?", x + TILE_SIZE/2 - 4, y + TILE_SIZE/2 + 4);
                            }
                        }
                    }
                }
            } finally {
                g2d.dispose();
            }
        }
    }
}