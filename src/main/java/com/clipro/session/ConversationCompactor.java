package com.clipro.session;

import com.clipro.llm.models.Message;
import java.util.*;

/**
 * Conversation compactor with UI notification support (M-02).
 * Reference: openclaude/src/utils/messages.ts compaction events
 */
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
        if (!needsCompaction(messages)) return new CompactionResult(messages, 0, false, "");

        List<Message> preserved = new ArrayList<>();
        List<Message> toCompact = new ArrayList<>();
        int systemCount = 0;

        for (Message m : messages) {
            if ("system".equals(m.getRole()) && systemCount < preserveSystemMessages) {
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
        // M-02: Create a detailed summary message
        Message summaryMsg = new Message("system",
            "Context compacted — " + toCompact.size() + " messages removed to stay within token budget.\n" +
            "[Previous conversation: " + summary + "]");
        summaryMsg.setName("compacted-" + System.currentTimeMillis());
        preserved.add(0, summaryMsg);
        return new CompactionResult(preserved, toCompact.size(), true, summary);
    }

    private String createSummary(List<Message> old) {
        if (old.isEmpty()) return "";
        int u=0,a=0,t=0;
        for (Message m : old) {
            String r = m.getRole();
            if ("user".equals(r)) u++;
            else if ("assistant".equals(r)) a++;
            else if ("tool".equals(r)) t++;
        }
        return u + " user msgs, " + a + " assistant, " + t + " tools";
    }

    /**
     * Generate a collapsible summary for UI display (M-02).
     */
    public String generateCollapsibleSummary(List<Message> compacted) {
        int msgCount = compacted.size();
        return "┌─ [COMPACTED] " + msgCount + " messages hidden ──────────────┐\n" +
               "│ Click to expand and review compacted messages        │\n" +
               "└────────────────────────────────────────────────────┘";
    }

    public record CompactionResult(List<Message> compactedMessages, int messagesRemoved, boolean wasCompacted, String summary) {}
}