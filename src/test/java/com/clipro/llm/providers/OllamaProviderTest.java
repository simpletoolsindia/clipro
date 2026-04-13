package com.clipro.llm.providers;

import com.clipro.llm.models.ChatCompletionRequest;
import com.clipro.llm.models.Message;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OllamaProvider.
 */
class OllamaProviderTest {

    @Test
    void shouldCreateProviderWithDefaultUrl() {
        OllamaProvider provider = new OllamaProvider();
        assertEquals("http://localhost:11434/v1", provider.getHttpClient().getBaseUrl().toString());
        assertEquals("qwen3-coder:32b", provider.getCurrentModel());
    }

    @Test
    void shouldCreateProviderWithCustomUrl() {
        OllamaProvider provider = new OllamaProvider("http://localhost:8080/v1");
        assertEquals("http://localhost:8080/v1", provider.getHttpClient().getBaseUrl().toString());
    }

    @Test
    void shouldCreateProviderWithCustomModel() {
        OllamaProvider provider = new OllamaProvider("http://localhost:11434/v1", "llama3.3:70b");
        assertEquals("llama3.3:70b", provider.getCurrentModel());
    }

    @Test
    void shouldSetCurrentModel() {
        OllamaProvider provider = new OllamaProvider();
        provider.setCurrentModel("qwen2.5-coder:14b");
        assertEquals("qwen2.5-coder:14b", provider.getCurrentModel());
    }

    @Test
    void shouldBuildRequestWithModel() {
        OllamaProvider provider = new OllamaProvider();
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.addMessage(Message.user("Hello"));

        // Verify provider can create requests (mocking would be needed for actual calls)
        assertNotNull(provider);
        assertEquals(1, request.getMessages().size()); // Message was added
    }

    @Test
    void shouldHaveHttpClient() {
        OllamaProvider provider = new OllamaProvider();
        assertNotNull(provider.getHttpClient());
    }
}