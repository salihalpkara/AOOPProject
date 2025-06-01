package com.aoopproject.games.samegame;

import com.aoopproject.framework.core.AbstractGameController;

/**
 * Controller for the SameGame.
 * Currently, it relies mostly on the functionality provided by
 * {@link AbstractGameController}. It can be extended if SameGame
 * requires specific controller logic beyond user input processing.
 */
public class SameGameController extends AbstractGameController {

    public SameGameController() {
        super();
        System.out.println("SameGameController created.");
    }

    @Override
    public void startGame() {
        super.startGame();
        System.out.println("SameGame has started! Use the console to input moves (e.g., 'row col').");
    }
}