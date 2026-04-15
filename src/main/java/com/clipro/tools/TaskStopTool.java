package com.clipro.tools;

import java.util.*;

/**
 * Tool for stopping/canceling tasks.
 * Part of the task management tools suite.
 *
 * Reference: openclaude/src/tools/TaskStopTool.ts
 */
public class TaskStopTool implements Tool {

    private final TaskStore taskStore;

    public TaskStopTool() {
        this.taskStore = TaskStore.getInstance();
    }

    @Override
    public String getName() {
        return "task_stop";
    }

    @Override
    public String getDescription() {
        return "Stop/cancel a running task.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "taskId", Map.of(
                    "type", "string",
                    "description", "The task ID to stop"
                ),
                "reason", Map.of(
                    "type", "string",
                    "description", "Optional reason for stopping"
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

        boolean stopped = taskStore.stop(taskId);

        return String.format(
            "{\n  \"taskId\": \"%s\",\n  \"title\": \"%s\",\n  \"stopped\": %s,\n  \"stoppedAt\": %d\n}",
            taskId,
            escapeJson(task.title),
            stopped,
            task.stoppedAt
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
