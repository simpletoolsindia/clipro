package com.clipro;

import com.clipro.cli.CommandRegistry;
import com.clipro.logging.Logger;
import com.clipro.ui.tamboui.TamboUIAdapter;
import com.clipro.ui.tamboui.TuiAdapter;

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
    private boolean running = true;
    private CompletableFuture<Void> pendingRequest;

    public App() {
        LOG.info("Initializing CLIPRO...");
        this.ui = new TamboUIAdapter();
        this.commands = new CommandRegistry();
        this.commandContext = new CommandRegistry.CommandContext();
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

        // It's a user message - send to agent
        ui.addUserMessage(input);
        ui.setStatus("Processing...");

        pendingRequest = CompletableFuture.runAsync(() -> {
            try {
                // This would normally call the agent
                // For now, simulate a response
                Thread.sleep(500); // Simulate processing
                ui.addAssistantMessage("Processing: " + input + "\n\nNote: Agent integration pending.");
                ui.setStatus("Ready");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void onInputChange(String input) {
        // Handle autocomplete, input validation, etc.
        // This could be used to show command suggestions
        if (input.startsWith("/")) {
            // Slash command mode
            ui.addSystemMessage("Commands: /clear, /model, /exit");
        }
    }

    private void checkConnection() {
        // Check if Ollama is connected
        boolean connected = true; // TODO: actual health check
        ui.setConnected(connected);
        ui.setModel("qwen3-coder:32b");
        ui.setStatus(connected ? "Ready" : "Disconnected");
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
