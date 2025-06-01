package com.aoopproject.common.score;

import com.aoopproject.common.model.DifficultyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link HighScoreManager} class.
 * Tests cover adding scores, retrieving scores, saving to, and loading from a file.
 */
class HighScoreManagerTest {

    @TempDir
    Path tempDir;

    private HighScoreManager highScoreManager;
    private String testFilename;

    /**
     * Sets up a new HighScoreManager instance using a temporary file before each test.
     * This ensures that each test runs with a fresh, isolated high score file.
     */
    @BeforeEach
    void setUp() {
        File testFile = tempDir.resolve("test_highscores.dat").toFile();
        testFilename = testFile.getAbsolutePath();

        highScoreManager = new HighScoreManager(testFilename);
    }

    /**
     * Optional: Cleans up the test file after each test if not using @TempDir's auto-cleanup effectively,
     * or if specific cleanup logic is needed. @TempDir usually handles this.
     */

    /**
     * Tests adding a single score to an empty list for a specific difficulty.
     * Verifies that the score is added, the list size is 1, and it's the top score.
     */
    @Test
    void testAddScore_emptyList_isAddedAndIsTop() {
        boolean added = highScoreManager.addScore(DifficultyLevel.EASY, "Player1", 100);
        assertTrue(added, "Score should be added to an empty list if it qualifies (or fills up).");

        List<ScoreEntry> easyScores = highScoreManager.getHighScores(DifficultyLevel.EASY);
        assertEquals(1, easyScores.size(), "Easy scores list should have 1 entry.");
        assertEquals("Player1", easyScores.get(0).playerName(), "Player name should match.");
        assertEquals(100, easyScores.get(0).score(), "Score should match.");
        assertEquals(DifficultyLevel.EASY, easyScores.get(0).difficulty(), "Difficulty should match.");

        ScoreEntry topEasyScore = highScoreManager.getTopScore(DifficultyLevel.EASY);
        assertNotNull(topEasyScore, "Top score for Easy should not be null.");
        assertEquals(100, topEasyScore.score(), "Top score for Easy should be 100.");
    }

    /**
     * Tests that adding scores fills up the list up to MAX_SCORES_PER_LEVEL
     * and that they are sorted correctly.
     */
    @Test
    void testAddScore_fillUpListAndOrder() {
        int maxScores = 10;

        highScoreManager.addScore(DifficultyLevel.MEDIUM, "P1", 10);
        highScoreManager.addScore(DifficultyLevel.MEDIUM, "P2", 50);
        highScoreManager.addScore(DifficultyLevel.MEDIUM, "P3", 20);
        highScoreManager.addScore(DifficultyLevel.MEDIUM, "P4", 5);
        highScoreManager.addScore(DifficultyLevel.MEDIUM, "P5", 100);

        List<ScoreEntry> mediumScores = highScoreManager.getHighScores(DifficultyLevel.MEDIUM);
        assertEquals(5, mediumScores.size(), "Should have 5 scores for Medium.");
        assertEquals(100, mediumScores.get(0).score(), "P5 should be top score.");
        assertEquals(50, mediumScores.get(1).score(), "P2 should be second.");
        assertEquals(20, mediumScores.get(2).score(), "P3 should be third.");
        assertEquals(10, mediumScores.get(3).score(), "P1 should be fourth.");
        assertEquals(5, mediumScores.get(4).score(), "P4 should be fifth.");
    }

    /**
     * Tests adding a score when the list is full and the new score is high enough to be included.
     * Verifies that the new score is added, the lowest score is removed, and the list remains sorted.
     */
    @Test
    void testAddScore_listFull_newHighScoreAdded_lowestRemoved() {
        int maxScores = 10;
        for (int i = 0; i < maxScores; i++) {
            highScoreManager.addScore(DifficultyLevel.HARD, "Player" + i, (i + 1) * 10);
        }

        List<ScoreEntry> hardScoresBefore = highScoreManager.getHighScores(DifficultyLevel.HARD);
        assertEquals(maxScores, hardScoresBefore.size(), "Hard scores list should be full.");
        assertEquals(10, hardScoresBefore.get(maxScores - 1).score(), "Lowest score should be 10 before adding new high score.");
        boolean added = highScoreManager.addScore(DifficultyLevel.HARD, "NewBest", 55);
        assertTrue(added, "Score 55 should be added to the full list, replacing the lowest.");

        List<ScoreEntry> hardScoresAfter = highScoreManager.getHighScores(DifficultyLevel.HARD);
        assertEquals(maxScores, hardScoresAfter.size(), "Hard scores list should still be full (size " + maxScores + ").");
        boolean found55 = false;
        int indexOf55 = -1;
        for(int i=0; i<hardScoresAfter.size(); i++){
            if(hardScoresAfter.get(i).score() == 55 && "NewBest".equals(hardScoresAfter.get(i).playerName())){
                found55 = true;
                indexOf55 = i;
                break;
            }
        }
        assertTrue(found55, "Score 55 by NewBest should be in the list.");
        if(indexOf55 > 0 && indexOf55 < hardScoresAfter.size() -1) {
            assertTrue(hardScoresAfter.get(indexOf55-1).score() > 55, "Score before 55 should be greater.");
            assertTrue(hardScoresAfter.get(indexOf55+1).score() < 55, "Score after 55 should be smaller.");
        } else if (indexOf55 == 0) {
        } else if (indexOf55 == hardScoresAfter.size() -1 ) {
        }
        boolean score10Present = false;
        for (ScoreEntry entry : hardScoresAfter) {
            if (entry.score() == 10) {
                score10Present = true;
                break;
            }
        }
        assertFalse(score10Present, "Score 10 should have been removed from the list.");
    }

    /**
     * Tests adding a score when the list is full but the new score is not high enough.
     * Verifies that the score is not added and the list remains unchanged.
     */
    @Test
    void testAddScore_listFull_notAHighScore() {
        int maxScores = 10;
        for (int i = 0; i < maxScores; i++) {
            highScoreManager.addScore(DifficultyLevel.EASY, "P" + i, (i + 1) * 10);
        }

        boolean added = highScoreManager.addScore(DifficultyLevel.EASY, "LowScorer", 5);
        assertFalse(added, "Score 5 should not be added as it's lower than the lowest in the full list.");

        List<ScoreEntry> easyScores = highScoreManager.getHighScores(DifficultyLevel.EASY);
        assertEquals(maxScores, easyScores.size(), "List size should remain " + maxScores + ".");
        assertEquals(10, easyScores.get(maxScores - 1).score(), "Lowest score should still be 10.");
    }


    /**
     * Tests the persistence of scores by saving and then loading them.
     * Adds scores, creates a new HighScoreManager instance for the same file,
     * and verifies that the loaded scores match the saved ones.
     */
    @Test
    void testSaveAndLoadHighScores() {
        highScoreManager.addScore(DifficultyLevel.EASY, "EasyPlayer1", 150);
        highScoreManager.addScore(DifficultyLevel.EASY, "EasyPlayer2", 120);
        highScoreManager.addScore(DifficultyLevel.MEDIUM, "MedPlayer1", 250);
        HighScoreManager manager2 = new HighScoreManager(testFilename);

        List<ScoreEntry> loadedEasyScores = manager2.getHighScores(DifficultyLevel.EASY);
        assertEquals(2, loadedEasyScores.size(), "Should load 2 easy scores.");
        assertEquals(150, loadedEasyScores.get(0).score(), "Loaded EasyPlayer1 score mismatch.");
        assertEquals("EasyPlayer1", loadedEasyScores.get(0).playerName());
        assertEquals(120, loadedEasyScores.get(1).score(), "Loaded EasyPlayer2 score mismatch.");

        List<ScoreEntry> loadedMediumScores = manager2.getHighScores(DifficultyLevel.MEDIUM);
        assertEquals(1, loadedMediumScores.size(), "Should load 1 medium score.");
        assertEquals(250, loadedMediumScores.get(0).score(), "Loaded MedPlayer1 score mismatch.");

        List<ScoreEntry> loadedHardScores = manager2.getHighScores(DifficultyLevel.HARD);
        assertTrue(loadedHardScores.isEmpty(), "Hard scores should be empty as none were added.");
    }

    /**
     * Tests the isHighScore method under various conditions.
     */
    @Test
    void testIsHighScoreLogic() {
        assertTrue(highScoreManager.isHighScore(DifficultyLevel.EASY, 10), "Any score should be high if list is empty.");
        highScoreManager.addScore(DifficultyLevel.EASY, "P1", 10);
        assertTrue(highScoreManager.isHighScore(DifficultyLevel.EASY, 5), "Any score should be high if list is not full (1/10).");
        for (int i = 1; i <= 10; i++) {
            highScoreManager.addScore(DifficultyLevel.EASY, "P" + i, i * 10);
        }
        assertFalse(highScoreManager.isHighScore(DifficultyLevel.EASY, 5), "Score 5 should not be high score if list is full and lowest is 10.");
        assertFalse(highScoreManager.isHighScore(DifficultyLevel.EASY, 10), "Score 10 is not > lowest (10), so should be false.");
        assertFalse(highScoreManager.isHighScore(DifficultyLevel.EASY, 10), "Score 10 is not strictly greater than lowest (10), so should be false.");
        assertTrue(highScoreManager.isHighScore(DifficultyLevel.EASY, 11), "Score 11 should be high score if list is full and lowest is 10.");
        assertTrue(highScoreManager.isHighScore(DifficultyLevel.EASY, 101), "Score 101 should be high score.");
    }
}