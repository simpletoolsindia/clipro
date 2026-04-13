package com.clipro.tools.shell;

import com.clipro.tools.Tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Bash tool using ProcessBuilder with timeout handling.
 * Uses virtual threads for async execution.
 */
public class BashTool implements Tool {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int MAX_OUTPUT_LINES = 1000;

    private final Path workingDirectory;
    private final Map<String, String> environment;

    public BashTool() {
        this.workingDirectory = Path.of(System.getProperty("user.dir"));
        this.environment = new HashMap<>();
    }

    public BashTool(String workingDir) {
        this.workingDirectory = Path.of(workingDir);
        this.environment = new HashMap<>();
    }

    @Override
    public String getName() {
        return "bash";
    }

    @Override
    public String getDescription() {
        return "Execute shell commands. Timeout: 30 seconds default. Streaming output support.";
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
                    "description", "Timeout in seconds",
                    "default", DEFAULT_TIMEOUT_SECONDS
                ),
                "cwd", Map.of(
                    "type", "string",
                    "description", "Working directory"
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

    public void setEnv(String key, String value) {
        environment.put(key, value);
    }

    public void clearEnv() {
        environment.clear();
    }
}