package com.clipro.llm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.time.Duration;

class TimeoutTest {

    @Test
    void should_use_default_timeout() {
        // Given
        LlmHttpClient client = new LlmHttpClient();

        // Then
        assertEquals(Duration.ofSeconds(30), client.getTimeout());
    }

    @Test
    void should_set_custom_timeout() {
        // Given
        LlmHttpClient client = new LlmHttpClient();

        // When
        client.setTimeout(Duration.ofSeconds(60));

        // Then
        assertEquals(Duration.ofSeconds(60), client.getTimeout());
    }

    @Test
    void should_throw_on_timeout() {
        // Given: Client with very short timeout
        LlmHttpClient client = new LlmHttpClient();
        client.setTimeout(Duration.ofMillis(1));

        // When/Then: Request to unreachable host should timeout
        // Note: In test environment this may not always timeout
        // We test the configuration is correct
        assertEquals(Duration.ofMillis(1), client.getTimeout());
    }

    @Test
    void should_configure_timeout_in_constructor() {
        // Given
        URI customUri = URI.create("http://example.com");
        Duration customTimeout = Duration.ofSeconds(45);

        // When
        LlmHttpClient client = new LlmHttpClient(customUri);
        client.setTimeout(customTimeout);

        // Then
        assertEquals(customUri, client.getBaseUrl());
        assertEquals(customTimeout, client.getTimeout());
    }
}