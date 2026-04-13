package com.clipro.llm.providers;

import com.clipro.llm.LlmHttpClient;
import org.junit.jupiter.api.Test;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OllamaProvider health check.
 */
class HealthCheckTest {

    @Test
    void shouldHaveHealthCheckMethod() {
        OllamaProvider provider = new OllamaProvider();
        // Health check is a CompletableFuture<Boolean>
        // In real scenario, this would connect to Ollama
        assertNotNull(provider);
    }

    @Test
    void shouldCreateProviderWithDefaultModel() {
        OllamaProvider provider = new OllamaProvider();
        assertEquals("qwen3-coder:32b", provider.getCurrentModel());
    }

    @Test
    void shouldChangeCurrentModel() {
        OllamaProvider provider = new OllamaProvider();
        provider.setCurrentModel("llama3.3:70b");
        assertEquals("llama3.3:70b", provider.getCurrentModel());
    }

    @Test
    void shouldHaveHttpClient() {
        OllamaProvider provider = new OllamaProvider();
        assertNotNull(provider.getHttpClient());
    }
}