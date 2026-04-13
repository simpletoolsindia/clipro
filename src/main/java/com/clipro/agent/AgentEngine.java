package com.clipro.agent;

import com.clipro.llm.LlmHttpClient;
import com.clipro.llm.models.*;
import com.clipro.llm.providers.OllamaProvider;
import com.clipro.tools.Tool;
import com.clipro.tools.registry.ToolRegistry;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Agent engine with ReAct loop (Reasoning + Acting + Observation).
 */
public class AgentEngine {

    private static final int DEFAULT_MAX_ITERATIONS = 10;
    private static final int DEFAULT_TOKEN_BUDGET = 20000;

    private final OllamaProvider provider;
    private final ToolRegistry toolRegistry;
    private final TokenBudget tokenBudget;

    private int maxIterations;
    private Consumer<String> onThought;
    private Consumer<String> onToolCall;
    private Consumer<String> onResponse;

    public AgentEngine() {
        this.provider = new OllamaProvider();
        this.toolRegistry = new ToolRegistry();
        this.tokenBudget = new TokenBudget();
        this.maxIterations = DEFAULT_MAX_ITERATIONS;
    }

    public AgentEngine(String ollamaUrl, String model) {
        this.provider = new OllamaProvider(ollamaUrl, model);
        this.toolRegistry = new ToolRegistry();
        this.tokenBudget = new TokenBudget();
        this.maxIterations = DEFAULT_MAX_ITERATIONS;
    }

    public void registerTool(Tool tool) {
        toolRegistry.register(tool);
    }

    public void registerTools(List<Tool> tools) {
        tools.forEach(toolRegistry::register);
    }

    public void setMaxIterations(int max) {
        this.maxIterations = max;
    }

    public void onThought(Consumer<String> callback) {
        this.onThought = callback;
    }

    public void onToolCall(Consumer<String> callback) {
        this.onToolCall = callback;
    }

    public void onResponse(Consumer<String> callback) {
        this.onResponse = callback;
    }

    /**
     * Run the agent loop.
     */
    public CompletableFuture<String> run(String userMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.user(userMessage));

        return runLoop(messages, 0);
    }

    /**
     * Run the ReAct loop.
     */
    private CompletableFuture<String> runLoop(List<Message> messages, int iteration) {
        if (iteration >= maxIterations) {
            return CompletableFuture.completedFuture(
                "Max iterations reached. Please provide more context or break down the task."
            );
        }

        // Build request with tools
        ChatCompletionRequest request = new ChatCompletionRequest(
            provider.getCurrentModel(),
            messages
        );

        // Add tools to request
        List<ToolDefinition> schemas = toolRegistry.getSchemas();
        schemas.forEach(request::addTool);

        return provider.chat(request)
            .thenCompose(response -> {
                // Extract response
                Message assistantMsg = response.getFirstMessage();
                String content = assistantMsg != null ? assistantMsg.getContent() : "";
                String reasoning = extractReasoning(content);

                if (onThought != null && reasoning != null) {
                    onThought.accept(reasoning);
                }

                // Check for tool calls
                ToolCall[] toolCalls = assistantMsg != null ? assistantMsg.getToolCalls() : null;

                if (toolCalls != null && toolCalls.length > 0) {
                    // Execute tool calls
                    return executeToolCalls(toolCalls, messages, assistantMsg, iteration);
                } else {
                    // Final response
                    if (onResponse != null) {
                        onResponse.accept(content);
                    }
                    return CompletableFuture.completedFuture(content);
                }
            })
            .exceptionally(ex -> {
                return "Error: " + ex.getMessage();
            });
    }

    /**
     * Execute tool calls and continue the loop.
     */
    private CompletableFuture<String> executeToolCalls(
            ToolCall[] toolCalls,
            List<Message> messages,
            Message assistantMsg,
            int iteration) {

        List<Message> toolMessages = new ArrayList<>();

        for (ToolCall call : toolCalls) {
            String toolName = call.getFunction().getName();
            String args = call.getFunction().getArguments();

            if (onToolCall != null) {
                onToolCall.accept(toolName + "(" + args + ")");
            }

            // Execute tool
            Tool tool = toolRegistry.get(toolName);
            String result;

            if (tool != null) {
                Map<String, Object> argsMap = parseArgs(args);
                result = tool.execute(argsMap);
            } else {
                result = "Error: Unknown tool: " + toolName;
            }

            // Add tool message
            toolMessages.add(Message.tool(result, call.getId()));

            // Track tokens
            tokenBudget.addUsage(result.length() / 4); // Approximate token count
        }

        // Add assistant message and tool results
        messages.add(assistantMsg);
        messages.addAll(toolMessages);

        // Check budget
        if (tokenBudget.isOverBudget(DEFAULT_TOKEN_BUDGET)) {
            return CompletableFuture.completedFuture(
                "Token budget exceeded. Consider starting a new conversation."
            );
        }

        // Continue loop
        return runLoop(messages, iteration + 1);
    }

    private String extractReasoning(String content) {
        if (content == null) return null;
        // Look for <thinking> tags
        int start = content.indexOf("<thinking>");
        int end = content.indexOf("</thinking>");
        if (start >= 0 && end >= 0) {
            return content.substring(start + 10, end).trim();
        }
        return null;
    }

    private Map<String, Object> parseArgs(String argsJson) {
        Map<String, Object> args = new HashMap<>();
        if (argsJson == null || argsJson.isEmpty()) return args;

        // Simple JSON parsing
        String content = argsJson.trim();
        if (content.startsWith("{") && content.endsWith("}")) {
            content = content.substring(1, content.length() - 1);
            String[] pairs = content.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    args.put(key, value);
                }
            }
        }
        return args;
    }

    public OllamaProvider getProvider() {
        return provider;
    }

    public ToolRegistry getToolRegistry() {
        return toolRegistry;
    }

    public TokenBudget getTokenBudget() {
        return tokenBudget;
    }
}