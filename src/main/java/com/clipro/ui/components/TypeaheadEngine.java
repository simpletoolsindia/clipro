package com.clipro.ui.components;

import java.util.*;

/**
 * Typeahead engine for fuzzy command/file autocomplete.
 * Matches OpenClaude's useTypeahead.tsx functionality.
 *
 * Reference: openclaude/src/hooks/useTypeahead.tsx
 */
public class TypeaheadEngine {

    private final List<CommandSuggestion> commands = new ArrayList<>();
    private final List<FileSuggestion> files = new ArrayList<>();
    private final Map<String, List<String>> cache = new HashMap<>();

    private int maxSuggestions = 10;
    private boolean fuzzyEnabled = true;

    public TypeaheadEngine() {}

    /**
     * Register a command for autocomplete.
     */
    public void registerCommand(String name, String description, String category) {
        commands.removeIf(c -> c.name().equals(name));
        commands.add(new CommandSuggestion(name, description, category, 1.0));
    }

    /**
     * Register multiple commands.
     */
    public void registerCommands(List<CommandSuggestion> newCommands) {
        for (CommandSuggestion cmd : newCommands) {
            registerCommand(cmd.name(), cmd.description(), cmd.category());
        }
    }

    /**
     * Index files for path completion.
     */
    public void indexFiles(List<String> filePaths) {
        files.clear();
        for (String path : filePaths) {
            String name = path.substring(path.lastIndexOf('/') + 1);
            files.add(new FileSuggestion(name, path));
        }
    }

    /**
     * Search for suggestions matching prefix.
     */
    public List<Suggestion> search(String prefix, SuggestionType type) {
        if (prefix == null || prefix.isEmpty()) {
            return getDefaultSuggestions(type);
        }

        List<Suggestion> results = new ArrayList<>();
        String lower = prefix.toLowerCase();

        switch (type) {
            case COMMAND -> searchCommands(lower, results);
            case FILE -> searchFiles(lower, results);
            case ALL -> {
                searchCommands(lower, results);
                searchFiles(lower, results);
            }
        }

        // Sort by score and limit
        results.sort((a, b) -> Double.compare(b.score(), a.score()));
        return results.subList(0, Math.min(results.size(), maxSuggestions));
    }

    private void searchCommands(String lower, List<Suggestion> results) {
        for (CommandSuggestion cmd : commands) {
            double score = fuzzyMatch(lower, cmd.name().toLowerCase());
            if (score > 0) {
                results.add(new Suggestion(
                    cmd.name(),
                    cmd.description(),
                    cmd.category(),
                    score,
                    SuggestionType.COMMAND
                ));
            }
        }
    }

    private void searchFiles(String lower, List<Suggestion> results) {
        for (FileSuggestion file : files) {
            double score = fuzzyMatch(lower, file.name().toLowerCase());
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

    /**
     * Fuzzy match with scoring.
     * Returns score 0.0-1.0 based on match quality.
     */
    private double fuzzyMatch(String pattern, String text) {
        if (!fuzzyEnabled) {
            return text.startsWith(pattern) ? 1.0 : 0.0;
        }

        if (text.equals(pattern)) return 1.0;
        if (text.startsWith(pattern)) return 0.9;
        if (text.contains(pattern)) return 0.7;

        // Character-by-character fuzzy match
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

    /**
     * Get default suggestions (all commands when no prefix).
     */
    private List<Suggestion> getDefaultSuggestions(SuggestionType type) {
        List<Suggestion> results = new ArrayList<>();

        switch (type) {
            case COMMAND, ALL -> {
                for (CommandSuggestion cmd : commands) {
                    results.add(new Suggestion(
                        cmd.name(),
                        cmd.description(),
                        cmd.category(),
                        0.5,
                        SuggestionType.COMMAND
                    ));
                }
            }
            case FILE -> {
                for (FileSuggestion file : files.subList(0, Math.min(20, files.size()))) {
                    results.add(new Suggestion(
                        file.name(),
                        file.path(),
                        "file",
                        0.5,
                        SuggestionType.FILE
                    ));
                }
            }
        }

        results.sort((a, b) -> a.name().compareTo(b.name()));
        return results.subList(0, Math.min(results.size(), maxSuggestions));
    }

    /**
     * Check if input is a partial command (starts with /).
     */
    public boolean isPartialCommand(String input) {
        return input != null && input.startsWith("/") && input.length() > 1;
    }

    /**
     * Get command completion for partial input.
     */
    public List<Suggestion> completeCommand(String input) {
        if (!isPartialCommand(input)) return Collections.emptyList();
        String prefix = input.substring(1); // Remove leading /
        return search(prefix, SuggestionType.COMMAND);
    }

    // Configuration
    public void setMaxSuggestions(int max) { this.maxSuggestions = max; }
    public void setFuzzyEnabled(boolean enabled) { this.fuzzyEnabled = enabled; }

    // Records
    public record CommandSuggestion(String name, String description, String category, double score) {}
    public record FileSuggestion(String name, String path) {}
    public record Suggestion(String name, String description, String category, double score, SuggestionType type) {}

    public enum SuggestionType { COMMAND, FILE, ALL }
}
