package com.clipro.tools;

import java.util.List;
import java.util.Map;

/**
 * Tool for listing MCP (Model Context Protocol) resources.
 * MCP allows connecting to external tools and data sources.
 */
public class MCPTool implements Tool {

    private final Map<String, MCPResource> resources;

    public MCPTool() {
        this.resources = new java.util.HashMap<>();
    }

    @Override
    public String getName() {
        return "mcp";
    }

    @Override
    public String getDescription() {
        return "Model Context Protocol tool. List, read, and manage MCP resources. Usage: /mcp list | /mcp read <resource>";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "action", Map.of(
                    "type", "string",
                    "description", "Action: list, read, add, remove",
                    "enum", List.of("list", "read", "add", "remove")
                ),
                "resource", Map.of(
                    "type", "string",
                    "description", "Resource name for read/remove"
                ),
                "server", Map.of(
                    "type", "string",
                    "description", "Server name for add"
                )
            ),
            "required", List.of("action")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String action = (String) args.getOrDefault("action", "list");

        switch (action) {
            case "list":
                return listResources();
            case "read":
                String resource = (String) args.get("resource");
                return readResource(resource);
            case "add":
                String server = (String) args.get("server");
                return addServer(server);
            case "remove":
                String name = (String) args.get("resource");
                return removeResource(name);
            default:
                return "Unknown action: " + action + "\nUsage: /mcp list | /mcp read <name> | /mcp add <server>";
        }
    }

    private String listResources() {
        if (resources.isEmpty()) {
            return "MCP Resources: None configured\n\n" +
                   "Configure MCP by creating ~/.clipro/mcp.json\n" +
                   "Example: /mcp add myserver";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("MCP Resources (").append(resources.size()).append("):\n");
        for (MCPResource r : resources.values()) {
            sb.append("  - ").append(r.name).append(": ").append(r.description).append("\n");
        }
        return sb.toString();
    }

    private String readResource(String name) {
        if (name == null || name.isEmpty()) {
            return "Usage: /mcp read <resource-name>";
        }
        MCPResource resource = resources.get(name);
        if (resource == null) {
            return "Resource not found: " + name;
        }
        return resource.toString();
    }

    private String addServer(String server) {
        if (server == null || server.isEmpty()) {
            return "Usage: /mcp add <server-name>\n" +
                   "Example: /mcp add github";
        }
        if (resources.containsKey(server)) {
            return "Server already exists: " + server;
        }
        resources.put(server, new MCPResource(server, "MCP server", "pending"));
        return "MCP server added: " + server;
    }

    private String removeResource(String name) {
        if (name == null || name.isEmpty()) {
            return "Usage: /mcp remove <resource-name>";
        }
        if (resources.remove(name) != null) {
            return "Resource removed: " + name;
        }
        return "Resource not found: " + name;
    }

    public static class MCPResource {
        public final String name;
        public final String description;
        public final String status;
        public final Map<String, String> tools;

        public MCPResource(String name, String description, String status) {
            this.name = name;
            this.description = description;
            this.status = status;
            this.tools = new java.util.HashMap<>();
        }

        @Override
        public String toString() {
            return "MCPResource[" +
                   "name=" + name +
                   ", description=" + description +
                   ", status=" + status +
                   ", tools=" + tools.size() +
                   "]";
        }
    }
}
