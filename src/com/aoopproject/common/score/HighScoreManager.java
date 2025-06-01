package com.aoopproject.common.score;

import com.aoopproject.common.model.DifficultyLevel;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages high scores for different difficulty levels of a game.
 * Scores are stored in a map where the key is the {@link DifficultyLevel}
 * and the value is a list of {@link ScoreEntry} objects.
 * Provides functionality to add scores, retrieve top scores, and save/load scores
 * from a file using Java serialization.
 */
public class HighScoreManager {
    private Map<DifficultyLevel, List<ScoreEntry>> allHighScores;
    private final String filename;
    private static final int MAX_SCORES_PER_LEVEL = 10;

    /**
     * Constructs a HighScoreManager.
     * Loads existing high scores from the specified file, or initializes an empty score map.
     * @param filename The name of the file to store/load high scores.
     */
    public HighScoreManager(String filename) {
        this.filename = filename;
        this.allHighScores = loadHighScores();
        for (DifficultyLevel level : DifficultyLevel.values()) {
            this.allHighScores.putIfAbsent(level, new ArrayList<>());
        }
    }

    /**
     * Adds a new score to the high score list for the specified difficulty level,
     * if the score is high enough to make it to the top list.
     * After adding, the scores are saved to the file.
     *
     * @param difficulty The difficulty level for which the score was achieved.
     * @param playerName The name of the player.
     * @param score      The score achieved by the player.
     * @return true if the score was added to the high score list, false otherwise.
     */
    public boolean addScore(DifficultyLevel difficulty, String playerName, int score) {
        List<ScoreEntry> scoresForLevel = allHighScores.getOrDefault(difficulty, new ArrayList<>());
        ScoreEntry newEntry = new ScoreEntry(playerName, score, difficulty, new Date());
        scoresForLevel.add(newEntry);
        Collections.sort(scoresForLevel);
        while (scoresForLevel.size() > MAX_SCORES_PER_LEVEL) {
            scoresForLevel.remove(scoresForLevel.size() - 1);
        }

        allHighScores.put(difficulty, scoresForLevel);
        boolean added = scoresForLevel.contains(newEntry) && scoresForLevel.indexOf(newEntry) < MAX_SCORES_PER_LEVEL;
        if (added) {
            saveHighScores();
        }
        return added;
    }

    /**
     * Checks if a given score qualifies for the high score list for a specific difficulty.
     * @param difficulty The difficulty level.
     * @param score The score to check.
     * @return true if the score is high enough, false otherwise.
     */
    public boolean isHighScore(DifficultyLevel difficulty, int score) {
        List<ScoreEntry> scoresForLevel = allHighScores.getOrDefault(difficulty, new ArrayList<>());
        if (scoresForLevel.size() < MAX_SCORES_PER_LEVEL) {
            return true;
        }
        return score > scoresForLevel.get(scoresForLevel.size() - 1).score();
    }


    /**
     * Retrieves the list of top high scores for a given difficulty level.
     *
     * @param difficulty The difficulty level.
     * @return A list of {@link ScoreEntry} objects, sorted from highest to lowest score.
     * Returns an empty list if no scores are present for that difficulty.
     */
    public List<ScoreEntry> getHighScores(DifficultyLevel difficulty) {
        return Collections.unmodifiableList(allHighScores.getOrDefault(difficulty, new ArrayList<>()));
    }

    /**
     * Retrieves the single top score for a given difficulty level.
     *
     * @param difficulty The difficulty level.
     * @return The top {@link ScoreEntry}, or {@code null} if no scores exist for that level.
     */
    public ScoreEntry getTopScore(DifficultyLevel difficulty) {
        List<ScoreEntry> scoresForLevel = allHighScores.getOrDefault(difficulty, new ArrayList<>());
        return scoresForLevel.isEmpty() ? null : scoresForLevel.get(0);
    }

    /**
     * Saves the current high scores map to a file using Java serialization.
     */
    @SuppressWarnings("unchecked")
    private Map<DifficultyLevel, List<ScoreEntry>> loadHighScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object data = ois.readObject();
            if (data instanceof Map) {
                return (Map<DifficultyLevel, List<ScoreEntry>>) data;
            }
        } catch (FileNotFoundException e) {
            System.out.println("High score file not found: " + filename + ". A new one will be created.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading high scores from " + filename + ": " + e.getMessage());
        }
        return new HashMap<>();
    }

    /**
     * Saves the current map of high scores to the specified file using serialization.
     */
    public void saveHighScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(allHighScores);
            System.out.println("High scores saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving high scores to " + filename + ": " + e.getMessage());
        }
    }
    public static int getMaxScoresPerLevel() {
        return MAX_SCORES_PER_LEVEL;
    }
}