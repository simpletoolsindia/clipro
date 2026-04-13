package com.clipro.cli;

import java.util.*;
import java.util.function.Function;

/**
 * Command registry for CLI commands.
 * Supports slash commands like /help, /clear, /exit, /model, etc.
 */
public class CommandRegistry {

    private final Map<String, Command> commands;

    public CommandRegistry() {
        this.commands = new HashMap<>();
        registerDefaultCommands();
    }

    private void registerDefaultCommands() {
        // Core commands
        register(new Command("help", "Show help", (ctx) -> showHelp()));
        register(new Command("clear", "Clear conversation", (ctx) -> {
            ctx.clearHistory();
            return "Conversation cleared.";
        }));
        register(new Command("exit", "Exit the application", (ctx) -> {
            ctx.exit();
            return "Goodbye!";
        }));
        register(new Command("quit", "Exit the application", (ctx) -> {
            ctx.exit();
            return "Goodbye!";
        }));

        // Model commands
        register(new Command("model", "Show/set current model", (ctx) -> {
            String model = ctx.getCurrentModel();
            return "Current model: " + model;
        }));

        // Git commands
        register(new Command("status", "Show git status", (ctx) -> "Use git_status tool instead"));
        register(new Command("diff", "Show git diff", (ctx) -> "Use git_diff tool instead"));
        register(new Command("commit", "Commit changes", (ctx) -> "Use git_commit tool instead"));
    }

    public void register(Command command) {
        commands.put(command.getName(), command);
    }

    public void unregister(String name) {
        commands.remove(name);
    }

    public Command get(String name) {
        return commands.get(name);
    }

    public Set<String> getCommandNames() {
        return commands.keySet();
    }

    public boolean hasCommand(String name) {
        return commands.containsKey(name);
    }

    /**
     * Parse and execute a command.
     */
    public String execute(String input, CommandContext context) {
        String trimmed = input.trim();

        if (!trimmed.startsWith("/")) {
            return null; // Not a command, treat as regular message
        }

        String[] parts = trimmed.split("\\s+", 2);
        String name = parts[0].substring(1); // Remove leading /
        String args = parts.length > 1 ? parts[1] : "";

        Command cmd = commands.get(name);
        if (cmd == null) {
            return "Unknown command: /" + name + ". Type /help for available commands.";
        }

        context.setArgs(args);
        return cmd.execute(context);
    }

    /**
     * Get help text for all commands.
     */
    public String showHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available Commands:\n\n");

        for (Command cmd : commands.values()) {
            sb.append("/").append(cmd.getName())
              .append(" - ").append(cmd.getDescription())
              .append("\n");
        }

        sb.append("\nTools:\n");
        sb.append("Use natural language to trigger tools.\n");

        return sb.toString();
    }

    /**
     * Command class.
     */
    public static class Command {
        private final String name;
        private final String description;
        private final Function<CommandContext, String> action;

        public Command(String name, String description, Function<CommandContext, String> action) {
            this.name = name;
            this.description = description;
            this.action = action;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String execute(CommandContext context) { return action.apply(context); }
    }

    /**
     * Context passed to command execution.
     */
    public static class CommandContext {
        private String args;
        private AgentContext agentContext;

        public void setArgs(String args) { this.args = args; }
        public String getArgs() { return args; }

        public void setAgentContext(AgentContext ctx) { this.agentContext = ctx; }
        public AgentContext getAgentContext() { return agentContext; }

        public String getCurrentModel() {
            return agentContext != null ? agentContext.getCurrentModel() : "unknown";
        }

        public void clearHistory() {
            if (agentContext != null) agentContext.clearHistory();
        }

        public void exit() {
            if (agentContext != null) agentContext.exit();
        }
    }

    /**
     * Agent context interface.
     */
    public interface AgentContext {
        String getCurrentModel();
        void clearHistory();
        void exit();
    }
}