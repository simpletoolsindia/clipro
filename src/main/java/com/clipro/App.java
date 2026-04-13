package com.clipro;

import com.clipro.agent.AgentEngine;
import com.clipro.cli.CommandRegistry;
import com.clipro.logging.Logger;
import com.clipro.tools.file.*;
import com.clipro.tools.git.*;
import com.clipro.tools.shell.BashTool;
import com.clipro.tools.web.*;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * CLIPRO - Java AI Coding CLI
 * LOCAL-FIRST approach: Ollama + Native Tools
 */
public class App {

    private static final Logger LOG = new Logger("App");
    private static final Logger UI = new Logger("UI");

    private final AgentEngine agent;
    private final CommandRegistry commands;
    private final CommandRegistry.CommandContext commandContext;
    private boolean running = true;

    public App() {
        LOG.info("Initializing CLIPRO...");
        this.agent = new AgentEngine();
        this.commands = new CommandRegistry();
        this.commandContext = new CommandRegistry.CommandContext();
        commandContext.setAgentContext(new AppAgentContext());
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
        printBanner();
        try (Scanner scanner = new Scanner(System.in)) {
            while (running && scanner.hasNextLine()) {
                processInput(scanner.nextLine());
            }
        }
    }

    private void processInput(String input) {
        if (input == null || input.trim().isEmpty()) return;
        String cmdResult = commands.execute(input, commandContext);
        if (cmdResult != null) {
            UI.info(cmdResult);
            return;
        }
        UI.info("Thinking...");
        CompletableFuture<String> future = agent.run(input);
        future.thenAccept(response -> {
            UI.info("\n" + response);
            UI.info("\n[Ready]");
        }).exceptionally(ex -> {
            UI.error("Error: " + ex.getMessage());
            return null;
        });
        try {
            future.join();
        } catch (Exception e) {
            UI.error("Error: " + e.getMessage());
        }
    }

    private void printBanner() {
        System.out.println();
        UI.info("╔═══════════════════════════════════════════╗");
        UI.info("║         CLIPRO - Java AI CLI            ║");
        UI.info("║         LOCAL-FIRST: Ollama             ║");
        UI.info("╚═══════════════════════════════════════════╝");
        System.out.println();
        UI.info("Model: " + agent.getProvider().getCurrentModel());
        UI.info("Tools: " + agent.getToolRegistry().getToolNames().size() + " registered");
        System.out.println();
        UI.info("Type /help for commands, or ask me anything!");
        UI.info("[Ready]");
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
            UI.info("History cleared");
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
            LOG.info("Shutdown hook triggered");
        }));
        app.run();
        LOG.info("CLIPRO exited");
    }
}
