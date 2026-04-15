package com.clipro.tools.git;

import com.clipro.tools.Tool;

import java.io.File;
import java.util.*;

/**
 * Tool for exiting and optionally removing a git worktree.
 *
 * Reference: openclaude/src/utils/worktree.ts
 */
public class ExitWorktreeTool implements Tool {

    private final EnterWorktreeTool enterWorktreeTool;

    public ExitWorktreeTool() {
        // Share worktree state with EnterWorktreeTool via static access
        this.enterWorktreeTool = new EnterWorktreeTool();
    }

    @Override
    public String getName() {
        return "exit_worktree";
    }

    @Override
    public String getDescription() {
        return "Exit a git worktree and optionally remove it.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "name", Map.of(
                    "type", "string",
                    "description", "Name of the worktree to exit"
                ),
                "remove", Map.of(
                    "type", "boolean",
                    "description", "Remove the worktree after exiting"
                ),
                "force", Map.of(
                    "type", "boolean",
                    "description", "Force remove even with uncommitted changes"
                )
            ),
            "required", List.of("name")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String name = getString(args, "name", "");
        boolean remove = getBoolean(args, "remove", false);
        boolean force = getBoolean(args, "force", false);

        if (name.isEmpty()) {
            return "{\"error\": \"Missing required parameter: name\"}";
        }

        var worktreeInfo = enterWorktreeTool.getWorktree(name);
        if (worktreeInfo == null) {
            return "{\"error\": \"Worktree not found: " + name + "\"}";
        }

        try {
            String cwd = System.getProperty("user.dir");

            if (remove) {
                // Remove the worktree
                List<String> cmd = new ArrayList<>();
                cmd.add("git");
                cmd.add("worktree");
                cmd.add("remove");
                if (force) cmd.add("--force");
                cmd.add(worktreeInfo.path);

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.directory(new File(worktreeInfo.originalPath));
                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    String error = new String(process.getErrorStream().readAllBytes());
                    return "{\"error\": \"Git worktree remove failed: " + escapeJson(error) + "\"}";
                }
            }

            // Return to original directory
            System.setProperty("user.dir", worktreeInfo.originalPath);

            return String.format(
                "{\n  \"worktreeName\": \"%s\",\n  \"branch\": \"%s\",\n  \"removed\": %s,\n  \"returnedTo\": \"%s\"\n}",
                name,
                worktreeInfo.branch,
                remove,
                worktreeInfo.originalPath
            );

        } catch (Exception e) {
            return "{\"error\": \"Failed to exit worktree: " + escapeJson(e.getMessage()) + "\"}";
        }
    }

    private String getString(Map<String, Object> args, String key, String defaultValue) {
        Object value = args.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private boolean getBoolean(Map<String, Object> args, String key, boolean defaultValue) {
        Object value = args.get(key);
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        return defaultValue;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
