package com.clipro.ui.components;

import com.clipro.ui.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StreamingMessageTest {

    @BeforeEach
    void setUp() {
        Terminal.setColumns(80);
        Terminal.setRows(24);
    }

    @Test
    void shouldCreateStreamingMessage() {
        StreamingMessage msg = new StreamingMessage(MessageRole.ASSISTANT);
        assertTrue(msg.isStreaming());
        assertFalse(msg.isComplete());
        assertEquals("", msg.getContent());
    }

    @Test
    void shouldAppendCharacters() {
        StreamingMessage msg = new StreamingMessage(MessageRole.ASSISTANT);
        msg.append('H');
        msg.append('i');

        assertEquals("Hi", msg.getContent());
        assertEquals(2, msg.getCharCount());
    }

    @Test
    void shouldAppendString() {
        StreamingMessage msg = new StreamingMessage(MessageRole.ASSISTANT);
        msg.append("Hello");
        msg.append(" World");

        assertEquals("Hello World", msg.getContent());
    }

    @Test
    void shouldAppendChunks() {
        StreamingMessage msg = new StreamingMessage(MessageRole.ASSISTANT);
        msg.appendChunk("Hello ");
        msg.appendChunk("World");

        assertEquals("Hello World", msg.getContent());
    }

    @Test
    void shouldComplete() {
        StreamingMessage msg = new StreamingMessage(MessageRole.ASSISTANT);
        msg.append("Hello");

        msg.complete();

        assertTrue(msg.isComplete());
    }

    @Test
    void shouldCompleteWithFinalContent() {
        StreamingMessage msg = new StreamingMessage(MessageRole.ASSISTANT);
        msg.append("Hel");
        msg.complete("Hello");

        assertEquals("Hello", msg.getContent());
        assertTrue(msg.isComplete());
    }

    @Test
    void shouldRenderWithCursor() {
        StreamingMessage msg = new StreamingMessage(MessageRole.ASSISTANT);
        msg.append("Hi");

        String rendered = msg.getContentWithCursor();
        assertTrue(rendered.contains("Hi"));
        // Check for cursor indicator (ANSI dim or ▌ character)
        assertTrue(rendered.contains("▌") || rendered.contains("\033["));
    }

    @Test
    void shouldRenderMarkdown() {
        StreamingMessage msg = new StreamingMessage(MessageRole.ASSISTANT);
        msg.append("Hello **world**");

        String rendered = msg.renderMarkdown();
        assertTrue(rendered.contains("Hello"));
    }

    @Test
    void shouldClear() {
        StreamingMessage msg = new StreamingMessage(MessageRole.ASSISTANT);
        msg.append("Hello");
        msg.complete();

        msg.clear();

        assertEquals("", msg.getContent());
        assertFalse(msg.isComplete());
    }

    @Test
    void shouldCallOnUpdateCallback() {
        StringBuilder updates = new StringBuilder();
        StreamingMessage msg = new StreamingMessage(MessageRole.ASSISTANT, updates::append);

        msg.append("Hello");

        assertEquals("Hello", updates.toString());
    }

    @Test
    void shouldNotCallCallbackWhenNull() {
        StreamingMessage msg = new StreamingMessage(MessageRole.ASSISTANT);
        msg.setOnUpdate(null);

        // Should not throw
        msg.append("test");
        assertEquals("test", msg.getContent());
    }
}
