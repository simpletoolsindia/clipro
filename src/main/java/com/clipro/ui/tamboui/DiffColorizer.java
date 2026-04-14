package com.clipro.ui.tamboui;

/**
 * Diff colorizer for syntax-highlighting file changes.
 * Supports word-level and line-level highlighting with dimmed variants.
 *
 * Reference: openclaude/src/utils/theme.ts (diff colors)
 */
public class DiffColorizer {

    private final ThemeManager themeManager;
    private boolean wordLevel = true;

    public DiffColorizer() {
        this.themeManager = ThemeManager.getInstance();
    }

    public DiffColorizer(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    /**
     * Colorize a diff line (added or removed).
     */
    public String colorize(String line, DiffType type) {
        Theme theme = themeManager.getTheme();

        return switch (type) {
            case ADDED -> theme.getDiffAdded() + line + "\u001B[0m";
            case REMOVED -> theme.getDiffRemoved() + line + "\u001B[0m";
            case CONTEXT -> theme.getSubtle() + line + "\u001B[0m";
            case HEADER -> theme.getSuggestion() + line + "\u001B[0m";
        };
    }

    /**
     * Colorize a word (added or removed).
     */
    public String colorizeWord(String word, DiffType type) {
        Theme theme = themeManager.getTheme();

        return switch (type) {
            case ADDED -> theme.getDiffAddedWord() + word + "\u001B[0m";
            case REMOVED -> theme.getDiffRemovedWord() + word + "\u001B[0m";
            default -> word;
        };
    }

    /**
     * Colorize a diff line with dimmed variant (for collapsed views).
     */
    public String colorizeDimmed(String line, DiffType type) {
        Theme theme = themeManager.getTheme();

        return switch (type) {
            case ADDED -> theme.getDiffAddedDimmed() + line + "\u001B[0m";
            case REMOVED -> theme.getDiffRemovedDimmed() + line + "\u001B[0m";
            case CONTEXT -> theme.getSubtle() + line + "\u001B[0m";
            default -> line;
        };
    }

    /**
     * Colorize a unified diff hunk header.
     */
    public String colorizeHunkHeader(String header) {
        Theme theme = themeManager.getTheme();
        return theme.getSuggestion() + header + "\u001B[0m";
    }

    /**
     * Colorize line numbers.
     */
    public String colorizeLineNumber(int num, boolean isOld) {
        Theme theme = themeManager.getTheme();
        String color = isOld ? theme.getDiffRemoved() : theme.getDiffAdded();
        String formatted = String.format("%4d", num);
        return color + formatted + "\u001B[0m";
    }

    /**
     * Parse and colorize a unified diff line.
     */
    public String parseDiffLine(String line) {
        if (line == null || line.isEmpty()) return "";

        Theme theme = themeManager.getTheme();

        if (line.startsWith("+++") || line.startsWith("---")) {
            return colorize(line, DiffType.HEADER);
        }

        if (line.startsWith("@@")) {
            return colorizeHunkHeader(line);
        }

        if (line.startsWith("+")) {
            return colorize(line.substring(1), DiffType.ADDED);
        }

        if (line.startsWith("-")) {
            return colorize(line.substring(1), DiffType.REMOVED);
        }

        if (line.startsWith(" ")) {
            return colorize(line.substring(1), DiffType.CONTEXT);
        }

        // Fallback
        return theme.getText() + line + "\u001B[0m";
    }

    /**
     * Colorize a character diff (word-by-word).
     */
    public String colorizeCharDiff(String text, boolean[] added) {
        if (text == null || added == null || text.length() != added.length) {
            return text;
        }

        Theme theme = themeManager.getTheme();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            String color = added[i] ? theme.getDiffAddedWord() : theme.getDiffRemovedWord();
            sb.append(color).append(text.charAt(i));
        }

        sb.append("\u001B[0m");
        return sb.toString();
    }

    /**
     * Set word-level diff highlighting.
     */
    public void setWordLevel(boolean wordLevel) {
        this.wordLevel = wordLevel;
    }

    /**
     * Check if using word-level highlighting.
     */
    public boolean isWordLevel() {
        return wordLevel;
    }

    /**
     * Diff line types.
     */
    public enum DiffType {
        ADDED,
        REMOVED,
        CONTEXT,
        HEADER
    }
}
