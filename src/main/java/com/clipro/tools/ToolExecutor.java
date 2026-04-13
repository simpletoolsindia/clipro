package com.clipro.tools;

import com.clipro.llm.models.ChatCompletionRequest;
import com.clipro.llm.models.ChatCompletionResponse;
import com.clipro.llm.models.Message;
import com.clipro.llm.models.ToolCall;
import com.clipro.llm.models.ToolDefinition;
import com.clipro.llm.providers.OllamaProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Executes tool calls from LLM responses.
 */
public class ToolExecutor {

    private final Map<String, Tool> tools;
    private final OllamaProvider provider;

    public ToolExecutor(OllamaProvider provider) {
        this.tools = new HashMap<>();
        this.provider = provider;
    }

    public void registerTool(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    public void registerTools(List<Tool> toolList) {
        for (Tool tool : toolList) {
            registerTool(tool);
        }
    }

    public void unregisterTool(String name) {
        tools.remove(name);
    }

    public Set<String> getToolNames() {
        return tools.keySet();
    }

    /**
     * Add tool definitions to a request for tool calling.
     */
    public void addToolsToRequest(ChatCompletionRequest request) {
        for (Tool tool : tools.values()) {
            ToolDefinition def = new ToolDefinition(
                tool.getName(),
                tool.getDescription(),
                tool.getParameters()
            );
            request.addTool(def);
        }
    }

    /**
     * Check if a response contains tool calls.
     */
    public boolean hasToolCalls(ChatCompletionResponse response) {
        if (response.getChoices() == null || response.getChoices().isEmpty()) {
            return false;
        }
        Message msg = response.getChoices().get(0).getMessage();
        return msg != null && msg.getToolCalls() != null && msg.getToolCalls().length > 0;
    }

    /**
     * Extract tool calls from a response.
     */
    public ToolCall[] extractToolCalls(ChatCompletionResponse response) {
        if (response.getChoices() == null || response.getChoices().isEmpty()) {
            return new ToolCall[0];
        }
        Message msg = response.getChoices().get(0).getMessage();
        if (msg == null || msg.getToolCalls() == null) {
            return new ToolCall[0];
        }
        return msg.getToolCalls();
    }

    /**
     * Execute a tool call.
     */
    public String executeTool(ToolCall toolCall) {
        Tool tool = tools.get(toolCall.getFunction().getName());
        if (tool == null) {
            return "Error: Unknown tool: " + toolCall.getFunction().getName();
        }

        try {
            Map<String, Object> args = parseArgs(toolCall.getFunction().getArguments());
            return tool.execute(args);
        } catch (Exception e) {
            return "Error executing tool: " + e.getMessage();
        }
    }

    /**
     * Execute all tool calls from a response and return tool messages.
     */
    public List<Message> executeToolCalls(ChatCompletionResponse response) {
        List<Message> toolMessages = new ArrayList<>();
        ToolCall[] calls = extractToolCalls(response);

        for (ToolCall call : calls) {
            String result = executeTool(call);
            toolMessages.add(Message.tool(result, call.getId()));
        }

        return toolMessages;
    }

    private Map<String, Object> parseArgs(String argsJson) {
        if (argsJson == null || argsJson.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            // Simple JSON parsing - in production use ObjectMapper
            Map<String, Object> args = new HashMap<>();
            argsJson = argsJson.trim();
            if (argsJson.startsWith("{") && argsJson.endsWith("}")) {
                String content = argsJson.substring(1, argsJson.length() - 1);
                // Basic parsing for key-value pairs
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
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public Tool getTool(String name) {
        return tools.get(name);
    }
}