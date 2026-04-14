package com.clipro.ui.tamboui;

import com.clipro.ui.components.Message;
import com.clipro.ui.components.MessageRole;

import java.util.List;
import java.util.function.Consumer;

/**
 * TuiAdapter - abstraction layer for TUI implementation.
 * Allows swapping between TamboUI and other TUI frameworks.
 * Matches the architecture of openclaude/src/components/FullscreenLayout.tsx
 */
public interface TuiAdapter {

    // ===== Lifecycle =====

    /** Initialize the TUI. Called once at startup. */
    void init() throws Exception;

    /** Shutdown the TUI. Called on exit. */
    void shutdown();

    /** Run the event loop. Blocks until exit. */
    void run() throws Exception;

    /** Request exit from event loop. */
    void quit();

    // ===== Message Management =====

    /**
     * Add a user message to the message list.
     * Matches: openclaude user message type
     */
    void addUserMessage(String content);

    /**
     * Add an assistant (Claude) response.
     * Matches: openclaude assistant message type
     */
    void addAssistantMessage(String content);

    /**
     * Add a system message (tool calls, thinking, events).
     * Matches: openclaude system message type
     */
    void addSystemMessage(String content);

    /**
     * Start streaming an assistant message (tokens appear in real-time).
     * Returns a consumer that accepts tokens as they arrive.
     */
    Consumer<String> startStreamingMessage();

    /**
     * Complete the current streaming message.
     */
    void completeStreamingMessage(String finalContent);

    /**
     * Clear all messages.
     */
    void clearMessages();

    // ===== Prompt Input =====

    /**
     * Get current input text.
     */
    String getInput();

    /**
     * Set input text (for suggestions, etc).
     */
    void setInput(String text);

    /**
     * Set prompt prefix.
     */
    void setPrompt(String prompt);

    /**
     * Set vim mode indicator.
     */
    void setVimMode(String mode);

    // ===== Header/Stats =====

    /**
     * Set connection status (Ollama, OpenRouter, etc).
     */
    void setConnected(boolean connected);

    /**
     * Set current model name.
     */
    void setModel(String modelName);

    /**
     * Get current model name.
     */
    String getModel();

    /**
     * Set status text (Ready, Processing, Streaming, etc).
     */
    void setStatus(String status);

    /**
     * Set token counts (input/output).
     */
    void setTokens(int inputTokens, int outputTokens);

    /**
     * Set latency in milliseconds.
     */
    void setLatency(long ms);

    // ===== Callbacks (set by App) =====

    /**
     * Set callback for user input submission.
     * Called when user presses Enter.
     */
    void onInput(Consumer<String> callback);

    /**
     * Set callback for input changes (for autocomplete, etc).
     */
    void onInputChange(Consumer<String> callback);

    /**
     * Set callback for keyboard shortcuts.
     */
    void onKeyEvent(KeyEventHandler handler);

    // ===== Key Event Handler =====

    interface KeyEventHandler {
        boolean handle(KeyType key, String text);
    }

    enum KeyType {
        ENTER, ESCAPE, BACKSPACE, DELETE,
        ARROW_UP, ARROW_DOWN, ARROW_LEFT, ARROW_RIGHT,
        TAB, CTRL_C, CTRL_L, CTRL_U,
        CHAR, UNKNOWN
    }
}
