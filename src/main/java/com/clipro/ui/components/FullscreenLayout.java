package com.clipro.ui.components;

import com.clipro.ui.Terminal;

/**
 * Fullscreen layout combining header, messages, input, and status.
 * Pixel-perfect OpenClaude style.
 * Reference: openclaude/src/components/FullscreenLayout.tsx
 */
public class FullscreenLayout {
    private final HeaderBar header;
    private final MessageList messages;
    private final StatusBar status;
    private final InputField input;
    private StreamingMessage currentStreamingMessage;
    private int lastMessageIndex = -1;

    public FullscreenLayout() {
        this.header = new HeaderBar();
        this.messages = new MessageList();
        this.status = new StatusBar();
        this.input = new InputField();
    }

    public FullscreenLayout(String modelName) {
        this.header = new HeaderBar(modelName);
        this.messages = new MessageList();
        this.status = new StatusBar();
        this.input = new InputField();
    }

    /**
     * Start a streaming assistant message that updates in real-time.
     * @param onUpdate Callback for each token update
     * @return The StreamingMessage for updating
     */
    public StreamingMessage startStreamingMessage(java.util.function.Consumer<String> onUpdate) {
        currentStreamingMessage = new StreamingMessage(MessageRole.ASSISTANT, onUpdate);
        messages.add(currentStreamingMessage);
        lastMessageIndex = messages.size() - 1;
        return currentStreamingMessage;
    }

    /**
     * Update the last streaming message content.
     */
    public void updateStreamingMessage(String content) {
        if (currentStreamingMessage != null) {
            currentStreamingMessage.clear();
            currentStreamingMessage.append(content);
        }
    }

    /**
     * Complete the current streaming message.
     */
    public void completeStreamingMessage(String finalContent) {
        if (currentStreamingMessage != null) {
            currentStreamingMessage.complete(finalContent);
            currentStreamingMessage = null;
        }
    }

    /**
     * Check if currently streaming.
     */
    public boolean isStreaming() {
        return currentStreamingMessage != null && !currentStreamingMessage.isComplete();
    }

    public HeaderBar getHeader() {
        return header;
    }

    public MessageList getMessages() {
        return messages;
    }

    public StatusBar getStatus() {
        return status;
    }

    public InputField getInput() {
        return input;
    }

    public void setModel(String model) {
        header.setModel(model);
    }

    public void setConnected(boolean connected) {
        header.setConnected(connected);
    }

    public void setStatusText(String statusText) {
        header.setStatus(statusText);
    }

    public void init() {
        Terminal.clear();
        Terminal.hideCursor();
        Terminal.enterAltScreen();
    }

    public void shutdown() {
        Terminal.showCursor();
        Terminal.exitAltScreen();
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        int width = Terminal.getColumns();

        // Header with box border
        sb.append(header.render());
        sb.append("\n");

        // Divider
        sb.append(HeaderBar.divider());
        sb.append("\n");

        // Messages
        if (!messages.isEmpty()) {
            sb.append(messages.render());
        }

        // Fill remaining space with dim lines
        int rows = Terminal.getRows();
        int contentRows = 4 + messages.size(); // header, divider, input, status, messages
        int spacerRows = Math.max(0, rows - contentRows - 2);

        for (int i = 0; i < spacerRows; i++) {
            sb.append(Terminal.BORDER_V);
            sb.append(Terminal.repeat(" ", width - 2));
            sb.append(Terminal.BORDER_V);
            sb.append("\n");
        }

        // Input with border
        sb.append(Terminal.BORDER_V).append(" ");
        sb.append(input.render());
        sb.append(Terminal.padRight("", width - input.getLength() - 3));
        sb.append(Terminal.BORDER_V).append("\n");

        // Status bar
        sb.append(status.render());

        return sb.toString();
    }

    public String renderMinimal() {
        // Minimal render for input updates only
        StringBuilder sb = new StringBuilder();
        int width = Terminal.getColumns();

        // Move cursor to input line and update
        sb.append("\r");
        sb.append(Terminal.BORDER_V).append(" ");
        sb.append(input.render());
        sb.append(Terminal.padRight("", width - input.getLength() - 3));
        sb.append(Terminal.BORDER_V);

        return sb.toString();
    }

    public String renderWithInput(String inputText) {
        input.clear();
        input.insert(inputText);
        return renderMinimal();
    }

    public void addUserMessage(String content) {
        messages.addUser(content);
    }

    public void addAssistantMessage(String content) {
        messages.addAssistant(content);
    }

    public void addSystemMessage(String content) {
        messages.addSystem(content);
    }

    public void clearMessages() {
        messages.clear();
    }

    public void clearAll() {
        messages.clear();
        input.clear();
    }
}
