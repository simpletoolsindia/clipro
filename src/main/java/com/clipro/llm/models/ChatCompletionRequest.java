package com.clipro.llm.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Chat completion request model for Ollama's OpenAI-compatible API.
 * Reference: OpenAI Chat Completions API format
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionRequest {

    private String model;
    private List<Message> messages;
    private boolean stream;

    // max_tokens for OpenAI-compatible APIs (also used by GitHub Models)
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    // Ollama-specific options
    @JsonProperty("options")
    private Map<String, Object> options;

    // Tool calling support
    @JsonProperty("tools")
    private List<ToolDefinition> tools;

    public ChatCompletionRequest() {
        this.messages = new ArrayList<>();
        this.stream = false;
    }

    public ChatCompletionRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
        this.stream = false;
    }

    // Getters and setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public List<ToolDefinition> getTools() {
        return tools;
    }

    public void setTools(List<ToolDefinition> tools) {
        this.tools = tools;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void addTool(ToolDefinition tool) {
        if (this.tools == null) {
            this.tools = new ArrayList<>();
        }
        this.tools.add(tool);
    }
}
