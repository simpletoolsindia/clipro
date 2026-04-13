package com.clipro.session;

import com.clipro.ui.components.Message;
import com.clipro.ui.components.MessageRole;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    @Test
    void shouldCreateHistoryManager() {
        HistoryManager manager = new HistoryManager();
        assertNotNull(manager);
        assertNotNull(manager.getCurrentSession());
    }

    @Test
    void shouldAddMessages() {
        HistoryManager manager = new HistoryManager();
        manager.addUserMessage("Hello");
        manager.addAssistantMessage("Hi there");

        assertEquals(2, manager.getCurrentMessages().size());
    }

    @Test
    void shouldAddMessagesToCurrentSession() {
        HistoryManager manager = new HistoryManager();
        manager.addUserMessage("Test");

        Message msg = manager.getCurrentMessages().get(0);
        assertEquals(MessageRole.USER, msg.getRole());
        assertEquals("Test", msg.getContent());
    }

    @Test
    void shouldClearCurrentSession() {
        HistoryManager manager = new HistoryManager();
        manager.addUserMessage("Test");
        manager.addAssistantMessage("Response");

        manager.clearCurrentSession();

        assertEquals(0, manager.getCurrentMessages().size());
    }

    @Test
    void shouldStartNewSession() {
        HistoryManager manager = new HistoryManager();
        var oldSession = manager.getCurrentSession();
        manager.addUserMessage("Old message");

        manager.startNewSession();
        var newSession = manager.getCurrentSession();

        assertNotSame(oldSession, newSession);
        assertEquals(0, newSession.size());
    }

    @Test
    void shouldTrimOldSessions() {
        HistoryManager manager = new HistoryManager();
        manager.setMaxSessions(2);

        manager.startNewSession(); // session 2
        manager.addUserMessage("msg2");
        manager.startNewSession(); // session 3
        manager.addUserMessage("msg3");
        manager.startNewSession(); // session 4

        // Only 2 sessions should remain
        assertTrue(manager.getSessions().size() <= 2);
    }

    @Test
    void shouldHaveUniqueSessionIds() {
        HistoryManager manager = new HistoryManager();
        var session1 = manager.getCurrentSession();
        manager.startNewSession();
        var session2 = manager.getCurrentSession();

        assertNotEquals(session1.getId(), session2.getId());
    }

    @Test
    void shouldCountTotalMessages() {
        HistoryManager manager = new HistoryManager();
        manager.addUserMessage("1");
        manager.addAssistantMessage("2");
        manager.startNewSession();
        manager.addUserMessage("3");

        assertEquals(3, manager.getTotalMessageCount());
    }

    @Test
    void shouldGetSessionStartTime() {
        HistoryManager manager = new HistoryManager();
        var session = manager.getCurrentSession();

        assertNotNull(session.getStartTime());
    }
}
