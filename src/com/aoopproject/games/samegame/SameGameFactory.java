package com.aoopproject.games.samegame;

import com.aoopproject.common.input.MouseInputStrategy;
import com.aoopproject.framework.core.AbstractGameController;
import com.aoopproject.framework.core.AbstractGameModel;
import com.aoopproject.framework.core.GameFactory;
import com.aoopproject.framework.core.GameView;
import com.aoopproject.framework.core.InputStrategy;
import com.aoopproject.games.samegame.observers.SameGameSoundObserver;
import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete factory for creating all necessary components for the SameGame.
 * This factory implements the {@link GameFactory} interface and is responsible for
 * instantiating the {@link SameGameModel} (after prompting the user for a {@link DifficultyLevel}),
 * {@link SameGameViewSwing}, {@link SameGameController}, the {@link MouseInputStrategy},
 * and the {@link SameGameSoundObserver}.
 * <p>
 * If the user cancels the difficulty selection dialog, model creation (and thus game setup)
 * might be aborted, in which case {@link #setupGame()} can return {@code null}.
 * </p>
 */
public class SameGameFactory implements GameFactory {

    /**
     * Stores the last selected difficulty by the user for this factory instance.
     * Can be used as a default for the next difficulty prompt.
     */
    private DifficultyLevel lastSelectedDifficulty = DifficultyLevel.MEDIUM;

    /**
     * Default constructor for SameGameFactory.
     * Initializes the factory. Game difficulty is prompted interactively when the model is created.
     */
    public SameGameFactory() {
        System.out.println("SameGameFactory created. Difficulty will be prompted on game setup.");
    }

    /**
     * Creates a {@link SameGameModel} instance for SameGame.
     * This method interactively prompts the user to select a predefined {@link DifficultyLevel}
     * (Easy, Medium, Hard) via a {@link JOptionPane}. The selected difficulty is then
     * passed to the {@link SameGameModel} constructor.
     *
     * @return A configured {@link SameGameModel} based on the user's selected difficulty.
     * Returns {@code null} if the user cancels the difficulty selection dialog, indicating
     * that game setup should be aborted.
     */
    @Override
    public AbstractGameModel createModel() {
        DifficultyLevel[] levels = DifficultyLevel.values();
        DifficultyLevel selectedLevel = (DifficultyLevel) JOptionPane.showInputDialog(
                null,
                "Select Difficulty Level:",
                "SameGame Difficulty",
                JOptionPane.QUESTION_MESSAGE,
                null,
                levels,
                this.lastSelectedDifficulty
        );

        if (selectedLevel != null) {
            this.lastSelectedDifficulty = selectedLevel;
            System.out.println("SameGame model will be created with difficulty: " + selectedLevel.getDisplayName());
            return new SameGameModel(selectedLevel);
        } else {
            System.out.println("Difficulty selection cancelled for SameGame. Model creation aborted.");
            return null;
        }
    }

    /**
     * Creates a list of {@link GameView}s for SameGame.
     * Currently, it creates and returns a list containing a single {@link SameGameViewSwing} instance.
     * If the provided model is {@code null} (e.g., if model creation was cancelled),
     * an empty list is returned.
     *
     * @param model The {@link AbstractGameModel} that the views will observe. Can be {@code null}.
     * @return A list containing the {@link SameGameViewSwing}, or an empty list if model is {@code null}.
     */
    @Override
    public List<GameView> createViews(AbstractGameModel model) {
        List<GameView> views = new ArrayList<>();
        if (model == null) {
            return views;
        }
        SameGameViewSwing swingView = new SameGameViewSwing();
        views.add(swingView);
        return views;
    }

    /**
     * Creates the game-specific controller, {@link SameGameController}.
     *
     * @return A new instance of {@link SameGameController}.
     */
    @Override
    public AbstractGameController createController() {
        return new SameGameController();
    }

    /**
     * This method from the {@link GameFactory} interface is not directly used by this factory's
     * overridden {@link #setupGame()} method, as {@link MouseInputStrategy} requires a controller
     * instance at creation, which is handled within {@code setupGame}.
     *
     * @return This implementation throws an {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException to indicate that input strategy creation is handled elsewhere.
     */
    @Override
    public InputStrategy createInputStrategy() {
        throw new UnsupportedOperationException(
                "For SameGameFactory (Swing version), InputStrategy (MouseInputStrategy) is created " +
                        "within the overridden setupGame() method as it requires a controller instance."
        );
    }

    /**
     * Sets up the complete SameGame application with a Swing UI, MouseInputStrategy, and SoundObserver.
     * This method overrides the default implementation from {@link GameFactory}.
     * It orchestrates the creation of the model (which prompts for difficulty settings),
     * controller, views, input strategy, and sound observer, and wires them together.
     * <p>
     * If model creation is cancelled by the user (e.g., by closing the difficulty settings dialog),
     * this method will return {@code null} to indicate that the game setup should be aborted,
     * allowing {@code MainApplication} to handle this gracefully (e.g., by exiting).
     * </p>
     *
     * @return A fully configured {@link AbstractGameController} ready to start the SameGame,
     * or {@code null} if the game setup was aborted.
     */
    @Override
    public AbstractGameController setupGame() {
        AbstractGameModel model = createModel();

        if (model == null) {
            System.out.println("SameGameFactory: Model creation failed or was cancelled by user. Aborting game setup.");
            return null;
        }
        AbstractGameController controller = createController();
        controller.setGameModel(model);
        List<GameView> views = createViews(model);
        SameGameViewSwing swingViewForMouseInput = null;
        for (GameView view : views) {
            controller.addGameView(view);
            if (view instanceof SameGameViewSwing) {
                swingViewForMouseInput = (SameGameViewSwing) view;
            }
        }
        InputStrategy strategy = new MouseInputStrategy(controller);
        if (swingViewForMouseInput != null) {
            System.out.println("SameGameFactory: Initializing MouseInputStrategy with SameGameViewSwing.");
            strategy.initialize(swingViewForMouseInput);
        } else {
            System.err.println("SameGameFactory: CRITICAL - No SameGameViewSwing found for MouseInputStrategy initialization.");
            strategy.initialize(null);
        }
        controller.setInputStrategy(strategy);
        SameGameSoundObserver soundObserver = new SameGameSoundObserver();
        model.addObserver(soundObserver);
        System.out.println("SameGameFactory: SameGameSoundObserver registered with the model.");

        System.out.println("SameGameFactory: All components created and wired successfully.");
        return controller;
    }
}