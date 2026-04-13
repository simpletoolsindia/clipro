package com.clipro.llm.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Chat message model.
 * Roles: system, user, assistant, tool
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {

    private String role;
    private String content;

    @JsonProperty("tool_calls")
    private ToolCall[] toolCalls;

    @JsonProperty("tool_call_id")
    private String toolCallId;

    @JsonProperty("name")
    private String name;

    public Message() {}

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // Static factory methods
    public static Message system(String content) {
        return new Message("system", content);
    }

    public static Message user(String content) {
        return new Message("user", content);
    }

    public static Message assistant(String content) {
        return new Message("assistant", content);
    }

    public static Message tool(String content, String toolCallId) {
        Message msg = new Message("tool", content);
        msg.setToolCallId(toolCallId);
        return msg;
    }

    // Getters and setters
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ToolCall[] getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(ToolCall[] toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
