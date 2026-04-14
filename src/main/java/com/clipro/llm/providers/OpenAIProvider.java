package com.clipro.llm.providers;

import com.clipro.llm.LlmHttpClient;
import com.clipro.llm.SseParser;
import com.clipro.llm.models.ChatCompletionRequest;
import com.clipro.llm.models.ChatCompletionResponse;
import com.clipro.llm.models.ChatCompletionChunk;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * OpenAI provider for GPT-4, GPT-4o, GPT-3.5-turbo models.
 * L-09: Azure OpenAI uses similar API, can be extended.
 * Reference: https://platform.openai.com/docs/api-reference
 */
public class OpenAIProvider implements LlmProvider {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final String CHAT_ENDPOINT = "/chat/completions";
    private static final String MODELS_ENDPOINT = "/models";

    private final LlmHttpClient httpClient;
    private final SseParser sseParser;
    private String currentModel;
    private String apiKey;

    public OpenAIProvider() {
        this(DEFAULT_BASE_URL);
    }

    public OpenAIProvider(String baseUrl) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.currentModel = "gpt-4o";
    }

    public OpenAIProvider(String baseUrl, String model) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.currentModel = model;
    }

    /**
     * Set API key for OpenAI authentication.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        // Set authorization header
        httpClient.addHeader("Authorization", "Bearer " + apiKey);
    }

    public String getApiKey() {
        return apiKey;
    }

    /**
     * Check if API key is configured.
     */
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Send a chat completion request.
     */
    @Override
    public CompletableFuture<ChatCompletionResponse> chat(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(currentModel);
        }

        Map<String, Object> body = Map.of(
            "model", request.getModel() != null ? request.getModel() : currentModel,
            "messages", request.getMessages(),
            "stream", false
        );

        return httpClient.postAsync(CHAT_ENDPOINT, body, getHeaders())
            .thenApply(response -> {
                try {
                    return httpClient.getObjectMapper().readValue(
                        response.body(), ChatCompletionResponse.class);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse OpenAI response: " + response.body(), e);
                }
            });
    }

    /**
     * Send a streaming chat completion request.
     */
    @Override
    public CompletableFuture<Void> chatStream(ChatCompletionRequest request, Consumer<ChatCompletionChunk> onChunk) {
        if (request.getModel() == null) {
            request.setModel(currentModel);
        }

        Map<String, Object> body = Map.of(
            "model", request.getModel() != null ? request.getModel() : currentModel,
            "messages", request.getMessages(),
            "stream", true
        );

        return httpClient.postStreaming(CHAT_ENDPOINT, body, getHeaders())
            .thenAccept(response -> {
                String bodyStr = response.body();
                sseParser.parseSseStream(bodyStr, onChunk, () -> {});
            });
    }

    private Map<String, String> getHeaders() {
        return Map.of(
            "Authorization", "Bearer " + (apiKey != null ? apiKey : ""),
            "Content-Type", "application/json"
        );
    }

    @Override
    public String getCurrentModel() {
        return currentModel;
    }

    @Override
    public void setCurrentModel(String model) {
        this.currentModel = model;
    }

    public LlmHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Check if OpenAI API is healthy.
     */
    public CompletableFuture<Boolean> healthCheck() {
        if (!hasApiKey()) {
            return CompletableFuture.completedFuture(false);
        }
        return httpClient.getAsync(MODELS_ENDPOINT, getHeaders())
            .thenApply(response -> true)
            .exceptionally(ex -> false);
    }

    /**
     * Get available models.
     */
    public CompletableFuture<String> getModels() {
        return httpClient.getAsync(MODELS_ENDPOINT, getHeaders())
            .thenApply(response -> response.body());
    }

    public static List<String> getDefaultModels() {
        return List.of(
            "gpt-4o",
            "gpt-4o-mini",
            "gpt-4-turbo",
            "gpt-4",
            "gpt-3.5-turbo"
        );
    }
}