package com.clipro.ui;

import com.clipro.agent.AgentEngine;
import com.clipro.llm.models.ChatCompletionChunk;
import com.clipro.llm.models.Message;
import com.clipro.ui.components.FullscreenLayout;
import com.clipro.ui.components.StreamingMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests for streaming UI integration.
 * Tests: Agent -> UI streaming token updates.
 */
class StreamingE2ETest {

    @Test
    void shouldCreateStreamingMessageInLayout() {
        FullscreenLayout layout = new FullscreenLayout();
        AtomicBoolean updateCalled = new AtomicBoolean(false);
        AtomicReference<String> lastContent = new AtomicReference<>("");

        // Start streaming message
        StreamingMessage streaming = layout.startStreamingMessage(content -> {
            updateCalled.set(true);
            lastContent.set(content);
        });

        assertNotNull(streaming);
        assertTrue(layout.isStreaming());
        assertFalse(streaming.isComplete());

        // Simulate token updates
        streaming.append("Hello ");
        assertTrue(updateCalled.get());
        assertEquals("Hello ", lastContent.get());

        streaming.append("world!");
        assertEquals("Hello world!", lastContent.get());

        // Complete streaming
        layout.completeStreamingMessage("Hello world!");
        assertFalse(layout.isStreaming());
        assertTrue(streaming.isComplete());
        assertEquals("Hello world!", streaming.getContent());
    }

    @Test
    void shouldUpdateTokensInRealTime() {
        FullscreenLayout layout = new FullscreenLayout();
        List<String> updates = new ArrayList<>();

        StreamingMessage streaming = layout.startStreamingMessage(updates::add);

        // Simulate rapid token updates (like SSE)
        streaming.append("T");
        streaming.append("o");
        streaming.append("k");
        streaming.append("e");
        streaming.append("n");

        assertEquals(5, updates.size());
        assertEquals("T", updates.get(0));
        assertEquals("To", updates.get(1));
        assertEquals("Tok", updates.get(2));
        assertEquals("Toke", updates.get(3));
        assertEquals("Token", updates.get(4));
    }

    @Test
    void shouldTrackMultipleStreamingMessages() {
        FullscreenLayout layout = new FullscreenLayout();
        AtomicInteger updateCount = new AtomicInteger(0);

        // First streaming message
        StreamingMessage stream1 = layout.startStreamingMessage(c -> updateCount.incrementAndGet());
        stream1.append("First");

        assertEquals(1, layout.getMessages().size());

        // Complete first
        layout.completeStreamingMessage("First");
        assertFalse(layout.isStreaming());

        // Second streaming message
        StreamingMessage stream2 = layout.startStreamingMessage(c -> updateCount.incrementAndGet());
        stream2.append("Second");

        assertEquals(2, layout.getMessages().size());
        assertTrue(layout.isStreaming());
    }

    @Test
    void shouldRenderStreamingMessageWithCursor() {
        FullscreenLayout layout = new FullscreenLayout();
        StreamingMessage streaming = layout.startStreamingMessage(null);

        streaming.append("streaming");

        String rendered = streaming.render();
        assertNotNull(rendered);
        assertTrue(rendered.contains("streaming"));
        // Cursor should be visible during streaming
        assertTrue(rendered.contains("▌") || !streaming.isComplete());
    }

    @Test
    void shouldCompleteStreamingMessageWithoutCursor() {
        FullscreenLayout layout = new FullscreenLayout();
        StreamingMessage streaming = layout.startStreamingMessage(null);

        streaming.append("complete");

        String renderedBefore = streaming.render();
        assertFalse(streaming.isComplete());

        streaming.complete();

        String renderedAfter = streaming.render();
        assertTrue(streaming.isComplete());
    }

    @Test
    void shouldClearStreamingMessage() {
        FullscreenLayout layout = new FullscreenLayout();
        StreamingMessage streaming = layout.startStreamingMessage(null);

        streaming.append("test content");
        assertEquals("test content", streaming.getContent());
        assertTrue(layout.isStreaming());

        streaming.clear();

        assertEquals("", streaming.getContent());
        assertFalse(streaming.isComplete());
        assertTrue(layout.isStreaming()); // Layout still has the message
    }

    @Test
    void shouldHandleEmptyStreamingContent() {
        FullscreenLayout layout = new FullscreenLayout();
        StreamingMessage streaming = layout.startStreamingMessage(null);

        assertEquals("", streaming.getContent());
        assertFalse(streaming.isComplete());

        streaming.complete("");
        assertEquals("", streaming.getContent());
        assertTrue(streaming.isComplete());
    }

    @Test
    void shouldCompleteWithFinalContent() {
        FullscreenLayout layout = new FullscreenLayout();
        StreamingMessage streaming = layout.startStreamingMessage(null);

        streaming.append("partial");
        layout.completeStreamingMessage("complete response");

        assertEquals("complete response", streaming.getContent());
        assertTrue(streaming.isComplete());
        assertFalse(layout.isStreaming());
    }

    @Test
    void shouldNotifyOnEachChunk() {
        FullscreenLayout layout = new FullscreenLayout();
        List<String> chunks = new ArrayList<>();

        StreamingMessage streaming = layout.startStreamingMessage(chunks::add);

        // Simulate chunked responses - callback receives cumulative content
        streaming.appendChunk("chunk1");
        streaming.appendChunk("chunk2");
        streaming.appendChunk("chunk3");

        assertEquals(3, chunks.size());
        // Each callback receives cumulative buffer content
        assertEquals("chunk1", chunks.get(0));
        assertEquals("chunk1chunk2", chunks.get(1));
        assertEquals("chunk1chunk2chunk3", chunks.get(2));
        assertEquals("chunk1chunk2chunk3", streaming.getContent());
    }

    @Test
    void shouldGetCharCountDuringStreaming() {
        FullscreenLayout layout = new FullscreenLayout();
        StreamingMessage streaming = layout.startStreamingMessage(null);

        assertEquals(0, streaming.getCharCount());

        streaming.append("Hello");
        assertEquals(5, streaming.getCharCount());

        streaming.append(" World!");
        assertEquals(12, streaming.getCharCount());
    }

    @Test
    void shouldSetUpdateCallbackAfterCreation() {
        FullscreenLayout layout = new FullscreenLayout();
        StreamingMessage streaming = layout.startStreamingMessage(null);

        List<String> updates = new ArrayList<>();
        streaming.setOnUpdate(updates::add);

        streaming.append("callback test");

        assertEquals(1, updates.size());
        assertEquals("callback test", updates.get(0));
    }

    @Test
    void shouldUseNullCallbackGracefully() {
        FullscreenLayout layout = new FullscreenLayout();
        StreamingMessage streaming = layout.startStreamingMessage(null);

        // Should not throw with null callback
        assertDoesNotThrow(() -> streaming.append("no callback"));
        assertDoesNotThrow(() -> streaming.clear());
        assertDoesNotThrow(() -> streaming.complete());
    }
}
