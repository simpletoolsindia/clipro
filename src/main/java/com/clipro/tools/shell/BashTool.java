package com.clipro.tools.shell;

import com.clipro.tools.Tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Bash tool with permission system and security.
 * Implements Read-only and BASH permission modes.
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

    private final Path workingDirectory;
    private final Map<String, String> environment;
    private PermissionMode permissionMode = PermissionMode.BASH;
    private Path sandboxDirectory = null;

    public BashTool() {
        this.workingDirectory = Path.of(System.getProperty("user.dir"));
        this.environment = new HashMap<>();
    }

    public BashTool(String workingDir) {
        this.workingDirectory = Path.of(workingDir);
        this.environment = new HashMap<>();
    }

    /**
     * Set a sandbox directory for command execution.
     * Commands can only access files within this directory.
     */
    public void setSandboxDirectory(Path path) {
        this.sandboxDirectory = path.toAbsolutePath();
    }

    /**
     * Set a sandbox directory from string.
     */
    public void setSandboxDirectory(String path) {
        this.sandboxDirectory = Paths.get(path).toAbsolutePath();
    }

    public Path getSandboxDirectory() {
        return sandboxDirectory;
    }

    /**
     * Check if a path is within the sandbox.
     */
    public boolean isPathInSandbox(String path) {
        if (sandboxDirectory == null) {
            return true; // No sandbox set
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

        // Check permission mode
        Object modeObj = args.get("permissionMode");
        PermissionMode mode = this.permissionMode;
        if (modeObj instanceof String modeStr) {
            try {
                mode = PermissionMode.valueOf(modeStr);
            } catch (IllegalArgumentException ignored) {}
        }

        // Security: Check command safety
        SecurityCheck check = checkCommandSecurity(command, mode);
        if (!check.allowed) {
            return "Error: " + check.reason;
        }

        // Check for destructive commands
        if (isDestructive(command)) {
            return "Warning: Destructive command '" + extractCommand(command) + "' requires explicit confirmation. " +
                   "Use permissionMode: BASH to override.";
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

    private SecurityCheck checkCommandSecurity(String command, PermissionMode mode) {
        String normalized = command.trim().toLowerCase();
        String primaryCmd = extractCommand(normalized);

        // Path traversal prevention
        if (command.contains("..")) {
            if (!isPathTraversalAllowed(command)) {
                return new SecurityCheck(false, "Path traversal not allowed in this mode");
            }
            // Check if path is in sandbox
            if (sandboxDirectory != null) {
                if (!isSandboxCompliant(command)) {
                    return new SecurityCheck(false, "Path outside sandbox directory: " + sandboxDirectory);
                }
            }
        }

        // Sandbox enforcement (even without traversal)
        if (sandboxDirectory != null && containsFilePath(command)) {
            if (!isSandboxCompliant(command)) {
                return new SecurityCheck(false, "Access outside sandbox: " + sandboxDirectory);
            }
        }

        switch (mode) {
            case READ_ONLY:
                // Only safe read commands allowed
                if (!SAFE_READ_COMMANDS.contains(primaryCmd)) {
                    // Special case: git is read-mostly
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
                // Only safe commands
                if (!SAFE_READ_COMMANDS.contains(primaryCmd) && !SAFE_WRITE_COMMANDS.contains(primaryCmd)) {
                    return new SecurityCheck(false,
                        "Command '" + primaryCmd + "' not allowed in RESTRICTED mode");
                }
                return new SecurityCheck(true, null);

            case BASH:
            default:
                // Full access but still check for dangerous patterns
                if (command.contains("&&") || command.contains("||")) {
                    // Compound command - check all parts
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
        // Allow in BASH mode only, and only within sandbox
        if (permissionMode == PermissionMode.BASH) {
            return sandboxDirectory == null || isSandboxCompliant(command);
        }
        return false;
    }

    /**
     * Check if command path references are within sandbox.
     */
    private boolean isSandboxCompliant(String command) {
        if (sandboxDirectory == null) return true;

        // Extract potential file paths from command
        String[] parts = command.split("\\s+");
        for (String part : parts) {
            // Skip options and flags
            if (part.startsWith("-") || part.startsWith("$")) continue;

            // Check for path-like strings
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

    /**
     * Check if command contains file path references.
     */
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

            // Add environment variables
            Map<String, String> env = builder.environment();
            env.putAll(environment);

            Process process = builder.start();

            // Read output with timeout
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
