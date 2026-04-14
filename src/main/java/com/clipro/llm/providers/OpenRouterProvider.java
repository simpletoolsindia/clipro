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
 * OpenRouter provider for accessing 300+ models through unified API.
 * Priority: SECOND (after Ollama local)
 * Reference: https://openrouter.ai/docs
 */
public class OpenRouterProvider implements LlmProvider {

    private static final String DEFAULT_BASE_URL = "https://openrouter.ai/api/v1";
    private static final String CHAT_ENDPOINT = "/chat/completions";
    private static final String MODELS_ENDPOINT = "/models";

    private final LlmHttpClient httpClient;
    private final SseParser sseParser;
    private String currentModel;
    private String apiKey;

    public OpenRouterProvider() {
        this(DEFAULT_BASE_URL);
    }

    public OpenRouterProvider(String baseUrl) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.currentModel = "anthropic/claude-sonnet-4";
    }

    public OpenRouterProvider(String baseUrl, String model) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.currentModel = model;
    }

    /**
     * Set API key for OpenRouter authentication.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
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
     * Send a chat completion request with custom endpoint.
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
     * Get available models from OpenRouter.
     */
    public CompletableFuture<String> getModels() {
        return httpClient.getAsync(MODELS_ENDPOINT)
            .thenApply(HttpResponse::body);
    }

    /**
     * Check if OpenRouter API is healthy.
     */
    public CompletableFuture<Boolean> healthCheck() {
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }
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

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }

    public static List<String> getDefaultModels() {
        return List.of(
            "qwen/qwen3.6-plus",
            "anthropic/claude-sonnet-4",
            "anthropic/claude-opus-4",
            "openai/gpt-4o",
            "google/gemini-2.0-flash-thinking-exp",
            "meta-llama/llama-3.3-70b-instruct"
        );
    }
}