package com.clipro.ui.components;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {
    private final String id;
    private final MessageRole role;
    private final String content;
    private final LocalDateTime timestamp;
    private final boolean isStreaming;

    public Message(MessageRole role, String content) {
        this.id = UUID.randomUUID().toString();
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isStreaming = false;
    }

    public Message(MessageRole role, String content, boolean isStreaming) {
        this.id = UUID.randomUUID().toString();
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isStreaming = isStreaming;
    }

    public String getId() {
        return id;
    }

    public MessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public boolean isUser() {
        return role == MessageRole.USER;
    }

    public boolean isAssistant() {
        return role == MessageRole.ASSISTANT;
    }

    public boolean isSystem() {
        return role == MessageRole.SYSTEM;
    }
}
