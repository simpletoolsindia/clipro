package com.clipro.agent;

import com.clipro.llm.providers.OllamaProvider;
import com.clipro.llm.providers.OpenRouterProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Model router with fallback chain.
 * Priority: Ollama (local) → OpenRouter (cloud)
 */
public class ModelRouter {

    private final List<ProviderWrapper> providers;
    private int currentProviderIndex;

    public ModelRouter() {
        this.providers = new ArrayList<>();
        this.currentProviderIndex = 0;
    }

    public void addProvider(OllamaProvider provider) {
        providers.add(new ProviderWrapper(provider));
    }

    public void addProvider(OpenRouterProvider provider) {
        providers.add(new ProviderWrapper(provider));
    }

    public void addProvider(ProviderWrapper provider) {
        providers.add(provider);
    }

    public void removeProvider(int index) {
        if (index >= 0 && index < providers.size()) {
            providers.remove(index);
            if (index < currentProviderIndex) {
                currentProviderIndex--;
            }
        }
    }

    public Object getCurrentProvider() {
        if (providers.isEmpty()) return null;
        return providers.get(currentProviderIndex).getProvider();
    }

    public void setCurrentProvider(int index) {
        if (index >= 0 && index < providers.size()) {
            currentProviderIndex = index;
        }
    }

    public int getCurrentProviderIndex() {
        return currentProviderIndex;
    }

    public int getProviderCount() {
        return providers.size();
    }

    /**
     * Get next provider in fallback chain.
     */
    public Object getNextProvider() {
        if (providers.isEmpty()) return null;
        int nextIndex = (currentProviderIndex + 1) % providers.size();
        return providers.get(nextIndex).getProvider();
    }

    /**
     * Fallback to next provider.
     */
    public boolean fallback() {
        if (providers.size() <= 1) return false;
        currentProviderIndex = (currentProviderIndex + 1) % providers.size();
        return true;
    }

    /**
     * Check all providers and find available one.
     */
    public CompletableFuture<Object> findAvailableProvider() {
        List<CompletableFuture<Object>> futures = new ArrayList<>();

        for (ProviderWrapper wrapper : providers) {
            futures.add(wrapper.healthCheck()
                .thenApply(healthy -> healthy ? wrapper.getProvider() : null)
                .exceptionally(ex -> null));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                for (CompletableFuture<Object> f : futures) {
                    Object p = f.join();
                    if (p != null) return p;
                }
                return null;
            });
    }

    /**
     * Wrapper to handle different provider types.
     */
    public static class ProviderWrapper {
        private final Object provider;
        private final CompletableFuture<Boolean> healthFuture;

        public ProviderWrapper(Object provider) {
            this.provider = provider;
            // Try to get healthCheck from OllamaProvider or OpenRouterProvider
            if (provider instanceof com.clipro.llm.providers.OllamaProvider) {
                this.healthFuture = ((com.clipro.llm.providers.OllamaProvider) provider).healthCheck();
            } else if (provider instanceof com.clipro.llm.providers.OpenRouterProvider) {
                this.healthFuture = ((com.clipro.llm.providers.OpenRouterProvider) provider).healthCheck();
            } else {
                this.healthFuture = CompletableFuture.completedFuture(false);
            }
        }

        public Object getProvider() {
            return provider;
        }

        public CompletableFuture<Boolean> healthCheck() {
            return healthFuture;
        }
    }
}