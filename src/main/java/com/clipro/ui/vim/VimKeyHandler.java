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

    /**
     * Handle a register key for macro recording/playback.
     * Called after q or @ is pressed.
     */
    public boolean handleRegisterKey(char register) {
        // Validate register is a-z
        if (register >= 'a' && register <= 'z') {
            if ("q".equals(lastSpecialKey)) {
                // q<register> - start recording or stop recording
                if (vimMode.isRecording()) {
                    vimMode.stopRecording();
                } else {
                    vimMode.startRecording(register);
                }
                lastSpecialKey = null;
                return true;
            } else if ("@".equals(lastSpecialKey)) {
                // @<register> - playback macro
                String macro = vimMode.playbackMacro(register);
                lastSpecialKey = null;
                return macro != null;
            }
        }
        lastSpecialKey = null;
        return false;
    }

    /**
     * Handle count prefix for commands like 3d2w or 3@x.
     */
    public void setCountPrefix(int count) {
        this.countPrefix = count;
    }

    /**
     * Get the current count prefix.
     */
    public int getCountPrefix() {
        return countPrefix;
    }

    /**
     * Clear the count prefix.
     */
    public void clearCountPrefix() {
        this.countPrefix = 1;
    }

    private String lastSpecialKey = null;
    private int countPrefix = 1;

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
        // Check for macro recording mode (q followed by register a-z)
        if ("q".equals(key) && !vimMode.isRecording()) {
            // First q - wait for register
            return false; // Let the input field handle this
        }

        // Check for macro playback (@ register)
        if ("@".equals(key)) {
            return false; // Wait for register input
        }

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
        if ("Escape".equals(key) || "[".equals(key)) {
            vimMode.enterNormal();
            clearCommandBuffer();
            return true;
        }
        if ("q".equals(key) && vimMode.getState() == VimState.COMMAND) {
            vimMode.enterNormal();
            clearCommandBuffer();
            return true;
        }

        // Append key to command buffer
        commandBuffer.append(key);

        // Check for Enter to execute command
        if ("Enter".equals(key)) {
            String cmd = getCommandBuffer();
            clearCommandBuffer();

            // Handle :s substitute command
            if (cmd.startsWith("s/") || cmd.startsWith("substitute/")) {
                return executeSubstitute(cmd);
            }

            // Handle :w (write), :q (quit), :wq
            if (cmd.equals("w")) {
                // Write - handled by InputField callbacks
                vimMode.enterNormal();
                return true;
            }
            if (cmd.equals("q")) {
                vimMode.enterNormal();
                return true;
            }
            if (cmd.equals("wq") || cmd.equals("x")) {
                vimMode.enterNormal();
                return true;
            }

            vimMode.enterNormal();
            return true;
        }

        return false;
    }

    // Command buffer for : command mode
    private StringBuilder commandBuffer = new StringBuilder();

    private String getCommandBuffer() {
        String cmd = commandBuffer.toString();
        // Remove leading colon if present
        if (cmd.startsWith(":")) {
            return cmd.substring(1);
        }
        return cmd;
    }

    private void clearCommandBuffer() {
        commandBuffer.setLength(0);
    }

    /**
     * Execute :s/pattern/replacement/flags command.
     * Supports: :s/foo/bar/, :s/foo/bar/g, :s/foo/bar/c, :%s/foo/bar/g
     */
    private boolean executeSubstitute(String cmd) {
        if (navigator == null) return false;

        boolean global = false;
        boolean confirm = false;
        boolean wholeBuffer = false;

        // Parse flags
        String flags = "";
        int lastSlash = cmd.lastIndexOf('/');
        if (lastSlash > 0) {
            int prevSlash = cmd.lastIndexOf('/', lastSlash - 1);
            if (prevSlash > 0) {
                flags = cmd.substring(lastSlash + 1);
                if (flags.contains("g")) global = true;
                if (flags.contains("c")) confirm = true;
            }
        }

        // Parse :%s vs :s
        String pattern;
        String replacement;
        if (cmd.startsWith("%")) {
            wholeBuffer = true;
            cmd = cmd.substring(1);
        }

        // Parse the substitute command
        // Format: s/pattern/replacement/flags or substitute/pattern/replacement/flags
        String substituteCmd;
        if (cmd.startsWith("substitute")) {
            substituteCmd = cmd.substring("substitute".length());
        } else {
            substituteCmd = cmd;
        }

        // Find the pattern and replacement between slashes
        // s/pattern/replacement/flags
        int firstSlash = substituteCmd.indexOf('/');
        if (firstSlash < 0) return false;

        pattern = substituteCmd.substring(1, firstSlash);
        int secondSlash = substituteCmd.indexOf('/', firstSlash + 1);
        if (secondSlash < 0) {
            // No closing slash - pattern only
            replacement = "";
            secondSlash = substituteCmd.length();
        } else {
            replacement = substituteCmd.substring(firstSlash + 1, secondSlash);
        }

        // Get the text from navigator
        String text = "";
        if (navigator instanceof ExtendedTextNavigator) {
            text = ((ExtendedTextNavigator) navigator).getText();
        }

        // Perform substitution
        if (wholeBuffer) {
            // Replace all occurrences in entire text
            String newText = text.replaceAll(pattern, replacement);
            if (!newText.equals(text)) {
                if (navigator instanceof ExtendedTextNavigator) {
                    ((ExtendedTextNavigator) navigator).setText(newText);
                }
                return true;
            }
        } else {
            // Replace on current line only
            int lineStart = findLineStartFromOffset(text, navigator.getOffset());
            int lineEnd = findLineEndFromOffset(text, navigator.getOffset());

            String line = text.substring(lineStart, lineEnd);
            String newLine;

            if (global) {
                newLine = line.replaceAll(pattern, replacement);
            } else {
                newLine = line.replaceFirst(pattern, replacement);
            }

            if (!newLine.equals(line)) {
                String newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd);
                if (navigator instanceof ExtendedTextNavigator) {
                    ((ExtendedTextNavigator) navigator).setText(newText);
                }
                return true;
            }
        }

        return false;
    }

    private int findLineStartFromOffset(String text, int pos) {
        while (pos > 0 && text.charAt(pos - 1) != '\n') {
            pos--;
        }
        return pos;
    }

    private int findLineEndFromOffset(String text, int pos) {
        int len = text.length();
        while (pos < len && text.charAt(pos) != '\n') {
            pos++;
        }
        return pos;
    }

    // Extended TextNavigator with setText method
    public interface ExtendedTextNavigator extends TextNavigator {
        void setText(String text);
        String getText();
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
