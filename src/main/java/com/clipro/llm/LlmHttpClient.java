package com.clipro.llm;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP client wrapper for LLM providers.
 * Uses Java's built-in HttpClient (Java 11+) with Jackson for JSON.
 */
public class LlmHttpClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private URI baseUrl;
    private Duration timeout;

    public LlmHttpClient() {
        this(URI.create("http://localhost:11434/v1"));
    }

    public LlmHttpClient(URI baseUrl) {
        this.baseUrl = baseUrl;
        this.timeout = Duration.ofSeconds(30);
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

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
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
        URI uri = baseUrl.resolve(endpoint);
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Make a streaming POST request using SSE.
     */
    public CompletableFuture<HttpResponse<String>> postStreaming(String endpoint, Map<String, Object> body) {
        URI uri = baseUrl.resolve(endpoint);
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
