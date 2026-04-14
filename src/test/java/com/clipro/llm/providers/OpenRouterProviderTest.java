package com.clipro.llm.providers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpenRouterProviderTest {

    @Test
    void should_create_with_default_url() {
        // Given/When
        OpenRouterProvider provider = new OpenRouterProvider();

        // Then
        assertEquals("https://openrouter.ai/api/v1", provider.getHttpClient().getBaseUrl().toString());
        assertEquals("anthropic/claude-sonnet-4", provider.getCurrentModel());
    }

    @Test
    void should_create_with_custom_url() {
        // Given
        String customUrl = "https://api.example.com/v1";

        // When
        OpenRouterProvider provider = new OpenRouterProvider(customUrl);

        // Then
        assertEquals(customUrl, provider.getHttpClient().getBaseUrl().toString());
    }

    @Test
    void should_create_with_custom_model() {
        // Given
        String customModel = "openai/gpt-4o";

        // When
        OpenRouterProvider provider = new OpenRouterProvider("https://openrouter.ai/api/v1", customModel);

        // Then
        assertEquals(customModel, provider.getCurrentModel());
    }

    @Test
    void should_set_and_get_api_key() {
        // Given
        OpenRouterProvider provider = new OpenRouterProvider();
        String apiKey = "sk-or-v1-test-key";

        // When
        provider.setApiKey(apiKey);

        // Then
        assertEquals(apiKey, provider.getApiKey());
    }

    @Test
    void should_return_false_health_check_without_api_key() throws Exception {
        // Given
        OpenRouterProvider provider = new OpenRouterProvider();

        // When
        Boolean healthy = provider.healthCheck().get();

        // Then
        assertFalse(healthy);
    }

    @Test
    void should_set_current_model() {
        // Given
        OpenRouterProvider provider = new OpenRouterProvider();
        String newModel = "google/gemini-pro";

        // When
        provider.setCurrentModel(newModel);

        // Then
        assertEquals(newModel, provider.getCurrentModel());
    }
}