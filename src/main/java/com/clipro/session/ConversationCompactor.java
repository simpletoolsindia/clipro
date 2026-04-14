package com.clipro.session;

import com.clipro.llm.models.Message;
import com.clipro.ui.components.MessageRole;
import java.util.*;

public class ConversationCompactor {
    private final int maxMessages;
    private final int preserveSystemMessages;
    private final int summarizeThreshold;

    public ConversationCompactor() { this(1000, 10, 50); }

    public ConversationCompactor(int maxMessages, int preserveSystemMessages, int summarizeThreshold) {
        this.maxMessages = maxMessages;
        this.preserveSystemMessages = preserveSystemMessages;
        this.summarizeThreshold = summarizeThreshold;
    }

    public boolean needsCompaction(List<Message> messages) { return messages.size() > maxMessages; }

    public CompactionResult compact(List<Message> messages) {
        if (!needsCompaction(messages)) return new CompactionResult(messages, 0, false);

        List<Message> preserved = new ArrayList<>();
        List<Message> toCompact = new ArrayList<>();
        int systemCount = 0;

        for (Message m : messages) {
            if (m.getRole() == MessageRole.SYSTEM && systemCount < preserveSystemMessages) {
                preserved.add(m); systemCount++;
            }
        }

        int preservedCount = preserved.size();
        for (int i = preservedCount; i < messages.size(); i++) {
            int fromEnd = messages.size() - i;
            if (fromEnd > summarizeThreshold) toCompact.add(messages.get(i));
            else preserved.add(messages.get(i));
        }

        String summary = createSummary(toCompact);
        preserved.add(0, new Message("compacted-" + System.currentTimeMillis(), MessageRole.SYSTEM, "[Previous: " + summary + "]", false));
        return new CompactionResult(preserved, toCompact.size(), true);
    }

    private String createSummary(List<Message> old) {
        if (old.isEmpty()) return "";
        int u=0,a=0,t=0;
        for (Message m : old) { switch (m.getRole()) { case USER -> u++; case ASSISTANT -> a++; case TOOL -> t++; default -> {} } }
        return u + " user msgs, " + a + " assistant, " + t + " tools";
    }

    public record CompactionResult(List<Message> compactedMessages, int messagesRemoved, boolean wasCompacted) {}
}
