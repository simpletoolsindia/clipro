package com.clipro.tools;

import java.util.*;

/**
 * Tool for updating tasks.
 * Part of the task management tools suite.
 *
 * Reference: openclaude/src/tools/TaskUpdateTool.ts
 */
public class TaskUpdateTool implements Tool {

    private final TaskStore taskStore;

    public TaskUpdateTool() {
        this.taskStore = TaskStore.getInstance();
    }

    @Override
    public String getName() {
        return "task_update";
    }

    @Override
    public String getDescription() {
        return "Update an existing task's properties.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "taskId", Map.of(
                    "type", "string",
                    "description", "The task ID to update"
                ),
                "title", Map.of(
                    "type", "string",
                    "description", "New task title"
                ),
                "notes", Map.of(
                    "type", "string",
                    "description", "New task notes"
                ),
                "priority", Map.of(
                    "type", "string",
                    "description", "New priority: high, medium, low",
                    "enum", List.of("high", "medium", "low")
                ),
                "status", Map.of(
                    "type", "string",
                    "description", "New status: pending, in_progress, completed, blocked",
                    "enum", List.of("pending", "in_progress", "completed", "blocked")
                )
            ),
            "required", List.of("taskId")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String taskId = getString(args, "taskId", "");
        if (taskId.isEmpty()) {
            return "{\"error\": \"Missing required parameter: taskId\"}";
        }

        TaskStore.Task task = taskStore.get(taskId);
        if (task == null) {
            return "{\"error\": \"Task not found: " + taskId + "\"}";
        }

        // Update fields
        String newTitle = getStringOrNull(args, "title");
        String newNotes = getStringOrNull(args, "notes");
        String newPriority = getStringOrNull(args, "priority");
        String newStatus = getStringOrNull(args, "status");

        TaskStore.Task updated = taskStore.update(taskId, newTitle, newNotes, newPriority, newStatus);

        if (updated == null) {
            return "{\"error\": \"Failed to update task\"}";
        }

        return String.format(
            "{\n  \"taskId\": \"%s\",\n  \"title\": \"%s\",\n  \"notes\": \"%s\",\n  \"priority\": \"%s\",\n  \"status\": \"%s\",\n  \"updatedAt\": %d\n}",
            updated.id,
            escapeJson(updated.title),
            escapeJson(updated.notes),
            updated.priority,
            updated.status,
            updated.updatedAt
        );
    }

    private String getString(Map<String, Object> args, String key, String defaultValue) {
        Object value = args.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private String getStringOrNull(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) return null;
        String s = value.toString();
        return s.isEmpty() ? null : s;
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
