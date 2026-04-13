package com.clipro.ui.components;

import com.clipro.ui.Terminal;

/**
 * Fullscreen layout combining header, messages, and status.
 * Reference: openclaude/src/components/FullscreenLayout.tsx
 */
public class FullscreenLayout {
    private final HeaderBar header;
    private final MessageList messages;
    private final StatusBar status;
    private final InputField input;

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

    public String render() {
        StringBuilder sb = new StringBuilder();

        // Clear screen and enter alt screen (these print directly)
        Terminal.clear();
        Terminal.enterAltScreen();

        // Header
        sb.append(header.render());
        sb.append("\n");

        // Divider
        sb.append(HeaderBar.divider());
        sb.append("\n");

        // Messages
        if (!messages.isEmpty()) {
            sb.append(messages.render());
            sb.append("\n");
        }

        // Spacer
        int rows = Terminal.getRows();
        int usedRows = 4; // header + divider + input + status
        for (int i = 0; i < Math.max(0, rows - usedRows - messages.size()); i++) {
            sb.append("\n");
        }

        // Input
        sb.append(input.renderWithCursor());

        return sb.toString();
    }

    public String renderMinimal() {
        // Minimal render for updates (just input area)
        return input.renderWithCursor();
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
