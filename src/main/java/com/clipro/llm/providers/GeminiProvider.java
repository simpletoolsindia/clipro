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
 * Google Gemini provider for Gemini models.
 * L-07: Google Gemini via Google AI API or Vertex AI.
 * Reference: https://ai.google.dev/docs
 */
public class GeminiProvider implements LlmProvider {

    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String MODELS_ENDPOINT = "/models";
    private static final String CHAT_ENDPOINT = "/chat/completions";

    private LlmHttpClient httpClient;
    private final SseParser sseParser;
    private String currentModel;
    private String apiKey;
    private boolean useVertexAI;
    private String projectId;

    public GeminiProvider() {
        this(DEFAULT_BASE_URL, "gemini-2.0-flash");
    }

    public GeminiProvider(String model) {
        this(DEFAULT_BASE_URL, model);
    }

    public GeminiProvider(String baseUrl, String model) {
        this.httpClient = new LlmHttpClient(URI.create(baseUrl));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.currentModel = model;
        this.useVertexAI = false;
    }

    /**
     * Set API key for Google authentication.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    /**
     * Configure for Vertex AI (Google Cloud).
     */
    public void setVertexAI(String projectId) {
        this.useVertexAI = true;
        this.projectId = projectId;
        this.httpClient = new LlmHttpClient(URI.create("https://eu-langchain-generativelanguage.googleapis.com/v1"));
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
        String model = request.getModel() != null ? request.getModel() : currentModel;
        String endpoint = buildEndpoint(model);

        // Convert messages to Gemini format
        List<Map<String, Object>> contents = convertToGeminiMessages(request.getMessages());

        Map<String, Object> body = Map.of(
            "contents", contents
        );

        return httpClient.postAsync(endpoint, body, getHeaders())
            .thenApply(response -> {
                try {
                    return parseGeminiResponse(response.body());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse Gemini response: " + response.body(), e);
                }
            });
    }

    /**
     * Send a streaming chat completion request.
     */
    @Override
    public CompletableFuture<Void> chatStream(ChatCompletionRequest request, Consumer<ChatCompletionChunk> onChunk) {
        String model = request.getModel() != null ? request.getModel() : currentModel;
        String endpoint = buildEndpoint(model) + "?alt=sse";

        List<Map<String, Object>> contents = convertToGeminiMessages(request.getMessages());

        Map<String, Object> body = Map.of(
            "contents", contents,
            "generationConfig", Map.of(
                "temperature", 0.9,
                "maxOutputTokens", 8192
            )
        );

        return httpClient.postStreaming(endpoint, body, getHeaders())
            .thenAccept(response -> {
                String bodyStr = response.body();
                parseGeminiStream(bodyStr, onChunk);
            });
    }

    private String buildEndpoint(String model) {
        if (useVertexAI) {
            return "/projects/" + projectId + "/locations/us-central1/publishers/google/models/" + model + ":generateContent";
        }
        String baseUrl = DEFAULT_BASE_URL;
        if (!apiKey.isEmpty()) {
            baseUrl += "/generativelanguage.googleapis.com";
        }
        return baseUrl + "/v1beta/models/" + model + ":generateContent?key=" + apiKey;
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new java.util.HashMap<>();
        headers.put("Content-Type", "application/json");
        if (useVertexAI) {
            headers.put("Authorization", "Bearer " + getVertexToken());
        }
        return headers;
    }

    private String getVertexToken() {
        // In production, use Application Default Credentials
        // This is a placeholder
        return "";
    }

    /**
     * Convert ChatML messages to Gemini format.
     */
    private List<Map<String, Object>> convertToGeminiMessages(List<?> messages) {
        List<Map<String, Object>> contents = new java.util.ArrayList<>();

        for (Object msg : messages) {
            if (msg instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) msg;
                String role = (String) message.getOrDefault("role", "user");
                Object content = message.get("content");

                // Map roles: user/model, system (ignored), assistant
                String geminiRole = "user";
                if ("model".equals(role) || "assistant".equals(role)) {
                    geminiRole = "model";
                }

                String text = content != null ? content.toString() : "";

                // Gemini expects parts array
                Map<String, Object> part = Map.of("text", text);
                Map<String, Object> contentMap = Map.of(
                    "role", geminiRole,
                    "parts", List.of(part)
                );
                contents.add(contentMap);
            }
        }

        return contents;
    }

    /**
     * Parse Gemini response into ChatCompletionResponse format.
     */
    private ChatCompletionResponse parseGeminiResponse(String body) {
        // Simple parsing - in production use proper JSON parsing
        ChatCompletionResponse response = new ChatCompletionResponse();
        response.setModel(currentModel);

        // Parse candidates[0].content.parts[0].text
        // This is a simplified implementation
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(body);
            var candidates = node.get("candidates");
            if (candidates != null && candidates.size() > 0) {
                var content = candidates.get(0).get("content");
                if (content != null) {
                    var parts = content.get("parts");
                    if (parts != null && parts.size() > 0) {
                        String text = parts.get(0).get("text").asText();
                        response.setContent(text);
                    }
                }
            }
        } catch (Exception e) {
            // Fallback
            response.setContent(body);
        }

        return response;
    }

    /**
     * Parse Gemini SSE stream.
     */
    private void parseGeminiStream(String body, Consumer<ChatCompletionChunk> onChunk) {
        // Parse Server-Sent Events format from Gemini
        for (String line : body.split("\n")) {
            if (line.startsWith("data: ")) {
                String json = line.substring(6);
                try {
                    ChatCompletionChunk chunk = new ChatCompletionChunk();
                    // Extract text from Gemini stream format
                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    var node = mapper.readTree(json);
                    var candidates = node.get("candidates");
                    if (candidates != null && candidates.size() > 0) {
                        var parts = candidates.get(0).get("content").get("parts");
                        if (parts != null && parts.size() > 0) {
                            String text = parts.get(0).get("text").asText();
                            chunk.setDeltaContent(text);
                            onChunk.accept(chunk);
                        }
                    }
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

    /**
     * Check if Gemini API is healthy.
     */
    public CompletableFuture<Boolean> healthCheck() {
        if (!hasApiKey() && !useVertexAI) {
            return CompletableFuture.completedFuture(false);
        }
        String endpoint = "/v1beta/models?key=" + apiKey;
        return httpClient.getAsync(endpoint, getHeaders())
            .thenApply(response -> true)
            .exceptionally(ex -> false);
    }

    public static List<String> getDefaultModels() {
        return List.of(
            "gemini-2.0-flash",
            "gemini-2.0-flash-exp",
            "gemini-1.5-flash",
            "gemini-1.5-pro",
            "gemini-pro"
        );
    }
}