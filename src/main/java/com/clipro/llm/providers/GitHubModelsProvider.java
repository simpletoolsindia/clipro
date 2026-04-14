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
 * GitHub Models provider for Copilot integration.
 * Connects to Azure AI endpoints provided by GitHub Models (https://models.inference.ai.azure.com).
 * Uses standard OpenAI compatible ChatCompletion API.
 * 
 * L-08: GitHub Models Provider
 */
public class GitHubModelsProvider implements LlmProvider {

    private static final String DEFAULT_BASE_URL = "https://models.inference.ai.azure.com";
    private static final String CHAT_ENDPOINT = "/chat/completions";

    private final LlmHttpClient httpClient;
    private final SseParser sseParser;
    private String currentModel;
    private String apiKey; // Usually a PAT with Copilot access

    public GitHubModelsProvider() {
        this(DEFAULT_BASE_URL);
    }

    public GitHubModelsProvider(String baseUrl) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.currentModel = "gpt-4o";
    }

    public GitHubModelsProvider(String baseUrl, String model) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.currentModel = model;
    }

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
                    throw new RuntimeException("Failed to parse GitHub Models response: " + response.body(), e);
                }
            });
    }

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

    /**
     * Check if GitHub Models API is available.
     * Use a minimal completion request as test since /models isn't openly enumerable.
     */
    public CompletableFuture<Boolean> healthCheck() {
        if (!hasApiKey()) {
            return CompletableFuture.completedFuture(false);
        }
        
        ChatCompletionRequest req = new ChatCompletionRequest();
        req.setModel(currentModel);
        req.getMessages().add(Map.of("role", "system", "content", "test"));
        req.setMaxTokens(1);
        
        return chat(req)
            .thenApply(response -> true)
            .exceptionally(ex -> false);
    }

    public static List<String> getDefaultModels() {
        return List.of(
            "gpt-4o",
            "gpt-4o-mini",
            "o1-preview",
            "o1-mini",
            "meta-llama-3.1-70b-instruct",
            "meta-llama-3.1-405b-instruct",
            "cohere-command-r-plus"
        );
    }
}
