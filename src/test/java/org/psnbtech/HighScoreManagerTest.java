package org.psnbtech;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HighScoreManagerTest {

    @TempDir
    Path tmp;

    @Test
    void startsEmptyWhenFileMissing() {
        HighScoreManager m = new HighScoreManager(tmp.resolve("none.json"));
        assertEquals(0, m.getHighScore());
        assertTrue(m.getScores().isEmpty());
    }

    @Test
    void savesAndReloadsFromDisk() {
        Path f = tmp.resolve("scores.json");
        new HighScoreManager(f).add("Ann", 500);

        HighScoreManager reloaded = new HighScoreManager(f);
        assertEquals(500, reloaded.getHighScore());
        assertEquals("Ann", reloaded.getScores().get(0).name);
    }

    @Test
    void keepsScoresSortedHighestFirst() {
        HighScoreManager m = new HighScoreManager(tmp.resolve("s.json"));
        m.add("A", 100);
        m.add("B", 300);
        m.add("C", 200);
        assertEquals(300, m.getScores().get(0).score);
        assertEquals(100, m.getScores().get(2).score);
    }

    @Test
    void keepsOnlyTopTen() {
        HighScoreManager m = new HighScoreManager(tmp.resolve("s.json"));
        for (int i = 1; i <= 15; i++) m.add("P" + i, i * 10);
        assertEquals(HighScoreManager.MAX_ENTRIES, m.getScores().size());
        assertEquals(150, m.getHighScore());
    }

    @Test
    void survivesCorruptFile() throws Exception {
        Path f = tmp.resolve("bad.json");
        Files.writeString(f, "{ this is not json");
        HighScoreManager m = new HighScoreManager(f);
        assertEquals(0, m.getHighScore());
    }
}