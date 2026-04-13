package com.clipro.llm.providers;

import com.clipro.llm.LlmHttpClient;
import com.clipro.llm.SseParser;
import com.clipro.llm.models.ChatCompletionRequest;
import com.clipro.llm.models.ChatCompletionResponse;
import com.clipro.llm.models.ChatCompletionChunk;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Ollama provider using OpenAI-compatible API.
 * Priority: LOCAL-FIRST (1st choice)
 */
public class OllamaProvider {

    private static final String DEFAULT_BASE_URL = "http://localhost:11434/v1";
    private static final String CHAT_ENDPOINT = "/chat/completions";
    private static final String MODELS_ENDPOINT = "/api/tags";

    private final LlmHttpClient httpClient;
    private final SseParser sseParser;
    private String currentModel;

    public OllamaProvider() {
        this(DEFAULT_BASE_URL);
    }

    public OllamaProvider(String baseUrl) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.currentModel = "qwen3-coder:32b";
    }

    public OllamaProvider(String baseUrl, String model) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.currentModel = model;
    }

    /**
     * Send a chat completion request.
     */
    public CompletableFuture<ChatCompletionResponse> chat(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(currentModel);
        }
        return chat(request, CHAT_ENDPOINT);
    }

    /**
     * Send a chat completion request to a specific endpoint.
     */
    public CompletableFuture<ChatCompletionResponse> chat(ChatCompletionRequest request, String endpoint) {
        Map<String, Object> body = Map.of(
            "model", request.getModel() != null ? request.getModel() : currentModel,
            "messages", request.getMessages(),
            "stream", false
        );

        return httpClient.postAsync(endpoint, body)
            .thenApply(response -> {
                try {
                    return httpClient.getObjectMapper().readValue(
                        response.body(), ChatCompletionResponse.class);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse response: " + response.body(), e);
                }
            });
    }

    /**
     * Send a streaming chat completion request.
     * @param request The chat completion request
     * @param onChunk Callback for each streaming chunk
     * @return CompletableFuture that completes when streaming is done
     */
    public CompletableFuture<Void> chatStream(ChatCompletionRequest request, Consumer<ChatCompletionChunk> onChunk) {
        if (request.getModel() == null) {
            request.setModel(currentModel);
        }

        Map<String, Object> body = Map.of(
            "model", request.getModel() != null ? request.getModel() : currentModel,
            "messages", request.getMessages(),
            "stream", true
        );

        return httpClient.postStreaming(CHAT_ENDPOINT, body)
            .thenAccept(response -> {
                String bodyStr = response.body();
                sseParser.parseSseStream(bodyStr, onChunk, () -> {});
            });
    }

    /**
     * Get available models from Ollama.
     */
    public CompletableFuture<String> getModels() {
        return httpClient.getAsync(MODELS_ENDPOINT)
            .thenApply(HttpResponse::body);
    }

    /**
     * Check if Ollama is healthy.
     */
    public CompletableFuture<Boolean> healthCheck() {
        return getModels()
            .thenApply(body -> true)
            .exceptionally(ex -> false);
    }

    public String getCurrentModel() {
        return currentModel;
    }

    public void setCurrentModel(String model) {
        this.currentModel = model;
    }

    public LlmHttpClient getHttpClient() {
        return httpClient;
    }
}