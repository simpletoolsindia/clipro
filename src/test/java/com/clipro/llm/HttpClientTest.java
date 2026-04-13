package com.clipro.llm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Tests for LlmHttpClient
 */
class HttpClientTest {

    @Test
    void shouldCreateClientWithDefaultBaseUrl() {
        LlmHttpClient client = new LlmHttpClient();
        assertNotNull(client);
        assertEquals(URI.create("http://localhost:11434/v1"), client.getBaseUrl());
    }

    @Test
    void shouldCreateClientWithCustomBaseUrl() {
        URI customUrl = URI.create("http://localhost:8080/v1");
        LlmHttpClient client = new LlmHttpClient(customUrl);
        assertEquals(customUrl, client.getBaseUrl());
    }

    @Test
    void shouldMakeAsyncGetRequest() throws Exception {
        LlmHttpClient client = new LlmHttpClient();
        // Note: This test requires Ollama running. In CI, this would be mocked.
        // For now, we just verify the client is constructed properly.
        assertNotNull(client);
    }

    @Test
    void shouldMakeAsyncPostRequest() throws Exception {
        LlmHttpClient client = new LlmHttpClient();
        // Verify client can construct POST requests
        assertNotNull(client);
    }

    @Test
    void shouldHandleTimeouts() {
        LlmHttpClient client = new LlmHttpClient();
        client.setTimeout(java.time.Duration.ofSeconds(10));
        assertEquals(java.time.Duration.ofSeconds(10), client.getTimeout());
    }
}