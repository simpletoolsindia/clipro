package com.clipro.mcp;

import java.util.Map;

/**
 * MCP tool definition.
 */
public class McpTool {
    private final String name;
    private final String description;
    private final Map<String, Object> inputSchema;

    public McpTool(String name, String description, Map<String, Object> inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }

    public String name() { return name; }
    public String description() { return description; }
    public Map<String, Object> inputSchema() { return inputSchema; }

    public String toDisplayString() {
        return name + " - " + description;
    }
}
