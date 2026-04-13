package com.clipro.llm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatRequestTest {

    @Test
    void shouldCreateRequest() {
        ChatRequest request = new ChatRequest("qwen3-coder:32b", null);
        assertEquals("qwen3-coder:32b", request.getModel());
    }

    @Test
    void shouldSetTemperature() {
        ChatRequest request = new ChatRequest();
        request.setTemperature(0.5);
        assertEquals(0.5, request.getTemperature());
    }

    @Test
    void shouldSetMaxTokens() {
        ChatRequest request = new ChatRequest();
        request.setMaxTokens(2000);
        assertEquals(2000, request.getMaxTokens());
    }

    @Test
    void shouldSetStream() {
        ChatRequest request = new ChatRequest();
        assertFalse(request.isStream());
        request.setStream(true);
        assertTrue(request.isStream());
    }

    @Test
    void shouldCreateMessage() {
        ChatRequest.Message msg = new ChatRequest.Message("user", "Hello");
        assertEquals("user", msg.getRole());
        assertEquals("Hello", msg.getContent());
    }
}

class ChatResponseTest {

    @Test
    void shouldCreateResponse() {
        ChatResponse response = new ChatResponse();
        assertNotNull(response);
    }

    @Test
    void shouldSetContent() {
        ChatResponse response = new ChatResponse();
        response.setContent("Hello, how can I help?");
        assertEquals("Hello, how can I help?", response.getContent());
    }

    @Test
    void shouldDetectError() {
        ChatResponse response = new ChatResponse();
        assertFalse(response.hasError());
        response.setError("Something went wrong");
        assertTrue(response.hasError());
    }

    @Test
    void shouldSetTokenCounts() {
        ChatResponse response = new ChatResponse();
        response.setInputTokens(100);
        response.setOutputTokens(200);
        assertEquals(100, response.getInputTokens());
        assertEquals(200, response.getOutputTokens());
    }

    @Test
    void shouldCreateToolCall() {
        ChatResponse.ToolCall tc = new ChatResponse.ToolCall();
        tc.setId("call_123");
        tc.setName("web_search");
        tc.setArguments("{\"query\":\"test\"}");

        assertEquals("call_123", tc.getId());
        assertEquals("web_search", tc.getName());
        assertEquals("{\"query\":\"test\"}", tc.getArguments());
    }
}

class LlmProviderTest {

    @Test
    void shouldCreateModel() {
        LlmProvider.Model model = new LlmProvider.Model("test-model", "Test Model", true, true);
        assertEquals("test-model", model.getId());
        assertEquals("Test Model", model.getName());
        assertTrue(model.isLocal());
        assertTrue(model.supportsToolCalling());
    }

    @Test
    void shouldCreateHealthStatus() {
        LlmProvider.HealthStatus status = new LlmProvider.HealthStatus(true, "OK", 100);
        assertTrue(status.isHealthy());
        assertEquals("OK", status.getMessage());
        assertEquals(100, status.getLatencyMs());
    }
}

class OllamaProviderTest {

    @Test
    void shouldCreateProvider() {
        OllamaProvider provider = new OllamaProvider();
        assertEquals("Ollama", provider.getName());
    }

    @Test
    void shouldGetAvailableModels() {
        OllamaProvider provider = new OllamaProvider();
        var models = provider.getAvailableModels();
        assertFalse(models.isEmpty());
        assertTrue(models.stream().anyMatch(m -> m.getId().contains("qwen")));
    }

    @Test
    void shouldGetDefaultModel() {
        OllamaProvider provider = new OllamaProvider();
        var model = provider.getDefaultModel();
        assertNotNull(model);
        assertTrue(model.getId().contains("qwen"));
    }

    @Test
    void shouldCheckHealth() {
        OllamaProvider provider = new OllamaProvider();
        var status = provider.checkHealth();
        assertNotNull(status);
        // May fail if Ollama not running, that's OK
    }
}
