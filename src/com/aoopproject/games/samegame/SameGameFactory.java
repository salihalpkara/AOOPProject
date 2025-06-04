package com.aoopproject.games.samegame;

import com.aoopproject.common.input.MouseInputStrategy;
import com.aoopproject.common.model.DifficultyLevel;
import com.aoopproject.common.score.HighScoreManager;
import com.aoopproject.framework.core.AbstractGameController;
import com.aoopproject.framework.core.AbstractGameModel;
import com.aoopproject.framework.core.GameFactory;
import com.aoopproject.framework.core.GameView;
import com.aoopproject.framework.core.InputStrategy;
import com.aoopproject.games.samegame.observers.SameGameSoundObserver;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Concrete factory for creating all necessary components for the SameGame.
 * This factory interactively prompts for difficulty settings and then instantiates
 * and wires together the {@link SameGameModel}, {@link SameGameViewSwing},
 * a generic {@link AbstractGameController}, the {@link MouseInputStrategy},
 * and the {@link SameGameSoundObserver}.
 * <p>
 * A single instance of {@link HighScoreManager} is created by this factory and
 * shared with both the {@code SameGameViewSwing} (for displaying and submitting scores)
 * and the {@code SameGameSoundObserver} (for determining if a game end resulted
 * in a high score to play the appropriate sound).
 * </p>
 * If model creation is cancelled by the user (e.g., closing the difficulty settings dialog),
 * game setup might be aborted, in which case {@link #setupGame()} can return {@code null}.
 */
public class SameGameFactory implements GameFactory {

    /** Stores the last selected difficulty by the user for this factory instance. */
    private DifficultyLevel lastSelectedDifficulty = DifficultyLevel.MEDIUM;

    /**
     * Default constructor for SameGameFactory.
     */
    public SameGameFactory() {
        System.out.println("SameGameFactory created. Difficulty will be prompted on game setup.");
    }

    /**
     * Creates a {@link SameGameModel} instance after prompting the user for a {@link DifficultyLevel}.
     * @return A configured {@link SameGameModel}, or {@code null} if settings dialog is cancelled.
     */
    @Override
    public AbstractGameModel createModel() {
        DifficultyLevel[] levels = DifficultyLevel.values();
        DifficultyLevel selectedLevel = (DifficultyLevel) JOptionPane.showInputDialog(
                null, "Select Difficulty Level:", "SameGame Difficulty",
                JOptionPane.QUESTION_MESSAGE, null, levels, this.lastSelectedDifficulty);

        if (selectedLevel != null) {
            this.lastSelectedDifficulty = selectedLevel;
            System.out.println("SameGame model to be created with difficulty: " + selectedLevel.getDisplayName());
            return new SameGameModel(selectedLevel);
        } else {
            System.out.println("Difficulty selection cancelled. Model creation aborted.");
            return null;
        }
    }

    /**
     * Creates a list of {@link GameView} instances for the SameGame.
     * This implementation now creates and returns both a {@link SameGameViewSwing}
     * for graphical user interaction and a {@link SameGameViewConsole} for
     * text-based console output. Both views will observe the provided game model.
     * <p>
     * The shared {@link com.aoopproject.common.score.HighScoreManager} is typically set on the
     * {@code SameGameViewSwing} instance later by the {@link #setupGame()} method
     * before the view's {@code initialize} method is called by the controller.
     * The {@code initialize} method for both views (which associates them with the model)
     * will be called by the controller when they are added via {@code controller.addGameView(view)}.
     * </p>
     *
     * @param model The {@link AbstractGameModel} that the views will observe. This model
     * should not be {@code null} for views to be created meaningfully.
     * @return A list containing a {@link SameGameViewSwing} instance and a
     * {@link SameGameViewConsole} instance. Returns an empty list if the provided
     * model is {@code null}.
     */
    @Override
    public List<GameView> createViews(AbstractGameModel model) {
        List<GameView> views = new ArrayList<>();
        if (model == null) {
            return views;
        }
        SameGameViewSwing swingView = new SameGameViewSwing();

        views.add(swingView);

        SameGameViewConsole consoleView = new SameGameViewConsole();
        views.add(consoleView);
        return views;
    }

    /**
     * Creates a generic {@link AbstractGameController}.
     * Assumes {@link AbstractGameController} is a concrete class or has a suitable concrete subclass.
     * @return A new {@link AbstractGameController}.
     */
    @Override
    public AbstractGameController createController() {
        return new AbstractGameController();
    }

    /**
     * This method from the {@link GameFactory} interface is not directly used by this factory's
     * overridden {@link #setupGame()} method for creating {@link MouseInputStrategy},
     * as that strategy requires a controller instance which is handled within {@code setupGame}.
     *
     * @return This implementation throws an {@link UnsupportedOperationException}.
     */
    @Override
    public InputStrategy createInputStrategy() {
        throw new UnsupportedOperationException(
                "For SameGameFactory's Swing setup, MouseInputStrategy is created and initialized within setupGame()."
        );
    }

    /**
     * Sets up the complete SameGame application with a Swing UI and relevant components.
     * This method orchestrates the creation and wiring of:
     * <ol>
     * <li>A shared {@link HighScoreManager}.</li>
     * <li>The {@link SameGameModel} (which prompts for difficulty).</li>
     * <li>The {@link AbstractGameController}, linking it with the model.</li>
     * <li>The {@link SameGameViewSwing}, providing it with the shared {@code HighScoreManager},
     * and adding it to the controller (which also initializes the view with the model).</li>
     * <li>The {@link MouseInputStrategy}, linking it to the controller and initializing it
     * with the {@code SameGameViewSwing} instance.</li>
     * <li>The {@link SameGameSoundObserver}, providing it with the shared {@code HighScoreManager}
     * and registering it with the model.</li>
     * </ol>
     * If model creation is cancelled by the user (e.g., by closing the difficulty settings dialog),
     * this method will return {@code null}, signaling that the game setup should be aborted.
     *
     * @return A fully configured {@link AbstractGameController} ready to start SameGame,
     * or {@code null} if the game setup was aborted.
     */
    @Override
    public AbstractGameController setupGame() {
        System.out.println("SameGameFactory: Starting game setup...");
        HighScoreManager highScoreManager = new HighScoreManager(SameGameViewSwing.HIGH_SCORE_FILE);
        System.out.println("SameGameFactory: HighScoreManager created for file: " + SameGameViewSwing.HIGH_SCORE_FILE);
        AbstractGameModel model = createModel();
        if (model == null) {
            System.out.println("SameGameFactory: Model creation failed or was cancelled. Aborting game setup.");
            return null;
        }
        AbstractGameController controller = createController();
        Objects.requireNonNull(controller, "Controller cannot be null from createController()");
        controller.setGameModel(model);
        System.out.println("SameGameFactory: Controller created and model linked.");
        List<GameView> views = createViews(model);
        SameGameViewSwing swingViewInstance = null;

        for (GameView view : views) {
            if (view instanceof SameGameViewSwing) {
                swingViewInstance = (SameGameViewSwing) view;
                swingViewInstance.setHighScoreManager(highScoreManager);
                System.out.println("SameGameFactory: HighScoreManager set for SameGameViewSwing.");
            }
            controller.addGameView(view);
        }
        InputStrategy strategy = new MouseInputStrategy(controller);

        if (swingViewInstance != null) {
            System.out.println("SameGameFactory: Initializing MouseInputStrategy with SameGameViewSwing.");
            strategy.initialize(swingViewInstance);
        } else {
            System.err.println("SameGameFactory: WARNING - No SameGameViewSwing instance found to initialize MouseInputStrategy.");
            strategy.initialize(views.isEmpty() ? null : views.getFirst());
        }
        controller.setInputStrategy(strategy);
        SameGameSoundObserver soundObserver = new SameGameSoundObserver(highScoreManager);
        model.addObserver(soundObserver);
        System.out.println("SameGameFactory: SameGameSoundObserver created with shared HighScoreManager and registered with model.");

        System.out.println("SameGameFactory: All SameGame components created and wired successfully.");
        return controller;
    }
}