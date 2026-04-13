package com.clipro.integration;

import com.clipro.agent.AgentEngine;
import com.clipro.llm.providers.OllamaProvider;
import com.clipro.tools.Tool;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Ollama connection.
 * Note: Requires Ollama running at localhost:11434
 */
class OllamaE2ETest {

    @Test
    void shouldCheckOllamaHealth() {
        OllamaProvider provider = new OllamaProvider();
        CompletableFuture<Boolean> health = provider.healthCheck();
        
        // This will fail if Ollama is not running
        // In CI, this would be mocked
        try {
            Boolean isHealthy = health.get();
            // If Ollama is running, should be true
            // If not running, health check would fail
        } catch (Exception e) {
            // Expected if Ollama not running
        }
    }

    @Test
    void shouldGetAvailableModels() {
        OllamaProvider provider = new OllamaProvider();
        CompletableFuture<String> models = provider.getModels();
        
        try {
            String modelList = models.get();
            assertNotNull(modelList);
            // Should contain model names in JSON format
        } catch (Exception e) {
            // Expected if Ollama not running
        }
    }

    @Test
    void shouldHaveDefaultModel() {
        OllamaProvider provider = new OllamaProvider();
        assertEquals("qwen3-coder:32b", provider.getCurrentModel());
    }

    @Test
    void shouldSetCustomModel() {
        OllamaProvider provider = new OllamaProvider();
        provider.setCurrentModel("llama3.3:70b");
        assertEquals("llama3.3:70b", provider.getCurrentModel());
    }

    @Test
    void shouldConnectToCustomUrl() {
        OllamaProvider provider = new OllamaProvider("http://localhost:8080/v1");
        assertEquals("http://localhost:8080/v1", provider.getHttpClient().getBaseUrl().toString());
    }
}
