package com.clipro.tools.lsp;

import com.clipro.tools.Tool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * H-07: LSP Tool - Language Server Protocol via stdio JSON-RPC.
 * Communicates with language servers (pyright, tsserver, gopls, rust-analyzer)
 * using JSON-RPC 2.0 over stdin/stdout.
 *
 * Supported servers:
 * - pyright (Python): npm install -g pyright
 * - typescript (tsserver): npm install -g typescript
 * - gopls (Go): go install golang.org/x/tools/gopls@latest
 * - rust-analyzer: rustup component add rust-analyzer
 */
public class LSPTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Supported language servers
    public enum Language {
        PYTHON("pyright", "python", "python", "source.python"),
        TYPESCRIPT("tsserver", "typescript", "typescript", "source.ts"),
        JAVASCRIPT("tsserver", "javascript", "javascript", "source.js"),
        GO("gopls", "go", "go", "source.go"),
        RUST("rust-analyzer", "rust", "rust", "source.rust");

        public final String command;
        public final String name;
        public final String langId;
        public final String uriScheme;

        Language(String command, String name, String langId, String uriScheme) {
            this.command = command;
            this.name = name;
            this.langId = langId;
            this.uriScheme = uriScheme;
        }
    }

    private Language language;
    private String workspaceRoot;
    private Process process;
    private BufferedWriter stdin;
    private BufferedReader stdout;
    private int nextId = 1;
    private final Map<Integer, CompletableFuture<Map<String, Object>>> pending = new ConcurrentHashMap<>();
    private boolean initialized = false;

    public LSPTool() {
        this(Language.PYTHON);
    }

    public LSPTool(Language language) {
        this.language = language;
        this.workspaceRoot = System.getProperty("user.dir");
    }

    public LSPTool(String languageId) {
        this.language = fromLanguageId(languageId);
        this.workspaceRoot = System.getProperty("user.dir");
    }

    private Language fromLanguageId(String id) {
        for (Language lang : Language.values()) {
            if (lang.name.equalsIgnoreCase(id) || lang.langId.equalsIgnoreCase(id)) {
                return lang;
            }
        }
        return Language.PYTHON;
    }

    /**
     * H-07: Start the language server process.
     */
    public void start() throws Exception {
        if (process != null && process.isAlive()) return;

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(language.command);
        pb.directory(Paths.get(workspaceRoot).toFile());
        pb.environment().put("NO_COLOR", "1");

        process = pb.start();
        stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        stdout = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

        // Start reader thread for responses
        new Thread(this::readResponses, "lsp-reader").start();

        // H-07: Initialize
        initialize();
    }

    private void initialize() throws Exception {
        Map<String, Object> params = Map.of(
            "processId", ProcessHandle.current().pid(),
            "rootUri", toUri(workspaceRoot),
            "rootPath", workspaceRoot,
            "capabilities", Map.of(
                "textDocument", Map.of(
                    "synchronization", Map.of("willSave", true, "willSaveWaitUntil", true),
                    "hover", Map.of("dynamicRegistration", true),
                    "completion", Map.of("dynamicRegistration", true),
                    "definition", Map.of("dynamicRegistration", true),
                    "references", Map.of("dynamicRegistration", true),
                    "documentSymbol", Map.of("dynamicRegistration", true)
                ),
                "workspace", Map.of("applyEdit", true)
            ),
            "workspaceFolders", List.of(Map.of(
                "uri", toUri(workspaceRoot),
                "name", Paths.get(workspaceRoot).getFileName().toString()
            ))
        );

        sendRequest("initialize", params).get();
        initialized = true;

        // Send initialized notification
        sendNotification("initialized", Map.of());

        // Send didOpen notification for initial file
        sendNotification("textDocument/didOpen", Map.of(
            "textDocument", Map.of(
                "uri", toUri(workspaceRoot + "/.lsp_initial"),
                "languageId", language.langId,
                "version", 1,
                "text", ""
            )
        ));
    }

    /**
     * H-07: Open a document in the language server.
     */
    public void openDocument(String filePath, String content) throws Exception {
        Map<String, Object> params = Map.of(
            "textDocument", Map.of(
                "uri", toUri(filePath),
                "languageId", language.langId,
                "version", 1,
                "text", content
            )
        );
        sendNotification("textDocument/didOpen", params);
    }

    /**
     * H-07: Go to definition at cursor position.
     */
    public CompletableFuture<List<DefinitionResult>> gotoDefinition(String filePath, int line, int col) {
        return sendRequest("textDocument/definition", Map.of(
            "textDocument", Map.of("uri", toUri(filePath)),
            "position", Map.of("line", line, "character", col)
        )).thenApply(response -> {
            List<DefinitionResult> results = new ArrayList<>();
            Object result = response.get("result");
            if (result instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) result;
                for (Map<String, Object> item : items) {
                    String uri = item.containsKey("uri") ? item.get("uri").toString() : "";
                    Map<String, Object> pos = item.containsKey("range") ?
                        (Map<String, Object>) ((Map<String, Object>) item.get("range")).get("start") :
                        Map.of("line", 0, "character", 0);
                    int l = pos.containsKey("line") ? ((Number) pos.get("line")).intValue() : 0;
                    int c = pos.containsKey("character") ? ((Number) pos.get("character")).intValue() : 0;
                    results.add(new DefinitionResult(fromUri(uri), l, c));
                }
            }
            return results;
        }).exceptionally(ex -> List.of());
    }

    /**
     * H-07: Find all references at cursor position.
     */
    public CompletableFuture<List<ReferenceResult>> findReferences(String filePath, int line, int col) {
        return sendRequest("textDocument/references", Map.of(
            "textDocument", Map.of("uri", toUri(filePath)),
            "position", Map.of("line", line, "character", col),
            "context", Map.of("includeDeclaration", true)
        )).thenApply(response -> {
            List<ReferenceResult> results = new ArrayList<>();
            Object result = response.get("result");
            if (result instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) result;
                for (Map<String, Object> item : items) {
                    String uri = item.containsKey("uri") ? item.get("uri").toString() : "";
                    Map<String, Object> pos = item.containsKey("range") ?
                        (Map<String, Object>) ((Map<String, Object>) item.get("range")).get("start") :
                        Map.of("line", 0, "character", 0);
                    int l = pos.containsKey("line") ? ((Number) pos.get("line")).intValue() : 0;
                    int c = pos.containsKey("character") ? ((Number) pos.get("character")).intValue() : 0;
                    results.add(new ReferenceResult(fromUri(uri), l, c));
                }
            }
            return results;
        }).exceptionally(ex -> List.of());
    }

    /**
     * H-07: Get hover information at cursor position.
     */
    public CompletableFuture<String> hover(String filePath, int line, int col) {
        return sendRequest("textDocument/hover", Map.of(
            "textDocument", Map.of("uri", toUri(filePath)),
            "position", Map.of("line", line, "character", col)
        )).thenApply(response -> {
            Object result = response.get("result");
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> hover = (Map<String, Object>) result;
                Object contents = hover.get("contents");
                if (contents instanceof Map) {
                    return ((Map<String, Object>) contents).get("value").toString();
                } else if (contents instanceof String) {
                    return contents.toString();
                }
            }
            return "No hover information available.";
        }).exceptionally(ex -> "Error: " + ex.getMessage());
    }

    /**
     * H-07: Get code completions at cursor position.
     */
    public CompletableFuture<List<CompletionItem>> completions(String filePath, int line, int col) {
        return sendRequest("textDocument/completion", Map.of(
            "textDocument", Map.of("uri", toUri(filePath)),
            "position", Map.of("line", line, "character", col)
        )).thenApply(response -> {
            List<CompletionItem> results = new ArrayList<>();
            Object result = response.get("result");
            if (result instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) result;
                for (Map<String, Object> item : items) {
                    String label = item.containsKey("label") ? item.get("label").toString() : "";
                    String insert = item.containsKey("insertText") ? item.get("insertText").toString() : label;
                    String kind = item.containsKey("kind") ? item.get("kind").toString() : "";
                    results.add(new CompletionItem(label, insert, kind));
                }
            }
            return results;
        }).exceptionally(ex -> List.of());
    }

    // JSON-RPC helpers
    private synchronized CompletableFuture<Map<String, Object>> sendRequest(String method, Map<String, Object> params) {
        int id = nextId++;
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        pending.put(id, future);

        try {
            Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "id", id,
                "method", method,
                "params", params
            );
            stdin.write(MAPPER.writeValueAsString(request));
            stdin.write("\n");
            stdin.flush();
        } catch (Exception e) {
            pending.remove(id);
            future.completeExceptionally(e);
        }
        return future;
    }

    private void sendNotification(String method, Map<String, Object> params) {
        try {
            Map<String, Object> notification = Map.of(
                "jsonrpc", "2.0",
                "method", method,
                "params", params
            );
            stdin.write(MAPPER.writeValueAsString(notification));
            stdin.write("\n");
            stdin.flush();
        } catch (Exception ignored) {}
    }

    private void readResponses() {
        try {
            String line;
            while ((line = stdout.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    JsonNode node = MAPPER.readTree(line);
                    if (node.has("id")) {
                        int id = node.get("id").asInt();
                        CompletableFuture<Map<String, Object>> future = pending.remove(id);
                        if (future != null) {
                            if (node.has("result")) {
                                future.complete(MAPPER.convertValue(node.get("result"), Map.class));
                            } else if (node.has("error")) {
                                future.completeExceptionally(new RuntimeException(
                                    "LSP error: " + node.get("error")));
                            }
                        }
                    }
                    // Handle $/progress notifications
                    // Handle window/logMessage notifications
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    // URI helpers
    private String toUri(String path) {
        try {
            return Paths.get(path).toUri().toString();
        } catch (Exception e) {
            return "file://" + path;
        }
    }

    private String fromUri(String uri) {
        try {
            return Paths.get(new java.net.URI(uri)).toString();
        } catch (Exception e) {
            return uri;
        }
    }

    public void stop() {
        try {
            sendNotification("shutdown", Map.of());
            if (process != null) process.destroy();
        } catch (Exception ignored) {}
    }

    public void setWorkspaceRoot(String path) {
        this.workspaceRoot = path;
    }

    // Tool interface
    @Override public String getName() { return "lsp"; }
    @Override public String getDescription() { return "LSP: goto-definition, find-references, hover, completions. Languages: " + Arrays.toString(Language.values()); }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "action", Map.of("type", "string", "description", "Action: goto-definition, find-references, hover, completions"),
                "file", Map.of("type", "string", "description", "File path"),
                "line", Map.of("type", "integer", "description", "Line number (0-indexed)"),
                "col", Map.of("type", "integer", "description", "Column number (0-indexed)"),
                "language", Map.of("type", "string", "description", "Language: python, typescript, go, rust")
            ),
            "required", List.of("action", "file")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String action = (String) args.get("action");
        String file = (String) args.get("file");
        int line = args.containsKey("line") ? ((Number) args.get("line")).intValue() : 0;
        int col = args.containsKey("col") ? ((Number) args.get("col")).intValue() : 0;

        if (action == null || file == null) {
            return "Error: action and file are required";
        }

        try {
            if (!initialized) start();

            switch (action) {
                case "goto-definition" -> {
                    var results = gotoDefinition(file, line, col).get();
                    if (results.isEmpty()) return "No definition found.";
                    StringBuilder sb = new StringBuilder("Definitions:\n");
                    for (var r : results) {
                        sb.append("  → ").append(r.file).append(":").append(r.line + 1).append(":").append(r.col + 1).append("\n");
                    }
                    return sb.toString();
                }
                case "find-references" -> {
                    var results = findReferences(file, line, col).get();
                    if (results.isEmpty()) return "No references found.";
                    StringBuilder sb = new StringBuilder("References (").append(results.size()).append("):\n");
                    for (var r : results) {
                        sb.append("  → ").append(r.file).append(":").append(r.line + 1).append(":").append(r.col + 1).append("\n");
                    }
                    return sb.toString();
                }
                case "hover" -> {
                    return hover(file, line, col).get();
                }
                case "completions" -> {
                    var results = completions(file, line, col).get();
                    if (results.isEmpty()) return "No completions available.";
                    StringBuilder sb = new StringBuilder("Completions (").append(results.size()).append("):\n");
                    for (var r : results) {
                        sb.append("  • ").append(r.label);
                        if (!r.label.equals(r.insertText)) sb.append(" → ").append(r.insertText);
                        sb.append(" (").append(r.kind).append(")\n");
                    }
                    return sb.toString();
                }
                default -> { return "Unknown action: " + action + ". Use: goto-definition, find-references, hover, completions"; }
            }
        } catch (Exception e) {
            return "LSP error: " + e.getMessage();
        }
    }

    public record DefinitionResult(String file, int line, int col) {}
    public record ReferenceResult(String file, int line, int col) {}
    public record CompletionItem(String label, String insertText, String kind) {}
}
