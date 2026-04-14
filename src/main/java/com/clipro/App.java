package com.clipro;

import com.clipro.cli.CommandRegistry;
import com.clipro.logging.Logger;
import com.clipro.ui.Terminal;
import com.clipro.ui.UIController;

import java.util.Scanner;

/**
 * CLIPRO - Java AI Coding CLI
 * Pixel-perfect OpenClaude UI with LOCAL-FIRST Ollama
 */
public class App {

    private static final Logger LOG = new Logger("App");
    private static final Logger UI = new Logger("UI");

    private final UIController ui;
    private final CommandRegistry commands;
    private final CommandRegistry.CommandContext commandContext;
    private boolean running = true;

    public App() {
        LOG.info("Initializing CLIPRO...");
        this.ui = new UIController();
        this.commands = new CommandRegistry();
        this.commandContext = new CommandRegistry.CommandContext();
        commandContext.setAgentContext(new AppAgentContext());
        LOG.info("CLIPRO initialized successfully");
    }

    public void run() {
        LOG.info("Starting CLIPRO...");
        ui.getLayout().init();
        printBanner();
        try (Scanner scanner = new Scanner(System.in)) {
            while (running && scanner.hasNextLine()) {
                processInput(scanner.nextLine());
            }
        } finally {
            ui.getLayout().shutdown();
        }
    }

    private void processInput(String input) {
        if (input == null || input.trim().isEmpty()) return;

        String cmdResult = commands.execute(input, commandContext);
        if (cmdResult != null) {
            ui.getLayout().addSystemMessage(cmdResult);
            render();
            return;
        }

        ui.sendMessage(input);
        render();
    }

    private void render() {
        System.out.print(ui.render());
    }

    private void printBanner() {
        ui.checkConnection();
        ui.getLayout().getHeader().setStatus("Ready");
        System.out.print(ui.render());
    }

    public void stop() {
        running = false;
        LOG.info("Shutting down CLIPRO...");
    }

    private class AppAgentContext implements CommandRegistry.AgentContext {
        @Override
        public String getCurrentModel() {
            return ui.getModel();
        }
        @Override
        public void clearHistory() {
            ui.clear();
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
