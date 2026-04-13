package com.clipro.llm;

import com.clipro.llm.models.ChatCompletionChunk;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

/**
 * SSE (Server-Sent Events) parser for streaming responses.
 * Ollama uses SSE for streaming chat completions.
 */
public class SseParser {

    private final ObjectMapper objectMapper;

    public SseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseParser() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Parse SSE data line and return ChatCompletionChunk.
     */
    public ChatCompletionChunk parseLine(String line) {
        if (line == null || !line.startsWith("data: ")) {
            return null;
        }

        String data = line.substring(6).trim();
        if (data.isEmpty() || data.equals("[DONE]")) {
            return null;
        }

        try {
            return objectMapper.readValue(data, ChatCompletionChunk.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse a complete SSE response body.
     */
    public void parseSseStream(String body, Consumer<ChatCompletionChunk> onChunk, Runnable onDone) {
        String[] lines = body.split("\n");
        for (String line : lines) {
            ChatCompletionChunk chunk = parseLine(line);
            if (chunk != null) {
                onChunk.accept(chunk);
            } else if (line.trim().equals("data: [DONE]") || line.trim().equals("[DONE]")) {
                onDone.run();
                return;
            }
        }
        onDone.run();
    }

    /**
     * Extract content delta from a chunk.
     */
    public String extractContent(ChatCompletionChunk chunk) {
        return chunk != null ? chunk.getDeltaContent() : null;
    }

    /**
     * Build complete message from streaming chunks.
     */
    public String extractFullContent(ChatCompletionChunk[] chunks) {
        StringBuilder sb = new StringBuilder();
        for (ChatCompletionChunk chunk : chunks) {
            String content = extractContent(chunk);
            if (content != null) {
                sb.append(content);
            }
        }
        return sb.toString();
    }
}