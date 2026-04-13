package com.clipro.session;

import com.clipro.ui.components.Message;
import com.clipro.ui.components.MessageRole;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages conversation history.
 */
public class HistoryManager {
    private final List<Session> sessions = new ArrayList<>();
    private Session currentSession;
    private int maxSessions = 10;

    public HistoryManager() {
        startNewSession();
    }

    public void startNewSession() {
        currentSession = new Session();
        sessions.add(0, currentSession);
        trimSessions();
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public List<Session> getSessions() {
        return new ArrayList<>(sessions);
    }

    public List<Message> getCurrentMessages() {
        return currentSession != null ? currentSession.getMessages() : new ArrayList<>();
    }

    public void addUserMessage(String content) {
        if (currentSession != null) {
            currentSession.addMessage(MessageRole.USER, content);
        }
    }

    public void addAssistantMessage(String content) {
        if (currentSession != null) {
            currentSession.addMessage(MessageRole.ASSISTANT, content);
        }
    }

    public void addSystemMessage(String content) {
        if (currentSession != null) {
            currentSession.addMessage(MessageRole.SYSTEM, content);
        }
    }

    public void clearCurrentSession() {
        if (currentSession != null) {
            currentSession.clear();
        }
    }

    private void trimSessions() {
        while (sessions.size() > maxSessions) {
            sessions.remove(sessions.size() - 1);
        }
    }

    public void setMaxSessions(int max) {
        this.maxSessions = max;
        trimSessions();
    }

    public int getTotalMessageCount() {
        return sessions.stream().mapToInt(s -> s.getMessages().size()).sum();
    }

    public static class Session {
        private final String id;
        private final LocalDateTime startTime;
        private LocalDateTime endTime;
        private final List<Message> messages = new ArrayList<>();

        public Session() {
            this.id = generateId();
            this.startTime = LocalDateTime.now();
        }

        public String getId() {
            return id;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public List<Message> getMessages() {
            return new ArrayList<>(messages);
        }

        public void addMessage(MessageRole role, String content) {
            messages.add(new Message(role, content));
        }

        public void clear() {
            messages.clear();
        }

        public int size() {
            return messages.size();
        }

        public void end() {
            this.endTime = LocalDateTime.now();
        }

        private static String generateId() {
            return "session-" + UUID.randomUUID().toString();
        }
    }
}
