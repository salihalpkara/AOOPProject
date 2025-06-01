package com.aoopproject.common.score;

import com.aoopproject.common.model.DifficultyLevel;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents a single high score entry.
 * Includes player name, score, the difficulty level at which the score was achieved, and the date.
 * Must be Serializable to be saved to a file.
 */
public record ScoreEntry(String playerName, int score, DifficultyLevel difficulty, Date date)
        implements Serializable, Comparable<ScoreEntry> {
    @Override
    public int compareTo(ScoreEntry other) {
        int scoreCompare = Integer.compare(other.score, this.score);
        if (scoreCompare == 0) {
            return other.date.compareTo(this.date);
        }
        return scoreCompare;
    }

    @Override
    public String toString() {
        return String.format("%s: %d (%s - %s)", playerName, score, difficulty.getDisplayName(), date.toString());
    }
}