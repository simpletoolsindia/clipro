package com.clipro.session;

import com.clipro.llm.models.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HistoryManager.
 */
class HistoryManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldAddMessages() {
        HistoryManager history = new HistoryManager(tempDir.toString());
        history.addUser("Hello");
        history.addAssistant("Hi there!");
        
        assertEquals(2, history.size());
    }

    @Test
    void shouldClearHistory() {
        HistoryManager history = new HistoryManager(tempDir.toString());
        history.addUser("Hello");
        history.addAssistant("Hi");
        history.clear();
        
        assertEquals(0, history.size());
    }

    @Test
    void shouldGetMessages() {
        HistoryManager history = new HistoryManager(tempDir.toString());
        history.addUser("Hello");
        history.addAssistant("Hi");
        
        List<Message> messages = history.getMessages();
        assertEquals(2, messages.size());
        assertEquals("user", messages.get(0).getRole());
        assertEquals("assistant", messages.get(1).getRole());
    }

    @Test
    void shouldGetRecentMessages() {
        HistoryManager history = new HistoryManager(tempDir.toString());
        for (int i = 0; i < 10; i++) {
            history.addUser("Message " + i);
        }
        
        List<Message> recent = history.getRecentMessages(3);
        assertEquals(3, recent.size());
        assertTrue(recent.get(0).getContent().contains("7"));
    }

    @Test
    void shouldSearchMessages() {
        HistoryManager history = new HistoryManager(tempDir.toString());
        history.addUser("Hello world");
        history.addUser("Goodbye world");
        history.addUser("Hello again");
        
        List<Message> results = history.search("Hello");
        assertEquals(2, results.size());
    }

    @Test
    void shouldListSessions() {
        HistoryManager history = new HistoryManager(tempDir.toString());
        history.save();
        
        List<String> sessions = history.listSessions();
        assertTrue(sessions.size() >= 1);
    }

    @Test
    void shouldGetSessionId() {
        HistoryManager history = new HistoryManager(tempDir.toString());
        assertNotNull(history.getCurrentSessionId());
    }
}
