package com.clipro;

import com.clipro.agent.AgentEngine;
import com.clipro.cli.CommandRegistry;
import com.clipro.llm.providers.OllamaProvider;
import com.clipro.llm.providers.ProviderManager;
import com.clipro.logging.Logger;
import com.clipro.mcp.McpClient;
import com.clipro.tools.Tool;
import com.clipro.ui.tamboui.TamboUIAdapter;
import com.clipro.ui.tamboui.TuiAdapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * CLIPRO - Java AI Coding CLI
 * Pixel-perfect OpenClaude UI with TamboUI + LOCAL-FIRST Ollama
 */
public class App {

    private static final Logger LOG = new Logger("App");

    private TuiAdapter ui;
    private final CommandRegistry commands;
    private final CommandRegistry.CommandContext commandContext;
    private final ProviderManager providerManager;
    private boolean running = true;
    private CompletableFuture<Void> pendingRequest;

    // H-10: Agent engine and MCP clients
    private AgentEngine agentEngine;
    private List<McpClient> mcpClients;

    public App() {
        LOG.info("Initializing CLIPRO...");
        this.ui = new TamboUIAdapter();
        this.commands = new CommandRegistry();
        this.commandContext = new CommandRegistry.CommandContext();
        this.providerManager = new ProviderManager();
        commandContext.setAgentContext(new AppAgentContext());
        LOG.info("CLIPRO initialized with TamboUI");
    }

    public void run() {
        LOG.info("Starting CLIPRO...");
        try {
            // Initialize TUI
            ui.init();

            // Set up input callback
            ui.onInput(this::processInput);

            // Set up input change callback (for autocomplete, etc)
            ui.onInputChange(this::onInputChange);

            // Check connection and update UI
            checkConnection();

            // Run the TUI event loop
            ui.run();

        } catch (Exception e) {
            LOG.error("Error running CLIPRO: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ui.shutdown();
        }
        LOG.info("CLIPRO exited");
    }

    private void processInput(String input) {
        if (input == null || input.trim().isEmpty()) return;

        // Check for commands first
        String cmdResult = commands.execute(input, commandContext);
        if (cmdResult != null) {
            ui.addSystemMessage(cmdResult);
            return;
        }

        // H-10: It's a user message - send to AgentEngine
        ui.addUserMessage(input);
        ui.setStatus("Thinking...");

        // Get or create agent engine
        if (agentEngine == null) {
            agentEngine = createAgentEngine();
        }

        // Start streaming message
        java.util.function.Consumer<String> onToken = ui.startStreamingMessage();

        // Set up callbacks
        agentEngine.onResponse(token -> {
            // Stream token to UI immediately
            onToken.accept(token);
        });
        agentEngine.onThought(thought -> {
            ui.addSystemMessage("💭 " + thought);
        });
        agentEngine.onToolCall(call -> {
            ui.addSystemMessage("🔧 " + call);
        });

        pendingRequest = agentEngine.run(input).thenAccept(response -> {
            ui.completeStreamingMessage(response);
            ui.setStatus("Ready");

            // Update token stats
            if (agentEngine.getTokenBudget() != null) {
                int inputTokens = agentEngine.getTokenBudget().getPromptTokens();
                int outputTokens = agentEngine.getTokenBudget().getCompletionTokens();
                ui.setTokens(inputTokens, outputTokens);
            }
        }).exceptionally(ex -> {
            ui.completeStreamingMessage("Error: " + ex.getMessage());
            ui.setStatus("Ready");
            return null;
        });
    }

    /**
     * H-10: Create and configure the agent engine with all tools.
     */
    private AgentEngine createAgentEngine() {
        AgentEngine engine = new AgentEngine(
            providerManager.getCurrentProvider().getCurrentModel()
        );

        // Register all native tools
        engine.registerTools(getNativeTools());

        // Register MCP tools if servers are running
        if (mcpClients != null) {
            for (var client : mcpClients) {
                for (var tool : client.getTools()) {
                    // Wrap MCP tool
                    engine.registerTool(new Tool() {
                        @Override public String getName() { return "mcp_" + client.getServerName() + "_" + tool.name(); }
                        @Override public String getDescription() { return tool.description(); }
                        @Override public Object getParameters() { return tool.inputSchema() != null ? tool.inputSchema() : Map.of(); }
                        @Override
                        public String execute(Map<String, Object> args) {
                            try {
                                var result = client.callTool(tool.name(), args).get();
                                return result.toString();
                            } catch (Exception e) {
                                return "Error: " + e.getMessage();
                            }
                        }
                    });
                }
            }
        }

        return engine;
    }

    /**
     * H-10: Get all native tools for the agent.
     */
    private List<Tool> getNativeTools() {
        List<Tool> tools = new java.util.ArrayList<>();
        // File tools
        tools.add(new com.clipro.tools.file.FileReadTool());
        tools.add(new com.clipro.tools.file.FileWriteTool());
        tools.add(new com.clipro.tools.file.FileEditTool());
        tools.add(new com.clipro.tools.file.GlobTool());
        tools.add(new com.clipro.tools.file.GrepTool());
        // Git tools
        tools.add(new com.clipro.tools.git.GitStatusTool());
        tools.add(new com.clipro.tools.git.GitDiffTool());
        tools.add(new com.clipro.tools.git.GitLogTool());
        tools.add(new com.clipro.tools.git.GitCommitTool());
        // Shell
        tools.add(new com.clipro.tools.shell.BashTool());
        // Web
        tools.add(new com.clipro.tools.web.WebSearchTool());
        tools.add(new com.clipro.tools.web.WebFetchTool());
        // Task (H-02: Individual task tools)
        tools.add(new com.clipro.tools.TaskTool());
        tools.add(new com.clipro.tools.TaskCreateTool());
        tools.add(new com.clipro.tools.TaskGetTool());
        tools.add(new com.clipro.tools.TaskListTool());
        tools.add(new com.clipro.tools.TaskUpdateTool());
        tools.add(new com.clipro.tools.TaskStopTool());
        // Agent (H-01: Sub-agent spawning)
        tools.add(new com.clipro.tools.AgentTool());
        // Skill
        try { tools.add(new com.clipro.tools.skill.SkillTool()); } catch (Exception ignored) {}
        // LSP
        try { tools.add(new com.clipro.tools.lsp.LSPTool()); } catch (Exception ignored) {}
        return tools;
    }

    private void onInputChange(String input) {
        // Handle autocomplete, input validation, etc.
        // This could be used to show command suggestions
        if (input.startsWith("/")) {
            // Slash command mode
            ui.addSystemMessage("Commands: /clear, /model, /exit");
        }
    }

    /**
     * H-12: Check connection to LLM provider and update UI.
     * Performs async health check against Ollama to determine actual network status.
     */
    private void checkConnection() {
        ui.setStatus("Checking connection...");
        ui.setConnected(false); // Assume disconnected until proven

        CompletableFuture.supplyAsync(() -> {
            try {
                // H-12: Use OllamaProvider healthCheck() to verify connectivity
                OllamaProvider ollama = (OllamaProvider) providerManager.getCurrentProvider();
                if (ollama != null) {
                    return ollama.healthCheck().get();
                }
                return false;
            } catch (Exception e) {
                LOG.debug("Health check failed: " + e.getMessage());
                return false;
            }
        }).thenAccept(connected -> {
            String model = providerManager.getCurrentModel();
            ui.setConnected(connected);
            ui.setModel(model != null ? model : "qwen3-coder:32b");
            ui.setStatus(connected ? "Ready" : "Disconnected");

            if (connected) {
                LOG.info("Connected to " + providerManager.getCurrentProviderType().getName()
                    + " with model " + model);
            } else {
                LOG.warn("Could not connect to Ollama. Is it running?");
                ui.addSystemMessage("⚠ Could not connect to Ollama at http://localhost:11434\n"
                    + "  Make sure Ollama is installed and running:\n"
                    + "  → curl -fsSL https://ollama.com/install.sh | sh\n"
                    + "  → ollama pull qwen3-coder:32b");
            }
        });
    }

    public void stop() {
        running = false;
        LOG.info("Shutting down CLIPRO...");
        ui.quit();
        if (pendingRequest != null) {
            pendingRequest.cancel(true);
        }
    }

    private class AppAgentContext implements CommandRegistry.AgentContext {
        @Override
        public String getCurrentModel() {
            return ui.getModel();
        }
        @Override
        public void clearHistory() {
            ui.clearMessages();
        }
        @Override
        public void exit() {
            stop();
        }
    }

    // Helper to get model from adapter
    private String getModel() {
        // This requires exposing it from TuiAdapter
        return "qwen3-coder:32b";
    }

    public static void main(String[] args) {
        LOG.info("CLIPRO v0.2.0-TAMBOUI");
        LOG.info("Starting...");
        App app = new App();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook triggered");
            app.stop();
        }));

        app.run();
    }
}
