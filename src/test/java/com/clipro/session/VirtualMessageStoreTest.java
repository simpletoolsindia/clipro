package com.clipro.session;

import com.clipro.llm.models.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for VirtualMessageStore.
 */
class VirtualMessageStoreTest {

    private VirtualMessageStore store;

    @BeforeEach
    void setUp() {
        store = new VirtualMessageStore(10, 5); // Small limits for testing
    }

    @Test
    void shouldCreateEmptyStore() {
        assertTrue(store.isEmpty());
        assertEquals(0, store.size());
    }

    @Test
    void shouldAddMessages() {
        store.addUser("Hello");
        store.addAssistant("Hi there!");
        store.addUser("How are you?");

        assertEquals(3, store.size());
        assertFalse(store.isEmpty());
    }

    @Test
    void shouldEnforceMemoryLimit() {
        // Add 20 messages to a store with limit of 10
        for (int i = 0; i < 20; i++) {
            store.addUser("Message " + i);
        }

        // Should keep maxInMemory + maxArchived = 10 + 5 = 15
        assertEquals(15, store.size());
        // In memory should be last 10
        assertEquals(10, store.getInMemoryMessages().size());
    }

    @Test
    void shouldArchiveOldMessages() {
        // Add 20 messages
        for (int i = 0; i < 20; i++) {
            store.addUser("Message " + i);
        }

        // Total: maxInMemory + maxArchived = 15
        assertEquals(15, store.size());

        // Archived should have 5 (maxArchived)
        List<Message> archived = store.getArchivedMessages();
        assertEquals(5, archived.size());

        // In memory should have 10
        assertEquals(10, store.getInMemoryMessages().size());
    }

    @Test
    void shouldReturnMessagesInOrder() {
        store.addUser("First");
        store.addUser("Second");
        store.addUser("Third");

        List<Message> messages = store.getMessages();
        assertEquals(3, messages.size());
        assertEquals("First", messages.get(0).getContent());
        assertEquals("Second", messages.get(1).getContent());
        assertEquals("Third", messages.get(2).getContent());
    }

    @Test
    void shouldGetRecentMessages() {
        for (int i = 0; i < 15; i++) {
            store.addUser("Message " + i);
        }

        List<Message> recent = store.getRecentMessages(5);
        assertEquals(5, recent.size());
        assertEquals("Message 10", recent.get(0).getContent());
        assertEquals("Message 14", recent.get(4).getContent());
    }

    @Test
    void shouldTruncateLongMessages() {
        // Create a 5000 char string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) sb.append("xxxxxxxxxx");
        String longContent = sb.toString();

        store.addUser(longContent);

        List<Message> messages = store.getMessages();
        assertEquals(1, messages.size());
        String content = messages.get(0).getContent();
        assertTrue(content.length() < 5000,
            "Message should be truncated from 5000, but was " + content.length());
        assertTrue(content.contains("truncated"),
            "Content should contain truncated. Last 100 chars: " +
            content.substring(Math.max(0, content.length() - 100)));
    }

    @Test
    void shouldEstimateTokens() {
        store.addUser("Hello world"); // 11 chars + overhead
        List<Message> messages = store.getMessages();
        assertTrue(store.estimateTokens(messages.get(0)) > 0);
    }

    @Test
    void shouldGetMessagesWithinBudget() {
        // Add messages with varying sizes
        store.addUser("Short");        // ~5 tokens
        store.addUser("Medium message"); // ~15 tokens
        store.addUser("A longer message here"); // ~25 tokens

        // Should get messages within ~30 token budget
        List<Message> within = store.getMessagesWithinBudget(30);
        assertTrue(within.size() >= 1);
        assertTrue(within.size() <= 3);
    }

    @Test
    void shouldClearStore() {
        store.addUser("Hello");
        store.addUser("World");
        store.clear();

        assertTrue(store.isEmpty());
        assertEquals(0, store.size());
    }

    @Test
    void shouldSearchMessages() {
        store.addUser("Hello world");
        store.addUser("Goodbye world");
        store.addUser("Hello there");

        List<Message> results = store.search("Hello");
        assertEquals(2, results.size());
    }

    @Test
    void shouldReportStats() {
        store.addUser("Test");
        String stats = store.getStats();
        assertTrue(stats.contains("VirtualMessageStore"));
        assertTrue(stats.contains("total=1"));
    }

    @Test
    void shouldEstimateMemoryUsage() {
        store.addUser("Test message");
        long bytes = store.estimateMemoryUsage();
        assertTrue(bytes > 0);
    }

    @Test
    void shouldEnforceArchiveLimit() {
        // Add 30 messages (beyond 10+5 limit)
        for (int i = 0; i < 30; i++) {
            store.addUser("Message " + i);
        }

        // Total should be maxInMemory + maxArchived = 10 + 5 = 15
        assertEquals(15, store.size());

        // Stats should show messages removed
        String stats = store.getStats();
        assertTrue(stats.contains("removed=15"));
    }
}
