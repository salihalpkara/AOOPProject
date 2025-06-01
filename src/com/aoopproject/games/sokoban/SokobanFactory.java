package com.aoopproject.games.sokoban;

import com.aoopproject.framework.core.AbstractGameController;
import com.aoopproject.framework.core.AbstractGameModel;
import com.aoopproject.framework.core.GameFactory;
import com.aoopproject.framework.core.GameView;
import com.aoopproject.framework.core.InputStrategy;
import com.aoopproject.common.input.KeyboardInputStrategy;
import com.aoopproject.common.model.DifficultyLevel;
import com.aoopproject.games.sokoban.model.SokobanModel;
import com.aoopproject.games.sokoban.view.SokobanViewSwing;


import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete factory for creating all necessary components for the Sokoban game.
 * This factory implements the {@link GameFactory} interface and is responsible for
 * instantiating the {@link SokobanModel} (with a default level), {@link SokobanViewSwing},
 * a generic {@link AbstractGameController}, and the {@link KeyboardInputStrategy}.
 * It ensures all components are correctly wired together.
 */
public class SokobanFactory implements GameFactory {
    private DifficultyLevel lastSelectedDifficulty = DifficultyLevel.MEDIUM;


    /**
     * Default constructor for SokobanFactory.
     */
    public SokobanFactory() {
        System.out.println("SokobanFactory created.");
    }

    /**
     * Creates a {@link SokobanModel} instance for Sokoban, initialized with a default level.
     *
     * @return A new {@link SokobanModel}.
     */
    @Override
    public AbstractGameModel createModel() {
        DifficultyLevel[] levels = DifficultyLevel.values();
        DifficultyLevel choice = (DifficultyLevel) JOptionPane.showInputDialog(
                null, "Select Sokoban Difficulty:", "Sokoban Difficulty",
                JOptionPane.QUESTION_MESSAGE, null, levels, this.lastSelectedDifficulty);

        if (choice != null) {
            this.lastSelectedDifficulty = choice;
            System.out.println("Sokoban model will be created with difficulty: " + choice.getDisplayName());
            return new SokobanModel(choice);
        } else {
            System.out.println("Sokoban difficulty selection cancelled. Model creation aborted.");
            return null;
        }
    }

    /**
     * Creates a list of {@link GameView}s for Sokoban.
     * Currently, it creates and returns a list containing a single {@link SokobanViewSwing} instance.
     *
     * @param model The {@link AbstractGameModel} that the views will observe.
     * @return A list containing the {@link SokobanViewSwing}.
     */
    @Override
    public List<GameView> createViews(AbstractGameModel model) {
        List<GameView> views = new ArrayList<>();
        if (model == null) {
            return views;
        }
        SokobanViewSwing swingView = new SokobanViewSwing();
        views.add(swingView);
        return views;
    }

    /**
     * Creates the game controller. For Sokoban, a generic {@link AbstractGameController}
     * is currently used as no game-specific controller logic has been identified yet.
     *
     * @return A new instance of {@link AbstractGameController}.
     */
    @Override
    public AbstractGameController createController() {
        return new AbstractGameController() {
        };
    }

    /**
     * This method from the {@link GameFactory} interface is not directly used by this factory's
     * overridden {@link #setupGame()} method for creating {@link KeyboardInputStrategy},
     * as that strategy requires a controller instance which is handled within {@code setupGame}.
     *
     * @return This implementation throws an {@link UnsupportedOperationException}.
     */
    @Override
    public InputStrategy createInputStrategy() {
        throw new UnsupportedOperationException(
                "For SokobanFactory, InputStrategy (KeyboardInputStrategy) is created " +
                        "within the overridden setupGame() method as it typically requires context " +
                        "like a controller or specific view components for initialization."
        );
    }

    /**
     * Sets up the complete Sokoban game application with a Swing UI and KeyboardInputStrategy.
     * This method orchestrates the creation of the model, controller, views, and input strategy,
     * and wires them together, including initializing the strategy with the correct view component.
     *
     * @return A fully configured {@link AbstractGameController} ready to start Sokoban,
     * or {@code null} if the game setup was aborted.
     */
    @Override
    public AbstractGameController setupGame() {
        AbstractGameModel model = createModel();
        if (model == null) {
            System.err.println("SokobanFactory: Model creation failed. Aborting game setup.");
            return null;
        }
        AbstractGameController controller = createController();
        controller.setGameModel(model);
        List<GameView> views = createViews(model);
        SokobanViewSwing swingViewForKeyboardInput = null;
        for (GameView view : views) {
            controller.addGameView(view);
            if (view instanceof SokobanViewSwing) {
                swingViewForKeyboardInput = (SokobanViewSwing) view;
            }
        }
        InputStrategy strategy = new KeyboardInputStrategy(controller);
        if (swingViewForKeyboardInput != null) {
            System.out.println("SokobanFactory: Initializing KeyboardInputStrategy with SokobanViewSwing's JFrame.");
            strategy.initialize(swingViewForKeyboardInput);
        } else {
            System.err.println("SokobanFactory: CRITICAL - No SokobanViewSwing found for KeyboardInputStrategy initialization.");
            strategy.initialize(null);
        }
        controller.setInputStrategy(strategy);

        System.out.println("SokobanFactory: All Sokoban components created and wired successfully.");
        return controller;
    }
}