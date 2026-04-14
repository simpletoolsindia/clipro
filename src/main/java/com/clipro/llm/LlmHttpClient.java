package com.clipro.llm;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * HTTP client wrapper for LLM providers.
 * Uses Java's built-in HttpClient (Java 11+) with Jackson for JSON.
 */
public class LlmHttpClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private URI baseUrl;
    private Duration timeout;
    private int maxRetries;
    private long retryDelayMs;

    public LlmHttpClient() {
        this(URI.create("http://localhost:11434/v1"));
    }

    public LlmHttpClient(URI baseUrl) {
        this.baseUrl = baseUrl;
        this.timeout = Duration.ofSeconds(30);
        this.maxRetries = 3;
        this.retryDelayMs = 1000;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public URI getBaseUrl() {
        return baseUrl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    /**
     * Make a GET request to the given endpoint.
     */
    public CompletableFuture<HttpResponse<String>> getAsync(String endpoint) {
        URI uri = baseUrl.resolve(endpoint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(timeout)
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Make a POST request with a JSON body.
     */
    public CompletableFuture<HttpResponse<String>> postAsync(String endpoint, Map<String, Object> body) {
        return postAsync(endpoint, body, Map.of("Content-Type", "application/json"));
    }

    /**
     * Make a POST request with custom headers.
     */
    public CompletableFuture<HttpResponse<String>> postAsync(String endpoint, Map<String, Object> body,
                                                             Map<String, String> headers) {
        URI uri = baseUrl.resolve(endpoint);
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(timeout)
                .header("Content-Type", "application/json");
        headers.forEach(requestBuilder::header);
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Make a streaming POST request using SSE.
     */
    public CompletableFuture<HttpResponse<String>> postStreaming(String endpoint, Map<String, Object> body) {
        return postStreaming(endpoint, body, Map.of(
            "Content-Type", "application/json",
            "Accept", "text/event-stream"
        ));
    }

    /**
     * Make a streaming POST request with custom headers.
     */
    public CompletableFuture<HttpResponse<String>> postStreaming(String endpoint, Map<String, Object> body,
                                                               Map<String, String> headers) {
        URI uri = baseUrl.resolve(endpoint);
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMinutes(5))
                .header("Accept", "text/event-stream");
        headers.forEach(requestBuilder::header);
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Execute an action with retry logic.
     * @param action The action to execute
     * @param <T> The return type
     * @return The result
     * @throws RuntimeException if all retries fail
     */
    public <T> T executeWithRetry(Supplier<T> action) {
        Exception lastException = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return action.get();
            } catch (Exception e) {
                lastException = e;
                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ie);
                    }
                }
            }
        }
        throw new RuntimeException(lastException);
    }

    /**
     * Execute an async action with retry logic.
     * @param action The async action to execute
     * @param <T> The return type
     * @return CompletableFuture with the result
     */
    public <T> CompletableFuture<T> executeWithRetryAsync(Supplier<CompletableFuture<T>> action) {
        CompletableFuture<T> result = new CompletableFuture<>();
        executeWithRetryAsyncHelper(action, result, 0);
        return result;
    }

    private <T> void executeWithRetryAsyncHelper(Supplier<CompletableFuture<T>> action,
                                                 CompletableFuture<T> result, int attempt) {
        action.get().handle((value, ex) -> {
            if (ex != null) {
                if (attempt < maxRetries - 1) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        result.completeExceptionally(ie);
                        return null;
                    }
                    executeWithRetryAsyncHelper(action, result, attempt + 1);
                } else {
                    result.completeExceptionally(ex);
                }
            } else {
                result.complete(value);
            }
            return null;
        });
    }
}
