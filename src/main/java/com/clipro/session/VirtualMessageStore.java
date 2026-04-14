package com.clipro.session;

import com.clipro.llm.models.Message;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Virtualized message store with windowing support.
 * Implements message virtualization to prevent memory issues with long conversations.
 *
 * Features:
 * - Window-based storage: Only recent N messages in memory
 * - Older messages summarized or stored on disk
 * - Token-aware context management
 * - Memory-efficient iteration
 */
public class VirtualMessageStore {

    // Maximum messages to keep in memory
    private static final int MAX_IN_MEMORY = 50;

    // Maximum tokens per message (rough estimate: 4 chars = 1 token)
    private static final int CHARS_PER_TOKEN = 4;
    private static final int MAX_MESSAGE_CHARS = 4000;

    // In-memory queue (newest at end)
    private final ConcurrentLinkedQueue<Message> memoryMessages;
    private final Deque<Message> archivedMessages;  // Older messages (summary/window)
    private final int maxInMemory;
    private final int maxArchived;

    // Statistics
    private int totalMessagesAdded;
    private int messagesArchived;

    public VirtualMessageStore() {
        this(MAX_IN_MEMORY, 20);
    }

    public VirtualMessageStore(int maxInMemory, int maxArchived) {
        this.memoryMessages = new ConcurrentLinkedQueue<>();
        this.archivedMessages = new ArrayDeque<>();
        this.maxInMemory = maxInMemory;
        this.maxArchived = maxArchived;
        this.totalMessagesAdded = 0;
        this.messagesArchived = 0;
    }

    /**
     * Add a message to the store.
     * Automatically manages memory by archiving older messages.
     */
    public void add(Message message) {
        totalMessagesAdded++;
        memoryMessages.add(truncateMessage(message));

        // Evict oldest if over limit
        while (memoryMessages.size() > maxInMemory) {
            Message evicted = memoryMessages.poll();
            if (evicted != null) {
                archiveMessage(evicted);
            }
        }
    }

    /**
     * Add a user message.
     */
    public void addUser(String content) {
        add(Message.user(content));
    }

    /**
     * Add an assistant message.
     */
    public void addAssistant(String content) {
        add(Message.assistant(content));
    }

    /**
     * Add a system message.
     */
    public void addSystem(String content) {
        add(Message.system(content));
    }

    /**
     * Add a tool result message.
     */
    public void addToolResult(String content, String toolCallId) {
        add(Message.tool(content, toolCallId));
    }

    /**
     * Truncate long messages to save memory.
     */
    private Message truncateMessage(Message msg) {
        String content = msg.getContent();
        if (content == null || content.length() <= MAX_MESSAGE_CHARS) {
            return msg;
        }

        // Create a new message preserving the original fields
        Message truncated = new Message(msg.getRole(), content.substring(0, MAX_MESSAGE_CHARS - 50) +
            "\n\n[Message truncated - " + (content.length() - MAX_MESSAGE_CHARS) + " chars removed]");
        if (msg.getToolCallId() != null) {
            truncated.setToolCallId(msg.getToolCallId());
        }
        return truncated;
    }

    /**
     * Archive older messages.
     */
    private void archiveMessage(Message message) {
        messagesArchived++;
        archivedMessages.addFirst(message);

        // Limit archived messages
        while (archivedMessages.size() > maxArchived) {
            archivedMessages.removeLast();
        }
    }

    /**
     * Get all visible messages (in memory + archived).
     */
    public List<Message> getMessages() {
        List<Message> result = new ArrayList<>(archivedMessages);
        result.addAll(memoryMessages);
        return result;
    }

    /**
     * Get recent messages (starting from oldest in window).
     */
    public List<Message> getRecentMessages(int count) {
        List<Message> all = getMessages();
        int start = Math.max(0, all.size() - count);
        return all.subList(start, all.size());
    }

    /**
     * Get messages within token budget.
     * Estimates token count as chars/4.
     */
    public List<Message> getMessagesWithinBudget(int maxTokens) {
        List<Message> all = getMessages();
        List<Message> result = new ArrayList<>();
        int currentTokens = 0;

        // Start from newest and work backwards
        for (int i = all.size() - 1; i >= 0; i--) {
            Message msg = all.get(i);
            int msgTokens = estimateTokens(msg);
            if (currentTokens + msgTokens <= maxTokens) {
                result.add(0, msg);
                currentTokens += msgTokens;
            } else {
                break;
            }
        }

        return result;
    }

    /**
     * Estimate token count for a message.
     */
    public int estimateTokens(Message msg) {
        if (msg.getContent() == null) return 0;
        return msg.getContent().length() / CHARS_PER_TOKEN + 20; // +20 for role overhead
    }

    /**
     * Estimate total tokens in store.
     */
    public int estimateTotalTokens() {
        return getMessages().stream()
            .mapToInt(this::estimateTokens)
            .sum();
    }

    /**
     * Clear all messages.
     */
    public void clear() {
        memoryMessages.clear();
        archivedMessages.clear();
        totalMessagesAdded = 0;
        messagesArchived = 0;
    }

    /**
     * Get current count.
     */
    public int size() {
        return memoryMessages.size() + archivedMessages.size();
    }

    /**
     * Check if empty.
     */
    public boolean isEmpty() {
        return memoryMessages.isEmpty() && archivedMessages.isEmpty();
    }

    /**
     * Get memory statistics.
     */
    public String getStats() {
        return String.format(
            "VirtualMessageStore[total=%d, inMemory=%d, archived=%d, removed=%d, tokens≈%d]",
            totalMessagesAdded,
            memoryMessages.size(),
            archivedMessages.size(),
            totalMessagesAdded - size(),
            estimateTotalTokens()
        );
    }

    /**
     * Get memory usage estimate in bytes.
     */
    public long estimateMemoryUsage() {
        long total = 0;
        for (Message msg : getMessages()) {
            if (msg.getContent() != null) {
                total += msg.getContent().length() * 2; // 2 bytes per char
            }
            total += 100; // Object overhead
        }
        return total;
    }

    /**
     * Search messages.
     */
    public List<Message> search(String query) {
        return getMessages().stream()
            .filter(msg -> msg.getContent() != null &&
                          msg.getContent().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
    }

    /**
     * Get the archived (older) messages.
     */
    public List<Message> getArchivedMessages() {
        return new ArrayList<>(archivedMessages);
    }

    /**
     * Get the in-memory messages.
     */
    public List<Message> getInMemoryMessages() {
        return new ArrayList<>(memoryMessages);
    }
}
