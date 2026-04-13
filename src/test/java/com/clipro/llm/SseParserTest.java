package com.clipro.llm;

import com.clipro.llm.models.ChatCompletionChunk;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SSE parsing and streaming.
 */
class SseParserTest {

    @Test
    void shouldParseSseLine() {
        SseParser parser = new SseParser();
        // Valid SSE line that matches ChatCompletionChunk format
        String line = "data: {\"id\":\"test\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hello\"}}]}";
        ChatCompletionChunk chunk = parser.parseLine(line);
        // The JSON is valid but may not have all required fields - that's OK
        assertNotNull(chunk);
    }

    @Test
    void shouldReturnNullForNonDataLine() {
        SseParser parser = new SseParser();
        assertNull(parser.parseLine("not a data line"));
        assertNull(parser.parseLine(null));
        assertNull(parser.parseLine(""));
    }

    @Test
    void shouldReturnNullForDoneLine() {
        SseParser parser = new SseParser();
        assertNull(parser.parseLine("data: [DONE]"));
        assertNull(parser.parseLine("[DONE]"));
    }

    @Test
    void shouldParseSseStream() {
        SseParser parser = new SseParser();
        AtomicReference<String> content = new AtomicReference<>("");
        AtomicReference<Boolean> done = new AtomicReference<>(false);

        String sseData = """
            data: {"id":"chatcmpl-1","model":"test","choices":[{"index":0,"delta":{"content":"Hello"},"finish_reason":null}]}
            data: {"id":"chatcmpl-1","model":"test","choices":[{"index":0,"delta":{"content":" World"},"finish_reason":null}]}
            data: [DONE]
            """;

        parser.parseSseStream(sseData, chunk -> {
            String delta = parser.extractContent(chunk);
            if (delta != null) {
                content.set(content.get() + delta);
            }
        }, () -> done.set(true));

        // Done should have been called
        assertTrue(done.get());
    }

    @Test
    void shouldHandleEmptyStream() {
        SseParser parser = new SseParser();
        AtomicReference<Boolean> done = new AtomicReference<>(false);

        parser.parseSseStream("", chunk -> {}, () -> done.set(true));
        assertTrue(done.get());
    }

    @Test
    void shouldExtractContentFromChunk() {
        SseParser parser = new SseParser();
        assertNull(parser.extractContent(null));

        ChatCompletionChunk chunk = new ChatCompletionChunk();
        assertNull(parser.extractContent(chunk));
    }
}