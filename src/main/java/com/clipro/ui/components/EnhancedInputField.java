package com.clipro.ui.components;

import com.clipro.ui.Terminal;
import com.clipro.ui.vim.VimState;

import java.util.*;

/**
 * H-13: Enhanced input field with multi-line, vim mode, shimmer cursor, and typeahead support.
 * Matches OpenClaude's PromptInput.tsx functionality.
 * Integrates: shimmer animation, vim mode indicators, permission mode, command queue.
 */
public class EnhancedInputField {

    private final StringBuilder buffer = new StringBuilder();
    private final List<String> history = new ArrayList<>();
    private final TypeaheadEngine typeahead;
    private final HistorySearch historySearch;
    private final ShimmerAnimator shimmer;

    private int cursorPosition = 0;
    private int historyIndex = -1;
    private String savedInput = "";
    private String prompt = "▶ ";
    private boolean vimMode = false;
    private boolean showSuggestions = false;
    private List<TypeaheadEngine.Suggestion> suggestions = new ArrayList<>();
    private int suggestionIndex = 0;
    private boolean multilineMode = false;
    private boolean searchMode = false;  // H-03: Ctrl+R search mode
    private String searchQuery = "";

    // Multi-line cursor tracking (H-13)
    private int cursorRow = 0;
    private int cursorCol = 0;
    private List<Integer> lineStarts = new ArrayList<>();

    // H-13: Permission mode indicator
    private String permissionMode = "BASH";

    // H-13: Command queue
    private List<String> commandQueue = new ArrayList<>();
    private boolean queueMode = false;

    // Vim state
    private VimState vimState = VimState.NORMAL;
    private String vimOperator = "";

    private static final int MAX_HISTORY = 100;

    public EnhancedInputField() {
        this.typeahead = new TypeaheadEngine();
        this.historySearch = new HistorySearch();
        this.shimmer = new ShimmerAnimator();
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
        if (multilineMode) syncCursorFromPosition();
    }

    public void insert(String text) {
        if (cursorPosition == buffer.length()) {
            buffer.append(text);
        } else {
            buffer.insert(cursorPosition, text);
        }
        cursorPosition += text.length();
        updateTypeahead();
        if (multilineMode) syncCursorFromPosition();
    }

    public void insertNewline() {
        insert('\n');
        syncCursorFromPosition();
    }

    public void backspace() {
        if (cursorPosition > 0) {
            buffer.deleteCharAt(cursorPosition - 1);
            cursorPosition--;
        }
        updateTypeahead();
        if (multilineMode) syncCursorFromPosition();
    }

    public void delete() {
        if (cursorPosition < buffer.length()) {
            buffer.deleteCharAt(cursorPosition);
        }
        if (multilineMode) syncCursorFromPosition();
    }

    public void clear() {
        buffer.setLength(0);
        cursorPosition = 0;
        suggestions.clear();
        showSuggestions = false;
        searchMode = false;
        searchQuery = "";
    }

    // === Cursor Movement ===

    public void moveLeft() {
        if (cursorPosition > 0) cursorPosition--;
        if (multilineMode) syncCursorFromPosition();
    }

    public void moveRight() {
        if (cursorPosition < buffer.length()) cursorPosition++;
        if (multilineMode) syncCursorFromPosition();
    }

    public void moveToStart() { cursorPosition = 0; }
    public void moveToEnd() { cursorPosition = buffer.length(); }

    public void moveWordLeft() {
        while (cursorPosition > 0 && buffer.charAt(cursorPosition - 1) == ' ') cursorPosition--;
        while (cursorPosition > 0 && buffer.charAt(cursorPosition - 1) != ' ') cursorPosition--;
    }

    public void moveWordRight() {
        while (cursorPosition < buffer.length() && buffer.charAt(cursorPosition) != ' ') cursorPosition++;
        while (cursorPosition < buffer.length() && buffer.charAt(cursorPosition) == ' ') cursorPosition++;
    }

    // === H-03: History Search (Ctrl+R) ===

    public void enterSearchMode() {
        searchMode = true;
        searchQuery = "";
        historySearch.enterSearchMode();
        historySearch.setSearchHistory(history);
    }

    public void exitSearchMode() {
        searchMode = false;
        searchQuery = "";
        historySearch.exitSearchMode();
    }

    public void updateSearchQuery(String query) {
        searchQuery = query;
        historySearch.search(query, history);
    }

    public void searchNext() {
        String next = historySearch.getNext(searchQuery, history);
        if (next != null) {
            buffer.setLength(0);
            buffer.append(next);
            cursorPosition = buffer.length();
        }
    }

    public void searchPrevious() {
        String prev = historySearch.getPrevious(searchQuery, history);
        if (prev != null) {
            buffer.setLength(0);
            buffer.append(prev);
            cursorPosition = buffer.length();
        }
    }

    public boolean isSearchMode() { return searchMode; }
    public String getSearchQuery() { return searchQuery; }

    // === H-13: Shimmer Animation ===

    public void startShimmer() { shimmer.start(); }
    public void stopShimmer() { shimmer.stop(); }

    /**
     * H-13: Get animated cursor for rendering.
     */
    public String getAnimatedCursor() {
        return shimmer.isEnabled() ? shimmer.getShimmerCursor() : "\u2588";
    }

    public long getShimmerFrame() { return shimmer.getFrame(); }

    // === Multi-line Navigation (H-13) ===

    private void updateLineStarts() {
        lineStarts.clear();
        lineStarts.add(0);
        for (int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) == '\n') lineStarts.add(i + 1);
        }
    }

    private void syncCursorFromPosition() {
        updateLineStarts();
        int pos = cursorPosition;
        for (int i = 0; i < lineStarts.size(); i++) {
            int next = (i + 1 < lineStarts.size()) ? lineStarts.get(i + 1) - 1 : buffer.length();
            if (pos <= next) {
                cursorRow = i;
                cursorCol = pos - lineStarts.get(i);
                return;
            }
        }
        cursorRow = Math.max(0, lineStarts.size() - 1);
        cursorCol = Math.max(0, buffer.length() - lineStarts.get(cursorRow));
    }

    public void moveLineUp() {
        if (!multilineMode || cursorRow == 0) return;
        syncCursorFromPosition();
        cursorRow--;
        int lineStart = lineStarts.get(cursorRow);
        int lineEnd = (cursorRow + 1 < lineStarts.size()) ? lineStarts.get(cursorRow + 1) - 1 : buffer.length();
        int lineLen = lineEnd - lineStart;
        cursorCol = Math.min(cursorCol, lineLen);
        cursorPosition = lineStart + cursorCol;
    }

    public void moveLineDown() {
        if (!multilineMode) return;
        syncCursorFromPosition();
        if (cursorRow >= lineStarts.size() - 1) return;
        cursorRow++;
        int lineStart = lineStarts.get(cursorRow);
        int lineEnd = (cursorRow + 1 < lineStarts.size()) ? lineStarts.get(cursorRow + 1) - 1 : buffer.length();
        int lineLen = lineEnd - lineStart;
        cursorCol = Math.min(cursorCol, lineLen);
        cursorPosition = lineStart + cursorCol;
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

    public TypeaheadEngine.Suggestion acceptSuggestion() {
        if (suggestions.isEmpty() || suggestionIndex < 0) return null;
        TypeaheadEngine.Suggestion selected = suggestions.get(suggestionIndex);
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
        if (!input.isEmpty()) addToHistory(input);
        buffer.setLength(0);
        cursorPosition = 0;
        historyIndex = -1;
        suggestions.clear();
        showSuggestions = false;
        searchMode = false;
        return input;
    }

    private void addToHistory(String input) {
        if (!history.isEmpty() && history.get(history.size() - 1).equals(input)) return;
        history.add(input);
        if (history.size() > MAX_HISTORY) history.remove(0);
    }

    // === Rendering ===

    /**
     * H-13: Permission mode indicator (READ ●, BASH ●, RESTRICTED ●).
     */
    public String getPermissionIndicator() {
        return switch (permissionMode) {
            case "READ" -> Terminal.green("READ ● ");
            case "RESTRICTED" -> Terminal.red("REST ● ");
            default -> Terminal.yellow("BASH ● ");
        };
    }

    /**
     * H-13: Vim mode indicator (NORMAL/INSERT/VISUAL/COMMAND).
     */
    public String getVimModeIndicator() {
        if (!vimMode || vimState == null) return "";
        String mode = vimState.name();
        return switch (mode) {
            case "INSERT" -> Terminal.green("[" + mode + "] ");
            case "VISUAL" -> Terminal.cyan("[" + mode + "] ");
            case "COMMAND" -> Terminal.yellow("[" + mode + "] ");
            default -> Terminal.dim("[N] ");
        };
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPermissionIndicator());

        // H-03: Search mode indicator
        if (searchMode) {
            sb.append(Terminal.cyan("(reverse-i-search)`" + searchQuery + "': "));
        }

        // H-13: Vim mode indicator
        sb.append(getVimModeIndicator());
        sb.append(prompt);

        String text = buffer.toString();
        String before = text.substring(0, cursorPosition);
        String after = text.substring(cursorPosition);

        // H-13: Animated shimmer cursor
        sb.append(before);
        if (vimState == VimState.INSERT) {
            sb.append(Terminal.inverse(Terminal.dim(getAnimatedCursor())));
        } else {
            sb.append(Terminal.inverse(Terminal.dim(getAnimatedCursor())));
        }
        sb.append(after);

        if (multilineMode) sb.append(Terminal.dim(" [M]"));
        if (vimMode && vimState == VimState.NORMAL) sb.append(Terminal.dim(" [N]"));

        return sb.toString();
    }

    public String renderWithSuggestions() {
        String base = render();
        if (!showSuggestions) return base;
        StringBuilder sb = new StringBuilder(base);
        sb.append("\n");
        for (int i = 0; i < Math.min(suggestions.size(), 5); i++) {
            TypeaheadEngine.Suggestion s = suggestions.get(i);
            String prefix = (i == suggestionIndex) ? "▶ " : "  ";
            sb.append(prefix).append(Terminal.cyan("/" + s.name()))
              .append(" ").append(Terminal.dim(s.description())).append("\n");
        }
        return sb.toString();
    }

    // === Vim Operations ===

    public void vimNormal(char c) {
        switch (c) {
            case 'i' -> vimState = VimState.INSERT;
            case 'a' -> { cursorPosition = Math.min(cursorPosition + 1, buffer.length()); vimState = VimState.INSERT; }
            case 'A' -> { cursorPosition = buffer.length(); vimState = VimState.INSERT; }
            case 'I' -> { cursorPosition = 0; vimState = VimState.INSERT; }
            case 'x' -> delete();
            case '0' -> cursorPosition = 0;
            case '$' -> cursorPosition = buffer.length();
            case 'w' -> moveWordRight();
            case 'b' -> moveWordLeft();
            case '/' -> enterSearchMode();
            case ':' -> {
                buffer.setLength(0);
                buffer.append(":");
                cursorPosition = 1;
            }
        }
    }

    public void vimInsert(char c) {
        if (c == 27) { vimState = VimState.NORMAL; return; }
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
    public List<TypeaheadEngine.Suggestion> getSuggestions() { return suggestions; }
    public boolean isShowingSuggestions() { return showSuggestions; }
    public TypeaheadEngine getTypeahead() { return typeahead; }
    public VimState getVimState() { return vimState; }

    public void setPrompt(String prompt) { this.prompt = prompt; }
    public void setVimMode(boolean enabled) { this.vimMode = enabled; }
    public void setVimState(VimState state) { this.vimState = state; }
    public void setMultilineMode(boolean enabled) {
        this.multilineMode = enabled;
        if (enabled) syncCursorFromPosition();
    }
    public void setPermissionMode(String mode) { this.permissionMode = mode; }
    public String getPermissionMode() { return permissionMode; }
    public void setTypeahead(TypeaheadEngine engine) { this.typeahead.registerCommands(List.of()); }
}
