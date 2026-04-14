package com.clipro.ui.tamboui;

import com.clipro.ui.components.Message;
import com.clipro.ui.components.MessageRole;

import dev.tamboui.tui.*;
import dev.tamboui.tui.event.*;
import dev.tamboui.widgets.paragraph.Paragraph;
import dev.tamboui.terminal.Frame;
import dev.tamboui.layout.Rect;
import dev.tamboui.text.Text;

import java.util.*;
import java.util.function.Consumer;
import java.time.format.DateTimeFormatter;

/**
 * TamboUI implementation of TuiAdapter.
 * Matches openclaude FullscreenLayout architecture with:
 * - Scrollable message area (flexGrow=1)
 * - Fixed bottom prompt (flexShrink=0)
 * - Status bar
 *
 * Reference: openclaude/src/components/FullscreenLayout.tsx
 */
public class TamboUIAdapter implements TuiAdapter {

    private final List<Message> messages = new ArrayList<>();
    private final List<String> inputHistory = new ArrayList<>();
    private final StringBuilder currentInput = new StringBuilder();

    private TuiRunner tui;
    private Frame frame;
    private String modelName = "qwen3-coder:32b";
    private String status = "Ready";
    private boolean connected = false;
    private String vimMode = "";
    private int inputTokens = 0;
    private int outputTokens = 0;
    private long latencyMs = 0;
    private Consumer<String> inputCallback;
    private Consumer<String> inputChangeCallback;
    private KeyEventHandler keyHandler;
    private Consumer<String> streamingConsumer;
    private StringBuilder streamedContent = new StringBuilder();
    private int scrollOffset = 0;
    private int historyIndex = -1;
    private String savedInput = "";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void init() throws Exception {
        var config = TuiConfig.builder()
            .tickRate(java.time.Duration.ofMillis(100))
            .build();
        tui = TuiRunner.create(config);
    }

    @Override
    public void shutdown() {
        if (tui != null) {
            tui.close();
        }
    }

    @Override
    public void run() throws Exception {
        tui.run(this::handleEvent, this::render);
    }

    @Override
    public void quit() {
        // Will be handled via event
    }

    private boolean handleEvent(Event event, TuiRunner runner) {
        if (event instanceof KeyEvent k) {
            return handleKeyEvent(k, runner);
        }
        return false;
    }

    private boolean handleKeyEvent(KeyEvent key, TuiRunner runner) {
        var keyCode = key.code();

        // Handle Ctrl combinations for character keys
        if (key.hasCtrl()) {
            char ch = key.character();
            if (ch == 'l' || ch == 'L') {
                messages.clear();
                scrollOffset = 0;
                return true;
            }
            if (ch == 'u' || ch == 'U') {
                currentInput.setLength(0);
                notifyInputChange();
                return true;
            }
            if (ch == 'c' || ch == 'C') {
                currentInput.setLength(0);
                historyIndex = -1;
                notifyInputChange();
                return true;
            }
        }

        if (keyCode == KeyCode.ENTER) {
            String input = currentInput.toString();
            if (!input.isEmpty()) {
                inputHistory.add(input);
                historyIndex = -1;
                if (inputCallback != null) {
                    inputCallback.accept(input);
                }
                currentInput.setLength(0);
            }
            return true;
        }
        if (keyCode == KeyCode.BACKSPACE) {
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
                notifyInputChange();
            }
            return true;
        }
        if (keyCode == KeyCode.UP) {
            if (inputHistory.isEmpty()) return true;
            if (historyIndex == -1) {
                savedInput = currentInput.toString();
                historyIndex = 0;
            } else if (historyIndex < inputHistory.size() - 1) {
                historyIndex++;
            }
            currentInput.setLength(0);
            currentInput.append(inputHistory.get(inputHistory.size() - 1 - historyIndex));
            notifyInputChange();
            return true;
        }
        if (keyCode == KeyCode.DOWN) {
            if (historyIndex == -1) return true;
            historyIndex--;
            currentInput.setLength(0);
            if (historyIndex == -1) {
                currentInput.append(savedInput);
            } else {
                currentInput.append(inputHistory.get(inputHistory.size() - 1 - historyIndex));
            }
            notifyInputChange();
            return true;
        }
        if (keyCode == KeyCode.ESCAPE || keyCode == KeyCode.CHAR) {
            char ch = key.character();
            if (ch == 'q' || ch == 'Q') {
                if (vimMode.equals("NORMAL") || vimMode.equals("INSERT")) {
                    vimMode = "";
                    return true;
                }
                runner.quit();
                return false;
            }
            // Regular character input
            if (ch != 0 && !Character.isISOControl(ch)) {
                currentInput.append(ch);
                notifyInputChange();
            }
            if (keyHandler != null) {
                KeyType keyType = mapKeyType(keyCode);
                return keyHandler.handle(keyType, currentInput.toString());
            }
            return true;
        }

        // Arrow keys and other special keys
        if (keyHandler != null) {
            KeyType keyType = mapKeyType(keyCode);
            return keyHandler.handle(keyType, currentInput.toString());
        }
        return true;
    }

    private KeyType mapKeyType(KeyCode keyCode) {
        if (keyCode == KeyCode.ENTER) return KeyType.ENTER;
        if (keyCode == KeyCode.ESCAPE) return KeyType.ESCAPE;
        if (keyCode == KeyCode.BACKSPACE) return KeyType.BACKSPACE;
        if (keyCode == KeyCode.DELETE) return KeyType.DELETE;
        if (keyCode == KeyCode.UP) return KeyType.ARROW_UP;
        if (keyCode == KeyCode.DOWN) return KeyType.ARROW_DOWN;
        if (keyCode == KeyCode.LEFT) return KeyType.ARROW_LEFT;
        if (keyCode == KeyCode.RIGHT) return KeyType.ARROW_RIGHT;
        if (keyCode == KeyCode.TAB) return KeyType.TAB;
        return KeyType.CHAR;
    }

    private void notifyInputChange() {
        if (inputChangeCallback != null) {
            inputChangeCallback.accept(currentInput.toString());
        }
    }

    private void render(Frame frame) {
        this.frame = frame;
        Rect area = frame.area();
        int width = area.width();
        int height = area.height();

        // Build paragraph with full layout
        var paragraph = Paragraph.builder()
            .text(Text.from(buildLayout(width, height)))
            .build();

        frame.renderWidget(paragraph, area);
    }

    private String buildLayout(int width, int height) {
        StringBuilder sb = new StringBuilder();

        // ===== HEADER =====
        sb.append(renderHeader(width));
        sb.append("\n");

        // ===== DIVIDER =====
        sb.append(OpenClaudeTheme.dimText("─".repeat(Math.max(0, width - 2))));
        sb.append("\n");

        // ===== MESSAGES =====
        int headerRows = 3;
        int statusRows = 2;
        int inputRows = 2;
        int messageAreaHeight = height - headerRows - statusRows - inputRows;
        sb.append(renderMessages(messageAreaHeight, width));

        // ===== INPUT LINE =====
        sb.append(renderInput(width));
        sb.append("\n");

        // ===== STATUS BAR =====
        sb.append(renderStatusBar(width));

        return sb.toString();
    }

    private String renderHeader(int width) {
        StringBuilder sb = new StringBuilder();

        // Top border
        sb.append(OpenClaudeTheme.BORDER_TL);
        sb.append("─".repeat(width - 2));
        sb.append(OpenClaudeTheme.BORDER_TR);
        sb.append("\n");

        // Title row
        String connStatus = connected
            ? OpenClaudeTheme.successText("● Connected")
            : OpenClaudeTheme.errorText("○ Disconnected");
        String vimIndicator = vimMode.isEmpty() ? "" : " " + OpenClaudeTheme.warningText("[" + vimMode + "]");
        String title = OpenClaudeTheme.boldText(OpenClaudeTheme.claudeText(" CLIPRO "));
        String modelText = OpenClaudeTheme.dimText(modelName);

        sb.append(OpenClaudeTheme.BORDER_V).append(" ").append(title).append("  ").append(connStatus);
        int padding = width - stripAnsi(title).length() - stripAnsi(connStatus).length()
                      - modelName.length() - stripAnsi(vimIndicator).length() - 6;
        if (padding > 0) {
            sb.append(" ".repeat(padding));
        }
        sb.append(modelText).append(vimIndicator).append(" ").append(OpenClaudeTheme.BORDER_V);

        sb.append("\n");

        // Bottom border
        sb.append(OpenClaudeTheme.BORDER_BL);
        sb.append("─".repeat(width - 2));
        sb.append(OpenClaudeTheme.BORDER_BR);

        return sb.toString();
    }

    private String renderMessages(int maxLines, int width) {
        StringBuilder sb = new StringBuilder();

        if (messages.isEmpty()) {
            for (int i = 0; i < maxLines; i++) {
                sb.append(OpenClaudeTheme.BORDER_V);
                sb.append(" ".repeat(width - 2));
                sb.append(OpenClaudeTheme.BORDER_V);
                sb.append("\n");
            }
            return sb.toString();
        }

        // Calculate visible range
        int totalMessages = messages.size();
        int startIdx = Math.max(0, totalMessages - maxLines - scrollOffset);
        int endIdx = Math.min(totalMessages, startIdx + maxLines);

        // Scroll indicator at top
        if (scrollOffset > 0 || totalMessages > maxLines) {
            int moreCount = totalMessages - endIdx;
            if (moreCount > 0) {
                sb.append(OpenClaudeTheme.BORDER_V).append(" ");
                sb.append(OpenClaudeTheme.mutedText("↑ " + moreCount + " more ↑"));
                sb.append(" ".repeat(Math.max(0, width - 20)));
                sb.append(OpenClaudeTheme.BORDER_V).append("\n");
            }
        }

        for (int i = startIdx; i < endIdx; i++) {
            Message msg = messages.get(i);
            String rendered = renderMessageRow(i + 1, msg, width);
            sb.append(rendered).append("\n");
        }

        // Fill remaining space
        int rendered = endIdx - startIdx;
        for (int i = rendered; i < maxLines; i++) {
            sb.append(OpenClaudeTheme.BORDER_V);
            sb.append(" ".repeat(width - 2));
            sb.append(OpenClaudeTheme.BORDER_V);
            sb.append("\n");
        }

        return sb.toString();
    }

    private String renderMessageRow(int index, Message msg, int width) {
        StringBuilder sb = new StringBuilder();
        String indexStr = OpenClaudeTheme.dimText("[" + index + "] ");

        String rolePrefix;
        String content;

        switch (msg.getRole()) {
            case USER -> {
                rolePrefix = OpenClaudeTheme.BORDER_V + " " + OpenClaudeTheme.userText("User") + "    " + OpenClaudeTheme.BORDER_V + " ";
                content = truncate(msg.getContent(), width - indexStr.length() - rolePrefix.length() - 2);
            }
            case ASSISTANT -> {
                rolePrefix = OpenClaudeTheme.BORDER_V + " " + OpenClaudeTheme.assistantText("Claude") + " " + OpenClaudeTheme.BORDER_V + " ";
                content = truncate(msg.getContent(), width - indexStr.length() - rolePrefix.length() - 2);
            }
            case SYSTEM -> {
                rolePrefix = OpenClaudeTheme.BORDER_V + " " + OpenClaudeTheme.mutedText("System") + " " + OpenClaudeTheme.BORDER_V + " ";
                content = truncate(msg.getContent(), width - indexStr.length() - rolePrefix.length() - 2);
            }
            default -> {
                rolePrefix = OpenClaudeTheme.BORDER_V + " ???    " + OpenClaudeTheme.BORDER_V + " ";
                content = truncate(msg.getContent(), width - indexStr.length() - rolePrefix.length() - 2);
            }
        }

        sb.append(indexStr).append(rolePrefix).append(content);

        // Pad to width
        int totalLen = stripAnsi(indexStr).length() + rolePrefix.length() + content.length();
        if (totalLen < width - 1) {
            sb.append(" ".repeat(width - 1 - totalLen));
        }
        sb.append(OpenClaudeTheme.BORDER_V);

        return sb.toString();
    }

    private String renderInput(int width) {
        StringBuilder sb = new StringBuilder();
        sb.append(OpenClaudeTheme.BORDER_V).append(" ");
        sb.append(OpenClaudeTheme.boldText("▶ ")); // Prompt
        sb.append(currentInput);
        sb.append(OpenClaudeTheme.dimText("█")); // Block cursor

        // Pad to width
        int inputLen = currentInput.length() + 3;
        if (inputLen < width - 2) {
            sb.append(" ".repeat(width - 2 - inputLen));
        }
        sb.append(" ").append(OpenClaudeTheme.BORDER_V);
        return sb.toString();
    }

    private String renderStatusBar(int width) {
        StringBuilder sb = new StringBuilder();

        // Status line
        sb.append(OpenClaudeTheme.BORDER_BL);
        sb.append("─".repeat(width - 2));
        sb.append(OpenClaudeTheme.BORDER_BR);
        sb.append("\r");
        sb.append(OpenClaudeTheme.BORDER_V).append(" ");

        sb.append(OpenClaudeTheme.dimText("Tokens: "));
        sb.append(OpenClaudeTheme.cyan(inputTokens + "/" + outputTokens));
        if (latencyMs > 0) {
            sb.append(OpenClaudeTheme.dimText(" | "));
            sb.append(OpenClaudeTheme.successText(latencyMs + "ms"));
        }
        if (!vimMode.isEmpty()) {
            sb.append(OpenClaudeTheme.dimText(" | "));
            sb.append(OpenClaudeTheme.warningText(vimMode));
        }
        sb.append(OpenClaudeTheme.dimText(" | "));
        String statusDisplay = status.equals("Ready")
            ? OpenClaudeTheme.successText(status)
            : OpenClaudeTheme.warningText(status);

        String statusStr = "Tokens: " + inputTokens + "/" + outputTokens +
                          (latencyMs > 0 ? " | " + latencyMs + "ms" : "") +
                          (!vimMode.isEmpty() ? " | " + vimMode : "") +
                          " | " + status;

        if (statusStr.length() < width - 3) {
            sb.append(" ".repeat(width - 3 - statusStr.length()));
        }
        sb.append(statusDisplay);
        sb.append(" ").append(OpenClaudeTheme.BORDER_V);

        return sb.toString();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, Math.max(0, maxLen - 3)) + "...";
    }

    private String stripAnsi(String text) {
        return text.replaceAll("\\u001B\\[[0-9;]*m", "");
    }

    // ===== TuiAdapter Implementation =====

    @Override
    public void addUserMessage(String content) {
        messages.add(new Message(MessageRole.USER, content));
    }

    @Override
    public void addAssistantMessage(String content) {
        messages.add(new Message(MessageRole.ASSISTANT, content));
    }

    @Override
    public void addSystemMessage(String content) {
        messages.add(new Message(MessageRole.SYSTEM, content));
    }

    @Override
    public Consumer<String> startStreamingMessage() {
        streamedContent.setLength(0);
        messages.add(new Message(MessageRole.ASSISTANT, "", true));
        return token -> {
            streamedContent.append(token);
            if (!messages.isEmpty()) {
                int lastIdx = messages.size() - 1;
                messages.set(lastIdx, new Message(MessageRole.ASSISTANT, streamedContent.toString(), true));
            }
        };
    }

    @Override
    public void completeStreamingMessage(String finalContent) {
        if (!messages.isEmpty()) {
            int lastIdx = messages.size() - 1;
            messages.set(lastIdx, new Message(MessageRole.ASSISTANT, finalContent, false));
        }
    }

    @Override
    public void clearMessages() {
        messages.clear();
        scrollOffset = 0;
    }

    @Override
    public String getInput() {
        return currentInput.toString();
    }

    @Override
    public void setInput(String text) {
        currentInput.setLength(0);
        currentInput.append(text);
    }

    @Override
    public void setPrompt(String prompt) {
        // Prompt is rendered as "▶ " - customize if needed
    }

    @Override
    public void setVimMode(String mode) {
        this.vimMode = mode;
    }

    @Override
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public void setModel(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public String getModel() {
        return modelName;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void setTokens(int inputTokens, int outputTokens) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    @Override
    public void setLatency(long ms) {
        this.latencyMs = ms;
    }

    @Override
    public void onInput(Consumer<String> callback) {
        this.inputCallback = callback;
    }

    @Override
    public void onInputChange(Consumer<String> callback) {
        this.inputChangeCallback = callback;
    }

    @Override
    public void onKeyEvent(KeyEventHandler handler) {
        this.keyHandler = handler;
    }
}
