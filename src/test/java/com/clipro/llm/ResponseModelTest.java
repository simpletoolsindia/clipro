package com.clipro.llm;

import com.clipro.llm.models.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ChatCompletionResponse and related models.
 */
class ResponseModelTest {

    @Test
    void shouldParseBasicResponse() {
        ChatCompletionResponse response = new ChatCompletionResponse();
        response.setId("chatcmpl-123");
        response.setModel("qwen3-coder:32b");
        response.setCreated(System.currentTimeMillis() / 1000);

        Message msg = new Message("assistant", "Hello!");
        ChatCompletionResponse.Choice choice = new ChatCompletionResponse.Choice();
        choice.setIndex(0);
        choice.setMessage(msg);
        choice.setFinishReason("stop");
        response.setChoices(List.of(choice));

        assertEquals("chatcmpl-123", response.getId());
        assertEquals("qwen3-coder:32b", response.getModel());
        assertEquals("Hello!", response.getFirstContent());
        assertEquals("stop", response.getChoices().get(0).getFinishReason());
    }

    @Test
    void shouldHandleNullResponse() {
        ChatCompletionResponse response = new ChatCompletionResponse();
        assertNull(response.getFirstContent());
        assertNull(response.getFirstMessage());
    }

    @Test
    void shouldParseUsage() {
        ChatCompletionResponse.Usage usage = new ChatCompletionResponse.Usage();
        usage.setPromptTokens(10);
        usage.setCompletionTokens(20);
        usage.setTotalTokens(30);

        assertEquals(10, usage.getPromptTokens());
        assertEquals(20, usage.getCompletionTokens());
        assertEquals(30, usage.getTotalTokens());
    }

    @Test
    void shouldParseStreamingChunk() {
        ChatCompletionChunk chunk = new ChatCompletionChunk();
        chunk.setId("chatcmpl-123");
        chunk.setModel("qwen3-coder:32b");

        ChatCompletionChunk.Delta delta = new ChatCompletionChunk.Delta();
        delta.setContent("Hello");
        ChatCompletionChunk.Choice choice = new ChatCompletionChunk.Choice();
        choice.setIndex(0);
        choice.setDelta(delta);
        chunk.setChoices(new ChatCompletionChunk.Choice[]{choice});

        assertEquals("Hello", chunk.getDeltaContent());
    }

    @Test
    void shouldHandleEmptyChunk() {
        ChatCompletionChunk chunk = new ChatCompletionChunk();
        assertNull(chunk.getDeltaContent());
    }

    @Test
    void shouldParseToolCallInResponse() {
        ChatCompletionResponse response = new ChatCompletionResponse();
        response.setModel("qwen3-coder:32b");

        ToolCall toolCall = new ToolCall("call_123", "web_search", "{\"query\":\"java\"}");
        Message msg = new Message("assistant", "");
        msg.setToolCalls(new ToolCall[]{toolCall});

        ChatCompletionResponse.Choice choice = new ChatCompletionResponse.Choice();
        choice.setIndex(0);
        choice.setMessage(msg);
        choice.setFinishReason("tool_calls");
        response.setChoices(List.of(choice));

        assertEquals(1, response.getChoices().get(0).getMessage().getToolCalls().length);
        assertEquals("web_search", response.getChoices().get(0).getMessage().getToolCalls()[0].getFunction().getName());
    }
}