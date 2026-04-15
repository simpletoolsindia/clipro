package com.clipro.tools;

import java.util.*;

/**
 * Tool for getting a specific task by ID.
 * Part of the task management tools suite.
 *
 * Reference: openclaude/src/tools/TaskGetTool.ts
 */
public class TaskGetTool implements Tool {

    private final TaskStore taskStore;

    public TaskGetTool() {
        this.taskStore = TaskStore.getInstance();
    }

    @Override
    public String getName() {
        return "task_get";
    }

    @Override
    public String getDescription() {
        return "Get details of a specific task by ID.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "taskId", Map.of(
                    "type", "string",
                    "description", "The task ID to retrieve"
                ),
                "includeChildren", Map.of(
                    "type", "boolean",
                    "description", "Include child tasks in response"
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

        boolean includeChildren = getBoolean(args, "includeChildren", false);

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"taskId\": \"").append(task.id).append("\",\n");
        json.append("  \"title\": \"").append(escapeJson(task.title)).append("\",\n");
        json.append("  \"notes\": \"").append(escapeJson(task.notes)).append("\",\n");
        json.append("  \"priority\": \"").append(task.priority).append("\",\n");
        json.append("  \"status\": \"").append(task.status).append("\",\n");
        json.append("  \"createdAt\": ").append(task.createdAt).append(",\n");
        json.append("  \"updatedAt\": ").append(task.updatedAt).append(",\n");

        if (task.parentId != null) {
            json.append("  \"parentId\": \"").append(task.parentId).append("\",\n");
        }

        if (includeChildren && !task.children.isEmpty()) {
            json.append("  \"children\": [\n");
            for (int i = 0; i < task.children.size(); i++) {
                String childId = task.children.get(i);
                TaskStore.Task child = taskStore.get(childId);
                if (child != null) {
                    json.append("    {\"taskId\": \"").append(child.id)
                        .append("\", \"title\": \"").append(escapeJson(child.title))
                        .append("\", \"status\": \"").append(child.status).append("\"}");
                    if (i < task.children.size() - 1) json.append(",");
                    json.append("\n");
                }
            }
            json.append("  ],\n");
        }

        json.append("  \"completed\": ").append(task.isCompleted()).append("\n");
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

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
