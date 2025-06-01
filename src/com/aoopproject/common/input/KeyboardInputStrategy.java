package com.aoopproject.common.input;

import com.aoopproject.framework.core.AbstractGameController;
import com.aoopproject.framework.core.GameAction;
import com.aoopproject.framework.core.GameView;
import com.aoopproject.framework.core.InputStrategy;
import com.aoopproject.games.sokoban.action.Direction;
import com.aoopproject.games.sokoban.action.SokobanMoveAction;
import com.aoopproject.games.sokoban.view.SokobanViewSwing;
import com.aoopproject.common.action.UndoAction;
import com.aoopproject.common.action.NewGameAction;
import javax.swing.JFrame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * An {@link InputStrategy} that captures keyboard arrow key presses for movement.
 * It is designed to work with a Swing-based view that provides a focusable JFrame.
 * When an arrow key is pressed, it creates a {@link SokobanMoveAction} and submits
 * it directly to the game controller.
 */
public class KeyboardInputStrategy implements InputStrategy {

    private JFrame targetFrame;
    private AbstractGameController gameController;
    private GameKeyListener keyListener;

    /**
     * Constructs a KeyboardInputStrategy.
     *
     * @param controller The game controller to which actions will be submitted.
     * Cannot be null.
     * @throws IllegalArgumentException if the controller is null.
     */
    public KeyboardInputStrategy(AbstractGameController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Game controller cannot be null for KeyboardInputStrategy.");
        }
        this.gameController = controller;
        this.keyListener = new GameKeyListener();
    }

    /**
     * Initializes the keyboard input strategy by attaching a KeyListener to the
     * main JFrame of the provided {@link GameView}.
     * The GameView is expected to be a {@link SokobanViewSwing} or another view
     * that can provide a JFrame instance via a {@code getFrame()} method.
     *
     * @param gameView The game view from which the JFrame is obtained.
     * It should be able to provide a JFrame.
     * @throws IllegalArgumentException if gameView is null or cannot provide a JFrame.
     */
    @Override
    public void initialize(GameView gameView) {
        if (gameView == null) {
            throw new IllegalArgumentException("GameView cannot be null for KeyboardInputStrategy initialization.");
        }
        if (gameView instanceof SokobanViewSwing) {
            this.targetFrame = ((SokobanViewSwing) gameView).getFrame();
        } else {
            try {
                java.lang.reflect.Method getFrameMethod = gameView.getClass().getMethod("getFrame");
                this.targetFrame = (JFrame) getFrameMethod.invoke(gameView);
            } catch (Exception e) {
                throw new IllegalArgumentException("KeyboardInputStrategy requires a GameView that can provide a JFrame (e.g., via getFrame() method). Provided view: " + gameView.getClass().getName(), e);
            }
        }

        if (this.targetFrame == null) {
            throw new IllegalStateException("Target JFrame could not be obtained from the GameView.");
        }
        for (java.awt.event.KeyListener kl : this.targetFrame.getKeyListeners()) {
            if (kl == this.keyListener) {
                this.targetFrame.removeKeyListener(kl);
            }
        }
        this.targetFrame.addKeyListener(this.keyListener);
        this.targetFrame.setFocusable(true);
        this.targetFrame.requestFocusInWindow();
        System.out.println("KeyboardInputStrategy initialized and attached to JFrame.");
    }

    /**
     * For an event-driven strategy like keyboard input with Swing, this method is not
     * actively used if actions are directly submitted to the controller via listeners.
     * It returns null as actions are handled by the KeyListener.
     *
     * @return null, as actions are submitted directly by the key listener.
     */
    @Override
    public GameAction solicitAction() {
        return null;
    }

    /**
     * Disposes of the keyboard input strategy by removing the KeyListener
     * from the target JFrame.
     */
    @Override
    public void dispose() {
        if (this.targetFrame != null && this.keyListener != null) {
            this.targetFrame.removeKeyListener(this.keyListener);
            System.out.println("KeyboardInputStrategy disposed, KeyListener removed.");
        }
        this.targetFrame = null;
        this.gameController = null;
    }

    /**
     * Inner class to handle keyboard events.
     * Listens for arrow key presses and submits corresponding {@link SokobanMoveAction}s
     * to the game controller.
     */
    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (gameController == null) return;

            Direction selectedDirection = null;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    selectedDirection = Direction.UP;
                    break;
                case KeyEvent.VK_DOWN:
                    selectedDirection = Direction.DOWN;
                    break;
                case KeyEvent.VK_LEFT:
                    selectedDirection = Direction.LEFT;
                    break;
                case KeyEvent.VK_RIGHT:
                    selectedDirection = Direction.RIGHT;
                    break;
                 case KeyEvent.VK_U:
                     gameController.submitUserAction(new UndoAction());
                     return;
                 case KeyEvent.VK_R:
                     gameController.submitUserAction(new NewGameAction());
                     return;
            }

            if (selectedDirection != null) {
                SokobanMoveAction action = new SokobanMoveAction(selectedDirection);
                gameController.submitUserAction(action);
                e.consume();
            }
        }
    }
}