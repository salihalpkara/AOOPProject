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

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A Swing-based graphical view for the SameGame.
 * This class implements {@link GameView} and is responsible for rendering the game board,
 * current score, current difficulty's top score, game status, and action buttons
 * (Undo, Hint, View High Scores) in a JFrame window.
 * It observes the {@link AbstractGameModel} for changes and updates the UI accordingly.
 * It handles game over scenarios, prompts for player name if a high score is achieved,
 * and allows viewing high scores for the current difficulty level.
 */
public class SameGameViewSwing implements GameView {

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

    private HighScoreManager highScoreManager;
    private static final String HIGH_SCORE_FILE = "samegame_highscores.dat";

    private boolean viewInitialized = false;
    /** Pixel size (width and height) of each tile drawn on the board. */
    private static final int TILE_SIZE = 30;
    /** Padding in pixels around the game board within the GamePanel. */
    private static final int PADDING = 10;

    /** Flag to prevent showing the game over dialog multiple times for the same game over state. */
    private boolean gameOverDialogShownForCurrentGameOver = false;

    /**
     * Provides access to the GamePanel. Used by {@code MouseInputStrategy}.
     * @return The {@link GamePanel} instance.
     */
    public GamePanel getGamePanel() {
        return gamePanel;
    }

    /**
     * Initializes the Swing view components, sets up the layout, and prepares the UI.
     * It also initializes the {@link HighScoreManager}.
     *
     * @param model The game model that this view will represent and observe.
     */
    @Override
    public void initialize(AbstractGameModel model) {
        if (viewInitialized && this.model == model) {
            return;
        }
        this.model = model;
        this.highScoreManager = new HighScoreManager(HIGH_SCORE_FILE);

        frame = new JFrame("SameGame - AOOP Project");
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
        int rows;
        int cols;

        if (this.model instanceof SameGameModel && ((SameGameModel) this.model).getCurrentDifficulty() != null) {
            DifficultyLevel currentDiff = ((SameGameModel) this.model).getCurrentDifficulty();
            rows = currentDiff.getRows();
            cols = currentDiff.getCols();
        } else if (this.model != null && this.model.getGameBoard() != null) {
            rows = this.model.getGameBoard().getRows();
            cols = this.model.getGameBoard().getColumns();
        } else {
            System.out.println("Falling back to MEDIUM difficulty defaults for view initialization.");
            rows = DifficultyLevel.MEDIUM.getRows();
            cols = DifficultyLevel.MEDIUM.getCols();
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
     * Handles game events on the EDT. Updates UI, displays suggestions, and manages game over dialogs.
     * @param event The {@link GameEvent} to handle.
     */
    private void handleGameEvent(GameEvent event) {
        if (frame == null || model == null) return;

        final GameStatus currentModelStatus = model.getCurrentStatus();
        final String eventType = event.getType();
        if ("STATUS_CHANGED".equals(eventType) || "NEW_GAME_STARTED".equals(eventType)) {
            if (model.getCurrentStatus() == GameStatus.PLAYING) {
                gameOverDialogShownForCurrentGameOver = false;
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
                    System.out.println("View: Invalid selection - " + event.getPayload());
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
                        System.err.println("Error processing MOVE_SUGGESTION_AVAILABLE payload: " + e.getMessage());
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
                    if (highScoreManager.isHighScore(difficulty, finalScore)) {
                        String playerName = JOptionPane.showInputDialog(frame,
                                "New High Score: " + finalScore + " on " + difficulty.getDisplayName() + " level!\nEnter your name:",
                                "New High Score!", JOptionPane.PLAIN_MESSAGE);
                        if (playerName != null && !playerName.trim().isEmpty()) {
                            highScoreManager.addScore(difficulty, playerName.trim(), finalScore);
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
     * based on the current game model state.
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
            if (viewHighScoresButton != null) viewHighScoresButton.setEnabled(currentDiff != null);

            if (topScoreLabel != null && highScoreManager != null && currentDiff != null) {
                ScoreEntry top = highScoreManager.getTopScore(currentDiff);
                topScoreLabel.setText("Top Score (" + currentDiff.getDisplayName() + "): " + (top != null ? top.score() : "N/A"));
            } else if (topScoreLabel != null) {
                topScoreLabel.setText("Top Score: N/A");
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
     * Displays the high scores for the current game's difficulty level in a dialog.
     */
    private void showHighScoresDialog() {
        if (model == null || !(model instanceof SameGameModel) || highScoreManager == null) {
            JOptionPane.showMessageDialog(frame, "High score data is not available at the moment.",
                    "High Scores Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DifficultyLevel difficulty = ((SameGameModel) model).getCurrentDifficulty();
        if (difficulty == null) {
            JOptionPane.showMessageDialog(frame, "Current difficulty level is unknown. Cannot display high scores.",
                    "High Scores Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<ScoreEntry> scores = highScoreManager.getHighScores(difficulty);
        StringBuilder sb = new StringBuilder();
        sb.append("Top Scores for ").append(difficulty.getDisplayName()).append(":\n\n");

        if (scores.isEmpty()) {
            sb.append("No scores recorded yet for this level.\nPlay a game to be the first!");
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 300));

        JOptionPane.showMessageDialog(frame, scrollPane,
                "High Scores - " + difficulty.getDisplayName(),
                JOptionPane.PLAIN_MESSAGE);
    }

    /** Makes the game window visible. */
    @Override
    public void showView() {
        if (frame != null) {
            if (this.model != null && this.model.getGameBoard() != null && gamePanel != null) {
                int currentRows;
                int currentCols;
                if (this.model instanceof SameGameModel && ((SameGameModel) this.model).getCurrentDifficulty() != null) {
                    DifficultyLevel currentDiff = ((SameGameModel) this.model).getCurrentDifficulty();
                    currentRows = currentDiff.getRows();
                    currentCols = currentDiff.getCols();
                } else {
                    currentRows = this.model.getGameBoard().getRows();
                    currentCols = this.model.getGameBoard().getColumns();
                }

                if (gamePanel.numRows != currentRows || gamePanel.numCols != currentCols) {
                    gamePanel.setGridDimensions(currentRows, currentCols);
                    frame.pack();
                }
            }
            frame.setVisible(true);
            updateUIState();
            if(gamePanel != null) gamePanel.repaint();
        }
    }

    /** Hides the game window. */
    @Override
    public void hideView() { /* ... Javadoc from previous version ... */
        if (frame != null) frame.setVisible(false);
    }

    /** Displays a generic message dialog. @param message The message to display. */
    @Override
    public void displayMessage(String message) { /* ... Javadoc from previous version ... */
        if (frame != null && frame.isVisible()) JOptionPane.showMessageDialog(frame, message, "Game Message", JOptionPane.INFORMATION_MESSAGE);
        else System.out.println("SwingView (Hidden/NoFrame) Message: " + message);
    }

    /** Disposes of the main game JFrame. */
    @Override
    public void dispose() { /* ... Javadoc from previous version ... */
        if (frame != null) frame.dispose();
        System.out.println("Swing View disposed.");
    }

    /**
     * Inner class for drawing the game board, including highlighting suggested moves.
     */
    public class GamePanel extends JPanel {
        /** Current number of rows this panel is configured to display. */
        private int numRows;
        /** Current number of columns this panel is configured to display. */
        private int numCols;
        /** List of points (x=row, y=col) representing the group of tiles to highlight. */
        private List<Point> groupToHighlight = null;
        /** Color used as an overlay for highlighting the suggested group. */
        private final Color HIGHLIGHT_OVERLAY_COLOR = new Color(255, 255, 255, 100);
        /** Border color for highlighted tiles. */
        private final Color HIGHLIGHT_BORDER_COLOR = Color.ORANGE;

        /**
         * Constructs a GamePanel.
         * @param rows Initial number of rows.
         * @param cols Initial number of columns.
         */
        public GamePanel(int rows, int cols) { /* ... Javadoc from previous version ... */
            setGridDimensions(rows, cols);
        }

        /**
         * Sets/updates the grid dimensions for this panel.
         * @param rows New number of rows.
         * @param cols New number of columns.
         */
        public void setGridDimensions(int rows, int cols) { /* ... Javadoc from previous version ... */
            this.numRows = rows; this.numCols = cols;
            setPreferredSize(new Dimension(cols * TILE_SIZE + 2 * PADDING, rows * TILE_SIZE + 2 * PADDING));
            revalidate(); repaint();
        }

        /**
         * Sets the group of tiles to be highlighted.
         * @param groupPositions List of {@link Point} (x=row, y=col) for tiles to highlight.
         * Null or empty list clears highlight.
         */
        public void setHighlightGroup(List<Point> groupPositions) { /* ... Javadoc from previous version ... */
            this.groupToHighlight = (groupPositions != null && !groupPositions.isEmpty()) ? groupPositions : null;
            repaint();
        }

        /** Clears any current tile highlighting. */
        public void clearHighlightGroup() { /* ... Javadoc from previous version ... */
            setHighlightGroup(null);
        }

        /**
         * Paints the game board, tiles, and any highlighted group.
         * @param g The {@link Graphics} context.
         */
        @Override
        protected void paintComponent(Graphics g) { /* ... Javadoc from previous version, ensure highlight logic is covered ... */
            super.paintComponent(g);
            if (model == null || model.getGameBoard() == null) {
                g.setColor(Color.BLACK);
                g.drawString("Game board not available.", PADDING, PADDING + 15);
                return;
            }

            Graphics2D g2d = (Graphics2D) g.create();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                @SuppressWarnings("unchecked") Grid<SameGameTile> board = (Grid<SameGameTile>) model.getGameBoard();

                for (int r = 0; r < this.numRows; r++) {
                    for (int c = 0; c < this.numCols; c++) {
                        SameGameTile tile = board.getEntity(r, c);
                        int x = PADDING + c * TILE_SIZE;
                        int y = PADDING + r * TILE_SIZE;
                        boolean isThisTileHighlighted = false;
                        if (groupToHighlight != null) {
                            for (Point p : groupToHighlight) {
                                if (p.x == r && p.y == c) { isThisTileHighlighted = true; break; }
                            }
                        }

                        if (tile != null && !tile.isEmpty()) {
                            Object visual = tile.getVisualRepresentation();
                            if (visual instanceof Color) {
                                g2d.setColor((Color) visual);
                                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                                g2d.setColor(((Color) visual).darker());
                                g2d.drawRect(x, y, TILE_SIZE -1 , TILE_SIZE -1);
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
         * Converts mouse coordinates to grid cell (row, col) {@link Point}.
         * @param mouseX Mouse x-coordinate.
         * @param mouseY Mouse y-coordinate.
         * @return {@link Point} with x=row, y=col, or null if outside grid.
         */
        public Point getTileCoordinatesForMouse(int mouseX, int mouseY) { /* ... Javadoc from previous version ... */
            if (model == null || model.getGameBoard() == null) return null;
            if (mouseX < PADDING || mouseY < PADDING || mouseX >= PADDING + this.numCols * TILE_SIZE || mouseY >= PADDING + this.numRows * TILE_SIZE) return null;
            int c = (mouseX - PADDING) / TILE_SIZE; int r = (mouseY - PADDING) / TILE_SIZE;
            if (r >= 0 && r < this.numRows && c >= 0 && c < this.numCols) return new Point(r, c);
            return null;
        }
    }
}