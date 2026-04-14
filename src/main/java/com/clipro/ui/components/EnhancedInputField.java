package com.clipro.ui.components;

import com.clipro.ui.Terminal;
import java.util.*;

/**
 * Enhanced input field with multi-line, vim mode, and typeahead support.
 * Matches OpenClaude's PromptInput.tsx functionality.
 *
 * Reference: openclaude/src/components/PromptInput/
 */
public class EnhancedInputField {

    private final StringBuilder buffer = new StringBuilder();
    private final List<String> history = new ArrayList<>();
    private final TypeaheadEngine typeahead;
    private final HistorySearch historySearch;

    private int cursorPosition = 0;
    private int historyIndex = -1;
    private String savedInput = "";
    private String prompt = "▶ ";
    private boolean vimMode = false;
    private boolean showSuggestions = false;
    private List<Suggestion> suggestions = new ArrayList<>();
    private int suggestionIndex = 0;
    private boolean multilineMode = false;
    private List<String> lines = new ArrayList<>();

    // Vim state
    private VimState vimState = VimState.NORMAL;
    private String vimOperator = "";
    private String vimMotion = "";

    private static final int MAX_HISTORY = 100;

    public EnhancedInputField() {
        this.typeahead = new TypeaheadEngine();
        this.historySearch = new HistorySearch();
    }

    // === Input Handling ===

    public void insert(char c) {
        if (cursorPosition == buffer.length()) {
            buffer.append(c);
        } else {
            buffer.insert(cursorPosition, c);
        }
        cursorPosition++;
        updateTypeahead();
    }

    public void insert(String text) {
        if (cursorPosition == buffer.length()) {
            buffer.append(text);
        } else {
            buffer.insert(cursorPosition, text);
        }
        cursorPosition += text.length();
        updateTypeahead();
    }

    public void backspace() {
        if (cursorPosition > 0) {
            buffer.deleteCharAt(cursorPosition - 1);
            cursorPosition--;
        }
        updateTypeahead();
    }

    public void delete() {
        if (cursorPosition < buffer.length()) {
            buffer.deleteCharAt(cursorPosition);
        }
    }

    public void clear() {
        buffer.setLength(0);
        cursorPosition = 0;
        suggestions.clear();
        showSuggestions = false;
    }

    // === Cursor Movement ===

    public void moveLeft() {
        if (cursorPosition > 0) cursorPosition--;
    }

    public void moveRight() {
        if (cursorPosition < buffer.length()) cursorPosition++;
    }

    public void moveToStart() {
        cursorPosition = 0;
    }

    public void moveToEnd() {
        cursorPosition = buffer.length();
    }

    public void moveWordLeft() {
        while (cursorPosition > 0 && buffer.charAt(cursorPosition - 1) == ' ') {
            cursorPosition--;
        }
        while (cursorPosition > 0 && buffer.charAt(cursorPosition - 1) != ' ') {
            cursorPosition--;
        }
    }

    public void moveWordRight() {
        while (cursorPosition < buffer.length() && buffer.charAt(cursorPosition) != ' ') {
            cursorPosition++;
        }
        while (cursorPosition < buffer.length() && buffer.charAt(cursorPosition) == ' ') {
            cursorPosition++;
        }
    }

    // === History Navigation ===

    public void historyUp() {
        if (history.isEmpty()) return;

        if (historyIndex == -1) {
            savedInput = buffer.toString();
            historyIndex = 0;
        } else if (historyIndex < history.size() - 1) {
            historyIndex++;
        }

        loadHistoryEntry();
    }

    public void historyDown() {
        if (historyIndex <= 0) {
            historyIndex = -1;
            buffer.setLength(0);
            buffer.append(savedInput);
        } else {
            historyIndex--;
            loadHistoryEntry();
        }
        cursorPosition = buffer.length();
    }

    private void loadHistoryEntry() {
        buffer.setLength(0);
        buffer.append(history.get(history.size() - 1 - historyIndex));
        cursorPosition = buffer.length();
    }

    // === History Search ===

    public void historySearchStart() {
        historySearch.enterSearchMode();
    }

    public void historySearchNext(String query) {
        List<String> results = historySearch.search(query, history);
        if (!results.isEmpty()) {
            buffer.setLength(0);
            buffer.append(results.get(0));
            cursorPosition = buffer.length();
        }
    }

    // === Typeahead ===

    private void updateTypeahead() {
        String text = buffer.toString();
        if (typeahead.isPartialCommand(text)) {
            suggestions = typeahead.completeCommand(text);
            showSuggestions = !suggestions.isEmpty();
            suggestionIndex = 0;
        } else {
            suggestions = typeahead.search(text, TypeaheadEngine.SuggestionType.ALL);
            showSuggestions = !suggestions.isEmpty();
        }
    }

    public void suggestionUp() {
        if (!suggestions.isEmpty()) {
            suggestionIndex = (suggestionIndex - 1 + suggestions.size()) % suggestions.size();
        }
    }

    public void suggestionDown() {
        if (!suggestions.isEmpty()) {
            suggestionIndex = (suggestionIndex + 1) % suggestions.size();
        }
    }

    public Suggestion acceptSuggestion() {
        if (suggestions.isEmpty() || suggestionIndex < 0) return null;

        Suggestion selected = suggestions.get(suggestionIndex);
        buffer.setLength(0);

        if (selected.type() == TypeaheadEngine.SuggestionType.COMMAND) {
            buffer.append("/").append(selected.name());
        } else {
            buffer.append(selected.name());
        }

        cursorPosition = buffer.length();
        suggestions.clear();
        showSuggestions = false;

        return selected;
    }

    // === Submission ===

    public String submit() {
        String input = buffer.toString();
        if (!input.isEmpty()) {
            addToHistory(input);
        }
        buffer.setLength(0);
        cursorPosition = 0;
        historyIndex = -1;
        suggestions.clear();
        showSuggestions = false;
        return input;
    }

    private void addToHistory(String input) {
        if (!history.isEmpty() && history.get(history.size() - 1).equals(input)) {
            return;
        }
        history.add(input);
        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    // === Rendering ===

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(prompt);

        String text = buffer.toString();
        String before = text.substring(0, cursorPosition);
        String after = text.substring(cursorPosition);

        sb.append(before);
        sb.append(Terminal.inverse(Terminal.dim("\u2588"))); // Block cursor
        sb.append(after);

        // Vim mode indicator
        if (vimMode) {
            sb.append(Terminal.dim(" [").append(vimState.name().charAt(0)).append("]"));
        }

        return sb.toString();
    }

    public String renderWithSuggestions() {
        String base = render();

        if (!showSuggestions) return base;

        StringBuilder sb = new StringBuilder();
        sb.append(base).append("\n");

        // Render suggestion dropdown
        for (int i = 0; i < Math.min(suggestions.size(), 5); i++) {
            Suggestion s = suggestions.get(i);
            String prefix = (i == suggestionIndex) ? "▶ " : "  ";
            sb.append(prefix)
              .append(Terminal.suggestion("/" + s.name()))
              .append(" ")
              .append(Terminal.dim(s.description()))
              .append("\n");
        }

        return sb.toString();
    }

    // === Vim Operations ===

    public void vimNormal(char c) {
        switch (c) {
            case 'i' -> vimState = VimState.INSERT;
            case 'a' -> { cursorPosition++; vimState = VimState.INSERT; }
            case 'A' -> { cursorPosition = buffer.length(); vimState = VimState.INSERT; }
            case 'I' -> { cursorPosition = 0; vimState = VimState.INSERT; }
            case 'x' -> delete();
            case 'd' -> {
                vimOperator = "d";
                vimState = VimState.NORMAL;
            }
            case 'y' -> {
                vimOperator = "y";
                vimState = VimState.NORMAL;
            }
            case 'p' -> {
                // Paste - would integrate with register
                vimState = VimState.NORMAL;
            }
            case '0' -> cursorPosition = 0;
            case '$' -> cursorPosition = buffer.length();
            case 'w' -> moveWordRight();
            case 'b' -> moveWordLeft();
            case '/' -> historySearchStart();
            case ':' -> {
                buffer.setLength(0);
                buffer.append(":");
                cursorPosition = 1;
            }
        }
    }

    public void vimInsert(char c) {
        if (c == 27) { // Escape
            vimState = VimState.NORMAL;
            return;
        }
        insert(c);
    }

    // === Getters/Setters ===

    public String getText() { return buffer.toString(); }
    public int getCursorPosition() { return cursorPosition; }
    public int getLength() { return buffer.length(); }
    public boolean isEmpty() { return buffer.length() == 0; }
    public boolean isMultiline() { return multilineMode; }
    public boolean isVimMode() { return vimMode; }
    public List<String> getHistory() { return new ArrayList<>(history); }
    public List<Suggestion> getSuggestions() { return suggestions; }
    public boolean isShowingSuggestions() { return showSuggestions; }
    public TypeaheadEngine getTypeahead() { return typeahead; }

    public void setPrompt(String prompt) { this.prompt = prompt; }
    public void setVimMode(boolean enabled) { this.vimMode = enabled; }
    public void setMultilineMode(boolean enabled) { this.multilineMode = enabled; }
    public void setTypeahead(TypeaheadEngine engine) { this.typeahead.registerCommands(engine.getSuggestions().stream().map(s -> new TypeaheadEngine.CommandSuggestion(s.name(), s.description(), s.category(), s.score())).toList()); }

    // === Nested Suggestion record ===
    public record Suggestion(String name, String description, String category, double score, TypeaheadEngine.SuggestionType type) {}
}
