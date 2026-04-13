package com.clipro.llm.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;

/**
 * Tests for ChatCompletionRequest and related models.
 */
class RequestModelTest {

    @Test
    void shouldCreateBasicRequest() {
        List<Message> messages = List.of(
            Message.user("Hello"),
            Message.assistant("Hi there!")
        );
        ChatCompletionRequest request = new ChatCompletionRequest("qwen3-coder:32b", messages);

        assertEquals("qwen3-coder:32b", request.getModel());
        assertEquals(2, request.getMessages().size());
        assertFalse(request.isStream());
    }

    @Test
    void shouldAddMessages() {
        ChatCompletionRequest request = new ChatCompletionRequest("llama3.3:70b", new java.util.ArrayList<>());
        request.addMessage(Message.system("You are helpful."));
        request.addMessage(Message.user("What is Java?"));

        assertEquals(2, request.getMessages().size());
    }

    @Test
    void shouldSetStreaming() {
        ChatCompletionRequest request = new ChatCompletionRequest("qwen2.5-coder:14b", List.of());
        request.setStream(true);

        assertTrue(request.isStream());
    }

    @Test
    void shouldSetOptions() {
        ChatCompletionRequest request = new ChatCompletionRequest("qwen3-coder:32b", List.of());
        request.setOptions(Map.of("temperature", 0.7, "num_predict", 256));

        assertNotNull(request.getOptions());
        assertEquals(0.7, request.getOptions().get("temperature"));
    }

    @Test
    void shouldAddTools() {
        ChatCompletionRequest request = new ChatCompletionRequest("qwen3-coder:32b", List.of());
        ToolDefinition tool = new ToolDefinition("web_search", "Search the web", Map.of("type", "object"));
        request.addTool(tool);

        assertNotNull(request.getTools());
        assertEquals(1, request.getTools().size());
        assertEquals("web_search", request.getTools().get(0).getFunction().getName());
    }

    @Test
    void shouldCreateSystemMessage() {
        Message msg = Message.system("You are a coding assistant.");
        assertEquals("system", msg.getRole());
        assertEquals("You are a coding assistant.", msg.getContent());
    }

    @Test
    void shouldCreateUserMessage() {
        Message msg = Message.user("Write a hello world");
        assertEquals("user", msg.getRole());
        assertEquals("Write a hello world", msg.getContent());
    }

    @Test
    void shouldCreateAssistantMessage() {
        Message msg = Message.assistant("Here is the code:");
        assertEquals("assistant", msg.getRole());
        assertEquals("Here is the code:", msg.getContent());
    }

    @Test
    void shouldCreateToolMessage() {
        Message msg = Message.tool("Result: 42", "call_123");
        assertEquals("tool", msg.getRole());
        assertEquals("Result: 42", msg.getContent());
        assertEquals("call_123", msg.getToolCallId());
    }

    @Test
    void shouldCreateToolCall() {
        ToolCall toolCall = new ToolCall("call_abc", "web_search", "{\"query\":\"java\"}");
        assertEquals("call_abc", toolCall.getId());
        assertEquals("function", toolCall.getType());
        assertEquals("web_search", toolCall.getFunction().getName());
        assertEquals("{\"query\":\"java\"}", toolCall.getFunction().getArguments());
    }

    @Test
    void shouldCreateToolDefinition() {
        Object params = Map.of(
            "type", "object",
            "properties", Map.of("query", Map.of("type", "string"))
        );
        ToolDefinition tool = new ToolDefinition("web_search", "Search the web", params);

        assertEquals("function", tool.getType());
        assertEquals("web_search", tool.getFunction().getName());
        assertEquals("Search the web", tool.getFunction().getDescription());
    }
}