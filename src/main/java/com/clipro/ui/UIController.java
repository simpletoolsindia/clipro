package com.clipro.ui;

import com.clipro.agent.AgentEngine;
import com.clipro.agent.TokenBudget;
import com.clipro.llm.providers.OllamaProvider;
import com.clipro.tools.Tool;
import com.clipro.tools.file.*;
import com.clipro.tools.git.*;
import com.clipro.tools.shell.BashTool;
import com.clipro.tools.web.*;
import com.clipro.ui.components.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * UI Controller - connects AgentEngine to FullscreenLayout.
 * Handles message flow: UI -> Agent -> LLM -> Tools -> UI
 */
public class UIController {

    private final FullscreenLayout layout;
    private final AgentEngine agent;
    private final TokenBudget tokenBudget;

    private Consumer<String> onTokenUpdate;
    private Consumer<String> onStatusUpdate;

    public UIController() {
        this.layout = new FullscreenLayout();
        this.agent = new AgentEngine();
        this.tokenBudget = agent.getTokenBudget();
        setupCallbacks();
        registerTools();
    }

    public UIController(String ollamaUrl, String model) {
        this.layout = new FullscreenLayout(model);
        this.agent = new AgentEngine(ollamaUrl, model);
        this.tokenBudget = agent.getTokenBudget();
        setupCallbacks();
        registerTools();
    }

    private void setupCallbacks() {
        // Agent callbacks -> UI updates
        agent.onThought(thought -> {
            layout.addSystemMessage("[thinking] " + truncate(thought, 100));
            if (onStatusUpdate != null) onStatusUpdate.accept("Thinking...");
        });

        agent.onToolCall(call -> {
            layout.addSystemMessage("[tool] " + call);
            if (onStatusUpdate != null) onStatusUpdate.accept("Executing: " + extractToolName(call));
        });

        agent.onResponse(response -> {
            if (onStatusUpdate != null) onStatusUpdate.accept("Ready");
        });
    }

    private void registerTools() {
        // Register all native tools
        agent.registerTool(new FileReadTool());
        agent.registerTool(new FileWriteTool());
        agent.registerTool(new FileEditTool());
        agent.registerTool(new GlobTool());
        agent.registerTool(new GrepTool());
        agent.registerTool(new BashTool());
        agent.registerTool(new WebSearchTool());
        agent.registerTool(new WebFetchTool());
        agent.registerTool(new QuickFetchTool());
        agent.registerTool(new GitStatusTool());
        agent.registerTool(new GitDiffTool());
        agent.registerTool(new GitLogTool());
        agent.registerTool(new GitCommitTool());
    }

    /**
     * Send user message and get response.
     */
    public CompletableFuture<String> sendMessage(String message) {
        // Add user message to UI
        layout.addUserMessage(message);
        if (onStatusUpdate != null) onStatusUpdate.accept("Processing...");

        // Run agent
        return agent.run(message)
            .thenApply(response -> {
                layout.addAssistantMessage(response);
                updateTokenCount();
                if (onStatusUpdate != null) onStatusUpdate.accept("Ready");
                return response;
            })
            .exceptionally(ex -> {
                layout.addSystemMessage("[error] " + ex.getMessage());
                if (onStatusUpdate != null) onStatusUpdate.accept("Error");
                return "Error: " + ex.getMessage();
            });
    }

    /**
     * Set model.
     */
    public void setModel(String model) {
        agent.getProvider().setCurrentModel(model);
        layout.setModel(model);
    }

    /**
     * Get current model.
     */
    public String getModel() {
        return agent.getProvider().getCurrentModel();
    }

    /**
     * Get layout for rendering.
     */
    public FullscreenLayout getLayout() {
        return layout;
    }

    /**
     * Get AgentEngine for advanced operations.
     */
    public AgentEngine getAgent() {
        return agent;
    }

    /**
     * Render current state.
     */
    public String render() {
        return layout.render();
    }

    /**
     * Render minimal (just input area).
     */
    public String renderMinimal() {
        return layout.renderMinimal();
    }

    /**
     * Clear all messages.
     */
    public void clear() {
        layout.clearAll();
        tokenBudget.reset();
    }

    /**
     * Check if Ollama is connected.
     */
    public CompletableFuture<Boolean> checkConnection() {
        return agent.getProvider().healthCheck()
            .thenApply(connected -> {
                layout.setConnected(connected);
                return connected;
            });
    }

    /**
     * Get token usage info.
     */
    public String getTokenInfo() {
        return "Tokens: " + tokenBudget.getTotalTokens() + "/" + tokenBudget.getMaxTokens();
    }

    // Getter for max tokens (need to add to TokenBudget)
    public int getMaxTokens() {
        return tokenBudget.getMaxTokens();
    }

    public int getUsedTokens() {
        return tokenBudget.getTotalTokens();
    }

    private void updateTokenCount() {
        if (onTokenUpdate != null) {
            onTokenUpdate.accept(tokenBudget.getTotalTokens() + "/" + tokenBudget.getMaxTokens());
        }
        // TokenBudget uses single number (total), StatusBar uses input/output split
        // Approximate: 40% input, 60% output
        int total = tokenBudget.getTotalTokens();
        layout.getStatus().setTokens(total / 2, total - total / 2);
    }

    public void onTokenUpdate(Consumer<String> callback) {
        this.onTokenUpdate = callback;
    }

    public void onStatusUpdate(Consumer<String> callback) {
        this.onStatusUpdate = callback;
    }

    // Utility methods
    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private String extractToolName(String toolCall) {
        int idx = toolCall.indexOf('(');
        return idx > 0 ? toolCall.substring(0, idx) : toolCall;
    }
}