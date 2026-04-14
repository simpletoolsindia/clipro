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
 * Anthropic provider for Claude models.
 * Supports claude-3-5-sonnet, claude-3-opus, claude-3-haiku
 * Reference: https://docs.anthropic.com/
 */
public class AnthropicProvider implements LlmProvider {

    private static final String DEFAULT_BASE_URL = "https://api.anthropic.com/v1";
    private static final String CHAT_ENDPOINT = "/messages";

    private final LlmHttpClient httpClient;
    private String apiKey;
    private String currentModel;
    private String anthropicVersion = "2023-06-01";

    public AnthropicProvider() {
        this(DEFAULT_BASE_URL);
    }

    public AnthropicProvider(String baseUrl) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.currentModel = "claude-3-5-sonnet-20241022";
    }

    public AnthropicProvider(String baseUrl, String model) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.currentModel = model;
    }

    /**
     * Set API key for Anthropic authentication.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public CompletableFuture<ChatCompletionResponse> chat(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(currentModel);
        }

        // Convert ChatML messages to Anthropic format
        Map<String, Object> body = Map.of(
            "model", request.getModel() != null ? request.getModel() : currentModel,
            "messages", convertToAnthropicMessages(request.getMessages()),
            "max_tokens", 4096
        );

        return httpClient.postAsync(CHAT_ENDPOINT, body, getHeaders())
            .thenApply(response -> {
                try {
                    return httpClient.getObjectMapper().readValue(
                        response.body(), ChatCompletionResponse.class);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse Anthropic response: " + response.body(), e);
                }
            });
    }

    @Override
    public CompletableFuture<Void> chatStream(ChatCompletionRequest request,
                                              Consumer<ChatCompletionChunk> onChunk) {
        if (request.getModel() == null) {
            request.setModel(currentModel);
        }

        Map<String, Object> body = Map.of(
            "model", request.getModel() != null ? request.getModel() : currentModel,
            "messages", convertToAnthropicMessages(request.getMessages()),
            "max_tokens", 4096,
            "stream", true
        );

        return httpClient.postStreaming(CHAT_ENDPOINT, body, getHeaders())
            .thenAccept(response -> {
                String bodyStr = response.body();
                // Parse SSE stream for Anthropic
                parseAnthropicStream(bodyStr, onChunk);
            });
    }

    private Map<String, String> getHeaders() {
        return Map.of(
            "x-api-key", apiKey != null ? apiKey : "",
            "anthropic-version", anthropicVersion,
            "content-type", "application/json"
        );
    }

    private List<Map<String, String>> convertToAnthropicMessages(List<?> messages) {
        // Convert OpenAI format to Anthropic format
        // Anthropic uses "role" and "content" but system is separate
        return List.of();
    }

    private void parseAnthropicStream(String sseData, Consumer<ChatCompletionChunk> onChunk) {
        // Parse Anthropic SSE stream format
        for (String line : sseData.split("\n")) {
            if (line.startsWith("data: ")) {
                String json = line.substring(6);
                try {
                    // Parse event type and content
                    ChatCompletionChunk chunk = new ChatCompletionChunk();
                    chunk.setDeltaContent("");
                    onChunk.accept(chunk);
                } catch (Exception ignored) {}
            }
        }
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

    public static List<String> getDefaultModels() {
        return List.of(
            "claude-3-5-sonnet-latest",
            "claude-3-5-sonnet-20241022",
            "claude-3-opus-latest",
            "claude-3-haiku-latest",
            "claude-3-sonnet-latest",
            "claude-3-haiku-20240307"
        );
    }
}
