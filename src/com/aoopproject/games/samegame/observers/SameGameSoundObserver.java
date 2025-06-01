package com.aoopproject.games.samegame.observers;

import com.aoopproject.framework.core.GameEvent;
import com.aoopproject.framework.core.GameObserver;
import com.aoopproject.framework.core.GameStatus;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Observes SameGame events and plays corresponding sound effects.
 * Sounds are loaded from the "/samegame/sounds/" resource path.
 */
public class SameGameSoundObserver implements GameObserver {

    private final Map<String, String> eventSoundMap;
    private boolean soundsEnabled = true;

    /**
     * Initializes the SameGameSoundObserver and sets up sound mappings.
     */
    public SameGameSoundObserver() {
        eventSoundMap = new HashMap<>();
        eventSoundMap.put("TILES_REMOVED_SUCCESS", "/samegame/sounds/tile_remove.wav");
        eventSoundMap.put("INVALID_SELECTION", "/samegame/sounds/invalid_move.wav");
        eventSoundMap.put("GAME_OVER_WIN_SOUND", "/samegame/sounds/game_win.wav");
        eventSoundMap.put("GAME_OVER_LOSE_SOUND", "/samegame/sounds/game_lose.wav");
        eventSoundMap.put("UNDO_PERFORMED_SOUND", "/samegame/sounds/undo.wav");
        eventSoundMap.put("NEW_GAME_STARTED_SOUND", "/samegame/sounds/new_game.wav");
        eventSoundMap.put("NEW_HIGH_SCORE_ACHIEVED_SOUND", "/samegame/sounds/high_score.wav");
    }

    /**
     * Enables or disables sound playback.
     * @param enabled true to enable sounds, false to disable.
     */
    public void setSoundsEnabled(boolean enabled) {
        this.soundsEnabled = enabled;
    }

    @Override
    public void onGameEvent(GameEvent event) {
        if (!soundsEnabled) {
            return;
        }

        String soundFile = null;
        String eventType = event.getType();

        switch (eventType) {
            case "TILES_REMOVED_SUCCESS":
                soundFile = eventSoundMap.get(eventType);
                break;
            case "INVALID_SELECTION":
                soundFile = eventSoundMap.get(eventType);
                break;
            case "UNDO_PERFORMED":
                soundFile = eventSoundMap.get("UNDO_PERFORMED_SOUND");
                break;
            case "NEW_GAME_STARTED":
                soundFile = eventSoundMap.get("NEW_GAME_STARTED_SOUND");
                break;
            case "NEW_HIGH_SCORE_ACHIEVED":
                soundFile = eventSoundMap.get("NEW_HIGH_SCORE_ACHIEVED_SOUND");
                break;
            case "STATUS_CHANGED":
                if (event.getPayload() instanceof GameStatus) {
                    GameStatus status = (GameStatus) event.getPayload();
                    if (status == GameStatus.GAME_OVER_WIN) {
                        soundFile = eventSoundMap.get("GAME_OVER_WIN_SOUND");
                    } else if (status == GameStatus.GAME_OVER_LOSE) {
                        soundFile = eventSoundMap.get("GAME_OVER_LOSE_SOUND");
                    }
                }
                break;
        }

        if (soundFile != null) {
            playSound(soundFile);
        }
    }

    private void playSound(String soundFileName) {
        System.out.println("Attempting to play sound: " + soundFileName);
        try {
            InputStream audioSrc = SameGameSoundObserver.class.getResourceAsStream(soundFileName);
            if (audioSrc == null) {
                System.err.println("Sound file not found in resources: " + soundFileName);
                return;
            }
            System.out.println("Sound file found, creating streams for: " + soundFileName);
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
                        System.err.println("Error closing audio input stream for " + soundFileName + ": " + e.getMessage());
                    }
                }
            });

            clip.open(audioIn);
            clip.start();
            System.out.println("Successfully started playing: " + soundFileName);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound " + soundFileName + ": " + e.getMessage());
        }
    }
}