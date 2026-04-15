package com.clipro.ui.components;

import java.nio.file.*;
import java.util.*;

/**
 * H-02: Typeahead engine with filesystem traversal for path completion.
 * Matches OpenClaude's useTypeahead.tsx functionality.
 * Recursively indexes project files for /edit, /read, and import completions.
 */
public class TypeaheadEngine {

    private final List<CommandSuggestion> commands = new ArrayList<>();
    private final List<FileSuggestion> files = new ArrayList<>();
    private final Map<String, List<String>> cache = new HashMap<>();

    private int maxSuggestions = 10;
    private boolean fuzzyEnabled = true;
    private Set<String> excludedDirs;

    public TypeaheadEngine() {
        this.excludedDirs = Set.of(
            "node_modules", ".git", "build", "target", ".gradle",
            "__pycache__", ".venv", "venv", "dist", ".idea", ".vscode",
            "target", ".next", ".nuxt", "coverage", ".cache", "tmp"
        );
    }

    /**
     * H-02: Recursively index project files for path completion.
     * Omit node_modules, .git, build, target, etc.
     */
    public void indexProjectFiles(String projectRoot) {
        if (projectRoot == null || projectRoot.isEmpty()) {
            projectRoot = System.getProperty("user.dir");
        }
        files.clear();
        try {
            Path root = Paths.get(projectRoot);
            if (!Files.exists(root)) return;
            indexDirectory(root, 0, 5); // max 5 levels deep
        } catch (Exception e) {
            // Ignore - filesystem access may fail
        }
    }

    private void indexDirectory(Path dir, int depth, int maxDepth) {
        if (depth > maxDepth) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                // Skip excluded directories
                if (Files.isDirectory(entry)) {
                    if (excludedDirs.contains(name)) continue;
                    if (name.startsWith(".")) continue;
                    indexDirectory(entry, depth + 1, maxDepth);
                } else {
                    // Add file
                    String absPath = entry.toAbsolutePath().toString();
                    files.add(new FileSuggestion(name, absPath));
                }
            }
        } catch (Exception e) {
            // Ignore access errors
        }
    }

    /**
     * H-02: Get path/file completions for partial path input.
     * Handles: /edit path, /read path, import statements.
     */
    public List<Suggestion> getPathCompletions(String partialPath) {
        if (partialPath == null || partialPath.isEmpty()) {
            return Collections.emptyList();
        }
        List<Suggestion> results = new ArrayList<>();

        // Check if it's an absolute path
        if (partialPath.startsWith("/") || partialPath.matches("^[A-Za-z]:\\\\.*")) {
            // Absolute path - index that directory
            Path parent = Paths.get(partialPath).getParent();
            if (parent != null && Files.exists(parent)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent)) {
                    String basename = Paths.get(partialPath).getFileName().toString();
                    for (Path entry : stream) {
                        String entryName = entry.getFileName().toString();
                        if (entryName.toLowerCase().startsWith(basename.toLowerCase())) {
                            String fullPath = entry.toAbsolutePath().toString();
                            results.add(new Suggestion(
                                fullPath,
                                Files.isDirectory(entry) ? fullPath + "/" : fullPath,
                                Files.isDirectory(entry) ? "dir" : "file",
                                1.0,
                                SuggestionType.FILE
                            ));
                        }
                    }
                } catch (Exception ignored) {}
            }
            return results;
        }

        // Relative path - search indexed files
        String query = partialPath.toLowerCase();
        for (FileSuggestion file : files) {
            if (file.name().toLowerCase().contains(query) ||
                file.path().toLowerCase().contains(query)) {
                double score = fuzzyMatch(query, file.name().toLowerCase());
                if (score > 0) {
                    results.add(new Suggestion(
                        file.name(),
                        file.path(),
                        "file",
                        score,
                        SuggestionType.FILE
                    ));
                }
            }
        }
        results.sort((a, b) -> Double.compare(b.score(), a.score()));
        return results.subList(0, Math.min(results.size(), maxSuggestions));
    }

    public void registerCommand(String name, String description, String category) {
        commands.removeIf(c -> c.name().equals(name));
        commands.add(new CommandSuggestion(name, description, category, 1.0));
    }

    public void registerCommands(List<CommandSuggestion> newCommands) {
        for (CommandSuggestion cmd : newCommands) {
            registerCommand(cmd.name(), cmd.description(), cmd.category());
        }
    }

    public void indexFiles(List<String> filePaths) {
        files.clear();
        for (String path : filePaths) {
            String name = path.substring(path.lastIndexOf('/') + 1);
            files.add(new FileSuggestion(name, path));
        }
    }

    public List<Suggestion> search(String prefix, SuggestionType type) {
        if (prefix == null || prefix.isEmpty()) {
            return getDefaultSuggestions(type);
        }

        List<Suggestion> results = new ArrayList<>();
        String lower = prefix.toLowerCase();

        switch (type) {
            case COMMAND -> searchCommands(lower, results);
            case FILE -> {
                searchFiles(lower, results);
                // H-02: Also check path completions for /edit, /read
                if (prefix.contains("/") || prefix.contains(".")) {
                    results.addAll(getPathCompletions(prefix));
                }
            }
            case ALL -> {
                searchCommands(lower, results);
                searchFiles(lower, results);
                if (prefix.contains("/") || prefix.contains(".")) {
                    results.addAll(getPathCompletions(prefix));
                }
            }
        }

        results.sort((a, b) -> Double.compare(b.score(), a.score()));
        return results.subList(0, Math.min(results.size(), maxSuggestions));
    }

    private void searchCommands(String lower, List<Suggestion> results) {
        for (CommandSuggestion cmd : commands) {
            double score = fuzzyMatch(lower, cmd.name().toLowerCase());
            if (score > 0) {
                results.add(new Suggestion(
                    cmd.name(), cmd.description(), cmd.category(),
                    score, SuggestionType.COMMAND
                ));
            }
        }
    }

    private void searchFiles(String lower, List<Suggestion> results) {
        for (FileSuggestion file : files) {
            double score = fuzzyMatch(lower, file.name().toLowerCase());
            if (score > 0) {
                results.add(new Suggestion(
                    file.name(), file.path(), "file",
                    score, SuggestionType.FILE
                ));
            }
        }
    }

    private double fuzzyMatch(String pattern, String text) {
        if (!fuzzyEnabled) {
            return text.startsWith(pattern) ? 1.0 : 0.0;
        }
        if (text.equals(pattern)) return 1.0;
        if (text.startsWith(pattern)) return 0.9;
        if (text.contains(pattern)) return 0.7;

        int patternIdx = 0;
        int consecutiveBonus = 0;
        int score = 0;

        for (int i = 0; i < text.length() && patternIdx < pattern.length(); i++) {
            if (text.charAt(i) == pattern.charAt(patternIdx)) {
                score += 10 + consecutiveBonus;
                consecutiveBonus += 5;
                patternIdx++;
            } else {
                consecutiveBonus = 0;
            }
        }

        if (patternIdx < pattern.length()) return 0.0;
        return Math.min(1.0, (double) score / (text.length() * 10));
    }

    private List<Suggestion> getDefaultSuggestions(SuggestionType type) {
        List<Suggestion> results = new ArrayList<>();
        switch (type) {
            case COMMAND, ALL -> {
                for (CommandSuggestion cmd : commands) {
                    results.add(new Suggestion(
                        cmd.name(), cmd.description(), cmd.category(),
                        0.5, SuggestionType.COMMAND
                    ));
                }
            }
            case FILE -> {
                for (FileSuggestion file : files.subList(0, Math.min(20, files.size()))) {
                    results.add(new Suggestion(
                        file.name(), file.path(), "file",
                        0.5, SuggestionType.FILE
                    ));
                }
            }
        }
        results.sort(Comparator.comparing(Suggestion::name));
        return results.subList(0, Math.min(results.size(), maxSuggestions));
    }

    public boolean isPartialCommand(String input) {
        return input != null && input.startsWith("/") && input.length() > 1;
    }

    public List<Suggestion> completeCommand(String input) {
        if (!isPartialCommand(input)) return Collections.emptyList();
        return search(input.substring(1), SuggestionType.COMMAND);
    }

    public void setMaxSuggestions(int max) { this.maxSuggestions = max; }
    public void setFuzzyEnabled(boolean enabled) { this.fuzzyEnabled = enabled; }

    public record CommandSuggestion(String name, String description, String category, double score) {}
    public record FileSuggestion(String name, String path) {}
    public record Suggestion(String name, String description, String category, double score, SuggestionType type) {}

    public enum SuggestionType { COMMAND, FILE, ALL }
}
