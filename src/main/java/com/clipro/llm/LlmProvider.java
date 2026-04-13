package com.clipro.llm;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * LLM provider interface for multiple backends.
 */
public interface LlmProvider {
    String getName();

    boolean isAvailable();

    CompletableFuture<ChatResponse> chat(ChatRequest request);

    CompletableFuture<ChatResponse> chatStream(ChatRequest request, ChatStreamHandler handler);

    List<Model> getAvailableModels();

    Model getDefaultModel();

    HealthStatus checkHealth();

    class Model {
        private final String id;
        private final String name;
        private final boolean local;
        private final boolean toolCalling;

        public Model(String id, String name, boolean local, boolean toolCalling) {
            this.id = id;
            this.name = name;
            this.local = local;
            this.toolCalling = toolCalling;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public boolean isLocal() { return local; }
        public boolean supportsToolCalling() { return toolCalling; }
    }

    class HealthStatus {
        private final boolean healthy;
        private final String message;
        private final long latencyMs;

        public HealthStatus(boolean healthy, String message, long latencyMs) {
            this.healthy = healthy;
            this.message = message;
            this.latencyMs = latencyMs;
        }

        public boolean isHealthy() { return healthy; }
        public String getMessage() { return message; }
        public long getLatencyMs() { return latencyMs; }
    }

    interface ChatStreamHandler {
        void onChunk(String content);
        void onComplete(ChatResponse response);
        void onError(Exception e);
    }
}
