package com.clipro.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Command completer for slash commands and suggestions.
 * Provides fuzzy search and autocomplete for CLI commands.
 */
public class CommandCompleter {
    private final List<Command> commands = new ArrayList<>();

    public CommandCompleter() {
        initDefaultCommands();
    }

    private void initDefaultCommands() {
        // Core commands
        add("help", "Show all commands");
        add("clear", "Clear conversation");
        add("exit", "Exit CLI");
        add("quit", "Exit CLI");

        // Model commands
        add("model", "Show/set current model");
        add("models", "List available models");
        add("provider", "Show/switch LLM provider");

        // Git commands
        add("status", "Show git status");
        add("diff", "Show changes");
        add("log", "Show commit history");
        add("commit", "Commit with message");
        add("branch", "Show branches");
        add("stash", "Git stash operations");
        add("pull", "Git pull");
        add("push", "Git push");
        add("fetch", "Git fetch");
        add("merge", "Git merge");
        add("rebase", "Git rebase");

        // File/Search commands
        add("grep", "Search in files");
        add("find", "Find files");
        add("glob", "Glob pattern");
        add("read", "Read file");
        add("cat", "Display file");
        add("ls", "List directory");
        add("pwd", "Print working dir");

        // Web commands
        add("search", "Search web");
        add("web", "Web search");
        add("fetch", "Fetch URL");
        add("wget", "Download file");

        // Shell shortcuts
        add("bash", "Execute bash");
        add("sh", "Shell command");
        add("whoami", "Current user");
        add("date", "Current date");
        add("env", "Environment");
        add("uptime", "System uptime");
        add("df", "Disk usage");
        add("free", "Memory usage");

        // Session commands
        add("history", "Command history");
        add("sessions", "List sessions");
        add("compact", "Compact conversation");
        add("cache", "Cache management");

        // Stats/Config commands
        add("tokens", "Token usage");
        add("stats", "Statistics");
        add("cost", "API cost");
        add("context", "Context usage");
        add("config", "Configuration");
        add("key", "API key");
        add("info", "System info");
        add("mode", "Permission mode");
        add("version", "Version");
        add("api", "API info");

        // Developer commands
        add("test", "Run tests");
        add("build", "Build project");
        add("clean", "Clean build");
        add("jar", "Build JAR");
        add("debug", "Debug mode");
    }

    private void add(String name, String description) {
        commands.add(new Command(name, description));
    }

    /**
     * Complete a partial command input.
     */
    public List<String> complete(String input) {
        if (input == null || input.isEmpty()) {
            return getAllCommandNames();
        }

        // Remove leading slash if present
        String query = input.startsWith("/") ? input.substring(1) : input;

        List<String> matches = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Command cmd : commands) {
            if (cmd.name.toLowerCase().startsWith(lowerQuery)) {
                matches.add("/" + cmd.name);
            }
        }

        return matches;
    }

    /**
     * Fuzzy match for better autocomplete.
     */
    public List<String> fuzzyComplete(String input) {
        if (input == null || input.isEmpty()) {
            return getAllCommandNames();
        }

        String query = input.startsWith("/") ? input.substring(1) : input;
        String lowerQuery = query.toLowerCase();

        List<Match> matches = new ArrayList<>();

        for (Command cmd : commands) {
            int score = fuzzyScore(lowerQuery, cmd.name.toLowerCase());
            if (score > 0) {
                matches.add(new Match("/" + cmd.name, score));
            }
        }

        // Sort by score (best matches first)
        matches.sort((a, b) -> Integer.compare(b.score, a.score));

        List<String> result = new ArrayList<>();
        for (Match m : matches) {
            result.add(m.command);
        }
        return result;
    }

    private int fuzzyScore(String query, String text) {
        if (text.startsWith(query)) return 100; // Exact prefix match
        if (text.contains(query)) return 50;    // Contains match

        // Character-by-character fuzzy match
        int score = 0;
        int qi = 0;
        for (int i = 0; i < text.length() && qi < query.length(); i++) {
            if (text.charAt(i) == query.charAt(qi)) {
                score += 10;
                qi++;
            }
        }
        return qi == query.length() ? score : 0;
    }

    /**
     * Get single completion if unique.
     */
    public String getCompletion(String input) {
        List<String> completions = complete(input);
        if (completions.isEmpty()) {
            return null;
        }
        if (completions.size() == 1) {
            return completions.get(0);
        }

        // Return first match
        return completions.get(0);
    }

    /**
     * Get all command names.
     */
    public List<String> getAllCommandNames() {
        List<String> names = new ArrayList<>();
        for (Command cmd : commands) {
            names.add("/" + cmd.name);
        }
        return names;
    }

    /**
     * Add custom command.
     */
    public void addCommand(String name, String description) {
        // Normalize name (remove leading slash if present)
        String normalized = name.startsWith("/") ? name.substring(1) : name;
        commands.add(new Command(normalized, description));
    }

    /**
     * Remove command.
     */
    public void removeCommand(String name) {
        String normalized = name.startsWith("/") ? name.substring(1) : name;
        commands.removeIf(cmd -> cmd.name.equals(normalized));
    }

    /**
     * Check if input is a command.
     */
    public boolean isCommand(String input) {
        return input != null && input.startsWith("/");
    }

    /**
     * Check if input is a partial command.
     */
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

    /**
     * Get command description.
     */
    public String getDescription(String commandName) {
        String name = commandName.startsWith("/") ?
            commandName.substring(1) : commandName;
        for (Command cmd : commands) {
            if (cmd.name.equals(name)) {
                return cmd.description;
            }
        }
        return null;
    }

    /**
     * Get all commands.
     */
    public List<Command> getCommands() {
        return new ArrayList<>(commands);
    }

    /**
     * Get command count.
     */
    public int getCommandCount() {
        return commands.size();
    }

    public static class Command {
        public final String name;
        public final String description;

        public Command(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    private static class Match {
        final String command;
        final int score;
        Match(String command, int score) {
            this.command = command;
            this.score = score;
        }
    }
}
