package com.clipro.llm;

import java.util.List;

/**
 * Chat completion response model.
 */
public class ChatResponse {
    private String id;
    private String model;
    private String content;
    private String finishReason;
    private int inputTokens;
    private int outputTokens;
    private List<ToolCall> toolCalls;
    private boolean streaming;
    private String error;

    public ChatResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getFinishReason() { return finishReason; }
    public void setFinishReason(String finishReason) { this.finishReason = finishReason; }

    public int getInputTokens() { return inputTokens; }
    public void setInputTokens(int inputTokens) { this.inputTokens = inputTokens; }

    public int getOutputTokens() { return outputTokens; }
    public void setOutputTokens(int outputTokens) { this.outputTokens = outputTokens; }

    public List<ToolCall> getToolCalls() { return toolCalls; }
    public void setToolCalls(List<ToolCall> toolCalls) { this.toolCalls = toolCalls; }

    public boolean isStreaming() { return streaming; }
    public void setStreaming(boolean streaming) { this.streaming = streaming; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public boolean hasError() { return error != null && !error.isEmpty(); }

    public boolean hasToolCalls() { return toolCalls != null && !toolCalls.isEmpty(); }

    public static class ToolCall {
        private String id;
        private String name;
        private String arguments;

        public ToolCall() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getArguments() { return arguments; }
        public void setArguments(String arguments) { this.arguments = arguments; }
    }
}
