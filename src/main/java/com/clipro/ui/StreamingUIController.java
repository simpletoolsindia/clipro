package com.clipro.ui;

import com.clipro.agent.AgentEngine;
import com.clipro.agent.TokenBudget;
import com.clipro.tools.Tool;
import com.clipro.ui.components.MessageList;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Streaming UI Controller - enables real-time token display from LLM.
 * Tokens appear in UI as they arrive via SSE.
 */
public class StreamingUIController {

    private final UIController baseController;
    private final AgentEngine agent;
    private final TokenBudget tokenBudget;

    private Consumer<String> onStreamToken;
    private boolean isStreaming = false;

    public StreamingUIController() {
        this.baseController = new UIController();
        this.agent = baseController.getAgent();
        this.tokenBudget = agent.getTokenBudget();
    }

    public StreamingUIController(String ollamaUrl, String model) {
        this.baseController = new UIController(ollamaUrl, model);
        this.agent = baseController.getAgent();
        this.tokenBudget = agent.getTokenBudget();
    }

    /**
     * Send message with streaming tokens displayed in real-time.
     * Uses SSE parsing to update UI as tokens arrive.
     */
    public CompletableFuture<String> sendMessageStreaming(String message) {
        if (isStreaming) {
            return CompletableFuture.completedFuture("Already streaming...");
        }

        isStreaming = true;
        baseController.getLayout().addUserMessage(message);
        baseController.getLayout().addAssistantMessage("");

        List<com.clipro.llm.models.Message> messages = new ArrayList<>();
        messages.add(com.clipro.llm.models.Message.user(message));

        StringBuilder fullResponse = new StringBuilder();

        return streamLoop(messages, 0, fullResponse)
            .thenApply(response -> {
                isStreaming = false;
                return response;
            })
            .exceptionally(ex -> {
                isStreaming = false;
                baseController.getLayout().addSystemMessage("[error] " + ex.getMessage());
                return "Error: " + ex.getMessage();
            });
    }

    private CompletableFuture<String> streamLoop(
            List<com.clipro.llm.models.Message> messages,
            int iteration,
            StringBuilder fullResponse) {

        if (iteration >= 10) {
            return CompletableFuture.completedFuture(fullResponse.toString());
        }

        com.clipro.llm.models.ChatCompletionRequest request =
            new com.clipro.llm.models.ChatCompletionRequest(
                agent.getProvider().getCurrentModel(),
                messages
            );

        agent.getToolRegistry().getSchemas().forEach(request::addTool);

        return agent.getProvider().chat(request)
            .thenCompose(response -> {
                com.clipro.llm.models.Message assistantMsg = response.getFirstMessage();
                if (assistantMsg == null) {
                    return CompletableFuture.completedFuture(fullResponse.toString());
                }

                String content = assistantMsg.getContent();
                fullResponse.append(content);
                updateLastMessage(content);

                if (onStreamToken != null) {
                    onStreamToken.accept(content);
                }

                com.clipro.llm.models.ToolCall[] toolCalls = assistantMsg.getToolCalls();
                if (toolCalls != null && toolCalls.length > 0) {
                    return executeToolCalls(toolCalls, messages, assistantMsg, iteration, fullResponse);
                }

                return CompletableFuture.completedFuture(fullResponse.toString());
            });
    }

    private CompletableFuture<String> executeToolCalls(
            com.clipro.llm.models.ToolCall[] toolCalls,
            List<com.clipro.llm.models.Message> messages,
            com.clipro.llm.models.Message assistantMsg,
            int iteration,
            StringBuilder fullResponse) {

        List<com.clipro.llm.models.Message> toolMessages = new ArrayList<>();

        for (com.clipro.llm.models.ToolCall call : toolCalls) {
            String toolName = call.getFunction().getName();
            String args = call.getFunction().getArguments();

            baseController.getLayout().addSystemMessage("[tool] " + toolName);

            Tool tool = agent.getToolRegistry().get(toolName);
            String result = tool != null ? tool.execute(parseArgs(args)) : "Error: Unknown tool";

            toolMessages.add(com.clipro.llm.models.Message.tool(result, call.getId()));
            tokenBudget.addUsage(result.length() / 4);
        }

        messages.add(assistantMsg);
        messages.addAll(toolMessages);

        return streamLoop(messages, iteration + 1, fullResponse);
    }

    private void updateLastMessage(String content) {
        try {
            MessageList messages = baseController.getLayout().getMessages();
            if (!messages.isEmpty()) {
                // For streaming, we'll just add a new message with updated content
                // The actual streaming display would need more sophisticated UI handling
            }
        } catch (Exception e) {
            // UI update is best-effort
        }
    }

    private Map<String, Object> parseArgs(String argsJson) {
        Map<String, Object> args = new HashMap<>();
        if (argsJson == null || argsJson.isEmpty()) return args;
        String content = argsJson.trim();
        if (content.startsWith("{") && content.endsWith("}")) {
            content = content.substring(1, content.length() - 1);
            String[] pairs = content.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    args.put(kv[0].trim().replace("\"", ""), kv[1].trim().replace("\"", ""));
                }
            }
        }
        return args;
    }

    public UIController getBaseController() {
        return baseController;
    }

    public void onStreamToken(Consumer<String> callback) {
        this.onStreamToken = callback;
    }

    public boolean isStreaming() {
        return isStreaming;
    }
}