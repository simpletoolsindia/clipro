package com.clipro.llm;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Ollama LLM provider.
 * Connects to local Ollama instance at localhost:11434
 */
public class OllamaProvider implements LlmProvider {
    private static final String OLLAMA_URL = "http://localhost:11434";
    private final HttpClient httpClient;
    private final String baseUrl;

    public OllamaProvider() {
        this(OLLAMA_URL);
    }

    public OllamaProvider(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String getName() {
        return "Ollama";
    }

    @Override
    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tags"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public CompletableFuture<ChatResponse> chat(ChatRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = buildRequestJson(request);
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/api/chat"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .timeout(Duration.ofSeconds(60))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return parseResponse(response.body());
                } else {
                    ChatResponse error = new ChatResponse();
                    error.setError("HTTP " + response.statusCode() + ": " + response.body());
                    return error;
                }
            } catch (Exception e) {
                ChatResponse error = new ChatResponse();
                error.setError(e.getMessage());
                return error;
            }
        });
    }

    @Override
    public CompletableFuture<ChatResponse> chatStream(ChatRequest request, ChatStreamHandler handler) {
        // For now, use non-streaming and call handler per chunk
        request.setStream(false);
        return chat(request).thenApply(response -> {
            if (!response.hasError() && response.getContent() != null) {
                String content = response.getContent();
                // Simulate streaming by chunking
                int chunkSize = 10;
                for (int i = 0; i < content.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, content.length());
                    handler.onChunk(content.substring(i, end));
                }
            }
            handler.onComplete(response);
            return response;
        });
    }

    @Override
    public List<Model> getAvailableModels() {
        return List.of(
            new Model("qwen3-coder:32b", "Qwen3-Coder (32B)", true, true),
            new Model("qwen2.5-coder:14b", "Qwen2.5-Coder (14B)", true, true),
            new Model("llama3.3:70b", "Llama 3.3 (70B)", true, true),
            new Model("deepseek-r1:70b", "DeepSeek R1 (70B)", true, false)
        );
    }

    @Override
    public Model getDefaultModel() {
        return new Model("qwen3-coder:32b", "Qwen3-Coder (32B)", true, true);
    }

    @Override
    public HealthStatus checkHealth() {
        long start = System.currentTimeMillis();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tags"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - start;

            return new HealthStatus(true, "Ollama is running", latency);
        } catch (Exception e) {
            return new HealthStatus(false, e.getMessage(), 0);
        }
    }

    private String buildRequestJson(ChatRequest request) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"model\":\"").append(request.getModel()).append("\",");
        json.append("\"messages\":[");

        if (request.getMessages() != null) {
            for (int i = 0; i < request.getMessages().size(); i++) {
                ChatRequest.Message msg = request.getMessages().get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"role\":\"").append(msg.getRole()).append("\",");
                json.append("\"content\":\"").append(escapeJson(msg.getContent())).append("\"");
                json.append("}");
            }
        }

        json.append("],");
        json.append("\"stream\":").append(request.isStream()).append(",");
        json.append("\"options\":{");
        json.append("\"temperature\":").append(request.getTemperature());
        json.append("}");
        json.append("}");

        return json.toString();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private ChatResponse parseResponse(String json) {
        ChatResponse response = new ChatResponse();
        // Simple JSON parsing - extract content
        try {
            int contentStart = json.indexOf("\"content\":\"") + 11;
            int contentEnd = json.indexOf("\"", contentStart);
            if (contentStart > 10 && contentEnd > contentStart) {
                String content = json.substring(contentStart, contentEnd);
                content = content.replace("\\n", "\n").replace("\\\"", "\"");
                response.setContent(content);
            }
        } catch (Exception e) {
            response.setError("Parse error: " + e.getMessage());
        }
        return response;
    }
}
