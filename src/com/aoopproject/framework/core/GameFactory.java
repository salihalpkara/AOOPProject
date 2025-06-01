package com.aoopproject.framework.core;

import java.util.List;

/**
 * Defines a factory for creating the core components of a game.
 * Each specific game (e.g., SameGame, 2048) will implement this interface
 * to provide instances of its concrete model, views, and controller.
 * This pattern decouples the game setup logic from the main application launcher.
 */
public interface GameFactory {

    /**
     * Creates and returns an instance of the game's specific {@link AbstractGameModel}.
     *
     * @return A new game model instance.
     */
    AbstractGameModel createModel();

    /**
     * Creates and returns a list of {@link GameView}s for the game.
     * A game might have multiple views (e.g., a GUI view and a console view).
     *
     * @param model The game model that the views will observe.
     * @return A list of new game view instances.
     */
    List<GameView> createViews(AbstractGameModel model);

    /**
     * Creates and returns an instance of the game's specific {@link AbstractGameController}.
     *
     * @return A new game controller instance.
     */
    AbstractGameController createController();

    /**
     * Creates and returns an instance of the default {@link InputStrategy} for the game.
     *
     * @return A new input strategy instance.
     */
    InputStrategy createInputStrategy();

    /**
     * A convenience method to assemble a fully configured game.
     * It typically creates the model, views, input strategy, and controller,
     * and wires them together.
     *
     * @return A configured {@link AbstractGameController} ready to start the game.
     */
    default AbstractGameController setupGame() {
        AbstractGameModel model = createModel();
        AbstractGameController controller = createController();
        InputStrategy strategy = createInputStrategy();

        controller.setGameModel(model);
        List<GameView> views = createViews(model);
        for (GameView view : views) {
            controller.addGameView(view);
        }
        controller.setInputStrategy(strategy);

        return controller;
    }
}