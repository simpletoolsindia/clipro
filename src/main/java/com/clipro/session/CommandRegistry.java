package com.clipro.session;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Registry for CLI commands.
 */
public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry() {
        initDefaultCommands();
    }

    private void initDefaultCommands() {
        register("help", new Command("help", "Show this help message", ctx -> {
            ctx.appendOutput("Available commands:");
            for (Command cmd : commands.values()) {
                ctx.appendOutput("  " + cmd.name + " - " + cmd.description);
            }
        }));

        register("clear", new Command("clear", "Clear the chat", ctx -> {
            ctx.clearMessages();
            ctx.appendOutput("Chat cleared");
        }));

        register("exit", new Command("exit", "Exit the CLI", ctx -> {
            ctx.setExit(true);
        }));

        register("quit", new Command("quit", "Exit the CLI", ctx -> {
            ctx.setExit(true);
        }));

        register("model", new Command("model", "Show/set model", ctx -> {
            if (ctx.getArgs().isEmpty()) {
                ctx.appendOutput("Current model: " + ctx.getCurrentModel());
            } else {
                ctx.setModel(ctx.getArgs().get(0));
                ctx.appendOutput("Model set to: " + ctx.getArgs().get(0));
            }
        }));

        register("models", new Command("models", "List available models", ctx -> {
            ctx.appendOutput("Available models:");
            ctx.appendOutput("  qwen3-coder:32b (local)");
            ctx.appendOutput("  qwen2.5-coder:14b (local)");
            ctx.appendOutput("  llama3.3:70b (local)");
            ctx.appendOutput("  deepseek-r1:70b (local)");
        }));

        register("status", new Command("status", "Show connection status", ctx -> {
            ctx.appendOutput("Status: Connected");
            ctx.appendOutput("Model: " + ctx.getCurrentModel());
        }));

        register("token", new Command("token", "Show token usage", ctx -> {
            ctx.appendOutput("Tokens: " + ctx.getInputTokens() + " in / " + ctx.getOutputTokens() + " out");
        }));
    }

    public void register(String name, Command command) {
        commands.put(name, command);
    }

    public void unregister(String name) {
        commands.remove(name);
    }

    public Command get(String name) {
        return commands.get(name);
    }

    public boolean exists(String name) {
        return commands.containsKey(name);
    }

    public Map<String, Command> getAll() {
        return new HashMap<>(commands);
    }

    public boolean execute(String name, CommandContext context) {
        Command cmd = commands.get(name);
        if (cmd != null) {
            cmd.execute(context);
            return true;
        }
        return false;
    }

    public static class Command {
        public final String name;
        public final String description;
        public final Consumer<CommandContext> action;

        public Command(String name, String description, Consumer<CommandContext> action) {
            this.name = name;
            this.description = description;
            this.action = action;
        }

        public void execute(CommandContext context) {
            action.accept(context);
        }
    }

    public static class CommandContext {
        private final java.util.List<String> args;
        private final StringBuilder output = new StringBuilder();
        private String currentModel;
        private String selectedModel;
        private boolean shouldExit = false;
        private Runnable clearMessages;
        private int inputTokens;
        private int outputTokens;

        public CommandContext(java.util.List<String> args) {
            this.args = args;
        }

        public java.util.List<String> getArgs() {
            return args;
        }

        public void appendOutput(String line) {
            output.append(line).append("\n");
        }

        public String getOutput() {
            return output.toString();
        }

        public void clearOutput() {
            output.setLength(0);
        }

        public String getCurrentModel() {
            return currentModel != null ? currentModel : "not set";
        }

        public void setCurrentModel(String model) {
            this.currentModel = model;
        }

        public void setModel(String model) {
            this.selectedModel = model;
        }

        public String getSelectedModel() {
            return selectedModel;
        }

        public boolean isExit() {
            return shouldExit;
        }

        public void setExit(boolean exit) {
            this.shouldExit = exit;
        }

        public void clearMessages() {
            if (clearMessages != null) {
                clearMessages.run();
            }
        }

        public void setClearMessages(Runnable clearMessages) {
            this.clearMessages = clearMessages;
        }

        public int getInputTokens() {
            return inputTokens;
        }

        public void setInputTokens(int tokens) {
            this.inputTokens = tokens;
        }

        public int getOutputTokens() {
            return outputTokens;
        }

        public void setOutputTokens(int tokens) {
            this.outputTokens = tokens;
        }
    }
}
