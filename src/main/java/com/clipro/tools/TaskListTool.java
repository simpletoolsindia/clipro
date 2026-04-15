package com.clipro.tools;

import java.util.*;

/**
 * Tool for listing tasks.
 * Part of the task management tools suite.
 *
 * Reference: openclaude/src/tools/TaskListTool.ts
 */
public class TaskListTool implements Tool {

    private final TaskStore taskStore;

    public TaskListTool() {
        this.taskStore = TaskStore.getInstance();
    }

    @Override
    public String getName() {
        return "task_list";
    }

    @Override
    public String getDescription() {
        return "List all tasks. Optionally filter by status or priority.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "status", Map.of(
                    "type", "string",
                    "description", "Filter by status: pending, in_progress, completed, blocked, stopped",
                    "enum", List.of("pending", "in_progress", "completed", "blocked", "stopped")
                ),
                "priority", Map.of(
                    "type", "string",
                    "description", "Filter by priority: high, medium, low",
                    "enum", List.of("high", "medium", "low")
                ),
                "includeCompleted", Map.of(
                    "type", "boolean",
                    "description", "Include completed tasks in results"
                ),
                "sortBy", Map.of(
                    "type", "string",
                    "description", "Sort by: created, updated, priority",
                    "enum", List.of("created", "updated", "priority")
                ),
                "limit", Map.of(
                    "type", "integer",
                    "description", "Maximum number of tasks to return"
                )
            )
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String status = getString(args, "status", null);
        String priority = getString(args, "priority", null);
        boolean includeCompleted = getBoolean(args, "includeCompleted", true);
        String sortBy = getString(args, "sortBy", "created");
        int limit = getInt(args, "limit", 50);

        List<TaskStore.Task> tasks;

        if (status != null) {
            tasks = taskStore.listByStatus(status);
        } else {
            tasks = taskStore.list();
        }

        // Filter
        tasks = tasks.stream()
            .filter(t -> includeCompleted || !t.isCompleted())
            .filter(t -> priority == null || priority.equals(t.priority))
            .toList();

        // Sort
        tasks = switch (sortBy) {
            case "updated" -> tasks.stream()
                .sorted(Comparator.comparingLong((TaskStore.Task t) -> t.updatedAt).reversed())
                .toList();
            case "priority" -> {
                Map<String, Integer> priorityOrder = Map.of("high", 0, "medium", 1, "low", 2);
                yield tasks.stream()
                    .sorted(Comparator.comparingInt(t -> priorityOrder.getOrDefault(t.priority, 1)))
                    .toList();
            }
            default -> tasks.stream()
                .sorted(Comparator.comparingLong((TaskStore.Task t) -> t.createdAt).reversed())
                .toList();
        };

        // Limit
        if (tasks.size() > limit) {
            tasks = tasks.subList(0, limit);
        }

        // Build JSON response
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"tasks\": [\n");

        for (int i = 0; i < tasks.size(); i++) {
            TaskStore.Task task = tasks.get(i);
            json.append("    {\n");
            json.append("      \"taskId\": \"").append(task.id).append("\",\n");
            json.append("      \"title\": \"").append(escapeJson(task.title)).append("\",\n");
            json.append("      \"priority\": \"").append(task.priority).append("\",\n");
            json.append("      \"status\": \"").append(task.status).append("\",\n");
            json.append("      \"createdAt\": ").append(task.createdAt).append(",\n");
            if (task.parentId != null) {
                json.append("      \"parentId\": \"").append(task.parentId).append("\",\n");
            }
            json.append("      \"completed\": ").append(task.isCompleted()).append("\n");
            json.append("    }");
            if (i < tasks.size() - 1) json.append(",");
            json.append("\n");
        }

        json.append("  ],\n");
        json.append("  \"total\": ").append(tasks.size()).append("\n");
        json.append("}");

        return json.toString();
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

    private int getInt(Map<String, Object> args, String key, int defaultValue) {
        Object value = args.get(key);
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); } catch (NumberFormatException ignored) {}
        }
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
