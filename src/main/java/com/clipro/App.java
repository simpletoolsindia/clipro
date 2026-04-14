package com.clipro;

import com.clipro.agent.AgentEngine;
import com.clipro.cli.CommandRegistry;
import com.clipro.logging.Logger;
import com.clipro.ui.Terminal;
import com.clipro.ui.components.FullscreenLayout;
import com.clipro.tools.file.*;
import com.clipro.tools.git.*;
import com.clipro.tools.shell.BashTool;
import com.clipro.tools.web.*;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * CLIPRO - Java AI Coding CLI
 * Pixel-perfect OpenClaude UI with LOCAL-FIRST Ollama
 */
public class App {

    private static final Logger LOG = new Logger("App");
    private static final Logger UI = new Logger("UI");

    private final AgentEngine agent;
    private final CommandRegistry commands;
    private final CommandRegistry.CommandContext commandContext;
    private final FullscreenLayout layout;
    private boolean running = true;

    public App() {
        LOG.info("Initializing CLIPRO...");
        this.agent = new AgentEngine();
        this.commands = new CommandRegistry();
        this.commandContext = new CommandRegistry.CommandContext();
        commandContext.setAgentContext(new AppAgentContext());
        this.layout = new FullscreenLayout(agent.getProvider().getCurrentModel());
        registerDefaultTools();
        LOG.info("CLIPRO initialized successfully");
    }

    private void registerDefaultTools() {
        LOG.info("Registering default tools...");
        agent.registerTool(new FileReadTool());
        agent.registerTool(new FileWriteTool());
        agent.registerTool(new FileEditTool());
        agent.registerTool(new GlobTool());
        agent.registerTool(new GrepTool());
        agent.registerTool(new BashTool());
        agent.registerTool(new GitStatusTool());
        agent.registerTool(new GitDiffTool());
        agent.registerTool(new GitLogTool());
        agent.registerTool(new GitCommitTool());
        agent.registerTool(new WebSearchTool());
        agent.registerTool(new WebFetchTool());
        agent.registerTool(new QuickFetchTool());
        LOG.info("Registered " + agent.getToolRegistry().getToolNames().size() + " tools");
    }

    public void run() {
        LOG.info("Starting CLIPRO...");
        layout.init();
        printBanner();
        try (Scanner scanner = new Scanner(System.in)) {
            while (running && scanner.hasNextLine()) {
                processInput(scanner.nextLine());
            }
        } finally {
            layout.shutdown();
        }
    }

    private void processInput(String input) {
        if (input == null || input.trim().isEmpty()) return;

        // Add user message to UI
        layout.addUserMessage(input);
        layout.getStatus().setStatusText("Thinking...");

        String cmdResult = commands.execute(input, commandContext);
        if (cmdResult != null) {
            layout.addSystemMessage(cmdResult);
            render();
            return;
        }

        CompletableFuture<String> future = agent.run(input);
        future.thenAccept(response -> {
            layout.addAssistantMessage(response);
            layout.getStatus().setStatusText("Ready");
            render();
        }).exceptionally(ex -> {
            layout.addSystemMessage("Error: " + ex.getMessage());
            layout.getStatus().setStatusText("Error");
            render();
            return null;
        });

        try {
            future.join();
        } catch (Exception e) {
            layout.addSystemMessage("Error: " + e.getMessage());
            render();
        }
    }

    private void render() {
        System.out.print(layout.render());
    }

    private void printBanner() {
        layout.getHeader().setConnected(true);
        layout.getHeader().setStatus("Ready");
        System.out.print(layout.render());
    }

    public void stop() {
        running = false;
        LOG.info("Shutting down CLIPRO...");
    }

    private class AppAgentContext implements CommandRegistry.AgentContext {
        @Override
        public String getCurrentModel() {
            return agent.getProvider().getCurrentModel();
        }
        @Override
        public void clearHistory() {
            layout.clearMessages();
            render();
        }
        @Override
        public void exit() {
            stop();
        }
    }

    public static void main(String[] args) {
        LOG.info("CLIPRO v0.1.0");
        LOG.info("Starting...");
        App app = new App();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Terminal.showCursor();
            Terminal.exitAltScreen();
            LOG.info("Shutdown hook triggered");
        }));
        app.run();
        LOG.info("CLIPRO exited");
    }
}
