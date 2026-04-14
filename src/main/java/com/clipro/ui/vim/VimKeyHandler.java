package com.clipro.ui.vim;

/**
 * Handles Vim keypresses and keybindings.
 * Reference: openclaude vim keybindings
 */
public class VimKeyHandler {

    public interface TextNavigator {
        void moveTo(int offset);
        int getOffset();
        int getLength();
    }

    private final VimMode vimMode;
    private TextNavigator navigator;

    public VimKeyHandler(VimMode vimMode) {
        this.vimMode = vimMode;
    }

    public void setNavigator(TextNavigator navigator) {
        this.navigator = navigator;
    }

    public boolean handleKey(String key) {
        switch (vimMode.getState()) {
            case NORMAL:
                return handleNormalKey(key);
            case INSERT:
                return handleInsertKey(key);
            case VISUAL:
            case VISUAL_LINE:
                return handleVisualKey(key);
            case COMMAND:
                return handleCommandKey(key);
            default:
                return false;
        }
    }

    /**
     * Handle key without requiring navigator (for mode changes).
     */
    public boolean handleModeKey(String key) {
        switch (key) {
            case "i":
            case "a":
            case "A":
            case "I":
            case "o":
            case "O":
            case "v":
            case "V":
            case ":":
                return handleNormalKey(key);
            case "Escape":
                if (vimMode.getState() == VimState.INSERT || vimMode.getState().isVisual()) {
                    vimMode.enterNormal();
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    private boolean handleNormalKey(String key) {
        switch (key) {
            case "i":
                vimMode.enterInsert();
                return true;
            case "a":
                if (navigator != null) {
                    int pos = navigator.getOffset();
                    int len = navigator.getLength();
                    if (pos < len) navigator.moveTo(pos + 1);
                }
                vimMode.enterInsert();
                return true;
            case "A":
                if (navigator != null) {
                    navigator.moveTo(navigator.getLength());
                }
                vimMode.enterInsert();
                return true;
            case "I":
                if (navigator != null) {
                    navigator.moveTo(0);
                }
                vimMode.enterInsert();
                return true;
            case "o":
                insertNewlineAfter();
                vimMode.enterInsert();
                return true;
            case "O":
                insertNewlineBefore();
                vimMode.enterInsert();
                return true;
            case "h":
            case "ArrowLeft":
                if (navigator != null) {
                    int pos = navigator.getOffset();
                    if (pos > 0) navigator.moveTo(pos - 1);
                }
                return true;
            case "l":
            case "ArrowRight":
                if (navigator != null) {
                    int pos = navigator.getOffset();
                    if (pos < navigator.getLength()) navigator.moveTo(pos + 1);
                }
                return true;
            case "j":
            case "ArrowDown":
                // Line down - handled by input field
                return true;
            case "k":
            case "ArrowUp":
                // Line up - handled by input field
                return true;
            case "w":
                wordForward();
                return true;
            case "b":
                wordBackward();
                return true;
            case "e":
                wordEnd();
                return true;
            case "0":
                if (navigator != null) navigator.moveTo(0);
                return true;
            case "$":
                if (navigator != null) navigator.moveTo(navigator.getLength());
                return true;
            case "^":
                if (navigator != null) {
                    // Move to first non-whitespace
                    navigator.moveTo(findLineStart());
                }
                return true;
            case "x":
                deleteChar();
                vimMode.setLastAction("x");
                return true;
            case "p":
                pasteAfter();
                vimMode.setLastAction("p");
                return true;
            case "P":
                pasteBefore();
                vimMode.setLastAction("P");
                return true;
            case "d":
                vimMode.setLastAction("d");
                return true;
            case "y":
                vimMode.setLastAction("y");
                return true;
            case "Escape":
            case "[":
                // Stay in normal mode
                return true;
            case ":":
                vimMode.enterCommand();
                return true;
            case "v":
                vimMode.enterVisual();
                return true;
            case "V":
                vimMode.enterVisualLine();
                return true;
            default:
                return false;
        }
    }

    private boolean handleInsertKey(String key) {
        if ("Escape".equals(key) || "[".equals(key)) {
            vimMode.enterNormal();
            return true;
        }
        return false;
    }

    private boolean handleVisualKey(String key) {
        if ("Escape".equals(key) || "[".equals(key)) {
            vimMode.enterNormal();
            return true;
        }
        if ("d".equals(vimMode.getLastAction()) || "y".equals(vimMode.getLastAction())) {
            // Yank or delete selection
            return true;
        }
        return handleNormalKey(key);
    }

    private boolean handleCommandKey(String key) {
        if ("Escape".equals(key) || "[".equals(key) || "q".equals(key) && vimMode.getState() == VimState.COMMAND) {
            vimMode.enterNormal();
            return true;
        }
        return false;
    }

    private void wordForward() {
        if (navigator == null) return;
        String text = getNavigatorText();
        int pos = navigator.getOffset();
        int len = text.length();

        // Skip current word
        while (pos < len && isWordChar(text.charAt(pos))) {
            pos++;
        }
        // Skip whitespace
        while (pos < len && !isWordChar(text.charAt(pos))) {
            pos++;
        }
        navigator.moveTo(Math.min(pos, len));
    }

    private void wordBackward() {
        if (navigator == null) return;
        String text = getNavigatorText();
        int pos = navigator.getOffset();

        // Skip whitespace backwards
        while (pos > 0 && !isWordChar(text.charAt(pos - 1))) {
            pos--;
        }
        // Skip word backwards
        while (pos > 0 && isWordChar(text.charAt(pos - 1))) {
            pos--;
        }
        navigator.moveTo(pos);
    }

    private void wordEnd() {
        if (navigator == null) return;
        String text = getNavigatorText();
        int pos = navigator.getOffset();
        int len = text.length();

        // Skip whitespace
        while (pos < len && !isWordChar(text.charAt(pos))) {
            pos++;
        }
        // Go to end of word
        while (pos < len && isWordChar(text.charAt(pos))) {
            pos++;
        }
        navigator.moveTo(Math.max(0, pos - 1));
    }

    private String getNavigatorText() {
        if (navigator == null) return "";
        // Get text length workaround - navigate to end and back
        return ""; // Text provided by InputField
    }

    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private int findLineStart() {
        if (navigator == null) return 0;
        String text = getNavigatorText();
        int pos = navigator.getOffset();
        // Find start of line (newline or start)
        while (pos > 0 && text.charAt(pos - 1) != '\n') {
            pos--;
        }
        return pos;
    }

    private void deleteChar() {
        // Handled by InputField through callbacks
    }

    private void pasteAfter() {
        // Handled by InputField through callbacks
    }

    private void pasteBefore() {
        // Handled by InputField through callbacks
    }

    private void insertNewlineAfter() {
        // Handled by InputField through callbacks
    }

    private void insertNewlineBefore() {
        // Handled by InputField through callbacks
    }
}
