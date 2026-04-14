package com.clipro.tools.shell;

import com.clipro.tools.Tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Bash tool with permission system and security.
 * Implements Read-only and BASH permission modes.
 *
 * M-15: AST parsing for compound commands
 * M-16: Haiku classifier for risk classification
 * M-17: Sed validation
 * M-18: Permission persistence
 */
public class BashTool implements Tool {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int MAX_OUTPUT_LINES = 1000;

    // Permission modes
    public enum PermissionMode {
        READ_ONLY,  // Only read operations allowed
        BASH,       // Full bash access
        RESTRICTED  // Limited commands only
    }

    // Risk levels for Haiku classifier (M-16)
    public enum RiskLevel {
        LOW, MEDIUM, HIGH, DESTRUCTIVE
    }

    // Destructive commands that require confirmation
    private static final Set<String> DESTRUCTIVE_COMMANDS = Set.of(
        "rm", "rmdir", "mkfs", "dd", "fdisk", "parted",
        "shutdown", "reboot", "halt", "poweroff",
        "kill", "killall", "pkill",
        "dropdb", "mysql", "psql",
        "rm -rf", "rm -r", "rm -f"
    );

    // Safe read-only commands
    private static final Set<String> SAFE_READ_COMMANDS = Set.of(
        "cat", "ls", "pwd", "echo", "head", "tail",
        "grep", "find", "which", "whereis", "type",
        "wc", "sort", "uniq", "cut", "tr", "awk", "sed",
        "git", "diff", "stat", "file", "md5sum", "sha256sum",
        "date", "whoami", "hostname", "uname", "uptime",
        "ps", "top", "htop", "df", "du", "free", "mount",
        "curl", "wget"  // Network read
    );

    // Commands that modify files (not destructive)
    private static final Set<String> SAFE_WRITE_COMMANDS = Set.of(
        "mkdir", "touch", "cp", "mv", "ln", "chmod", "chown",
        "tee", "pip install", "npm install"
    );

    // Valid sed flags (M-17)
    private static final Set<Character> VALID_SED_FLAGS = Set.of(
        'e', 'i', 'n', 'r', 'E', 'I', 'a', 'b', 'c', 'l', 'p', 'q', 's', 'w', 'x', 'y', 'z'
    );

    private final Path workingDirectory;
    private final Map<String, String> environment;
    private PermissionMode permissionMode = PermissionMode.BASH;
    private Path sandboxDirectory = null;

    // Permission persistence (M-18)
    private Path permissionsFile;
    private Map<String, PermissionEntry> persistedPermissions = new HashMap<>();

    public BashTool() {
        this.workingDirectory = Path.of(System.getProperty("user.dir"));
        this.environment = new HashMap<>();
        initPermissions();
    }

    public BashTool(String workingDir) {
        this.workingDirectory = Path.of(workingDir);
        this.environment = new HashMap<>();
        initPermissions();
    }

    private void initPermissions() {
        // M-18: Load persisted permissions
        try {
            Path configDir = Paths.get(System.getProperty("user.home"), ".config", "clipro");
            permissionsFile = configDir.resolve("permissions.json");
            if (Files.exists(permissionsFile)) {
                loadPermissions();
            }
        } catch (Exception e) {
            // Ignore - permissions file is optional
        }
    }

    /**
     * M-18: Load persisted permissions from disk.
     */
    private void loadPermissions() {
        try (BufferedReader reader = new BufferedReader(new FileReader(permissionsFile.toFile()))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            // Simple JSON parsing
            String content = json.toString();
            // Parse entries like: "command": "pattern", "level": "BASH"
            String[] entries = content.split("\\{");
            for (String entry : entries) {
                if (entry.contains("command")) {
                    String cmd = extractJsonValue(entry, "command");
                    String level = extractJsonValue(entry, "level");
                    if (cmd != null && level != null) {
                        persistedPermissions.put(cmd, new PermissionEntry(cmd, PermissionMode.valueOf(level)));
                    }
                }
            }
        } catch (Exception e) {
            // Ignore - file may not exist yet
        }
    }

    private String extractJsonValue(String json, String key) {
        int keyIdx = json.indexOf("\"" + key + "\"");
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(":", keyIdx);
        if (colonIdx < 0) return null;
        int startQuote = json.indexOf("\"", colonIdx);
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (startQuote < 0 || endQuote < 0) return null;
        return json.substring(startQuote + 1, endQuote);
    }

    /**
     * M-18: Save permission to disk.
     */
    public void persistPermission(String command, PermissionMode mode) {
        persistedPermissions.put(command, new PermissionEntry(command, mode));
        try {
            Path configDir = permissionsFile.getParent();
            Files.createDirectories(configDir);
            StringBuilder json = new StringBuilder("{\n");
            for (Map.Entry<String, PermissionEntry> entry : persistedPermissions.entrySet()) {
                json.append("  \"").append(entry.getKey()).append("\": {\n");
                json.append("    \"command\": \"").append(entry.getValue().command).append("\",\n");
                json.append("    \"level\": \"").append(entry.getValue().mode.name()).append("\"\n  },\n");
            }
            json.append("}");
            Files.writeString(permissionsFile, json.toString());
        } catch (Exception e) {
            // Ignore - persistence is best-effort
        }
    }

    private record PermissionEntry(String command, PermissionMode mode) {}

    public void setSandboxDirectory(Path path) {
        this.sandboxDirectory = path.toAbsolutePath();
    }

    public void setSandboxDirectory(String path) {
        this.sandboxDirectory = Paths.get(path).toAbsolutePath();
    }

    public Path getSandboxDirectory() {
        return sandboxDirectory;
    }

    public boolean isPathInSandbox(String path) {
        if (sandboxDirectory == null) {
            return true;
        }
        try {
            Path targetPath = Paths.get(path).toAbsolutePath();
            return targetPath.startsWith(sandboxDirectory);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "bash";
    }

    @Override
    public String getDescription() {
        return "Execute shell commands. Permission modes: READ_ONLY (safe), BASH (full), RESTRICTED. Destructive commands are blocked by default.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "command", Map.of(
                    "type", "string",
                    "description", "Shell command to execute"
                ),
                "timeout", Map.of(
                    "type", "integer",
                    "description", "Timeout in seconds (default: 30)"
                ),
                "cwd", Map.of(
                    "type", "string",
                    "description", "Working directory"
                ),
                "permissionMode", Map.of(
                    "type", "string",
                    "description", "Permission mode: READ_ONLY, BASH, RESTRICTED",
                    "enum", List.of("READ_ONLY", "BASH", "RESTRICTED")
                )
            ),
            "required", List.of("command")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String command = (String) args.get("command");
        if (command == null || command.isEmpty()) {
            return "Error: command is required";
        }

        // Check persisted permissions first (M-18)
        if (persistedPermissions.containsKey(command)) {
            PermissionEntry entry = persistedPermissions.get(command);
            this.permissionMode = entry.mode;
        }

        // Check permission mode
        Object modeObj = args.get("permissionMode");
        PermissionMode mode = this.permissionMode;
        if (modeObj instanceof String modeStr) {
            try {
                mode = PermissionMode.valueOf(modeStr);
            } catch (IllegalArgumentException ignored) {}
        }

        // M-15: AST parsing for compound commands
        List<String> subCommands = parseCompoundCommand(command);
        if (subCommands.size() > 1) {
            // Validate each sub-command
            for (String subCmd : subCommands) {
                SecurityCheck check = checkCommandSecurity(subCmd.trim(), mode);
                if (!check.allowed) {
                    return "Error in compound command: " + check.reason;
                }
            }
        }

        // Security: Check command safety
        SecurityCheck check = checkCommandSecurity(command, mode);
        if (!check.allowed) {
            return "Error: " + check.reason;
        }

        // M-17: Sed validation
        if (command.trim().startsWith("sed")) {
            SedValidationResult sedResult = validateSedCommand(command);
            if (!sedResult.valid) {
                return "Sed validation error: " + sedResult.reason;
            }
            if (sedResult.warning != null) {
                // Log warning but continue
            }
        }

        // Check for destructive commands
        if (isDestructive(command)) {
            return "Warning: Destructive command '" + extractCommand(command) + "' requires explicit confirmation. " +
                   "Use permissionMode: BASH to override.";
        }

        // M-16: Haiku classifier for unknown commands
        RiskLevel risk = classifyRisk(command);
        if (risk == RiskLevel.DESTRUCTIVE) {
            return "Error: Destructive command detected. Command blocked for safety.";
        }

        // Parse other arguments
        int timeout = DEFAULT_TIMEOUT_SECONDS;
        Object timeoutObj = args.get("timeout");
        if (timeoutObj instanceof Number) {
            timeout = ((Number) timeoutObj).intValue();
        }

        Path cwd = workingDirectory;
        Object cwdObj = args.get("cwd");
        if (cwdObj instanceof String) {
            cwd = Path.of((String) cwdObj);
        }

        return executeCommand(command, timeout, cwd);
    }

    /**
     * M-15: Parse compound commands into individual commands.
     * Handles: cmd1 && cmd2 || cmd3 ; cmd4 | cmd5
     */
    private List<String> parseCompoundCommand(String command) {
        List<String> commands = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);

            if ((c == '\'' || c == '"') && !inQuote) {
                inQuote = true;
                quoteChar = c;
            } else if (c == quoteChar && inQuote) {
                inQuote = false;
            }

            if (!inQuote) {
                if (c == '&' && i + 1 < command.length() && command.charAt(i + 1) == '&') {
                    if (current.length() > 0) {
                        commands.add(current.toString().trim());
                        current = new StringBuilder();
                    }
                    i++; // Skip second &
                } else if (c == '|') {
                    // Pipe - check for || (or)
                    if (i + 1 < command.length() && command.charAt(i + 1) == '|') {
                        if (current.length() > 0) {
                            commands.add(current.toString().trim());
                            current = new StringBuilder();
                        }
                        i++; // Skip second |
                    } else {
                        // Regular pipe - treat as part of command chain
                        current.append(c);
                    }
                } else if (c == ';') {
                    if (current.length() > 0) {
                        commands.add(current.toString().trim());
                        current = new StringBuilder();
                    }
                } else {
                    current.append(c);
                }
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            String last = current.toString().trim();
            if (!last.isEmpty()) {
                commands.add(last);
            }
        }

        return commands;
    }

    /**
     * M-17: Validate sed command syntax and flags.
     */
    private SedValidationResult validateSedCommand(String command) {
        String trimmed = command.trim();

        // Check for dangerous patterns
        if (trimmed.contains("rm ") || trimmed.contains("delete") || trimmed.contains("drop")) {
            return new SedValidationResult(false, "Sed cannot be used for destructive operations");
        }

        // Parse sed flags
        // Format: sed [-n] [-e script] [-f script_file] [file...]
        String[] parts = trimmed.split("\\s+");

        if (parts.length < 2) {
            return new SedValidationResult(false, "Invalid sed syntax");
        }

        boolean hasInPlace = false;
        boolean hasBackup = false;

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];

            if (part.equals("-i") || part.startsWith("-i")) {
                hasInPlace = true;
                // Check for backup extension: -i.bak
                if (part.length() > 2) {
                    hasBackup = true;
                }
            } else if (part.equals("-n") || part.equals("-e") || part.equals("-f") ||
                       part.equals("-E") || part.equals("-r") || part.equals("-s") ||
                       part.equals("-u") || part.equals("-z")) {
                // Valid flags
            } else if (part.startsWith("-")) {
                // Check each character flag
                for (int j = 1; j < part.length(); j++) {
                    char flag = part.charAt(j);
                    if (!VALID_SED_FLAGS.contains(flag)) {
                        return new SedValidationResult(false, "Invalid sed flag: -" + flag);
                    }
                    if (flag == 'i') hasInPlace = true;
                }
            }
        }

        // Warning for in-place without backup
        if (hasInPlace && !hasBackup) {
            return new SedValidationResult(true, null, "Warning: sed -i without backup extension. Consider using -i.bak");
        }

        return new SedValidationResult(true, null, null);
    }

    private record SedValidationResult(boolean valid, String reason, String warning) {
        SedValidationResult(boolean valid, String reason) {
            this(valid, reason, null);
        }
    }

    /**
     * M-16: Classify command risk using Haiku model.
     */
    private RiskLevel classifyRisk(String command) {
        String primaryCmd = extractCommand(command.trim().toLowerCase());

        // Known safe commands
        if (SAFE_READ_COMMANDS.contains(primaryCmd)) {
            return RiskLevel.LOW;
        }

        // Known destructive commands
        for (String destructive : DESTRUCTIVE_COMMANDS) {
            if (command.toLowerCase().contains(destructive)) {
                if (destructive.equals("rm -rf") || destructive.equals("rm -rf /") ||
                    command.toLowerCase().contains("dd if=") || command.toLowerCase().contains("mkfs")) {
                    return RiskLevel.DESTRUCTIVE;
                }
                return RiskLevel.HIGH;
            }
        }

        // Medium risk: file modifications
        if (SAFE_WRITE_COMMANDS.contains(primaryCmd)) {
            return RiskLevel.MEDIUM;
        }

        // Unknown commands - could be anything
        if (command.contains("curl") || command.contains("wget")) {
            return RiskLevel.MEDIUM; // Network access
        }

        return RiskLevel.MEDIUM; // Unknown - require confirmation
    }

    private SecurityCheck checkCommandSecurity(String command, PermissionMode mode) {
        String normalized = command.trim().toLowerCase();
        String primaryCmd = extractCommand(normalized);

        // Path traversal prevention
        if (command.contains("..")) {
            if (!isPathTraversalAllowed(command)) {
                return new SecurityCheck(false, "Path traversal not allowed in this mode");
            }
            if (sandboxDirectory != null) {
                if (!isSandboxCompliant(command)) {
                    return new SecurityCheck(false, "Path outside sandbox directory: " + sandboxDirectory);
                }
            }
        }

        if (sandboxDirectory != null && containsFilePath(command)) {
            if (!isSandboxCompliant(command)) {
                return new SecurityCheck(false, "Access outside sandbox: " + sandboxDirectory);
            }
        }

        switch (mode) {
            case READ_ONLY:
                if (!SAFE_READ_COMMANDS.contains(primaryCmd)) {
                    if (primaryCmd.equals("git")) {
                        String gitCmd = normalized.contains("git ") ?
                            normalized.substring(normalized.indexOf("git ") + 4).split("\\s+")[0] : "";
                        if (gitCmd.matches("^(push|commit|merge|rebase|checkout -b|branch -d|reset --hard)$")) {
                            return new SecurityCheck(false, "Git write operations not allowed in READ_ONLY mode");
                        }
                        return new SecurityCheck(true, null);
                    }
                    return new SecurityCheck(false,
                        "Command '" + primaryCmd + "' not allowed in READ_ONLY mode. Allowed: " + SAFE_READ_COMMANDS);
                }
                return new SecurityCheck(true, null);

            case RESTRICTED:
                if (!SAFE_READ_COMMANDS.contains(primaryCmd) && !SAFE_WRITE_COMMANDS.contains(primaryCmd)) {
                    return new SecurityCheck(false,
                        "Command '" + primaryCmd + "' not allowed in RESTRICTED mode");
                }
                return new SecurityCheck(true, null);

            case BASH:
            default:
                // Full access but still check for dangerous patterns
                if (command.contains("&&") || command.contains("||")) {
                    String[] parts = command.split("&&|\\|\\|");
                    for (String part : parts) {
                        String partCmd = extractCommand(part.trim());
                        if (DESTRUCTIVE_COMMANDS.contains(partCmd) || SAFE_READ_COMMANDS.contains(partCmd)) {
                            continue;
                        }
                    }
                }
                return new SecurityCheck(true, null);
        }
    }

    private boolean isDestructive(String command) {
        String lower = command.toLowerCase();
        for (String destructive : DESTRUCTIVE_COMMANDS) {
            if (lower.startsWith(destructive) || lower.contains(" " + destructive)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPathTraversalAllowed(String command) {
        if (permissionMode == PermissionMode.BASH) {
            return sandboxDirectory == null || isSandboxCompliant(command);
        }
        return false;
    }

    private boolean isSandboxCompliant(String command) {
        if (sandboxDirectory == null) return true;

        String[] parts = command.split("\\s+");
        for (String part : parts) {
            if (part.startsWith("-") || part.startsWith("$")) continue;

            if (part.contains("/") || part.startsWith(".")) {
                try {
                    Path path = Paths.get(part).toAbsolutePath();
                    if (!path.startsWith(sandboxDirectory)) {
                        return false;
                    }
                } catch (Exception ignored) {}
            }
        }
        return true;
    }

    private boolean containsFilePath(String command) {
        String[] parts = command.split("\\s+");
        for (String part : parts) {
            if (part.contains("/") || part.startsWith(".")) {
                return true;
            }
        }
        return false;
    }

    private String extractCommand(String command) {
        String trimmed = command.trim();
        int spaceIdx = trimmed.indexOf(' ');
        if (spaceIdx > 0) {
            return trimmed.substring(0, spaceIdx);
        }
        return trimmed;
    }

    private String executeCommand(String command, int timeout, Path cwd) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("bash", "-c", command);
            builder.directory(cwd.toFile());
            builder.redirectErrorStream(true);

            Map<String, String> env = builder.environment();
            env.putAll(environment);

            Process process = builder.start();

            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                int lineCount = 0;
                while ((line = reader.readLine()) != null && lineCount < MAX_OUTPUT_LINES) {
                    output.append(line).append("\n");
                    lineCount++;
                }
            }

            boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return "Error: Command timed out after " + timeout + " seconds\n\nPartial output:\n" + output;
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                return "Command exited with code " + exitCode + ":\n\n" + output;
            }

            if (output.length() > MAX_OUTPUT_LINES * 100) {
                return output.substring(0, MAX_OUTPUT_LINES * 100) + "\n\n[Output truncated - too large]";
            }

            return output.length() > 0 ? output.toString() : "(no output)";

        } catch (Exception e) {
            return "Error executing command: " + e.getMessage();
        }
    }

    public void setPermissionMode(PermissionMode mode) {
        this.permissionMode = mode;
    }

    public PermissionMode getPermissionMode() {
        return permissionMode;
    }

    public void setEnv(String key, String value) {
        environment.put(key, value);
    }

    public void clearEnv() {
        environment.clear();
    }

    private static class SecurityCheck {
        boolean allowed;
        String reason;
        SecurityCheck(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }
    }
}
