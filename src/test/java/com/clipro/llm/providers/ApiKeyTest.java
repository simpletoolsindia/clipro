package com.clipro.llm.providers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;

/**
 * Test API key handling for OpenRouter provider.
 */
class ApiKeyTest {

    @Test
    void should_reject_empty_api_key() {
        // Given
        OpenRouterProvider provider = new OpenRouterProvider();

        // When/Then
        assertNull(provider.getApiKey());
        assertFalse(provider.healthCheck().join());
    }

    @Test
    void should_accept_valid_api_key_format() {
        // Given
        OpenRouterProvider provider = new OpenRouterProvider();
        String apiKey = "sk-or-v1-abcdef123456";

        // When
        provider.setApiKey(apiKey);

        // Then
        assertEquals(apiKey, provider.getApiKey());
    }

    @Test
    void should_allow_clearing_api_key() {
        // Given
        OpenRouterProvider provider = new OpenRouterProvider();
        provider.setApiKey("sk-or-v1-test");

        // When
        provider.setApiKey(null);

        // Then
        assertNull(provider.getApiKey());
    }

    @Test
    void should_preserve_api_key_on_model_change() {
        // Given
        OpenRouterProvider provider = new OpenRouterProvider();
        String apiKey = "sk-or-v1-preserve";
        provider.setApiKey(apiKey);

        // When
        provider.setCurrentModel("different/model");

        // Then
        assertEquals(apiKey, provider.getApiKey());
    }
}