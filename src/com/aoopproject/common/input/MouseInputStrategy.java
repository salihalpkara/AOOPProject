package com.aoopproject.common.input;

import com.aoopproject.framework.core.AbstractGameController;
import com.aoopproject.framework.core.GameAction;
import com.aoopproject.framework.core.GameView;
import com.aoopproject.framework.core.InputStrategy;
import com.aoopproject.games.samegame.SameGameViewSwing;
import com.aoopproject.games.samegame.action.SameGameSelectAction;

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * An {@link InputStrategy} that captures mouse clicks on a {@link SameGameViewSwing.GamePanel}.
 * It converts mouse coordinates to tile coordinates and generates {@link SameGameSelectAction}s.
 */
public class MouseInputStrategy implements InputStrategy {

    private SameGameViewSwing swingView;
    private SameGameViewSwing.GamePanel gamePanel;
    private AbstractGameController gameController;


    /**
     * Constructs a MouseInputStrategy.
     * Requires a reference to the game controller to submit actions.
     * @param controller The game controller to which actions will be submitted.
     */
    public MouseInputStrategy(AbstractGameController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Game controller cannot be null for MouseInputStrategy.");
        }
        this.gameController = controller;
    }

    @Override
    public void initialize(GameView gameView) {
        if (gameView == null) {
            throw new IllegalArgumentException("GameView passed to MouseInputStrategy.initialize cannot be null. Ensure a view is available.");
        }
        if (!(gameView instanceof SameGameViewSwing)) {
            throw new IllegalArgumentException("MouseInputStrategy requires a SameGameViewSwing instance. Received: " + gameView.getClass().getName());
        }
        this.swingView = (SameGameViewSwing) gameView;
        if (!(gameView instanceof SameGameViewSwing)) {
            throw new IllegalArgumentException("MouseInputStrategy requires a SameGameViewSwing instance.");
        }
        this.swingView = (SameGameViewSwing) gameView;
        this.gamePanel = this.swingView.getGamePanel();

        if (this.gamePanel == null) {
            throw new IllegalStateException("GamePanel is null in SameGameViewSwing. Ensure view is fully initialized.");
        }
        for (java.awt.event.MouseListener ml : this.gamePanel.getMouseListeners()) {
            if (ml instanceof PanelMouseListener) {
                this.gamePanel.removeMouseListener(ml);
            }
        }

        this.gamePanel.addMouseListener(new PanelMouseListener());
        System.out.println("MouseInputStrategy initialized and attached to GamePanel.");
    }

    /**
     * For an event-driven strategy like mouse input with Swing, this method might not be
     * actively used if actions are directly submitted to the controller via listeners.
     * It could block indefinitely or throw an UnsupportedOperationException.
     * For this implementation, it will return null or could be made to block if a
     * hybrid model was desired (not typical for GUI mouse input).
     *
     * @return null, as actions are submitted directly by the mouse listener.
     */
    @Override
    public GameAction solicitAction() {
        System.out.println("MouseInputStrategy.solicitAction() called, but actions are event-driven. Returning null.");
        return null;
    }

    @Override
    public void dispose() {
        if (this.gamePanel != null) {
            for (java.awt.event.MouseListener ml : this.gamePanel.getMouseListeners()) {
                if (ml instanceof PanelMouseListener) {
                    this.gamePanel.removeMouseListener(ml);
                }
            }
        }
        System.out.println("MouseInputStrategy disposed.");
    }

    /**
     * Inner class to handle mouse events on the GamePanel.
     * This listener converts click coordinates from the mouse event into
     * game-specific tile coordinates. It then creates a {@link SameGameSelectAction}
     * and submits this action directly to the game controller via its
     * {@link AbstractGameController#submitUserAction(GameAction)} method.
     */
    private class PanelMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (gamePanel == null || gameController == null) return;

            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }

            Point tileCoords = gamePanel.getTileCoordinatesForMouse(e.getX(), e.getY());

            if (tileCoords != null) {
                int row = tileCoords.x;
                int col = tileCoords.y;

                SameGameSelectAction action = new SameGameSelectAction(row, col);
                System.out.println("Mouse click generated action: " + action.getName());
                gameController.submitUserAction(action);
            }
        }
    }
}