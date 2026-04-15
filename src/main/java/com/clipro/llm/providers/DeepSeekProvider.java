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
 * DeepSeek provider for accessing DeepSeek models.
 * Reference: https://platform.deepseek.com/docs
 */
public class DeepSeekProvider implements LlmProvider {

    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    private static final String CHAT_ENDPOINT = "/chat/completions";

    private final LlmHttpClient httpClient;
    private final SseParser sseParser;
    private String currentModel;
    private String apiKey;

    public DeepSeekProvider() {
        this(DEFAULT_BASE_URL);
    }

    public DeepSeekProvider(String baseUrl) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.currentModel = "deepseek-chat";
    }

    public DeepSeekProvider(String baseUrl, String model) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.currentModel = model;
    }

    /**
     * Set API key for DeepSeek authentication.
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

        return httpClient.postAsync(CHAT_ENDPOINT, body, getAuthHeaders())
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

        return httpClient.postStreaming(CHAT_ENDPOINT, body, getAuthHeaders())
            .thenAccept(response -> {
                String bodyStr = response.body();
                sseParser.parseSseStream(bodyStr, onChunk, () -> {});
            });
    }

    /**
     * Get headers for authentication.
     */
    private Map<String, String> getAuthHeaders() {
        return Map.of("Authorization", "Bearer " + apiKey);
    }

    /**
     * Check if DeepSeek API is healthy.
     */
    public CompletableFuture<Boolean> healthCheck() {
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        Map<String, Object> body = Map.of(
            "model", currentModel,
            "messages", List.of(Map.of("role", "user", "content", "test")),
            "max_tokens", 1
        );

        return httpClient.postAsync(CHAT_ENDPOINT, body, getAuthHeaders())
            .thenApply(response -> response.statusCode() == 200)
            .exceptionally(ex -> false);
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

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Get available models.
     */
    public static List<String> getAvailableModels() {
        return List.of(
            "deepseek-chat",
            "deepseek-coder"
        );
    }
}
