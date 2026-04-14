package com.clipro.llm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryTest {

    @Test
    void should_retry_on_failure() {
        // Given: A service that fails 2 times then succeeds
        int[] attempts = {0};
        java.util.function.Supplier<String> action = () -> {
            attempts[0]++;
            if (attempts[0] < 3) throw new RuntimeException("Fail");
            return "success";
        };

        // When: Execute with retry
        String result = executeWithRetry(action, 3, 100);

        // Then: Should succeed after retries
        assertEquals("success", result);
        assertEquals(3, attempts[0]);
    }

    @Test
    void should_throw_after_max_retries() {
        // Given: A service that always fails
        java.util.function.Supplier<String> action = () -> {
            throw new RuntimeException("Always fails");
        };

        // When/Then: Should throw after max retries
        assertThrows(RuntimeException.class, () -> {
            executeWithRetry(action, 3, 100);
        });
    }

    @Test
    void should_respect_delay_between_retries() {
        // Given
        long startTime = System.currentTimeMillis();
        int[] attempts = {0};

        java.util.function.Supplier<String> action = () -> {
            attempts[0]++;
            if (attempts[0] < 3) throw new RuntimeException("Fail");
            return "success";
        };

        // When
        executeWithRetry(action, 3, 500);

        // Then: Should take at least 500ms * 2 retries = 1000ms
        long elapsed = System.currentTimeMillis() - startTime;
        assertTrue(elapsed >= 1000, "Expected at least 1000ms delay, got " + elapsed);
    }

    private String executeWithRetry(java.util.function.Supplier<String> action, int maxRetries, long delayMs) {
        Exception lastException = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return action.get();
            } catch (Exception e) {
                lastException = e;
                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ie);
                    }
                }
            }
        }
        throw new RuntimeException(lastException);
    }
}