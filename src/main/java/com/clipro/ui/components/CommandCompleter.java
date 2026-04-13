package com.clipro.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Command completer for slash commands and suggestions.
 */
public class CommandCompleter {
    private final List<Command> commands = new ArrayList<>();

    public CommandCompleter() {
        initDefaultCommands();
    }

    private void initDefaultCommands() {
        // Dev commands
        commands.add(new Command("/help", "Show help"));
        commands.add(new Command("/clear", "Clear chat"));
        commands.add(new Command("/exit", "Exit CLI"));
        commands.add(new Command("/quit", "Exit CLI"));

        // Model commands
        commands.add(new Command("/model", "Show/set model"));
        commands.add(new Command("/models", "List available models"));
        commands.add(new Command("/models", "List available models"));

        // Dev commands
        commands.add(new Command("/commit", "Git commit"));
        commands.add(new Command("/review", "Code review"));
        commands.add(new Command("/diff", "Show changes"));
        commands.add(new Command("/status", "Git status"));
        commands.add(new Command("/log", "Show git log"));
        commands.add(new Command("/branch", "Show branches"));

        // Tool commands
        commands.add(new Command("/search", "Search the web"));
        commands.add(new Command("/fetch", "Fetch URL content"));
        commands.add(new Command("/bash", "Run shell command"));
    }

    public List<String> complete(String input) {
        if (input == null || input.isEmpty()) {
            return getAllCommandNames();
        }

        List<String> matches = new ArrayList<>();

        for (Command cmd : commands) {
            if (cmd.name.toLowerCase().startsWith(input.toLowerCase())) {
                matches.add(cmd.name);
            }
        }

        return matches;
    }

    public String getCompletion(String input) {
        List<String> completions = complete(input);
        if (completions.isEmpty()) {
            return null;
        }
        if (completions.size() == 1) {
            return completions.get(0);
        }

        // Return first match for now (could show list for multiple)
        return completions.get(0);
    }

    public List<String> getAllCommandNames() {
        List<String> names = new ArrayList<>();
        for (Command cmd : commands) {
            names.add(cmd.name);
        }
        return names;
    }

    public void addCommand(String name, String description) {
        commands.add(new Command(name, description));
    }

    public void removeCommand(String name) {
        commands.removeIf(cmd -> cmd.name.equals(name));
    }

    public boolean isCommand(String input) {
        return input != null && input.startsWith("/");
    }

    public boolean isPartialCommand(String input) {
        return input != null && input.startsWith("/") && !containsWhitespace(input);
    }

    private boolean containsWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public String getDescription(String commandName) {
        for (Command cmd : commands) {
            if (cmd.name.equals(commandName)) {
                return cmd.description;
            }
        }
        return null;
    }

    public List<Command> getCommands() {
        return new ArrayList<>(commands);
    }

    public static class Command {
        public final String name;
        public final String description;

        public Command(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}
