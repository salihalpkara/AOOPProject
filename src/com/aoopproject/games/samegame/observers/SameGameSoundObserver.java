package com.aoopproject.games.samegame.observers;

import com.aoopproject.framework.core.AbstractGameModel;
import com.aoopproject.framework.core.GameEvent;
import com.aoopproject.framework.core.GameObserver;
import com.aoopproject.framework.core.GameStatus;
import com.aoopproject.common.score.HighScoreManager;
import com.aoopproject.common.model.DifficultyLevel;
import com.aoopproject.games.samegame.SameGameModel;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Observes {@link SameGameModel} events and plays corresponding sound effects.
 * This observer is responsible for selecting the appropriate sound when a game ends:
 * if the game end results in a new high score, a specific "high score" sound is played;
 * otherwise, the standard "game win" or "game lose" sound is played.
 * It requires a {@link HighScoreManager} instance to determine if a score is a high score.
 * Sound files are expected to be in the "/samegame/sounds/" resource path.
 */
public class SameGameSoundObserver implements GameObserver {

    private final Map<String, String> eventSoundMap;
    private boolean soundsEnabled = true;
    private final HighScoreManager highScoreManager;

    /**
     * Initializes the SameGameSoundObserver with a reference to the HighScoreManager.
     * It also pre-loads mappings between game event types and sound file names.
     *
     * @param highScoreManager The shared {@link HighScoreManager} instance used to check
     * if a game end resulted in a high score, to play the appropriate sound.
     * Must not be {@code null}.
     * @throws IllegalArgumentException if highScoreManager is {@code null}.
     */
    public SameGameSoundObserver(HighScoreManager highScoreManager) {
        this.highScoreManager = Objects.requireNonNull(highScoreManager, "HighScoreManager cannot be null for SoundObserver.");

        this.eventSoundMap = new HashMap<>();
        eventSoundMap.put("TILES_REMOVED_SUCCESS", "/samegame/sounds/tile_remove.wav");
        eventSoundMap.put("INVALID_SELECTION", "/samegame/sounds/invalid_move.wav");
        eventSoundMap.put("GAME_OVER_WIN_NORMAL_SOUND", "/samegame/sounds/game_win.wav");
        eventSoundMap.put("GAME_OVER_LOSE_NORMAL_SOUND", "/samegame/sounds/game_lose.wav");
        eventSoundMap.put("UNDO_PERFORMED", "/samegame/sounds/undo.wav");
        eventSoundMap.put("NEW_GAME_STARTED", "/samegame/sounds/new_game.wav");
        eventSoundMap.put("NEW_HIGH_SCORE_SOUND", "/samegame/sounds/high_score.wav");
    }

    /**
     * Enables or disables sound playback.
     * @param enabled {@code true} to enable sounds, {@code false} to disable.
     */
    public void setSoundsEnabled(boolean enabled) {
        this.soundsEnabled = enabled;
    }

    /**
     * Handles game events received from the {@link SameGameModel}.
     * For game end events (win/loss detected via "STATUS_CHANGED"), this method checks
     * with the {@link HighScoreManager} if the current score is a new high score for the
     * current difficulty. If it is, the "new high score" sound is played. Otherwise,
     * the standard "game win" or "game lose" sound is played.
     * Other game events trigger their directly mapped sounds.
     *
     * @param event The {@link GameEvent} that occurred.
     */
    @Override
    public void onGameEvent(GameEvent event) {
        if (!soundsEnabled) {
            return;
        }
        if (event == null || event.getSource() == null) {
            System.err.println("SoundObserver: Received null event or event with null source.");
            return;
        }

        String soundFileToPlay = null;
        String eventType = event.getType();
        Object eventSource = event.getSource();

        switch (eventType) {
            case "TILES_REMOVED_SUCCESS":
                soundFileToPlay = eventSoundMap.get(eventType);
                break;
            case "INVALID_SELECTION":
                soundFileToPlay = eventSoundMap.get(eventType);
                break;
            case "UNDO_PERFORMED":
                soundFileToPlay = eventSoundMap.get(eventType);
                break;
            case "NEW_GAME_STARTED":
                soundFileToPlay = eventSoundMap.get(eventType);
                break;
            case "NEW_HIGH_SCORE_ACHIEVED":
                System.out.println("SoundObserver: NEW_HIGH_SCORE_ACHIEVED event noted by observer.");
                soundFileToPlay = null;
                break;
            case "STATUS_CHANGED":
                if (event.getPayload() instanceof GameStatus && eventSource instanceof SameGameModel) {
                    GameStatus status = (GameStatus) event.getPayload();
                    SameGameModel gameModel = (SameGameModel) eventSource;
                    DifficultyLevel difficulty = gameModel.getCurrentDifficulty();

                    if (difficulty == null) {
                        System.err.println("SoundObserver: Difficulty is null in game model for STATUS_CHANGED event, cannot determine high score context for sound.");
                        break;
                    }
                    int currentScore = gameModel.getScore();

                    if (status == GameStatus.GAME_OVER_WIN) {
                        if (highScoreManager.isHighScore(difficulty, currentScore)) {
                            soundFileToPlay = eventSoundMap.get("NEW_HIGH_SCORE_SOUND");
                        } else {
                            soundFileToPlay = eventSoundMap.get("GAME_OVER_WIN_NORMAL_SOUND");
                        }
                    } else if (status == GameStatus.GAME_OVER_LOSE) {
                        if (highScoreManager.isHighScore(difficulty, currentScore)) {
                            soundFileToPlay = eventSoundMap.get("NEW_HIGH_SCORE_SOUND");
                        } else {
                            soundFileToPlay = eventSoundMap.get("GAME_OVER_LOSE_NORMAL_SOUND");
                        }
                    }
                } else if (!(eventSource instanceof SameGameModel) && event.getPayload() instanceof GameStatus) {
                    System.out.println("SoundObserver: STATUS_CHANGED event from non-SameGameModel source or unexpected payload.");
                }
                break;
            default:
                break;
        }

        if (soundFileToPlay != null) {
            playSound(soundFileToPlay);
        }
    }

    /**
     * Plays a sound file specified by its resource path.
     * The sound file must be a format supported by Java's Sound API (typically .wav).
     * The method handles loading the audio clip, playing it, and ensuring resources
     * (clip and streams) are closed after playback finishes.
     *
     * @param soundFileName The path to the sound file, relative to the classpath root
     * (e.g., "/samegame/sounds/effect.wav").
     */
    private void playSound(String soundFileName) {
        try {
            InputStream audioSrc = SameGameSoundObserver.class.getResourceAsStream(soundFileName);
            if (audioSrc == null) {
                System.err.println("SoundObserver: Sound file NOT FOUND in resources: " + soundFileName);
                return;
            }
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            final AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);

            final Clip clip = AudioSystem.getClip();
            clip.addLineListener(event -> {
                if (LineEvent.Type.STOP == event.getType()) {
                    Clip stoppedClip = (Clip) event.getLine();
                    stoppedClip.close();
                    try {
                        if (audioIn != null) {
                            audioIn.close();
                        }
                    } catch (IOException e) {
                        System.err.println("SoundObserver: Error closing audio input stream for " + soundFileName + ": " + e.getMessage());
                    }
                }
            });

            clip.open(audioIn);
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("SoundObserver: Error playing sound " + soundFileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}