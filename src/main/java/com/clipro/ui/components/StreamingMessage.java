package com.clipro.ui.components;

import java.util.function.Consumer;

/**
 * Streaming message that updates in real-time.
 * Reference: openclaude streaming components
 */
public class StreamingMessage extends Message {
    private final StringBuilder buffer = new StringBuilder();
    private boolean complete = false;
    private Consumer<String> onUpdate;

    public StreamingMessage(MessageRole role) {
        super(role, "", true);
    }

    public StreamingMessage(MessageRole role, Consumer<String> onUpdate) {
        super(role, "", true);
        this.onUpdate = onUpdate;
    }

    public void append(char c) {
        buffer.append(c);
        notifyUpdate();
    }

    public void append(String text) {
        buffer.append(text);
        notifyUpdate();
    }

    public void appendChunk(String chunk) {
        buffer.append(chunk);
        notifyUpdate();
    }

    private void notifyUpdate() {
        if (onUpdate != null) {
            onUpdate.accept(buffer.toString());
        }
    }

    public void complete() {
        this.complete = true;
        notifyComplete();
    }

    public void complete(String finalContent) {
        buffer.setLength(0);
        buffer.append(finalContent);
        this.complete = true;
        notifyComplete();
    }

    private void notifyComplete() {
        if (onUpdate != null) {
            onUpdate.accept(buffer.toString());
        }
    }

    @Override
    public String getContent() {
        return buffer.toString();
    }

    public boolean isComplete() {
        return complete;
    }

    public String getContentWithCursor() {
        return buffer.toString() + Terminal.dim(" ▌");
    }

    public int getCharCount() {
        return buffer.length();
    }

    public String render() {
        return MessageBox.renderAssistant(getContentWithCursor(), !complete);
    }

    public String renderMarkdown() {
        return MarkdownRenderer.render(getContent());
    }

    public void setOnUpdate(Consumer<String> callback) {
        this.onUpdate = callback;
    }

    public void clear() {
        buffer.setLength(0);
        complete = false;
        notifyUpdate();
    }
}
