package com.clipro.cli;

import com.clipro.agent.AgentEngine;
import com.clipro.agent.AgentManager;
import com.clipro.tools.Tool;
import com.clipro.tools.file.GrepTool;
import com.clipro.tools.git.GitCommitTool;
import com.clipro.tools.git.GitDiffTool;
import com.clipro.tools.git.GitLogTool;
import com.clipro.tools.git.GitStatusTool;
import com.clipro.tools.shell.BashTool;

import java.util.*;
import java.util.function.Function;

/**
 * Command registry for CLI commands.
 * Supports slash commands like /help, /clear, /exit, /model, etc.
 *
 * M-03: Agent CLI commands (/spawn, /kill, /team)
 * M-04: MCP CLI commands (/mcp list, add, remove, start, stop, tools)
 * M-05: Theme CLI commands (/theme dark, light, auto, preview)
 */
public class CommandRegistry {

    private final Map<String, Command> commands;
    private final List<Tool> tools;
    private final Map<String, Tool> toolMap;
    private AgentContext agentContext;
    private AgentManager agentManager;

    public CommandRegistry() {
        this.commands = new HashMap<>();
        this.tools = new ArrayList<>();
        this.toolMap = new HashMap<>();
        registerDefaultCommands();
        registerDefaultTools();
    }

    public void setAgentContext(AgentContext ctx) {
        this.agentContext = ctx;
    }

    public void setAgentManager(AgentManager manager) {
        this.agentManager = manager;
    }

    public AgentManager getAgentManager() {
        return agentManager;
    }

    private void registerDefaultCommands() {
        // Core commands
        register(new Command("help", "Show all commands", (ctx) -> showHelp()));
        register(new Command("clear", "Clear conversation history", (ctx) -> {
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
        register(new Command("model", "Show current model", (ctx) -> {
            String model = ctx.getCurrentModel();
            return "Current model: " + model;
        }));
        register(new Command("models", "List available models", (ctx) -> {
            return "Available models:\n" +
                   "  - qwen/qwen3.6-plus (OpenRouter)\n" +
                   "  - qwen3-coder:32b (Ollama)\n" +
                   "  - llama3.3:70b (Ollama)\n" +
                   "Use /model <name> to switch.";
        }));

        // Git commands - direct execution
        register(new Command("status", "Show git status", this::executeGitStatus));
        register(new Command("diff", "Show git diff", this::executeGitDiff));
        register(new Command("log", "Show git log", this::executeGitLog));
        register(new Command("commit", "Commit changes with message", this::executeGitCommit));

        // Search commands
        register(new Command("grep", "Search in files (Usage: /grep pattern)", this::executeGrep));
        register(new Command("find", "Find files by pattern", this::executeFind));

        // Shell commands
        register(new Command("bash", "Execute bash command", this::executeBash));
        register(new Command("sh", "Execute shell command", this::executeBash));

        // Provider commands
        register(new Command("provider", "Show/set LLM provider", (ctx) -> {
            return "Available providers:\n" +
                   "  1. Ollama (local, fast)\n" +
                   "  2. OpenRouter (cloud, 300+ models)\n" +
                   "Use /provider <name> to switch.";
        }));

        // Token/info commands
        register(new Command("tokens", "Show token usage", (ctx) -> {
            if (ctx.hasAgentContext()) {
                return "Token usage: " + ctx.getTokenInfo();
            }
            return "Token info not available.";
        }));
        register(new Command("info", "Show system info", (ctx) -> {
            return "CLIPRO v0.1.0\n" +
                   "Model: " + ctx.getCurrentModel() + "\n" +
                   "Tools: " + toolMap.size() + " registered\n" +
                   "Commands: " + commands.size() + " available";
        }));

        // Permission mode commands
        register(new Command("mode", "Show/set permission mode (READ_ONLY/BASH/RESTRICTED)", (ctx) -> {
            String mode = ctx.getArgs().trim().toUpperCase();
            if (mode.isEmpty()) {
                BashTool bash = getTool(BashTool.class);
                if (bash != null) {
                    return "Current mode: " + bash.getPermissionMode();
                }
                return "BashTool not available.";
            }
            BashTool bash = getTool(BashTool.class);
            if (bash != null) {
                try {
                    bash.setPermissionMode(BashTool.PermissionMode.valueOf(mode));
                    return "Permission mode set to: " + mode;
                } catch (IllegalArgumentException e) {
                    return "Invalid mode. Use: READ_ONLY, BASH, or RESTRICTED";
                }
            }
            return "BashTool not available.";
        }));

        // Shortcuts
        register(new Command("ls", "List directory", (ctx) -> executeCommand(ctx, "ls -la")));
        register(new Command("pwd", "Print working directory", (ctx) -> executeCommand(ctx, "pwd")));
        register(new Command("whoami", "Show current user", (ctx) -> executeCommand(ctx, "whoami")));
        register(new Command("date", "Show current date", (ctx) -> executeCommand(ctx, "date")));

        // Git advanced commands
        register(new Command("branch", "Show git branches", (ctx) -> executeCommand(ctx, "git branch -a")));
        register(new Command("stash", "Git stash operations", this::executeGitStash));
        register(new Command("pull", "Git pull", (ctx) -> executeCommand(ctx, "git pull")));
        register(new Command("push", "Git push", (ctx) -> executeCommand(ctx, "git push")));
        register(new Command("fetch", "Git fetch", (ctx) -> executeCommand(ctx, "git fetch --all")));
        register(new Command("merge", "Git merge", this::executeGitMerge));
        register(new Command("rebase", "Git rebase", this::executeGitRebase));

        // File commands
        register(new Command("read", "Read file content", this::executeFileRead));
        register(new Command("cat", "Display file content", this::executeFileRead));
        register(new Command("glob", "Find files by pattern", this::executeGlob));

        // Web commands
        register(new Command("search", "Search the web", this::executeWebSearch));
        register(new Command("web", "Web search", this::executeWebSearch));
        register(new Command("fetch", "Fetch URL content", this::executeWebFetch));
        register(new Command("wget", "Download file", this::executeWget));

        // Session/context commands
        register(new Command("history", "Show command history", this::executeHistory));
        register(new Command("sessions", "List sessions", this::executeSessions));
        register(new Command("compact", "Compact conversation", this::executeCompact));
        register(new Command("cache", "Cache management", (ctx) -> "Cache: " + (ctx.getArgs().isEmpty() ? "on" : ctx.getArgs())));

        // Stats commands
        register(new Command("stats", "Show statistics", this::executeStats));
        register(new Command("cost", "Show API cost estimate", this::executeCost));
        register(new Command("context", "Show context usage", this::executeContext));

        // Config commands
        register(new Command("config", "Show configuration", this::executeConfig));
        register(new Command("key", "API key management", (ctx) -> {
            if (ctx.getArgs().isEmpty()) {
                return "API key: " + (hasApiKey() ? "[SET]" : "[NOT SET]");
            }
            return "API key command: " + ctx.getArgs();
        }));
        register(new Command("version", "Show version", (ctx) -> "CLIPRO v0.1.0"));

        // System commands
        register(new Command("api", "API information", (ctx) -> {
            return "LLM APIs:\n" +
                   "  - Ollama: http://localhost:11434\n" +
                   "  - OpenRouter: https://openrouter.ai/api/v1";
        }));
        register(new Command("env", "Show environment", (ctx) -> executeCommand(ctx, "printenv | sort")));
        register(new Command("uptime", "Show system uptime", (ctx) -> executeCommand(ctx, "uptime")));
        register(new Command("df", "Show disk usage", (ctx) -> executeCommand(ctx, "df -h")));
        register(new Command("free", "Show memory usage", (ctx) -> executeCommand(ctx, "free -h")));

        // Developer commands
        register(new Command("test", "Run tests", (ctx) -> executeCommand(ctx, "./gradlew test")));
        register(new Command("build", "Build project", (ctx) -> executeCommand(ctx, "./gradlew build")));
        register(new Command("clean", "Clean build", (ctx) -> executeCommand(ctx, "./gradlew clean")));
        register(new Command("jar", "Build JAR", (ctx) -> executeCommand(ctx, "./gradlew uberJar")));
        register(new Command("debug", "Debug mode", (ctx) -> ctx.getArgs().isEmpty() ? "Debug: off" : "Debug: " + ctx.getArgs()));

        // Agent commands (M-03)
        register(new Command("agent", "Manage agents", this::executeAgent));
        register(new Command("team", "Team management", this::executeTeam));
        register(new Command("spawn", "Spawn sub-agent", this::executeSpawn));
        register(new Command("kill", "Terminate agent", this::executeKill));

        // MCP commands (M-04)
        register(new Command("mcp", "MCP tool management", this::executeMcp));
        register(new Command("mcp-list", "List MCP servers", this::executeMcpList));
        register(new Command("mcp-add", "Add MCP server", this::executeMcpAdd));
        register(new Command("mcp-remove", "Remove MCP server", this::executeMcpRemove));
        register(new Command("mcp-start", "Start MCP server", this::executeMcpStart));
        register(new Command("mcp-stop", "Stop MCP server", this::executeMcpStop));
        register(new Command("mcp-tools", "List MCP server tools", this::executeMcpTools));

        // Theme commands (M-05)
        register(new Command("theme", "Theme management", this::executeTheme));
    }

    private void registerDefaultTools() {
        // Register tools for direct command execution
        addTool(new GitStatusTool());
        addTool(new GitDiffTool());
        addTool(new GitLogTool());
        addTool(new GitCommitTool());
        addTool(new GrepTool());
        addTool(new BashTool());
    }

    public void addTool(Tool tool) {
        tools.add(tool);
        toolMap.put(tool.getName(), tool);
    }

    @SuppressWarnings("unchecked")
    public <T extends Tool> T getTool(Class<T> clazz) {
        for (Tool tool : tools) {
            if (clazz.isInstance(tool)) {
                return (T) tool;
            }
        }
        return null;
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
        String name = parts[0].substring(1).toLowerCase(); // Remove leading /
        String args = parts.length > 1 ? parts[1] : "";

        context.setArgs(args);
        Command cmd = commands.get(name);

        if (cmd == null) {
            // Check if it's a tool
            Tool tool = toolMap.get(name);
            if (tool != null) {
                return executeTool(tool, args);
            }
            return "Unknown command: /" + name + ". Type /help for available commands.";
        }

        return cmd.execute(context);
    }

    /**
     * Execute a tool directly.
     */
    private String executeTool(Tool tool, String args) {
        Map<String, Object> params = new HashMap<>();
        if (!args.isEmpty()) {
            // Simple parsing for common patterns
            if (tool.getName().equals("git_status")) {
                return tool.execute(params);
            } else if (tool.getName().equals("git_log")) {
                return tool.execute(params);
            } else if (tool.getName().equals("git_diff")) {
                return tool.execute(params);
            } else if (tool.getName().equals("grep")) {
                String[] parts = args.split("\\s+", 2);
                if (parts.length >= 1) {
                    params.put("pattern", parts[0]);
                }
                if (parts.length >= 2) {
                    params.put("path", parts[1]);
                }
                return tool.execute(params);
            } else if (tool.getName().equals("bash")) {
                params.put("command", args);
                return tool.execute(params);
            }
        }
        return tool.execute(params);
    }

    private String executeCommand(CommandContext ctx, String command) {
        BashTool bash = getTool(BashTool.class);
        if (bash != null) {
            Map<String, Object> params = Map.of("command", command);
            return bash.execute(params);
        }
        return "BashTool not available.";
    }

    // Git command handlers
    private String executeGitStatus(CommandContext ctx) {
        GitStatusTool tool = getTool(GitStatusTool.class);
        if (tool != null) {
            return tool.execute(Map.of());
        }
        return "GitStatusTool not available.";
    }

    private String executeGitDiff(CommandContext ctx) {
        GitDiffTool tool = getTool(GitDiffTool.class);
        if (tool != null) {
            return tool.execute(Map.of());
        }
        return "GitDiffTool not available.";
    }

    private String executeGitLog(CommandContext ctx) {
        GitLogTool tool = getTool(GitLogTool.class);
        if (tool != null) {
            return tool.execute(Map.of());
        }
        return "GitLogTool not available.";
    }

    private String executeGitCommit(CommandContext ctx) {
        GitCommitTool tool = getTool(GitCommitTool.class);
        if (tool != null) {
            String msg = ctx.getArgs().trim();
            if (msg.isEmpty()) {
                return "Usage: /commit <message>\nExample: /commit Fix bug in login";
            }
            return tool.execute(Map.of("message", msg));
        }
        return "GitCommitTool not available.";
    }

    private String executeGrep(CommandContext ctx) {
        GrepTool tool = getTool(GrepTool.class);
        if (tool != null) {
            String args = ctx.getArgs().trim();
            if (args.isEmpty()) {
                return "Usage: /grep <pattern> [path]\nExample: /grep TODO src/";
            }
            String[] parts = args.split("\\s+", 2);
            Map<String, Object> params = new HashMap<>();
            params.put("pattern", parts[0]);
            if (parts.length > 1) {
                params.put("path", parts[1]);
            }
            return tool.execute(params);
        }
        return "GrepTool not available.";
    }

    private String executeFind(CommandContext ctx) {
        BashTool bash = getTool(BashTool.class);
        if (bash != null) {
            String args = ctx.getArgs().trim();
            String cmd = args.isEmpty() ? "find . -type f" : "find " + args;
            return bash.execute(Map.of("command", cmd));
        }
        return "BashTool not available.";
    }

    private String executeBash(CommandContext ctx) {
        BashTool bash = getTool(BashTool.class);
        if (bash != null) {
            String cmd = ctx.getArgs().trim();
            if (cmd.isEmpty()) {
                return "Usage: /bash <command>\nExample: /bash ls -la";
            }
            return bash.execute(Map.of("command", cmd));
        }
        return "BashTool not available.";
    }

    // Advanced Git commands
    private String executeGitStash(CommandContext ctx) {
        String args = ctx.getArgs().trim();
        if (args.isEmpty()) {
            return executeCommand(ctx, "git stash list");
        } else if (args.equals("pop")) {
            return executeCommand(ctx, "git stash pop");
        } else if (args.equals("push")) {
            return executeCommand(ctx, "git stash push -m 'WIP'");
        }
        return executeCommand(ctx, "git stash " + args);
    }

    private String executeGitMerge(CommandContext ctx) {
        String msg = ctx.getArgs().trim();
        if (msg.isEmpty()) {
            return "Usage: /merge <branch>\nExample: /merge main";
        }
        return executeCommand(ctx, "git merge " + msg);
    }

    private String executeGitRebase(CommandContext ctx) {
        String msg = ctx.getArgs().trim();
        if (msg.isEmpty()) {
            return "Usage: /rebase <branch>\nExample: /rebase main";
        }
        return executeCommand(ctx, "git rebase " + msg);
    }

    // File commands
    private String executeFileRead(CommandContext ctx) {
        String path = ctx.getArgs().trim();
        if (path.isEmpty()) {
            return "Usage: /read <file>\nExample: /read src/main/java/App.java";
        }
        return executeCommand(ctx, "cat " + path);
    }

    private String executeGlob(CommandContext ctx) {
        String pattern = ctx.getArgs().trim();
        if (pattern.isEmpty()) {
            return "Usage: /glob <pattern>\nExample: /glob *.java";
        }
        return executeCommand(ctx, "find . -name '" + pattern + "'");
    }

    // Web commands
    private String executeWebSearch(CommandContext ctx) {
        String query = ctx.getArgs().trim();
        if (query.isEmpty()) {
            return "Usage: /search <query>\nExample: /search Java 21 features";
        }
        // Would integrate with WebSearchTool
        return "Search: " + query + "\n[Web search integration pending]";
    }

    private String executeWebFetch(CommandContext ctx) {
        String url = ctx.getArgs().trim();
        if (url.isEmpty()) {
            return "Usage: /fetch <url>\nExample: /fetch https://example.com";
        }
        return executeCommand(ctx, "curl -sL " + url + " | head -100");
    }

    private String executeWget(CommandContext ctx) {
        String url = ctx.getArgs().trim();
        if (url.isEmpty()) {
            return "Usage: /wget <url>\nExample: /wget https://example.com/file.zip";
        }
        return executeCommand(ctx, "wget -q " + url);
    }

    // Session commands
    private String executeHistory(CommandContext ctx) {
        return executeCommand(ctx, "history | tail -20");
    }

    private String executeSessions(CommandContext ctx) {
        return "Sessions:\n" +
               "  - ~/.clipro/history/*.json\n" +
               "Use /history <n> to load a session";
    }

    private String executeCompact(CommandContext ctx) {
        return "Conversation compaction:\n" +
               "  - Old messages summarized\n" +
               "  - Token budget reduced\n" +
               "[Compaction in progress...]";
    }

    // Stats commands
    private String executeStats(CommandContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║                      CLIPRO Statistics                       ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append("║ Commands: ").append(String.format("%-46s║\n", commands.size() + " registered"));
        sb.append("║ Tools:    ").append(String.format("%-46s║\n", tools.size() + " registered"));
        sb.append("║ Model:    ").append(String.format("%-46s║\n", ctx.getCurrentModel()));
        sb.append("║ Provider: ").append(String.format("%-46s║\n", "Ollama/OpenRouter"));
        sb.append("╚══════════════════════════════════════════════════════════════╝\n");
        return sb.toString();
    }

    private String executeCost(CommandContext ctx) {
        return "API Cost Estimate:\n" +
               "  - Ollama: Free (local)\n" +
               "  - OpenRouter: ~$0.001/1K tokens\n" +
               "Use /tokens to see actual usage";
    }

    private String executeContext(CommandContext ctx) {
        if (ctx.hasAgentContext()) {
            return "Context: " + ctx.getTokenInfo();
        }
        return "Context: No active session";
    }

    // Config commands
    private String executeConfig(CommandContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("CLIPRO Configuration:\n");
        sb.append("  Config: ~/.clipro/config.json\n");
        sb.append("  Secrets: ~/.clipro/secrets.properties\n");
        sb.append("  History: ~/.clipro/history/\n");
        sb.append("  Logs:    ~/.clipro/logs/\n");
        return sb.toString();
    }

    private boolean hasApiKey() {
        return false; // Check ConfigManager for actual key
    }

    // Agent command handler
    private String executeAgent(CommandContext ctx) {
        if (agentManager == null) {
            agentManager = new AgentManager();
        }

        String args = ctx.getArgs().trim();
        if (args.isEmpty()) {
            return agentManager.renderAgentStatus();
        }

        String[] parts = args.split("\\s+", 3);
        String action = parts[0].toLowerCase();

        switch (action) {
            case "list":
                return agentManager.renderAgentStatus();
            case "create":
                if (parts.length >= 2) {
                    String name = parts[1];
                    String prompt = parts.length >= 3 ? parts[2] : "You are a helpful assistant.";
                    String model = ctx.getCurrentModel();
                    String id = agentManager.createAgent(name, model, prompt);
                    if (id != null) {
                        return "Agent created: " + id + " (model: " + model + ")";
                    }
                    return "Failed to create agent. Max agents reached.";
                }
                return "Usage: /agent create <name> [prompt]";
            case "switch":
                if (parts.length >= 2) {
                    if (agentManager.switchAgent(parts[1])) {
                        return "Switched to agent: " + parts[1];
                    }
                    return "Agent not found: " + parts[1];
                }
                return "Usage: /agent switch <id>";
            case "remove":
            case "delete":
                if (parts.length >= 2) {
                    if (agentManager.removeAgent(parts[1])) {
                        return "Agent removed: " + parts[1];
                    }
                    return "Failed to remove agent: " + parts[1] + " (may be main agent)";
                }
                return "Usage: /agent remove <id>";
            default:
                return "Agent commands:\n" +
                       "  /agent list    - Show all agents\n" +
                       "  /agent create  - Create new agent\n" +
                       "  /agent switch  - Switch to agent\n" +
                       "  /agent remove  - Remove agent";
        }
    }

    // M-03: Spawn sub-agent
    private String executeSpawn(CommandContext ctx) {
        if (agentManager == null) {
            agentManager = new AgentManager();
        }
        String args = ctx.getArgs().trim();
        if (args.isEmpty()) {
            return "Usage: /spawn <model> [prompt]\nExample: /spawn claude-sonnet-4";
        }
        String[] parts = args.split("\\s+", 2);
        String model = parts[0];
        String prompt = parts.length > 1 ? parts[1] : "You are a helpful assistant.";
        String id = agentManager.createAgent("sub-" + System.currentTimeMillis(), model, prompt);
        return id != null ? "Spawned agent: " + id + " (model: " + model + ")" : "Failed to spawn agent";
    }

    // M-03: Kill agent
    private String executeKill(CommandContext ctx) {
        if (agentManager == null) {
            return "Agent manager not initialized";
        }
        String agentId = ctx.getArgs().trim();
        if (agentId.isEmpty()) {
            return "Usage: /kill <agent-id>";
        }
        if (agentManager.removeAgent(agentId)) {
            return "Agent killed: " + agentId;
        }
        return "Failed to kill agent: " + agentId;
    }

    // M-03: Team management
    private String executeTeam(CommandContext ctx) {
        if (agentManager == null) {
            agentManager = new AgentManager();
        }
        String args = ctx.getArgs().trim();
        if (args.isEmpty()) {
            return agentManager.renderAgentStatus();
        }
        String[] parts = args.split("\\s+", 3);
        String action = parts[0].toLowerCase();
        switch (action) {
            case "create":
                return "Team created: " + (parts.length > 1 ? parts[1] : "team-1");
            case "add":
                return "Agent added to team: " + (parts.length > 1 ? parts[1] : "");
            case "remove":
                return "Agent removed from team: " + (parts.length > 1 ? parts[1] : "");
            case "list":
                return agentManager.renderAgentStatus();
            default:
                return "Team commands:\n  /team create <name>\n  /team add <team> <agent-id>\n  /team remove <team> <agent-id>\n  /team list";
        }
    }

    // M-04: MCP commands
    private String executeMcp(CommandContext ctx) {
        return "MCP Commands:\n" +
               "  /mcp            - Show MCP status\n" +
               "  /mcp list       - List running servers\n" +
               "  /mcp add        - Add server config\n" +
               "  /mcp remove     - Remove server\n" +
               "  /mcp start      - Start server\n" +
               "  /mcp stop       - Stop server\n" +
               "  /mcp tools      - List server tools";
    }

    private String executeMcpList(CommandContext ctx) {
        return "MCP Servers:\n  No servers configured.\nUse /mcp add <name> <command> to register.";
    }

    private String executeMcpAdd(CommandContext ctx) {
        String args = ctx.getArgs().trim();
        if (args.isEmpty()) {
            return "Usage: /mcp add <name> <command> [args...]\nExample: /mcp add github npx @anthropic/mcp-server-github";
        }
        return "MCP server added: " + args.split("\\s+")[0];
    }

    private String executeMcpRemove(CommandContext ctx) {
        String name = ctx.getArgs().trim();
        if (name.isEmpty()) {
            return "Usage: /mcp remove <name>";
        }
        return "MCP server removed: " + name;
    }

    private String executeMcpStart(CommandContext ctx) {
        String name = ctx.getArgs().trim();
        if (name.isEmpty()) {
            return "Usage: /mcp start <name>";
        }
        return "Starting MCP server: " + name + "...";
    }

    private String executeMcpStop(CommandContext ctx) {
        String name = ctx.getArgs().trim();
        if (name.isEmpty()) {
            return "Usage: /mcp stop <name>";
        }
        return "Stopping MCP server: " + name + "...";
    }

    private String executeMcpTools(CommandContext ctx) {
        String server = ctx.getArgs().trim();
        if (server.isEmpty()) {
            return "Usage: /mcp tools <server>";
        }
        return "Tools for " + server + ":\n  (no tools loaded)";
    }

    // M-05: Theme commands
    private String executeTheme(CommandContext ctx) {
        String args = ctx.getArgs().trim().toLowerCase();
        if (args.isEmpty()) {
            return "Current theme: dark\n" +
                   "Usage: /theme <option>\n" +
                   "  /theme dark           - Switch to dark theme\n" +
                   "  /theme light          - Switch to light theme\n" +
                   "  /theme dark-ansi      - Dark with ANSI colors\n" +
                   "  /theme light-ansi     - Light with ANSI colors\n" +
                   "  /theme auto           - Auto-detect from terminal\n" +
                   "  /theme preview        - Preview all themes";
        }
        switch (args) {
            case "dark":
            case "dark-ansi":
                return "Theme set to: dark";
            case "light":
            case "light-ansi":
                return "Theme set to: light";
            case "auto":
                return "Theme set to: auto (detecting terminal...)";
            case "preview":
                return "Theme Preview:\n" +
                       "┌─ Dark ─────────────────────────────────┐\n" +
                       "│ \033[38;2;255;255;255mWhite\033[0m \033[32mGreen\033[0m \033[33mYellow\033[0m \033[31mRed\033[0m \033[34mBlue\033[0m │\n" +
                       "└────────────────────────────────────────┘\n" +
                       "┌─ Light ────────────────────────────────┐\n" +
                       "│ \033[38;2;0;0;0mBlack\033[0m \033[32mGreen\033[0m \033[33mYellow\033[0m \033[31mRed\033[0m \033[34mBlue\033[0m │\n" +
                       "└────────────────────────────────────────┘";
            default:
                return "Unknown theme: " + args + "\nUse /theme for available options.";
        }
    }

    /**
     * Get help text for all commands.
     */
    public String showHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    CLIPRO Commands                            ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Core commands
        sb.append("║ Core Commands                                                ║\n");
        sb.append("║   /help      - Show this help                                ║\n");
        sb.append("║   /clear     - Clear conversation                           ║\n");
        sb.append("║   /exit      - Exit CLIPRO                                   ║\n");
        sb.append("║   /quit      - Exit CLIPRO                                   ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Model commands
        sb.append("║ Model Commands                                               ║\n");
        sb.append("║   /model     - Show current model                           ║\n");
        sb.append("║   /models    - List available models                        ║\n");
        sb.append("║   /provider  - Show/switch LLM provider                    ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Git commands
        sb.append("║ Git Commands                                                ║\n");
        sb.append("║   /status    - Show git status                              ║\n");
        sb.append("║   /diff      - Show changes                                 ║\n");
        sb.append("║   /log       - Show commit history                          ║\n");
        sb.append("║   /commit    - Commit with message                          ║\n");
        sb.append("║   /branch    - Show git branches                            ║\n");
        sb.append("║   /stash     - Git stash (pop/push/list)                     ║\n");
        sb.append("║   /pull      - Git pull                                     ║\n");
        sb.append("║   /push      - Git push                                     ║\n");
        sb.append("║   /fetch     - Git fetch                                    ║\n");
        sb.append("║   /merge     - Git merge                                    ║\n");
        sb.append("║   /rebase    - Git rebase                                   ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Search/File commands
        sb.append("║ Search Commands                                             ║\n");
        sb.append("║   /grep      - Search in files                              ║\n");
        sb.append("║   /find      - Find files                                   ║\n");
        sb.append("║   /glob      - Glob pattern search                          ║\n");
        sb.append("║   /read      - Read file content                            ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Shell commands
        sb.append("║ Shell Commands                                              ║\n");
        sb.append("║   /bash      - Execute bash command                         ║\n");
        sb.append("║   /ls        - List directory                              ║\n");
        sb.append("║   /pwd       - Print working directory                      ║\n");
        sb.append("║   /whoami    - Current user                                ║\n");
        sb.append("║   /date      - Current date                                ║\n");
        sb.append("║   /env       - Environment variables                        ║\n");
        sb.append("║   /uptime    - System uptime                               ║\n");
        sb.append("║   /df        - Disk usage                                   ║\n");
        sb.append("║   /free      - Memory usage                                 ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Web commands
        sb.append("║ Web Commands                                                ║\n");
        sb.append("║   /search    - Search the web                                ║\n");
        sb.append("║   /fetch     - Fetch URL content                             ║\n");
        sb.append("║   /wget      - Download file                                ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Session commands
        sb.append("║ Session Commands                                            ║\n");
        sb.append("║   /history   - Show command history                         ║\n");
        sb.append("║   /sessions  - List saved sessions                          ║\n");
        sb.append("║   /compact   - Compact conversation                         ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Stats/Config commands
        sb.append("║ Stats Commands                                              ║\n");
        sb.append("║   /tokens    - Show token usage                             ║\n");
        sb.append("║   /stats     - Show statistics                              ║\n");
        sb.append("║   /cost      - API cost estimate                            ║\n");
        sb.append("║   /context   - Context usage                                ║\n");
        sb.append("║   /config    - Show configuration                           ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Dev commands
        sb.append("║ Developer Commands                                          ║\n");
        sb.append("║   /test      - Run tests                                    ║\n");
        sb.append("║   /build     - Build project                                 ║\n");
        sb.append("║   /clean     - Clean build                                   ║\n");
        sb.append("║   /jar       - Build JAR                                    ║\n");
        sb.append("║   /debug     - Debug mode                                   ║\n");
        sb.append("║   /info      - System information                            ║\n");
        sb.append("║   /mode      - Permission mode (READ_ONLY/BASH/RESTRICTED)   ║\n");
        sb.append("║   /version   - Show version                                  ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n");

        sb.append("\n").append(commands.size()).append(" commands registered");
        sb.append(" | ").append(tools.size()).append(" tools available");

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

        public boolean hasAgentContext() { return agentContext != null; }

        public String getCurrentModel() {
            return agentContext != null ? agentContext.getCurrentModel() : "unknown";
        }

        public String getTokenInfo() {
            return agentContext != null ? agentContext.getTokenInfo() : "unknown";
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
        default String getTokenInfo() { return "unknown"; }
    }
}
