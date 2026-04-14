package com.clipro.mcp;

import java.util.*;
import java.util.concurrent.*;

/**
 * MCP client wrapper for connecting to MCP servers.
 * Uses JSON-RPC 2.0 protocol for tool discovery and execution.
 *
 * Reference: openclaude/src/services/mcp/client.ts
 */
public class McpClient {

    private final String serverName;
    private final String address;
    private final Map<String, McpTool> tools = new ConcurrentHashMap<>();
    private volatile boolean connected = false;
    private final ConnectionState state = new ConnectionState();

    public McpClient(String serverName, String address) {
        this.serverName = serverName;
        this.address = address;
    }

    /**
     * Connect to MCP server and discover tools.
     */
    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            try {
                // Initialize connection
                state.status = "connecting";

                // Send initialize request
                Map<String, Object> response = sendRequest("initialize", Map.of(
                    "protocolVersion", "2024-11-05",
                    "capabilities", Map.of(
                        "tools", Map.of()
                    ),
                    "clientInfo", Map.of(
                        "name", "clipro",
                        "version", "1.0.0"
                    )
                ));

                state.status = "connected";
                connected = true;

                // Discover tools
                listTools();

            } catch (Exception e) {
                state.status = "error";
                state.error = e.getMessage();
                throw new RuntimeException("Failed to connect to MCP server: " + serverName, e);
            }
        });
    }

    /**
     * List available tools from server.
     */
    public CompletableFuture<List<McpTool>> listTools() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> response = sendRequest("tools/list", Map.of());

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> toolList = (List<Map<String, Object>>) response.get("tools");

                tools.clear();
                for (Map<String, Object> toolData : toolList) {
                    McpTool tool = new McpTool(
                        (String) toolData.get("name"),
                        (String) toolData.get("description"),
                        (Map<String, Object>) toolData.get("inputSchema")
                    );
                    tools.put(tool.name(), tool);
                }

                return new ArrayList<>(tools.values());

            } catch (Exception e) {
                throw new RuntimeException("Failed to list tools", e);
            }
        });
    }

    /**
     * Call a tool on the MCP server.
     */
    public CompletableFuture<Map<String, Object>> callTool(String toolName, Map<String, Object> arguments) {
        return CompletableFuture.supplyAsync(() -> {
            if (!connected) {
                throw new RuntimeException("Not connected to MCP server: " + serverName);
            }

            try {
                Map<String, Object> params = Map.of(
                    "name", toolName,
                    "arguments", arguments != null ? arguments : Map.of()
                );

                return sendRequest("tools/call", params);

            } catch (Exception e) {
                throw new RuntimeException("Failed to call tool: " + toolName, e);
            }
        });
    }

    /**
     * Send JSON-RPC request.
     */
    private Map<String, Object> sendRequest(String method, Map<String, Object> params) throws Exception {
        // Build JSON-RPC request
        int id = (int) (System.currentTimeMillis() % 10000);
        Map<String, Object> request = Map.of(
            "jsonrpc", "2.0",
            "id", id,
            "method", method,
            "params", params
        );

        String jsonRequest = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request);

        // For stdio servers, would use ProcessBuilder
        // For HTTP/SSE servers, would use HttpClient
        // This is a stub - actual implementation depends on transport type

        // Return mock response for now
        return Map.of("jsonrpc", "2.0", "id", id, "result", Map.of());
    }

    /**
     * Disconnect from MCP server.
     */
    public void disconnect() {
        connected = false;
        state.status = "disconnected";
        tools.clear();
    }

    /**
     * Check if connected.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Get server name.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Get address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Get available tools.
     */
    public Collection<McpTool> getTools() {
        return tools.values();
    }

    /**
     * Get connection state.
     */
    public ConnectionState getState() {
        return state;
    }

    /**
     * Connection state record.
     */
    public record ConnectionState() {
        volatile String status = "disconnected";
        volatile String error = null;
    }
}
