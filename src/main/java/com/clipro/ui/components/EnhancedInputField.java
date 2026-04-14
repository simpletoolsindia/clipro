package com.clipro.ui.components;

import com.clipro.ui.Terminal;
import com.clipro.ui.vim.VimState;
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
    private List<TypeaheadEngine.Suggestion> suggestions = new ArrayList<>();
    private int suggestionIndex = 0;
    private boolean multilineMode = false;

    // Multi-line cursor tracking (H-01)
    private int cursorRow = 0;       // Current line (0-indexed)
    private int cursorCol = 0;       // Column within current line
    private List<Integer> lineStarts = new ArrayList<>();  // Char offset where each line starts

    // L-12: Permission mode indicator
    private String permissionMode = "BASH";

    // L-14: Command queue
    private List<String> commandQueue = new ArrayList<>();
    private boolean queueMode = false;

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

    /**
     * Insert newline character (Enter key in multiline mode).
     */
    public void insertNewline() {
        if (!multilineMode) return;
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

    // === Multi-line Cursor Navigation (H-01) ===

    /**
     * Update line starts cache. Call after any buffer modification.
     */
    private void updateLineStarts() {
        lineStarts.clear();
        lineStarts.add(0);
        for (int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) == '\n') {
                lineStarts.add(i + 1);
            }
        }
    }

    /**
     * Sync cursorRow/cursorCol from char position.
     */
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

    /**
     * Sync char position from cursorRow/cursorCol.
     */
    private void syncPositionFromCursor() {
        updateLineStarts();
        if (cursorRow >= lineStarts.size()) {
            cursorRow = lineStarts.size() - 1;
        }
        int lineStart = lineStarts.get(cursorRow);
        int lineEnd = (cursorRow + 1 < lineStarts.size()) ? lineStarts.get(cursorRow + 1) - 1 : buffer.length();
        cursorPosition = Math.min(lineStart + cursorCol, lineEnd);
    }

    /**
     * Move cursor up one line.
     */
    public void moveLineUp() {
        if (!multilineMode || cursorRow == 0) return;
        syncCursorFromPosition();
        cursorRow--;
        int lineStart = lineStarts.get(cursorRow);
        int lineEnd = (cursorRow + 1 < lineStarts.size()) ? lineStarts.get(cursorRow + 1) - 1 : buffer.length();
        int lineLen = lineEnd - lineStart;
        cursorCol = Math.min(cursorCol, lineLen);
        syncPositionFromCursor();
    }

    /**
     * Move cursor down one line.
     */
    public void moveLineDown() {
        if (!multilineMode) return;
        syncCursorFromPosition();
        if (cursorRow >= lineStarts.size() - 1) return;
        cursorRow++;
        int lineStart = lineStarts.get(cursorRow);
        int lineEnd = (cursorRow + 1 < lineStarts.size()) ? lineStarts.get(cursorRow + 1) - 1 : buffer.length();
        int lineLen = lineEnd - lineStart;
        cursorCol = Math.min(cursorCol, lineLen);
        syncPositionFromCursor();
    }

    /**
     * Insert tab (4 spaces) at cursor. H-01.
     */
    public void insertTab() {
        insert("    ");
    }

    /**
     * Remove up to 4 spaces from start of current line. H-01.
     */
    public void removeTab() {
        if (!multilineMode) return;
        syncCursorFromPosition();
        if (cursorCol == 0) return; // At line start, nothing to remove

        int lineStart = lineStarts.get(cursorRow);
        int removeCount = 0;

        // Count leading spaces on current line
        for (int i = lineStart; i < Math.min(lineStart + 4, buffer.length()); i++) {
            if (buffer.charAt(i) == ' ') {
                removeCount++;
            } else {
                break;
            }
        }

        // Only remove if cursor is at or before where spaces end
        int spacesEnd = lineStart + removeCount;
        if (removeCount > 0 && cursorPosition <= spacesEnd) {
            for (int i = 0; i < removeCount; i++) {
                buffer.deleteCharAt(lineStart);
            }
            cursorPosition = lineStart;
            cursorCol = 0;
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

    /**
     * Get color-coded permission mode indicator (L-12).
     */
    private String getPermissionIndicator() {
        return switch (permissionMode) {
            case "READ" -> Terminal.green("[READ ▶] ");
            case "BASH" -> Terminal.yellow("[BASH ▶] ");
            case "REST" -> Terminal.red("[REST ▶] ");
            default -> Terminal.dim("[?] ");
        };
    }

    public String render() {
        StringBuilder sb = new StringBuilder();

        // L-12: Permission mode indicator
        sb.append(getPermissionIndicator());

        // L-14: Queue indicator
        if (queueMode && !commandQueue.isEmpty()) {
            sb.append(Terminal.cyan("[" + commandQueue.size() + " queued] "));
        }

        sb.append(prompt);

        if (multilineMode) {
            // Render each line with proper cursor positioning
            String text = buffer.toString();
            updateLineStarts();

            int globalPos = 0;
            for (int row = 0; row < lineStarts.size(); row++) {
                if (row > 0) {
                    sb.append("\n").append(prompt);
                }
                int lineStart = lineStarts.get(row);
                int lineEnd = (row + 1 < lineStarts.size()) ? lineStarts.get(row + 1) - 1 : text.length();
                int lineLen = lineEnd - lineStart;

                if (globalPos + lineLen <= cursorPosition) {
                    // Past cursor - render full line
                    sb.append(text.substring(globalPos, lineEnd));
                    if (row == cursorRow) {
                        sb.append(Terminal.inverse(Terminal.dim(" ")));
                    }
                } else if (globalPos >= cursorPosition) {
                    // Before cursor - render full line with cursor at start
                    if (row == cursorRow) {
                        sb.append(Terminal.inverse(Terminal.dim("\u2588")));
                    }
                    sb.append(text.substring(lineStart, lineEnd));
                } else {
                    // Cursor in this line
                    int localPos = cursorPosition - lineStart;
                    sb.append(text.substring(lineStart, lineStart + localPos));
                    sb.append(Terminal.inverse(Terminal.dim("\u2588")));
                    if (lineEnd > lineStart + localPos) {
                        sb.append(text.substring(lineStart + localPos + 1, lineEnd));
                    }
                }
                globalPos = lineEnd + 1; // +1 for newline
            }

            // Cursor at end of last line
            if (cursorPosition == buffer.length() && cursorRow >= lineStarts.size() - 1) {
                sb.append(Terminal.inverse(Terminal.dim("\u2588")));
            }
        } else {
            // Single-line mode
            String text = buffer.toString();
            String before = text.substring(0, cursorPosition);
            String after = text.substring(cursorPosition);

            sb.append(before);
            sb.append(Terminal.inverse(Terminal.dim("\u2588"))); // Block cursor
            sb.append(after);
        }

        // Indicators
        if (vimMode) {
            sb.append(Terminal.dim(" [" + vimState.name().charAt(0) + "]"));
        }
        if (multilineMode) {
            sb.append(Terminal.dim(" [M]"));
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
            TypeaheadEngine.Suggestion s = suggestions.get(i);
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
    public List<TypeaheadEngine.Suggestion> getSuggestions() { return suggestions; }
    public boolean isShowingSuggestions() { return showSuggestions; }
    public TypeaheadEngine getTypeahead() { return typeahead; }

    public void setPrompt(String prompt) { this.prompt = prompt; }
    public void setVimMode(boolean enabled) { this.vimMode = enabled; }
    public void setMultilineMode(boolean enabled) {
        this.multilineMode = enabled;
        if (enabled) syncCursorFromPosition();
    }

    // L-12: Permission mode setter
    public void setPermissionMode(String mode) { this.permissionMode = mode; }
    public String getPermissionMode() { return permissionMode; }

    // L-14: Command queue methods
    public void queueCommand(String command) {
        commandQueue.add(command);
        queueMode = true;
    }

    public void queueCurrentInput() {
        String input = buffer.toString();
        if (!input.isEmpty()) {
            queueCommand(input);
            buffer.setLength(0);
            cursorPosition = 0;
        }
    }

    public String dequeueNext() {
        if (commandQueue.isEmpty()) {
            queueMode = false;
            return null;
        }
        return commandQueue.remove(0);
    }

    public boolean isQueueMode() { return queueMode; }
    public int getQueueSize() { return commandQueue.size(); }
    public void clearQueue() { commandQueue.clear(); queueMode = false; }
    public void setTypeahead(TypeaheadEngine engine) {
        // Copy suggestions from engine
        List<TypeaheadEngine.CommandSuggestion> cmds = new ArrayList<>();
        for (var s : engine.search("", TypeaheadEngine.SuggestionType.COMMAND)) {
            cmds.add(new TypeaheadEngine.CommandSuggestion(s.name(), s.description(), s.category(), s.score()));
        }
        typeahead.registerCommands(cmds);
    }
}
