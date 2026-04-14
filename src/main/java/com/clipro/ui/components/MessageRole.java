package com.clipro.ui.components;

public enum MessageRole {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system"),
    TOOL("tool"),
    COMPACT("compact"),         // M-02: Compaction notification
    GROUPED_TOOL_USE("grouped_tool_use"),  // M-13: Grouped tool calls
    COLLAPSED_SEARCH("collapsed_search");   // M-14: Collapsed search results

    private final String value;

    MessageRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
