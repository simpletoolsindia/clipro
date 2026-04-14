package com.clipro.llm.providers;

import com.clipro.llm.models.ChatCompletionRequest;
import com.clipro.llm.models.ChatCompletionResponse;
import com.clipro.llm.models.ChatCompletionChunk;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Interface for LLM providers.
 * All providers (Ollama, OpenRouter, OpenAI, etc.) implement this interface.
 */
public interface LlmProvider {

    /**
     * Send a chat completion request.
     */
    CompletableFuture<ChatCompletionResponse> chat(ChatCompletionRequest request);

    /**
     * Send a streaming chat completion request.
     */
    CompletableFuture<Void> chatStream(ChatCompletionRequest request, Consumer<ChatCompletionChunk> onChunk);

    /**
     * Get the current model name.
     */
    String getCurrentModel();

    /**
     * Set the current model.
     */
    void setCurrentModel(String model);
}
