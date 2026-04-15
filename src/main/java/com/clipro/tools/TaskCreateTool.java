package com.clipro.tools;

import java.util.*;

/**
 * Tool for creating tasks/to-dos.
 * Part of the task management tools suite.
 *
 * Reference: openclaude/src/tools/TaskCreateTool.ts
 */
public class TaskCreateTool implements Tool {

    private final TaskStore taskStore;

    public TaskCreateTool() {
        this.taskStore = TaskStore.getInstance();
    }

    @Override
    public String getName() {
        return "task_create";
    }

    @Override
    public String getDescription() {
        return "Create a new task. Returns the task ID for tracking.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "title", Map.of(
                    "type", "string",
                    "description", "The task title/description"
                ),
                "notes", Map.of(
                    "type", "string",
                    "description", "Optional notes or details about the task"
                ),
                "priority", Map.of(
                    "type", "string",
                    "description", "Priority level: high, medium, low",
                    "enum", List.of("high", "medium", "low")
                ),
                "parentId", Map.of(
                    "type", "string",
                    "description", "Optional parent task ID for subtasks"
                ),
                "status", Map.of(
                    "type", "string",
                    "description", "Initial status: pending, in_progress, blocked",
                    "enum", List.of("pending", "in_progress", "blocked")
                )
            ),
            "required", List.of("title")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String title = getString(args, "title", "");
        if (title.isEmpty()) {
            return "{\"error\": \"Missing required parameter: title\"}";
        }

        String notes = getString(args, "notes", "");
        String priority = getString(args, "priority", "medium");
        String parentId = getString(args, "parentId", null);
        String status = getString(args, "status", "pending");

        TaskStore.Task task = taskStore.create(title, notes, priority, status, parentId);

        return String.format(
            "{\n  \"taskId\": \"%s\",\n  \"title\": \"%s\",\n  \"status\": \"%s\",\n  \"priority\": \"%s\",\n  \"createdAt\": %d\n}",
            task.id,
            escapeJson(task.title),
            task.status,
            task.priority,
            task.createdAt
        );
    }

    private String getString(Map<String, Object> args, String key, String defaultValue) {
        Object value = args.get(key);
        return value != null ? value.toString() : defaultValue;
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
