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
 * Streaming token callback interface.
 */
interface StreamCallback {
    void onToken(String token);
    void onChunk(ChatCompletionChunk chunk);
}

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
     * Run the agent loop with streaming tokens.
     * Tokens are streamed via onResponse callback for real-time UI updates.
     */
    public CompletableFuture<String> runStreaming(String userMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.user(userMessage));

        return runStreamingLoop(messages, 0, new StringBuilder());
    }

    /**
     * Run the streaming ReAct loop with token-by-token updates.
     */
    private CompletableFuture<String> runStreamingLoop(
            List<Message> messages,
            int iteration,
            StringBuilder fullResponse) {

        if (iteration >= maxIterations) {
            return CompletableFuture.completedFuture(fullResponse.toString());
        }

        ChatCompletionRequest request = new ChatCompletionRequest(
            provider.getCurrentModel(),
            messages
        );
        toolRegistry.getSchemas().forEach(request::addTool);

        StringBuilder currentResponse = new StringBuilder();

        return provider.chatStream(request, chunk -> {
            // Stream each token to UI immediately
            if (chunk != null && chunk.getChoices() != null && chunk.getChoices().length > 0) {
                ChatCompletionChunk.Choice choice = chunk.getChoices()[0];
                String delta = chunk.getDeltaContent();
                if (delta != null && !delta.isEmpty()) {
                    currentResponse.append(delta);
                    fullResponse.append(delta);
                    // Notify UI callback
                    if (onResponse != null) {
                        onResponse.accept(delta);
                    }
                }
            }
        }).thenCompose(v -> {
            // Streaming done, check for tool calls
            String content = currentResponse.toString();
            String reasoning = extractReasoning(content);

            if (onThought != null && reasoning != null) {
                onThought.accept(reasoning);
            }

            // Check for tool calls by making a non-streaming request to parse
            return parseAndExecuteTools(messages, content, iteration, fullResponse);
        }).exceptionally(ex -> {
            return "Error: " + ex.getMessage();
        });
    }

    /**
     * Parse response for tool calls and execute them.
     */
    private CompletableFuture<String> parseAndExecuteTools(
            List<Message> messages,
            String content,
            int iteration,
            StringBuilder fullResponse) {

        // Make a non-streaming request to get structured tool calls
        ChatCompletionRequest request = new ChatCompletionRequest(
            provider.getCurrentModel(),
            messages
        );
        toolRegistry.getSchemas().forEach(request::addTool);

        return provider.chat(request)
            .thenCompose(response -> {
                Message assistantMsg = response.getFirstMessage();
                if (assistantMsg == null) {
                    return CompletableFuture.completedFuture(fullResponse.toString());
                }

                ToolCall[] toolCalls = assistantMsg.getToolCalls();
                if (toolCalls != null && toolCalls.length > 0) {
                    return executeToolCallsStreaming(toolCalls, messages, assistantMsg, iteration, fullResponse);
                }

                return CompletableFuture.completedFuture(fullResponse.toString());
            });
    }

    /**
     * Execute tool calls and continue streaming loop.
     */
    private CompletableFuture<String> executeToolCallsStreaming(
            ToolCall[] toolCalls,
            List<Message> messages,
            Message assistantMsg,
            int iteration,
            StringBuilder fullResponse) {

        List<Message> toolMessages = new ArrayList<>();

        for (ToolCall call : toolCalls) {
            String toolName = call.getFunction().getName();
            String args = call.getFunction().getArguments();

            if (onToolCall != null) {
                onToolCall.accept(toolName + "(" + args + ")");
            }

            Tool tool = toolRegistry.get(toolName);
            String result = tool != null ? tool.execute(parseArgs(args)) : "Error: Unknown tool: " + toolName;

            toolMessages.add(Message.tool(result, call.getId()));
            tokenBudget.addUsage(result.length() / 4);
        }

        messages.add(assistantMsg);
        messages.addAll(toolMessages);

        if (tokenBudget.isOverBudget(DEFAULT_TOKEN_BUDGET)) {
            return CompletableFuture.completedFuture(
                "Token budget exceeded. Consider starting a new conversation."
            );
        }

        // Continue streaming loop
        return runStreamingLoop(messages, iteration + 1, fullResponse);
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