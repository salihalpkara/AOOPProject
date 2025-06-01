package com.aoopproject.framework.core;

/**
 * Represents the various statuses a game can be in.
 * This enum is used by the {@link AbstractGameModel} to indicate the
 * current state of the game, which can be observed by {@link GameView}s
 * or other {@link GameObserver}s.
 */
public enum GameStatus {
    /**
     * The game is actively being played.
     */
    PLAYING,

    /**
     * The game has not started yet or is being initialized.
     */
    INITIALIZING,

    /**
     * The game is paused. Player input is typically ignored.
     */
    PAUSED,

    /**
     * The game has ended, and the player has won.
     */
    GAME_OVER_WIN,

    /**
     * The game has ended, and the player has lost or can no longer make a move.
     */
    GAME_OVER_LOSE,

    /**
     * The game was explicitly ended by the user (e.g., by a quit action).
     */
    GAME_ENDED_USER_QUIT,

    /**
     * The game is ready to start, waiting for initial player action or a start command.
     */
    READY_TO_START
}