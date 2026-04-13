package com.clipro.ui.components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void shouldCreateMessageWithRoleAndContent() {
        Message msg = new Message(MessageRole.USER, "Hello");

        assertEquals(MessageRole.USER, msg.getRole());
        assertEquals("Hello", msg.getContent());
        assertNotNull(msg.getId());
        assertNotNull(msg.getTimestamp());
        assertFalse(msg.isStreaming());
    }

    @Test
    void shouldCreateStreamingMessage() {
        Message msg = new Message(MessageRole.ASSISTANT, "Streaming...", true);

        assertTrue(msg.isStreaming());
        assertEquals(MessageRole.ASSISTANT, msg.getRole());
    }

    @Test
    void shouldIdentifyRole() {
        Message userMsg = new Message(MessageRole.USER, "");
        Message assistantMsg = new Message(MessageRole.ASSISTANT, "");
        Message systemMsg = new Message(MessageRole.SYSTEM, "");

        assertTrue(userMsg.isUser());
        assertFalse(userMsg.isAssistant());

        assertTrue(assistantMsg.isAssistant());
        assertFalse(assistantMsg.isUser());

        assertTrue(systemMsg.isSystem());
        assertFalse(systemMsg.isUser());
    }

    @Test
    void shouldHaveUniqueIds() {
        Message msg1 = new Message(MessageRole.USER, "one");
        Message msg2 = new Message(MessageRole.USER, "two");

        assertNotEquals(msg1.getId(), msg2.getId());
    }
}
