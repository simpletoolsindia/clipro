package com.clipro.ui.components;

import com.clipro.ui.Terminal;
import java.util.ArrayList;
import java.util.List;

/**
 * Terminal input field - Pixel-perfect OpenClaude style with vim support.
 * Reference: ~/openclaude/src/components/Input.tsx
 */
public class InputField {
    private StringBuilder buffer = new StringBuilder();
    private int cursorPosition = 0;
    private final List<String> history = new ArrayList<>();
    private int historyIndex = -1;
    private String currentInput = "";
    private String prompt = "▶ ";
    private boolean masked = false;
    private String vimMode = "";

    public InputField() {}

    public InputField(String prompt) {
        this.prompt = prompt;
    }

    public void setPrompt(String prompt) { this.prompt = prompt; }
    public void setMasked(boolean masked) { this.masked = masked; }
    public void setVimMode(String mode) { this.vimMode = mode; }

    public void insert(char c) {
        if (cursorPosition == buffer.length()) buffer.append(c);
        else buffer.insert(cursorPosition, c);
        cursorPosition++;
    }

    public void insert(String text) {
        if (cursorPosition == buffer.length()) buffer.append(text);
        else buffer.insert(cursorPosition, text);
        cursorPosition += text.length();
    }

    public void backspace() {
        if (cursorPosition > 0) {
            cursorPosition--;
            buffer.deleteCharAt(cursorPosition);
        }
    }

    public void delete() {
        if (cursorPosition < buffer.length()) buffer.deleteCharAt(cursorPosition);
    }

    public void moveCursorLeft() { if (cursorPosition > 0) cursorPosition--; }
    public void moveCursorRight() { if (cursorPosition < buffer.length()) cursorPosition++; }
    public void moveCursorToStart() { cursorPosition = 0; }
    public void moveCursorToEnd() { cursorPosition = buffer.length(); }

    public void historyUp() {
        if (history.isEmpty()) return;
        if (historyIndex == -1) currentInput = buffer.toString();
        if (historyIndex < history.size() - 1) {
            historyIndex++;
            buffer.setLength(0);
            buffer.append(history.get(history.size() - 1 - historyIndex));
            cursorPosition = buffer.length();
        }
    }

    public void historyDown() {
        if (historyIndex == -1) return;
        historyIndex--;
        buffer.setLength(0);
        buffer.append(historyIndex == -1 ? currentInput : history.get(history.size() - 1 - historyIndex));
        cursorPosition = buffer.length();
    }

    public void addToHistory(String input) {
        if (input != null && !input.isEmpty() && (history.isEmpty() || !history.get(history.size() - 1).equals(input))) {
            history.add(input);
        }
        historyIndex = -1;
    }

    public String submit() {
        String input = buffer.toString();
        addToHistory(input);
        buffer.setLength(0);
        cursorPosition = 0;
        return input;
    }

    public void clear() { buffer.setLength(0); cursorPosition = 0; }
    public String getText() { return buffer.toString(); }
    public int getCursorPosition() { return cursorPosition; }
    public int getLength() { return buffer.length(); }
    public boolean isEmpty() { return buffer.length() == 0; }
    public List<String> getHistory() { return new ArrayList<>(history); }

    public String render() {
        String displayText = masked ? "•".repeat(buffer.length()) : buffer.toString();
        String before = displayText.substring(0, cursorPosition);
        String after = displayText.substring(cursorPosition);
        String cursor = Terminal.dim("\u2588"); // Block cursor

        String modeIndicator = vimMode.isEmpty() ? "" : Terminal.yellow(" " + vimMode);
        return prompt + before + cursor + after + modeIndicator;
    }

    public String renderWithCursor() {
        Terminal.clearLine();
        return "\r" + render();
    }

    // Vim motion commands
    public void deleteWord() {
        int start = cursorPosition;
        while (start > 0 && buffer.charAt(start - 1) == ' ') start--;
        while (start > 0 && buffer.charAt(start - 1) != ' ') start--;
        buffer.delete(start, cursorPosition);
        cursorPosition = start;
    }

    public void deleteLine() {
        buffer.setLength(0);
        cursorPosition = 0;
    }

    public void insertLineAbove() {
        // For multi-line input
    }
}