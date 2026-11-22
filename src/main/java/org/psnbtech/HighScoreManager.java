package org.psnbtech;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Saves and loads the top 10 scores as JSON using Gson. */
public class HighScoreManager {

    public static class Entry {
        public String name;
        public int score;
        public Entry() {}
        public Entry(String name, int score) { this.name = name; this.score = score; }
    }

    public static final int MAX_ENTRIES = 10;

    private final Path file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final List<Entry> scores = new ArrayList<>();

    public HighScoreManager(Path file) {
        this.file = file;
        load();
    }

    /** Default location: ~/.tetris/highscores.json */
    public static HighScoreManager createDefault() {
        return new HighScoreManager(
            Path.of(System.getProperty("user.home"), ".tetris", "highscores.json"));
    }

    public void add(String name, int score) {
        scores.add(new Entry(name, score));
        scores.sort(Comparator.comparingInt((Entry e) -> e.score).reversed());
        if (scores.size() > MAX_ENTRIES) {
            scores.subList(MAX_ENTRIES, scores.size()).clear();
        }
        save();
    }

    public List<Entry> getScores() { return List.copyOf(scores); }

    public int getHighScore() { return scores.isEmpty() ? 0 : scores.get(0).score; }

    private void load() {
        if (!Files.exists(file)) return;
        try (Reader r = Files.newBufferedReader(file)) {
            List<Entry> loaded = gson.fromJson(r, new TypeToken<List<Entry>>() {}.getType());
            if (loaded != null) scores.addAll(loaded);
        } catch (IOException | JsonParseException e) {
            System.err.println("Could not read high scores: " + e.getMessage());
        }
    }

    private void save() {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            try (Writer w = Files.newBufferedWriter(file)) {
                gson.toJson(scores, w);
            }
        } catch (IOException e) {
            System.err.println("Could not save high scores: " + e.getMessage());
        }
    }
}