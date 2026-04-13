package com.clipro.tools;

import com.clipro.llm.models.*;
import com.clipro.llm.providers.OllamaProvider;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ToolExecutor.
 */
class ToolExecutorTest {

    static class TestTool implements Tool {
        private final String name;
        private final String description;

        TestTool(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public String getName() { return name; }
        @Override
        public String getDescription() { return description; }
        @Override
        public Object getParameters() {
            return Map.of("type", "object", "properties", Map.of("input", Map.of("type", "string")));
        }
        @Override
        public String execute(Map<String, Object> args) {
            Object input = args.get("input");
            return "Echo: " + (input != null ? input : "nothing");
        }
    }

    @Test
    void shouldRegisterTool() {
        ToolExecutor executor = new ToolExecutor(new OllamaProvider());
        executor.registerTool(new TestTool("test", "A test tool"));
        assertTrue(executor.getToolNames().contains("test"));
    }

    @Test
    void shouldUnregisterTool() {
        ToolExecutor executor = new ToolExecutor(new OllamaProvider());
        executor.registerTool(new TestTool("test", "A test tool"));
        executor.unregisterTool("test");
        assertFalse(executor.getToolNames().contains("test"));
    }

    @Test
    void shouldGetToolByName() {
        ToolExecutor executor = new ToolExecutor(new OllamaProvider());
        TestTool tool = new TestTool("echo", "Echo tool");
        executor.registerTool(tool);
        assertNotNull(executor.getTool("echo"));
        assertNull(executor.getTool("nonexistent"));
    }

    @Test
    void shouldExecuteToolCall() {
        ToolExecutor executor = new ToolExecutor(new OllamaProvider());
        executor.registerTool(new TestTool("echo", "Echo tool"));

        ToolCall call = new ToolCall("call_1", "echo", "{\"input\":\"hello\"}");
        String result = executor.executeTool(call);
        assertEquals("Echo: hello", result);
    }

    @Test
    void shouldHandleUnknownTool() {
        ToolExecutor executor = new ToolExecutor(new OllamaProvider());
        ToolCall call = new ToolCall("call_1", "unknown", "{}");
        String result = executor.executeTool(call);
        assertTrue(result.startsWith("Error: Unknown tool"));
    }

    @Test
    void shouldAddToolsToRequest() {
        ToolExecutor executor = new ToolExecutor(new OllamaProvider());
        executor.registerTool(new TestTool("echo", "Echo tool"));

        ChatCompletionRequest request = new ChatCompletionRequest();
        executor.addToolsToRequest(request);

        assertNotNull(request.getTools());
        assertEquals(1, request.getTools().size());
        assertEquals("echo", request.getTools().get(0).getFunction().getName());
    }

    @Test
    void shouldHandleEmptyToolSet() {
        ToolExecutor executor = new ToolExecutor(new OllamaProvider());
        ChatCompletionRequest request = new ChatCompletionRequest();
        executor.addToolsToRequest(request);
        assertNull(request.getTools());
    }
}