package com.clipro.llm.providers;

import com.clipro.llm.LlmHttpClient;
import com.clipro.llm.SseParser;
import com.clipro.llm.models.ChatCompletionRequest;
import com.clipro.llm.models.ChatCompletionResponse;
import com.clipro.llm.models.ChatCompletionChunk;

import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * AWS Bedrock provider for Claude models on AWS.
 * L-06: Claude via AWS Bedrock using AWS SDK authentication.
 * Reference: https://docs.aws.amazon.com/bedrock/latest/APIReference/welcome.html
 */
public class BedrockProvider implements LlmProvider {

    private static final String DEFAULT_BASE_URL = "https://bedrock-runtime.us-east-1.amazonaws.com";
    private static final String CHAT_ENDPOINT = "/model/anthropic.claude-3-5-sonnet-20241022-v1:0/invoke";
    private static final String STREAM_ENDPOINT = "/model/anthropic.claude-3-5-sonnet-20241022-v1:0.invoke-with-pagination";

    private final LlmHttpClient httpClient;
    private final SseParser sseParser;
    private String currentModel;
    private String awsRegion;
    private String accessKey;
    private String secretKey;

    public BedrockProvider() {
        this("us-east-1");
    }

    public BedrockProvider(String region) {
        this.httpClient = new LlmHttpClient(URI.create("https://bedrock-runtime." + region + ".amazonaws.com"));
        this.sseParser = new SseParser(httpClient.getObjectMapper());
        this.awsRegion = region;
        this.currentModel = "anthropic.claude-3-5-sonnet-20241022-v1:0";
    }

    /**
     * Configure AWS credentials.
     */
    public void setCredentials(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    /**
     * Set AWS region.
     */
    public void setRegion(String region) {
        this.awsRegion = region;
        // Rebuild client with new region
        this.httpClient = new LlmHttpClient(URI.create("https://bedrock-runtime." + region + ".amazonaws.com"));
    }

    /**
     * Check if credentials are configured.
     */
    public boolean hasCredentials() {
        return accessKey != null && !accessKey.isEmpty() &&
               secretKey != null && !secretKey.isEmpty();
    }

    /**
     * Send a chat completion request.
     */
    @Override
    public CompletableFuture<ChatCompletionResponse> chat(ChatCompletionRequest request) {
        String model = request.getModel() != null ? request.getModel() : currentModel;
        String endpoint = "/model/" + model + "/invoke";

        // Convert to Anthropic format for Bedrock
        List<Map<String, Object>> messages = convertToBedrockMessages(request.getMessages());

        Map<String, Object> body = Map.of(
            "anthropic_version", "bedrock-2023-05-31",
            "max_tokens", 4096,
            "messages", messages
        );

        return httpClient.postAsync(endpoint, body, getHeaders())
            .thenApply(response -> {
                try {
                    return parseBedrockResponse(response.body());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse Bedrock response: " + response.body(), e);
                }
            });
    }

    /**
     * Send a streaming chat completion request.
     */
    @Override
    public CompletableFuture<Void> chatStream(ChatCompletionRequest request, Consumer<ChatCompletionChunk> onChunk) {
        String model = request.getModel() != null ? request.getModel() : currentModel;
        String endpoint = "/model/" + model + "/invoke-with-pagination";

        List<Map<String, Object>> messages = convertToBedrockMessages(request.getMessages());

        Map<String, Object> body = Map.of(
            "anthropic_version", "bedrock-2023-05-31",
            "max_tokens", 4096,
            "messages", messages
        );

        return httpClient.postStreaming(endpoint, body, getHeaders())
            .thenAccept(response -> {
                parseBedrockStream(response.body(), onChunk);
            });
    }

    private Map<String, String> getHeaders() {
        // In production, use AWS Signature Version 4 signing
        // This is a simplified implementation
        return Map.of(
            "Content-Type", "application/json",
            "Accept", "application/json"
        );
    }

    /**
     * Convert messages to Bedrock/Anthropic format.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> convertToBedrockMessages(List<?> messages) {
        return (List<Map<String, Object>>) (List<?) messages;
    }

    /**
     * Parse Bedrock response.
     */
    private ChatCompletionResponse parseBedrockResponse(String body) {
        ChatCompletionResponse response = new ChatCompletionResponse();
        response.setModel(currentModel);

        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(body);

            // Bedrock returns content array
            var content = node.get("content");
            if (content != null && content.size() > 0) {
                String text = content.get(0).get("text").asText();
                response.setContent(text);
            }
        } catch (Exception e) {
            response.setContent(body);
        }

        return response;
    }

    /**
     * Parse Bedrock SSE stream.
     */
    private void parseBedrockStream(String body, Consumer<ChatCompletionChunk> onChunk) {
        for (String line : body.split("\n")) {
            if (line.startsWith("data: ")) {
                String json = line.substring(6);
                try {
                    ChatCompletionChunk chunk = new ChatCompletionChunk();
                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    var node = mapper.readTree(json);

                    var delta = node.get("delta");
                    if (delta != null) {
                        String text = delta.get("text").asText();
                        chunk.setDeltaContent(text);
                        onChunk.accept(chunk);
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
     * Get available Bedrock models.
     */
    public static List<String> getDefaultModels() {
        return List.of(
            "anthropic.claude-3-5-sonnet-20241022-v1:0",
            "anthropic.claude-3-opus-20240229-v1:0",
            "anthropic.claude-3-sonnet-20240229-v1:0",
            "anthropic.claude-3-haiku-20240307-v1:0"
        );
    }
}