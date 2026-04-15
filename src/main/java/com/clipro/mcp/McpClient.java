package com.clipro.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * MCP client with real stdio transport layer.
 * H-11: Implements JSON-RPC 2.0 over stdio using ProcessBuilder.
 * Supports tool discovery and execution against MCP servers.
 */
public class McpClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String serverName;
    private final String command;
    private final String[] args;
    private final Map<String, McpTool> tools = new ConcurrentHashMap<>();
    private final List<McpServerConfig> serverConfigs = new ArrayList<>();

    private Process process;
    private BufferedWriter stdin;
    private BufferedReader stdout;
    private volatile boolean connected = false;
    private volatile ConnectionState state = ConnectionState.disconnected();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // H-06: MCP server config loaded from ~/.config/clipro/mcp.json
    public static class McpServerConfig {
        public String name;
        public String command;
        public List<String> args = new ArrayList<>();
        public Map<String, String> env = new HashMap<>();
    }

    public McpClient(String serverName, String command, String[] args) {
        this.serverName = serverName;
        this.command = command;
        this.args = args != null ? args : new String[0];
    }

    public McpClient(String serverName) {
        this(serverName, serverName, new String[0]);
    }

    /**
     * H-06: Load server configs from ~/.config/clipro/mcp.json
     */
    public static List<McpServerConfig> loadConfigs() {
        List<McpServerConfig> configs = new ArrayList<>();
        String home = System.getProperty("user.home");
        java.nio.file.Path configPath = java.nio.file.Paths.get(home, ".config", "clipro", "mcp.json");

        if (!java.nio.file.Files.exists(configPath)) {
            return configs;
        }

        try {
            String json = java.nio.file.Files.readString(configPath);
            JsonNode root = MAPPER.readTree(json);
            JsonNode servers = root.get("mcpServers");
            if (servers != null && servers.isObject()) {
                servers.fields().forEachRemaining(entry -> {
                    McpServerConfig cfg = new McpServerConfig();
                    cfg.name = entry.getKey();
                    JsonNode serverNode = entry.getValue();
                    if (serverNode.has("command")) cfg.command = serverNode.get("command").asText();
                    if (serverNode.has("args")) {
                        serverNode.get("args").forEach(a -> cfg.args.add(a.asText()));
                    }
                    if (serverNode.has("env")) {
                        serverNode.get("env").fields().forEachRemaining(e ->
                            cfg.env.put(e.getKey(), e.getValue().asText()));
                    }
                    configs.add(cfg);
                });
            }
        } catch (Exception e) {
            System.err.println("Failed to load MCP configs: " + e.getMessage());
        }
        return configs;
    }

    /**
     * H-06: Start all configured MCP servers.
     */
    public static List<McpClient> startAllServers() {
        List<McpClient> clients = new ArrayList<>();
        for (McpServerConfig cfg : loadConfigs()) {
            try {
                McpClient client = new McpClient(cfg.name, cfg.command, cfg.args.toArray(new String[0]));
                client.connect().join();
                clients.add(client);
                System.out.println("MCP server started: " + cfg.name + " (" + client.getTools().size() + " tools)");
            } catch (Exception e) {
                System.err.println("Failed to start MCP server " + cfg.name + ": " + e.getMessage());
            }
        }
        return clients;
    }

    /**
     * H-11: Connect via stdio ProcessBuilder.
     */
    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            try {
                state = new ConnectionState("connecting", null);

                ProcessBuilder pb = new ProcessBuilder();
                List<String> cmd = new java.util.ArrayList<>();
                cmd.add(command);
                cmd.addAll(java.util.Arrays.asList(args));
                pb.command(cmd);
                pb.environment().put("NO_COLOR", "1");
                pb.redirectErrorStream(false);

                process = pb.start();
                stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
                stdout = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

                // H-11: Send initialize request
                Map<String, Object> response = sendRequest("initialize", Map.of(
                    "protocolVersion", "2024-11-05",
                    "capabilities", Map.of("tools", Map.of()),
                    "clientInfo", Map.of("name", "clipro", "version", "1.0.0")
                ));

                // H-11: Send initialized notification
                sendNotification("initialized", Map.of());

                state = new ConnectionState("connected", null);
                connected = true;

                // Discover tools
                listTools();

            } catch (Exception e) {
                state = new ConnectionState("error", e.getMessage());
                throw new RuntimeException("Failed to connect to MCP server: " + serverName, e);
            }
        }, executor);
    }

    /**
     * H-11: List available tools from server.
     */
    public CompletableFuture<List<McpTool>> listTools() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> response = sendRequest("tools/list", Map.of());
                JsonNode result = (JsonNode) response.get("result");
                JsonNode toolList = result != null ? result.get("tools") : null;

                tools.clear();
                if (toolList != null && toolList.isArray()) {
                    for (JsonNode toolData : toolList) {
                        McpTool tool = new McpTool(
                            toolData.get("name").asText(),
                            toolData.has("description") ? toolData.get("description").asText() : "",
                            toolData.has("inputSchema") ? MAPPER.convertValue(
                                toolData.get("inputSchema"), Map.class) : null
                        );
                        tools.put(tool.name(), tool);
                    }
                }
                return new ArrayList<>(tools.values());
            } catch (Exception e) {
                throw new RuntimeException("Failed to list tools", e);
            }
        }, executor);
    }

    /**
     * H-11: Call a tool on the MCP server.
     */
    public CompletableFuture<Map<String, Object>> callTool(String toolName, Map<String, Object> arguments) {
        return CompletableFuture.supplyAsync(() -> {
            if (!connected) {
                throw new RuntimeException("Not connected to MCP server: " + serverName);
            }
            Map<String, Object> params = Map.of(
                "name", toolName,
                "arguments", arguments != null ? arguments : Map.of()
            );
            try {
                return sendRequest("tools/call", params);
            } catch (Exception e) {
                throw new RuntimeException("Tool call failed: " + e.getMessage(), e);
            }
        }, executor);
    }

    /**
     * H-11: Send JSON-RPC request over stdio.
     */
    private synchronized Map<String, Object> sendRequest(String method, Map<String, Object> params) throws Exception {
        int id = (int) (System.currentTimeMillis() % 10000);
        Map<String, Object> request = Map.of(
            "jsonrpc", "2.0",
            "id", id,
            "method", method,
            "params", params
        );

        String jsonRequest = MAPPER.writeValueAsString(request) + "\n";
        stdin.write(jsonRequest);
        stdin.flush();

        // Read response
        String line = stdout.readLine();
        while (line != null && line.trim().isEmpty()) {
            line = stdout.readLine();
        }

        if (line == null) {
            throw new RuntimeException("MCP server closed connection");
        }

        // Handle batch responses and notifications
        if (line.startsWith("[")) {
            // Batch - read until end bracket
            StringBuilder sb = new StringBuilder(line);
            int depth = 1;
            while (depth > 0 && (line = stdout.readLine()) != null) {
                sb.append(line);
                for (char c : line.toCharArray()) {
                    if (c == '[') depth++;
                    else if (c == ']') depth--;
                }
            }
            JsonNode batch = MAPPER.readTree(sb.toString());
            for (JsonNode item : batch) {
                if (item.has("id") && item.get("id").asInt() == id) {
                    return MAPPER.convertValue(item, Map.class);
                }
            }
            throw new RuntimeException("No matching response in batch");
        }

        JsonNode response = MAPPER.readTree(line);

        // Skip notifications (no id field)
        if (!response.has("id")) {
            return Map.of("jsonrpc", "2.0");
        }

        if (response.has("error")) {
            JsonNode error = response.get("error");
            throw new RuntimeException("JSON-RPC error: " + error.get("message"));
        }

        return MAPPER.convertValue(response, Map.class);
    }

    /**
     * H-11: Send JSON-RPC notification (no response expected).
     */
    private synchronized void sendNotification(String method, Map<String, Object> params) {
        try {
            Map<String, Object> notification = Map.of(
                "jsonrpc", "2.0",
                "method", method,
                "params", params
            );
            String json = MAPPER.writeValueAsString(notification) + "\n";
            stdin.write(json);
            stdin.flush();
        } catch (Exception e) {
            // Ignore notification errors
        }
    }

    /**
     * Disconnect from MCP server.
     */
    public void disconnect() {
        connected = false;
        state = ConnectionState.disconnected();
        tools.clear();
        try {
            if (stdin != null) stdin.close();
            if (stdout != null) stdout.close();
            if (process != null) process.destroy();
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    public boolean isConnected() { return connected; }
    public String getServerName() { return serverName; }
    public Collection<McpTool> getTools() { return tools.values(); }
    public ConnectionState getState() { return state; }

    public record ConnectionState(String status, String error) {
        public static ConnectionState disconnected() {
            return new ConnectionState("disconnected", null);
        }
    }
}
