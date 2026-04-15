package com.clipro.llm.providers;

import com.clipro.llm.models.ChatCompletionRequest;
import com.clipro.llm.models.ChatCompletionResponse;
import com.clipro.llm.models.ChatCompletionChunk;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Provider manager for switching between LLM providers.
 * Supports Ollama (local) and OpenRouter (cloud).
 */
public class ProviderManager {

    public enum ProviderType {
        OLLAMA("Ollama", "Local LLM (fast, private)"),
        OPENROUTER("OpenRouter", "Cloud LLM (300+ models)"),
        DEEPSEEK("DeepSeek", "DeepSeek models (cost-effective)"),
        GITHUBMODELS("GitHub Models", "Azure AI via GitHub Copilot"),
        OPENAI("OpenAI", "Standard OpenAI models"),
        GEMINI("Google Gemini", "Gemini via Vertex AI"),
        BEDROCK("AWS Bedrock", "Claude on AWS");

        private final String name;
        private final String description;

        ProviderType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    private final Map<ProviderType, LlmProvider> providers;
    private ProviderType currentProvider;

    // Default models per provider
    private static final Map<ProviderType, String> DEFAULT_MODELS = Map.of(
        ProviderType.OLLAMA, "qwen3-coder:32b",
        ProviderType.OPENROUTER, "qwen/qwen3.6-plus",
        ProviderType.DEEPSEEK, "deepseek-chat",
        ProviderType.GITHUBMODELS, "gpt-4o",
        ProviderType.OPENAI, "gpt-4o",
        ProviderType.GEMINI, "gemini-1.5-pro",
        ProviderType.BEDROCK, "anthropic.claude-3-5-sonnet-20241022-v2:0"
    );

    public ProviderManager() {
        this.providers = new ConcurrentHashMap<>();
        this.currentProvider = ProviderType.OLLAMA;

        // Initialize providers lazily
        initializeProvider(ProviderType.OLLAMA);
    }

    private void initializeProvider(ProviderType type) {
        if (!providers.containsKey(type)) {
            switch (type) {
                case OLLAMA:
                    providers.put(type, new OllamaProvider());
                    break;
                case OPENROUTER:
                    providers.put(type, new OpenRouterProvider());
                    break;
                case DEEPSEEK:
                    providers.put(type, new DeepSeekProvider());
                    break;
                case GITHUBMODELS:
                    providers.put(type, new GitHubModelsProvider());
                    break;
                case OPENAI:
                    providers.put(type, new OpenAIProvider());
                    break;
                case GEMINI:
                    providers.put(type, new GeminiProvider());
                    break;
                case BEDROCK:
                    providers.put(type, new BedrockProvider());
                    break;
            }
        }
    }

    /**
     * Get the current provider.
     */
    public LlmProvider getCurrentProvider() {
        return providers.get(currentProvider);
    }

    /**
     * Get the current provider type.
     */
    public ProviderType getCurrentProviderType() {
        return currentProvider;
    }

    /**
     * Switch to a different provider.
     */
    public boolean switchProvider(ProviderType type) {
        if (type == currentProvider) {
            return true; // Already on this provider
        }

        initializeProvider(type);

        if (providers.get(type) != null) {
            currentProvider = type;
            return true;
        }
        return false;
    }

    /**
     * Get available provider types with status.
     */
    public List<ProviderInfo> getAvailableProviders() {
        List<ProviderInfo> result = new ArrayList<>();

        for (ProviderType type : ProviderType.values()) {
            initializeProvider(type);
            LlmProvider provider = providers.get(type);
            boolean isHealthy = false;

            if (provider != null && provider instanceof OllamaProvider) {
                // Ollama - check local service
                try {
                    isHealthy = ((OllamaProvider) provider).healthCheck().get();
                } catch (Exception e) {
                    isHealthy = false;
                }
            } else if (provider != null && provider instanceof OpenRouterProvider) {
                // OpenRouter - check if API key is set
                isHealthy = ((OpenRouterProvider) provider).hasApiKey();
            }

            result.add(new ProviderInfo(
                type,
                type.getName(),
                type.getDescription(),
                isHealthy ? "Connected" : "Unavailable",
                isHealthy,
                provider != null ? provider.getCurrentModel() : null
            ));
        }

        return result;
    }

    /**
     * Send chat request to current provider.
     */
    public CompletableFuture<ChatCompletionResponse> chat(ChatCompletionRequest request) {
        LlmProvider provider = getCurrentProvider();
        if (provider == null) {
            return CompletableFuture.failedFuture(
                new RuntimeException("No provider available for " + currentProvider));
        }
        return provider.chat(request);
    }

    /**
     * Send streaming chat request to current provider.
     */
    public CompletableFuture<Void> chatStream(ChatCompletionRequest request,
                                              Consumer<ChatCompletionChunk> onChunk) {
        LlmProvider provider = getCurrentProvider();
        if (provider == null) {
            return CompletableFuture.failedFuture(
                new RuntimeException("No provider available for " + currentProvider));
        }
        return provider.chatStream(request, onChunk);
    }

    /**
     * Get available models for current provider.
     */
    public List<String> getAvailableModels() {
        LlmProvider provider = getCurrentProvider();
        if (provider == null) {
            return Collections.emptyList();
        }

        switch (currentProvider) {
            case OLLAMA:
                return OllamaProvider.getDefaultModels();
            case OPENROUTER:
                return OpenRouterProvider.getDefaultModels();
            case DEEPSEEK:
                return DeepSeekProvider.getAvailableModels();
            case GITHUBMODELS:
                return GitHubModelsProvider.getDefaultModels();
            case OPENAI:
                return OpenAIProvider.getDefaultModels();
            case GEMINI:
                return GeminiProvider.getDefaultModels();
            case BEDROCK:
                return BedrockProvider.getDefaultModels();
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Set the model for current provider.
     */
    public void setModel(String model) {
        LlmProvider provider = getCurrentProvider();
        if (provider != null) {
            provider.setCurrentModel(model);
        }
    }

    /**
     * Get current model name.
     */
    public String getCurrentModel() {
        LlmProvider provider = getCurrentProvider();
        return provider != null ? provider.getCurrentModel() : null;
    }

    /**
     * Check if provider is healthy.
     */
    public boolean isProviderHealthy() {
        LlmProvider provider = getCurrentProvider();
        if (provider == null) return false;

        if (provider instanceof OllamaProvider) {
            return ((OllamaProvider) provider).healthCheck().join();
        } else if (provider instanceof OpenRouterProvider) {
            return ((OpenRouterProvider) provider).hasApiKey();
        }
        return false;
    }

    /**
     * Provider info for UI display.
     */
    public static class ProviderInfo {
        private final ProviderType type;
        private final String name;
        private final String description;
        private final String status;
        private final boolean available;
        private final String currentModel;

        public ProviderInfo(ProviderType type, String name, String description,
                           String status, boolean available, String currentModel) {
            this.type = type;
            this.name = name;
            this.description = description;
            this.status = status;
            this.available = available;
            this.currentModel = currentModel;
        }

        public ProviderType getType() { return type; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public boolean isAvailable() { return available; }
        public String getCurrentModel() { return currentModel; }

        public String toDisplayString() {
            String modelStr = currentModel != null ? " [" + currentModel + "]" : "";
            return String.format("%s: %s (%s)%s",
                name, description, status, modelStr);
        }
    }

    /**
     * Render provider status as ASCII art.
     */
    public String renderProviderStatus() {
        StringBuilder sb = new StringBuilder();
        List<ProviderInfo> providers = getAvailableProviders();

        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    Provider Status                          ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        for (ProviderInfo info : providers) {
            String marker = info.getType() == currentProvider ? "▶" : " ";
            String status = info.isAvailable() ? Terminal.green("●") : Terminal.red("○");
            String model = info.getCurrentModel() != null ? info.getCurrentModel() : "N/A";

            sb.append(String.format("║ %s %-12s %s%-28s %s %-15s ║\n",
                marker,
                info.getName(),
                status,
                truncate(info.getDescription(), 28),
                Terminal.dim("│"),
                truncate(model, 15)));
        }

        sb.append("╚══════════════════════════════════════════════════════════════╝\n");
        sb.append("\nUse /provider <name> to switch (ollama, openrouter)");

        return sb.toString();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 2) + "..";
    }

    // Simple terminal colors
    static class Terminal {
        static String green(String s) { return "\033[32m" + s + "\033[0m"; }
        static String red(String s) { return "\033[31m" + s + "\033[0m"; }
        static String dim(String s) { return "\033[2m" + s + "\033[0m"; }
    }
}
