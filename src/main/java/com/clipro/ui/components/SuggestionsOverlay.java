package com.clipro.ui.components;

import com.clipro.ui.tamboui.OpenClaudeTheme;

import java.util.*;

import static com.clipro.ui.tamboui.OpenClaudeTheme.*;

/**
 * H-04: SuggestionsOverlay — slash command suggestions dropdown UI.
 * Pops up below the input row, shows 5-8 closest matching commands,
 * supports up/down arrow navigation and Enter to select.
 */
public class SuggestionsOverlay {

    private List<Suggestion> suggestions = new ArrayList<>();
    private int selectedIndex = 0;
    private boolean visible = false;
    private String searchQuery = "";

    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions != null ? new ArrayList<>(suggestions) : new ArrayList<>();
        this.selectedIndex = 0;
        this.visible = !this.suggestions.isEmpty();
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query : "";
    }

    public void moveUp() {
        if (suggestions.isEmpty()) return;
        selectedIndex = (selectedIndex - 1 + suggestions.size()) % suggestions.size();
    }

    public void moveDown() {
        if (suggestions.isEmpty()) return;
        selectedIndex = (selectedIndex + 1) % suggestions.size();
    }

    public void acceptSelected() {
        // Just confirms selection - caller uses getSelectedSuggestion()
    }

    public Suggestion getSelectedSuggestion() {
        if (suggestions.isEmpty() || selectedIndex < 0 || selectedIndex >= suggestions.size()) {
            return null;
        }
        return suggestions.get(selectedIndex);
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < suggestions.size()) {
            this.selectedIndex = index;
        }
    }

    public boolean isVisible() {
        return visible && !suggestions.isEmpty();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getSuggestionCount() {
        return suggestions.size();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public List<Suggestion> getSuggestions() {
        return new ArrayList<>(suggestions);
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * H-04: Render the suggestions overlay as a box.
     * Shows max 8 suggestions, each with: ▶ marker, name, description.
     */
    public String render(int width) {
        if (!isVisible() || suggestions.isEmpty()) {
            return "";
        }

        int displayCount = Math.min(suggestions.size(), 8);
        int innerWidth = Math.min(width - 4, 60);
        int rowHeight = 1;

        StringBuilder sb = new StringBuilder();

        // Top border
        sb.append(BORDER_V).append(" ");
        sb.append(BORDER_TL);
        sb.append("─".repeat(innerWidth - 2));
        sb.append(BORDER_TR);
        sb.append(" ");
        sb.append(BORDER_V).append("\n");

        // Suggestion rows
        for (int i = 0; i < displayCount; i++) {
            Suggestion s = suggestions.get(i);
            boolean selected = (i == selectedIndex);

            sb.append(BORDER_V).append(" ");
            if (selected) {
                sb.append(OpenClaudeTheme.successText("▶ "));
            } else {
                sb.append(OpenClaudeTheme.dimText("  "));
            }

            // Command name
            String name = "/" + s.name();
            String displayName = selected ? boldText(name) : cyan(name);

            // Category tag
            String category = s.category() != null ? s.category() : "command";
            String categoryTag = "[" + category + "]";

            // Description (truncated)
            String desc = s.description() != null ? s.description() : "";
            desc = truncate(desc, innerWidth - name.length() - categoryTag.length() - 8);

            int remaining = innerWidth - 4 - name.length() - desc.length() - categoryTag.length();
            sb.append(displayName);
            sb.append(OpenClaudeTheme.mutedText(" " + categoryTag));
            if (remaining > 0) sb.append(" ".repeat(remaining));
            sb.append(OpenClaudeTheme.dimText(desc));
            sb.append(" ");
            sb.append(BORDER_V).append("\n");
        }

        // Hint row
        sb.append(BORDER_V).append(" ");
        sb.append(OpenClaudeTheme.dimText("↑↓ navigate · Enter select · Esc dismiss"));
        int hintLen = "↑↓ navigate · Enter select · Esc dismiss".length();
        int pad = innerWidth - 2 - hintLen;
        if (pad > 0) sb.append(" ".repeat(pad));
        sb.append(" ");
        sb.append(BORDER_V).append("\n");

        // Bottom border
        sb.append(BORDER_V).append(" ");
        sb.append(BORDER_BL);
        sb.append("─".repeat(innerWidth - 2));
        sb.append(BORDER_BR);
        sb.append(" ");
        sb.append(BORDER_V);

        return sb.toString();
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s != null ? s : "";
        return s.substring(0, Math.max(0, max - 3)) + "...";
    }

    private String boldText(String s) { return ANSI_BOLD + s + ANSI_RESET; }
    private String cyan(String s) { return ANSI_BRIGHT_CYAN + s + ANSI_RESET; }

    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BRIGHT_CYAN = "\u001B[96m";
    private static final String BORDER_TL = "┌";
    private static final String BORDER_TR = "┐";
    private static final String BORDER_BL = "└";
    private static final String BORDER_BR = "┘";
    private static final String BORDER_V = "│";
}
