package com.clipro.ui.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrollable message list with virtualization support.
 * Reference: openclaude/src/components/VirtualMessageList.tsx
 */
public class MessageList {
    private final List<Message> messages = new ArrayList<>();
    private int scrollOffset = 0;
    private final int maxVisible;

    public MessageList() {
        this(100); // Default max visible lines
    }

    public MessageList(int maxVisible) {
        this.maxVisible = maxVisible;
    }

    public void add(Message message) {
        messages.add(message);
    }

    public void addUser(String content) {
        messages.add(new Message(MessageRole.USER, content));
    }

    public void addAssistant(String content) {
        messages.add(new Message(MessageRole.ASSISTANT, content));
    }

    public void addSystem(String content) {
        messages.add(new Message(MessageRole.SYSTEM, content));
    }

    public Message get(int index) {
        if (index >= 0 && index < messages.size()) {
            return messages.get(index);
        }
        return null;
    }

    public int size() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public void clear() {
        messages.clear();
        scrollOffset = 0;
    }

    public void scrollUp(int lines) {
        scrollOffset = Math.min(scrollOffset + lines, messages.size() - 1);
    }

    public void scrollDown(int lines) {
        scrollOffset = Math.max(scrollOffset - lines, 0);
    }

    public void scrollToTop() {
        scrollOffset = 0;
    }

    public void scrollToBottom() {
        scrollOffset = Math.max(0, messages.size() - 1);
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public List<Message> getVisibleMessages() {
        int start = Math.max(0, messages.size() - maxVisible - scrollOffset);
        int end = messages.size() - scrollOffset;
        if (end > start) {
            return new ArrayList<>(messages.subList(start, Math.min(end, messages.size())));
        }
        return new ArrayList<>();
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        List<Message> visible = getVisibleMessages();

        for (int i = 0; i < visible.size(); i++) {
            Message msg = visible.get(i);
            int index = messages.indexOf(msg);
            sb.append(MessageRow.renderWithIndex(index + 1, msg));
            if (i < visible.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public String renderAll() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < messages.size(); i++) {
            sb.append(MessageRow.renderWithIndex(i + 1, messages.get(i)));
            if (i < messages.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
