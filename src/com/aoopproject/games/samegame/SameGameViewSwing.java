package com.aoopproject.games.samegame;

import com.aoopproject.MainApplication;
import com.aoopproject.framework.core.AbstractGameModel;
import com.aoopproject.framework.core.GameEvent;
import com.aoopproject.framework.core.GameStatus;
import com.aoopproject.framework.core.GameView;
import com.aoopproject.framework.core.Grid;
import com.aoopproject.common.action.UndoAction;
import com.aoopproject.common.action.NewGameAction;
import com.aoopproject.common.action.HintRequestAction;
import com.aoopproject.common.score.HighScoreManager;
import com.aoopproject.common.score.ScoreEntry;
import com.aoopproject.common.model.DifficultyLevel;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * A Swing-based graphical view for the SameGame.
 * This class implements {@link GameView} and is responsible for rendering the game board,
 * current score, current difficulty's top score, game status, and action buttons
 * (Undo, Hint, View High Scores) in a JFrame window.
 * It observes the {@link AbstractGameModel} for changes and updates the UI accordingly.
 * It handles game over scenarios, prompts for player name if a high score is achieved
 * (using an externally provided {@link HighScoreManager}), and allows viewing high scores.
 */
public class SameGameViewSwing implements GameView {
    /**
     * Inner class representing the panel where the SameGame grid is drawn.
     * It extends {@link JPanel} and overrides {@link #paintComponent(Graphics)} to render the game tiles
     * based on the current state of the {@link AbstractGameModel}. It also provides methods
     * for highlighting suggested move groups and converting mouse coordinates to grid cell coordinates.
     */
    public class GamePanel extends JPanel {
        /** Current number of rows this panel is configured to display, derived from difficulty. */
        private int numRows;
        /** Current number of columns this panel is configured to display, derived from difficulty. */
        private int numCols;
        /** * List of points (where Point.x is row, Point.y is column) representing the group of tiles
         * to be highlighted as a move suggestion. Null if no group is currently highlighted.
         */
        private List<Point> groupToHighlight = null;
        /** Color used as an overlay for highlighting the suggested group. A semi-transparent white. */
        private final Color HIGHLIGHT_OVERLAY_COLOR = new Color(255, 255, 255, 100);
        /** Border color for highlighted tiles to make them stand out. */
        private final Color HIGHLIGHT_BORDER_COLOR = Color.ORANGE;

        /**
         * Constructs a GamePanel with specified initial dimensions for the grid.
         * @param rows The number of rows in the grid.
         * @param cols The number of columns in the grid.
         */
        public GamePanel(int rows, int cols) {
            setGridDimensions(rows, cols);
        }

        /**
         * Sets or updates the dimensions of the grid that this panel should display.
         * This method recalculates the preferred size of the panel based on the new dimensions
         * and the defined {@link SameGameViewSwing#TILE_SIZE} and {@link SameGameViewSwing#PADDING}.
         * It then triggers a re-layout and repaint of the panel.
         *
         * @param rows New number of rows for the grid.
         * @param cols New number of columns for the grid.
         */
        public void setGridDimensions(int rows, int cols) {
            this.numRows = rows;
            this.numCols = cols;
            int panelWidth = cols * TILE_SIZE + 2 * PADDING;
            int panelHeight = rows * TILE_SIZE + 2 * PADDING;
            setPreferredSize(new Dimension(panelWidth, panelHeight));
            revalidate();
            repaint();
        }

        /**
         * Sets the group of tiles to be visually highlighted on the next repaint.
         * Each {@link Point} in the list should have {@code Point.x} as the row
         * and {@code Point.y} as the column.
         *
         * @param groupPositions A List of {@link Point} objects representing the tiles to highlight.
         * Pass {@code null} or an empty list to clear any existing highlight.
         */
        public void setHighlightGroup(List<Point> groupPositions) {
            if (groupPositions != null && groupPositions.isEmpty()) {
                this.groupToHighlight = null;
            } else {
                this.groupToHighlight = groupPositions;
            }
            repaint();
        }

        /**
         * Clears any current tile highlighting by setting the highlighted group to null and repainting.
         */
        public void clearHighlightGroup() {
            setHighlightGroup(null);
        }

        /**
         * Overrides {@link JPanel#paintComponent(Graphics)} to custom-draw the SameGame grid and tiles.
         * It renders each tile with its color. If a group of tiles is marked for highlighting
         * (via {@link #setHighlightGroup(List)}), those tiles are overlaid with a semi-transparent
         * color and a distinct border to indicate a suggested move.
         *
         * @param g The {@link Graphics} context provided by Swing for painting.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (model == null || model.getGameBoard() == null) {
                g.setColor(Color.BLACK);
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                String message = "Game board not available for rendering.";
                FontMetrics fm = g.getFontMetrics();
                int stringWidth = fm.stringWidth(message);
                int stringAscent = fm.getAscent();
                int x = (panelWidth - stringWidth) / 2;
                int y = (panelHeight + stringAscent) / 2;
                g.drawString(message, x, y > 0 ? y : PADDING + 15);
                return;
            }

            Graphics2D g2d = (Graphics2D) g.create();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                @SuppressWarnings("unchecked")
                Grid<SameGameTile> board = (Grid<SameGameTile>) model.getGameBoard();
                this.numRows = board.getRows();
                this.numCols = board.getColumns();

                for (int r = 0; r < this.numRows; r++) {
                    for (int c = 0; c < this.numCols; c++) {
                        SameGameTile tile = board.getEntity(r, c);
                        int x = PADDING + c * TILE_SIZE;
                        int y = PADDING + r * TILE_SIZE;

                        boolean isThisTileHighlighted = false;
                        if (groupToHighlight != null) {
                            for (Point p : groupToHighlight) {
                                if (p.x == r && p.y == c) {
                                    isThisTileHighlighted = true;
                                    break;
                                }
                            }
                        }
                        if (tile != null && !tile.isEmpty()) {
                            Object visual = tile.getVisualRepresentation();
                            if (visual instanceof Color) {
                                g2d.setColor((Color) visual);
                                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                                g2d.setColor(((Color) visual).darker());
                                g2d.drawRect(x, y, TILE_SIZE -1 , TILE_SIZE -1);
                            } else {
                                g2d.setColor(Color.GRAY);
                                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                                g2d.setColor(Color.BLACK);
                                g2d.drawString("?", x + TILE_SIZE/2 - 4, y + TILE_SIZE/2 + 4);
                            }
                        } else {
                            g2d.setColor(Color.LIGHT_GRAY);
                            g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                            g2d.setColor(Color.GRAY);
                            g2d.drawRect(x, y, TILE_SIZE-1, TILE_SIZE-1);
                        }
                        if (isThisTileHighlighted && tile != null && !tile.isEmpty()) {
                            g2d.setColor(HIGHLIGHT_OVERLAY_COLOR);
                            g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                            g2d.setColor(HIGHLIGHT_BORDER_COLOR);
                            g2d.setStroke(new BasicStroke(2));
                            g2d.drawRect(x, y, TILE_SIZE - 1, TILE_SIZE - 1);
                            g2d.setStroke(new BasicStroke(1));
                        }
                    }
                }
            } finally {
                g2d.dispose();
            }
        }

        /**
         * Converts mouse click coordinates (relative to this panel) into
         * game grid cell coordinates (row and column).
         * This method is intended to be used by a {@code MouseInputStrategy} to determine
         * which tile a user clicked on.
         *
         * @param mouseX The x-coordinate of the mouse click within this panel.
         * @param mouseY The y-coordinate of the mouse click within this panel.
         * @return A {@link Point} object where {@code Point.x} is the row index and
         * {@code Point.y} is the column index of the clicked tile. Returns {@code null}
         * if the click was outside the valid tile grid area or if the model/board is not available.
         */
        public Point getTileCoordinatesForMouse(int mouseX, int mouseY) {
            if (model == null || model.getGameBoard() == null || this.numRows <= 0 || this.numCols <= 0) {
                return null;
            }

            if (mouseX < PADDING || mouseY < PADDING ||
                    mouseX >= PADDING + this.numCols * TILE_SIZE ||
                    mouseY >= PADDING + this.numRows * TILE_SIZE) {
                return null;
            }

            int c = (mouseX - PADDING) / TILE_SIZE;
            int r = (mouseY - PADDING) / TILE_SIZE;

            if (r >= 0 && r < this.numRows && c >= 0 && c < this.numCols) {
                return new Point(r, c);
            }
            return null;
        }
    }
    private AbstractGameModel model;
    private JFrame frame;
    private GamePanel gamePanel;
    private JLabel scoreLabel;
    private JLabel statusLabel;
    private JLabel topScoreLabel;
    private JLabel difficultyLabel;
    private JButton undoButton;
    private JButton hintButton;
    private JButton viewHighScoresButton;

    /**
     * Manages loading, saving, and querying high scores.
     * This instance is expected to be set by an external factory or setup mechanism
     * via the {@link #setHighScoreManager(HighScoreManager)} method.
     */
    private HighScoreManager highScoreManager;
    /**
     * Default filename for storing SameGame high scores.
     * This can be made configurable if needed.
     * Made public static so the Factory can also reference it if it creates the HighScoreManager.
     */
    public static final String HIGH_SCORE_FILE = "samegame_highscores.dat";

    private boolean viewInitialized = false;
    private static final int TILE_SIZE = 30;
    private static final int PADDING = 10;
    private boolean gameOverDialogShownForCurrentGameOver = false;

    /**
     * Provides access to the {@link GamePanel} where the game is rendered.
     * @return The {@link GamePanel} instance.
     */
    public GamePanel getGamePanel() {
        return gamePanel;
    }

    /**
     * Sets the {@link HighScoreManager} instance for this view.
     * This method should be called before the view is fully initialized or displayed,
     * typically by the factory that creates this view and the shared HighScoreManager.
     *
     * @param highScoreManager The shared {@link HighScoreManager} instance.
     */
    public void setHighScoreManager(HighScoreManager highScoreManager) {
        this.highScoreManager = highScoreManager;
        if (viewInitialized && frame != null && frame.isVisible()) {
            updateUIState();
        }
    }

    /**
     * Initializes the Swing view components, sets up the layout, and prepares the UI.
     * It relies on {@link #setHighScoreManager(HighScoreManager)} having been called
     * if high score functionalities are to be used immediately (e.g., in {@code updateUIState}
     * called at the end of this method).
     *
     * @param model The game model (must be an instance of {@link AbstractGameModel})
     * that this view will represent and observe.
     */
    @Override
    public void initialize(AbstractGameModel model) {
        if (viewInitialized && this.model == model) {
            return;
        }
        this.model = model;

        frame = new JFrame("SameGame - AOOP Project (Swing)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));

        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        statusLabel = new JLabel("Status: INITIALIZING", SwingConstants.CENTER);
        topScoreLabel = new JLabel("Top Score: N/A", SwingConstants.CENTER);
        difficultyLabel = new JLabel("Difficulty: N/A", SwingConstants.CENTER);

        undoButton = new JButton("Undo");
        undoButton.setFocusable(false);
        undoButton.setEnabled(false);
        undoButton.addActionListener(e -> {
            if (this.model != null) this.model.processInputAction(new UndoAction());
        });

        hintButton = new JButton("Hint");
        hintButton.setFocusable(false);
        hintButton.setEnabled(false);
        hintButton.addActionListener(e -> {
            if (this.model != null && this.model.getCurrentStatus() == GameStatus.PLAYING) {
                this.model.processInputAction(new HintRequestAction());
            }
        });

        viewHighScoresButton = new JButton("High Scores");
        viewHighScoresButton.setFocusable(false);
        viewHighScoresButton.addActionListener(e -> showHighScoresDialog());

        int rows = DifficultyLevel.MEDIUM.getRows();
        int cols = DifficultyLevel.MEDIUM.getCols();
        if (this.model instanceof SameGameModel) {
            DifficultyLevel currentDiff = ((SameGameModel) this.model).getCurrentDifficulty();
            if (currentDiff != null) {
                rows = currentDiff.getRows();
                cols = currentDiff.getCols();
            }
        } else if (this.model != null && this.model.getGameBoard() != null) {
            rows = this.model.getGameBoard().getRows();
            cols = this.model.getGameBoard().getColumns();
        }

        gamePanel = new GamePanel(rows, cols);
        JScrollPane scrollPane = new JScrollPane(gamePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel topInfoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        topInfoPanel.add(scoreLabel);
        topInfoPanel.add(statusLabel);
        topInfoPanel.add(difficultyLabel);
        topInfoPanel.add(topScoreLabel);
        frame.add(topInfoPanel, BorderLayout.NORTH);

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        bottomButtonPanel.add(undoButton);
        bottomButtonPanel.add(hintButton);
        bottomButtonPanel.add(viewHighScoresButton);
        frame.add(bottomButtonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setMinimumSize(frame.getPreferredSize());
        frame.setLocationRelativeTo(null);

        System.out.println("SameGame Swing View Initialized.");
        this.viewInitialized = true;
        updateUIState();
    }

    /**
     * Called by the model when a game event occurs. Ensures UI updates run on the EDT.
     * @param event The {@link GameEvent} from the model.
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
     * Handles game events on the EDT. Updates UI, displays suggestions, manages game over dialogs,
     * and processes high score submissions by prompting for player name if a new high score is achieved.
     *
     * @param event The {@link GameEvent} to handle.
     */
    private void handleGameEvent(GameEvent event) {
        if (frame == null || model == null) return;

        final GameStatus currentModelStatus = model.getCurrentStatus();
        final String eventType = event.getType();

        if ("STATUS_CHANGED".equals(eventType) || "NEW_GAME_STARTED".equals(eventType)) {
            if (model.getCurrentStatus() == GameStatus.PLAYING) {
                gameOverDialogShownForCurrentGameOver = false;
                if (gamePanel != null) gamePanel.clearHighlightGroup();
            }
        }
        updateUIState();

        switch (eventType) {
            case "BOARD_INITIALIZED":
            case "BOARD_CHANGED":
            case "TILES_REMOVED_SUCCESS":
            case "UNDO_PERFORMED":
                if (gamePanel != null) {
                    gamePanel.clearHighlightGroup();
                    gamePanel.repaint();
                }
                break;
            case "INVALID_SELECTION":
                if (event.getPayload() instanceof String) {
                    System.out.println("SameGameViewSwing: Invalid selection - " + event.getPayload());
                }
                if (gamePanel != null) gamePanel.clearHighlightGroup();
                break;
            case "UNDO_FAILED":
                if (event.getPayload() instanceof String && frame.isVisible()) {
                    JOptionPane.showMessageDialog(frame, (String) event.getPayload(), "Undo Failed", JOptionPane.WARNING_MESSAGE);
                }
                if (gamePanel != null) gamePanel.clearHighlightGroup();
                break;
            case "MOVE_SUGGESTION_AVAILABLE":
                if (event.getPayload() instanceof List && gamePanel != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        List<SameGameModel.SameGameTilePosition> suggestedGroup = (List<SameGameModel.SameGameTilePosition>) event.getPayload();
                        List<Point> highlightPoints = new ArrayList<>();
                        for (SameGameModel.SameGameTilePosition pos : suggestedGroup) {
                            highlightPoints.add(new Point(pos.row, pos.col));
                        }
                        gamePanel.setHighlightGroup(highlightPoints);
                    } catch (ClassCastException e) {
                        System.err.println("SameGameViewSwing: Error processing MOVE_SUGGESTION_AVAILABLE payload - " + e.getMessage());
                        if (gamePanel != null) gamePanel.clearHighlightGroup();
                    }
                }
                break;
            case "NO_SUGGESTION_AVAILABLE":
                if (event.getPayload() instanceof String && frame.isVisible()) {
                    JOptionPane.showMessageDialog(frame, (String) event.getPayload(), "Hint", JOptionPane.INFORMATION_MESSAGE);
                }
                if (gamePanel != null) gamePanel.clearHighlightGroup();
                break;
        }

        if (currentModelStatus.toString().startsWith("GAME_OVER") && !gameOverDialogShownForCurrentGameOver) {
            gameOverDialogShownForCurrentGameOver = true;
            if (gamePanel != null) gamePanel.clearHighlightGroup();

            String endMessage = "Game Over! Final Score: " + model.getScore();
            if (currentModelStatus == GameStatus.GAME_OVER_WIN) endMessage += " - Congratulations, you cleared the board!";
            else if (currentModelStatus == GameStatus.GAME_ENDED_USER_QUIT) endMessage += " - Game quit by user.";
            else if (currentModelStatus == GameStatus.GAME_OVER_LOSE) endMessage += " - No more moves possible.";

            final String finalEndMessage = endMessage;
            final int finalScore = model.getScore();

            SwingUtilities.invokeLater(() -> {
                if (!frame.isVisible() || (gamePanel != null && !gamePanel.isShowing())) return;

                JOptionPane.showMessageDialog(frame, finalEndMessage, "Game Over", JOptionPane.INFORMATION_MESSAGE);
                if (model instanceof SameGameModel && currentModelStatus != GameStatus.GAME_ENDED_USER_QUIT) {
                    SameGameModel sameGameModel = (SameGameModel) model;
                    DifficultyLevel difficulty = sameGameModel.getCurrentDifficulty();
                    if (this.highScoreManager != null && difficulty != null && highScoreManager.isHighScore(difficulty, finalScore)) {
                        String playerName = JOptionPane.showInputDialog(frame,
                                "New High Score: " + finalScore + " on " + difficulty.getDisplayName() + " level!\nEnter your name:",
                                "New High Score!", JOptionPane.PLAIN_MESSAGE);
                        if (playerName != null && !playerName.trim().isEmpty()) {
                            ScoreEntry newEntry = new ScoreEntry(playerName.trim(), finalScore, difficulty, new Date());
                            boolean scoreAdded = highScoreManager.addScore(difficulty, newEntry.playerName(), newEntry.score());
                            if (scoreAdded) {
                                System.out.println("View: New high score by " + playerName + " was added.");
                                if (this.model != null) {
                                    this.model.newHighScoreAchieved(newEntry);
                                }
                            }
                            updateUIState();
                        }
                    }
                }
                GameStatus statusAfterDialogs = model.getCurrentStatus();
                if (statusAfterDialogs != GameStatus.GAME_ENDED_USER_QUIT &&
                        statusAfterDialogs.toString().startsWith("GAME_OVER")) {
                    int choice = JOptionPane.showConfirmDialog(frame, "Play another game?", "New Game?",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        frame.dispose();
                        MainApplication.launchNewGame();
                    } else {
                        frame.dispose();
                    }
                } else if (statusAfterDialogs == GameStatus.GAME_ENDED_USER_QUIT) {
                    frame.dispose();
                }
            });
        }
    }

    /**
     * Updates UI labels (score, status, difficulty, top score) and button enabled states
     * based on the current game model state and {@link HighScoreManager} data.
     */
    private void updateUIState() {
        if (model != null) {
            scoreLabel.setText("Score: " + model.getScore());
            statusLabel.setText("Status: " + model.getCurrentStatus());
            boolean isPlaying = model.getCurrentStatus() == GameStatus.PLAYING;

            DifficultyLevel currentDiff = null;
            if (model instanceof SameGameModel) {
                SameGameModel sgModel = (SameGameModel) model;
                currentDiff = sgModel.getCurrentDifficulty();
                undoButton.setEnabled(sgModel.canUndo() && isPlaying);
                if (difficultyLabel != null) difficultyLabel.setText("Difficulty: " + (currentDiff != null ? currentDiff.getDisplayName() : "N/A"));
            } else {
                undoButton.setEnabled(false);
                if (difficultyLabel != null) difficultyLabel.setText("Difficulty: N/A");
            }

            if (hintButton != null) hintButton.setEnabled(isPlaying);
            if (viewHighScoresButton != null) viewHighScoresButton.setEnabled(highScoreManager != null && currentDiff != null);

            if (topScoreLabel != null) {
                if (highScoreManager != null && currentDiff != null) {
                    ScoreEntry top = highScoreManager.getTopScore(currentDiff);
                    topScoreLabel.setText("Top Score (" + currentDiff.getDisplayName() + "): " + (top != null ? top.score() : "N/A"));
                } else {
                    topScoreLabel.setText("Top Score: N/A");
                }
            }
        } else {
            scoreLabel.setText("Score: N/A");
            statusLabel.setText("Status: N/A");
            if (difficultyLabel != null) difficultyLabel.setText("Difficulty: N/A");
            if (topScoreLabel != null) topScoreLabel.setText("Top Score: N/A");
            if (undoButton != null) undoButton.setEnabled(false);
            if (hintButton != null) hintButton.setEnabled(false);
            if (viewHighScoresButton != null) viewHighScoresButton.setEnabled(false);
        }
    }

    /**
     * Displays a dialog showing the high scores for the current game's difficulty level.
     * Scores are retrieved from the {@link HighScoreManager}.
     */
    private void showHighScoresDialog() {
        if (this.highScoreManager == null) {
            JOptionPane.showMessageDialog(frame, "HighScoreManager not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (model == null || !(model instanceof SameGameModel)) {
            JOptionPane.showMessageDialog(frame, "Game data not available to determine difficulty for high scores.", "High Scores Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DifficultyLevel difficulty = ((SameGameModel) model).getCurrentDifficulty();
        if (difficulty == null) {
            JOptionPane.showMessageDialog(frame, "Current difficulty level is unknown.", "High Scores Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<ScoreEntry> scores = highScoreManager.getHighScores(difficulty);
        StringBuilder sb = new StringBuilder();
        sb.append("Top ").append(HighScoreManager.getMaxScoresPerLevel())
                .append(" Scores for ").append(difficulty.getDisplayName()).append(":\n\n");
        if (scores.isEmpty()) {
            sb.append("No scores recorded yet for this level.\nPlay a game to be the first!");
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int rank = 1;
            for (ScoreEntry entry : scores) {
                sb.append(String.format("%2d. %-20s %6d  (%s)\n",
                        rank++,
                        entry.playerName().length() > 20 ? entry.playerName().substring(0, 17) + "..." : entry.playerName(),
                        entry.score(),
                        dateFormat.format(entry.date())));
            }
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPaneForScores = new JScrollPane(textArea);
        scrollPaneForScores.setPreferredSize(new Dimension(480, 320));
        JOptionPane.showMessageDialog(frame, scrollPaneForScores, "High Scores - " + difficulty.getDisplayName(), JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Makes the game window (JFrame) visible to the user.
     * Before showing, it ensures that the {@link GamePanel} is correctly sized
     * according to the current game board's dimensions (retrieved from the model).
     * If the panel's dimensions need to change, the frame is repacked.
     * It then updates all UI elements (labels, buttons) and repaints the game panel.
     * Finally, it requests focus for the frame to ensure keyboard input (if any) is captured.
     */
    @Override
    public void showView() {
        if (frame != null) {
            if (this.model instanceof SameGameModel && ((SameGameModel)this.model).getCurrentDifficulty() != null && gamePanel != null) {
                DifficultyLevel currentDiff = ((SameGameModel)this.model).getCurrentDifficulty();
                int currentRows = currentDiff.getRows();
                int currentCols = currentDiff.getCols();

                if (gamePanel.numRows != currentRows || gamePanel.numCols != currentCols) {
                    gamePanel.setGridDimensions(currentRows, currentCols);
                    frame.pack();
                }
            } else if (this.model != null && this.model.getGameBoard() != null && gamePanel != null) {
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
                gamePanel.repaint();
            }
            frame.requestFocusInWindow();
        } else {
            System.err.println("SameGameViewSwing.showView(): JFrame (frame) is null. Cannot show view.");
        }
    }

    /**
     * Hides the game window (JFrame) from the user by setting its visibility to false.
     * The frame object itself is not disposed and can be shown again later.
     */
    @Override
    public void hideView() {
        if (frame != null) {
            frame.setVisible(false);
        } else {
            System.err.println("SameGameViewSwing.hideView(): JFrame (frame) is null. Cannot hide view.");
        }
    }

    /**
     * Displays a generic message to the user in a standard JOptionPane dialog.
     * The dialog is only shown if the main game frame is currently initialized and visible.
     * If the frame is not visible, the message is printed to the standard output as a fallback.
     *
     * @param message The string message to be displayed to the user.
     */
    @Override
    public void displayMessage(String message) {
        if (frame != null && frame.isVisible()) {
            JOptionPane.showMessageDialog(frame, message, "Game Message", JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println("SameGameViewSwing (Frame not visible or null) Message: " + message);
        }
    }

    /**
     * Disposes of the main game JFrame, releasing any operating system screen resources
     * it holds. This should be called when the view is permanently no longer needed
     * (e.g., when the application is exiting or a new game session with a new window
     * is being created). If {@link JFrame#EXIT_ON_CLOSE} is set on the frame (as it is
     * in this class's {@code initialize} method), disposing the last displayable frame
     * may also terminate the Java Virtual Machine.
     */
    @Override
    public void dispose() {
        if (frame != null) {
            System.out.println("SameGameViewSwing: Disposing JFrame.");
            frame.dispose();
            frame = null;
        } else {
            System.out.println("SameGameViewSwing: Dispose called, but JFrame (frame) was already null.");
        }
        viewInitialized = false;
    }
}