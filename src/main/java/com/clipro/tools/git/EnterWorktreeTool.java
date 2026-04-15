package com.clipro.tools.git;

import com.clipro.tools.Tool;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Tool for creating and entering a git worktree for parallel development.
 *
 * Reference: openclaude/src/utils/worktree.ts
 */
public class EnterWorktreeTool implements Tool {

    // Active worktrees
    private final Map<String, WorktreeInfo> worktrees = new HashMap<>();

    @Override
    public String getName() {
        return "enter_worktree";
    }

    @Override
    public String getDescription() {
        return "Create and enter a new git worktree for parallel development. " +
               "Worktrees allow working on multiple branches simultaneously.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "name", Map.of(
                    "type", "string",
                    "description", "Name for the worktree (used in path)"
                ),
                "branch", Map.of(
                    "type", "string",
                    "description", "Branch name to create or use"
                ),
                "path", Map.of(
                    "type", "string",
                    "description", "Path for the worktree directory"
                ),
                "createBranch", Map.of(
                    "type", "boolean",
                    "description", "Create a new branch if true"
                )
            ),
            "required", List.of("name", "branch")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String name = getString(args, "name", "");
        String branch = getString(args, "branch", "");
        String path = getString(args, "path", null);
        boolean createBranch = getBoolean(args, "createBranch", false);

        if (name.isEmpty() || branch.isEmpty()) {
            return "{\"error\": \"Missing required parameters: name, branch\"}";
        }

        // Validate name
        if (!isValidWorktreeName(name)) {
            return "{\"error\": \"Invalid worktree name\"}";
        }

        try {
            // Determine worktree path
            String worktreePath = path != null ? path :
                ".claude/worktrees/" + name;

            // Check if worktree already exists
            if (worktrees.containsKey(name)) {
                return "{\"error\": \"Worktree already exists: " + name + "\"}";
            }

            // Get current directory
            String cwd = System.getProperty("user.dir");

            // Build git worktree add command
            ProcessBuilder pb = new ProcessBuilder(
                "git", "worktree", "add",
                "--" + (createBranch ? "" : "no-create") + "detach",
                worktreePath, branch
            );
            pb.directory(new File(cwd));

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String error = new String(process.getErrorStream().readAllBytes());
                return "{\"error\": \"Git worktree add failed: " + escapeJson(error) + "\"}";
            }

            // Create worktree info
            WorktreeInfo info = new WorktreeInfo(name, branch, worktreePath, cwd);
            worktrees.put(name, info);

            return String.format(
                "{\n  \"worktreeName\": \"%s\",\n  \"branch\": \"%s\",\n  \"path\": \"%s\",\n  \"originalPath\": \"%s\",\n  \"status\": \"entered\"\n}",
                name,
                branch,
                worktreePath,
                cwd
            );

        } catch (Exception e) {
            return "{\"error\": \"Failed to create worktree: " + escapeJson(e.getMessage()) + "\"}";
        }
    }

    /**
     * Validate worktree name.
     */
    private boolean isValidWorktreeName(String name) {
        if (name == null || name.isEmpty() || name.length() > 64) {
            return false;
        }
        // No path traversal
        if (name.contains("..") || name.contains("/")) {
            return false;
        }
        // Only alphanumeric, dots, underscores, dashes
        return name.matches("[a-zA-Z0-9._-]+");
    }

    /**
     * Get worktree info.
     */
    public WorktreeInfo getWorktree(String name) {
        return worktrees.get(name);
    }

    /**
     * List all worktrees.
     */
    public List<WorktreeInfo> listWorktrees() {
        return new ArrayList<>(worktrees.values());
    }

    /**
     * Check if in a worktree.
     */
    public boolean isInWorktree() {
        return !worktrees.isEmpty();
    }

    /**
     * Worktree info record.
     */
    public static final class WorktreeInfo {
        public final String name;
        public final String branch;
        public final String path;
        public final String originalPath;

        public WorktreeInfo(String name, String branch, String path, String originalPath) {
            this.name = name;
            this.branch = branch;
            this.path = path;
            this.originalPath = originalPath;
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
