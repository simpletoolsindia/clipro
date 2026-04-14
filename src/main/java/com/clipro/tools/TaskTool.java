package com.clipro.tools;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Tool for managing tasks/to-dos.
 * Supports create, update, list, and complete operations.
 */
public class TaskTool implements Tool {

    private final Map<String, Task> tasks;
    private int nextId;

    public TaskTool() {
        this.tasks = new HashMap<>();
        this.nextId = 1;
    }

    @Override
    public String getName() {
        return "task";
    }

    @Override
    public String getDescription() {
        return "Task management tool. Create, list, update, and complete tasks.\n" +
               "Usage: /task create <title> | /task list | /task done <id> | /task delete <id>";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "action", Map.of(
                    "type", "string",
                    "description", "Action: create, list, done, delete",
                    "enum", List.of("create", "list", "done", "delete")
                ),
                "title", Map.of(
                    "type", "string",
                    "description", "Task title for create"
                ),
                "id", Map.of(
                    "type", "integer",
                    "description", "Task ID for done/delete"
                )
            ),
            "required", List.of("action")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String action = (String) args.getOrDefault("action", "list");

        switch (action) {
            case "create":
                String title = (String) args.get("title");
                return createTask(title);
            case "list":
                return listTasks();
            case "done":
            case "complete":
                Object idObj = args.get("id");
                int id = idObj instanceof Number ? ((Number) idObj).intValue() : parseId(idObj);
                return completeTask(id);
            case "delete":
            case "remove":
                Object delId = args.get("id");
                int delTaskId = delId instanceof Number ? ((Number) delId).intValue() : parseId(delId);
                return deleteTask(delTaskId);
            default:
                return "Unknown action: " + action + "\n" +
                       "Usage: /task create <title> | /task list | /task done <id>";
        }
    }

    private String createTask(String title) {
        if (title == null || title.isEmpty()) {
            return "Usage: /task create <task title>";
        }

        int id = nextId++;
        tasks.put(String.valueOf(id), new Task(id, title));
        return "Task created: #" + id + " - " + title;
    }

    private String listTasks() {
        if (tasks.isEmpty()) {
            return "Tasks: None\n\nUsage: /task create <title>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Tasks (").append(tasks.size()).append("):\n");

        for (Task t : tasks.values()) {
            String status = t.completed ? "[x]" : "[ ]";
            sb.append("  ").append(status).append(" #").append(t.id)
              .append(" - ").append(t.title);
            if (t.completed) {
                sb.append(" (done)");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String completeTask(int id) {
        Task task = tasks.get(String.valueOf(id));
        if (task == null) {
            return "Task not found: #" + id;
        }
        task.completed = true;
        return "Task completed: #" + id + " - " + task.title;
    }

    private String deleteTask(int id) {
        if (tasks.remove(String.valueOf(id)) != null) {
            return "Task deleted: #" + id;
        }
        return "Task not found: #" + id;
    }

    private int parseId(Object obj) {
        if (obj instanceof String s) {
            try {
                return Integer.parseInt(s.replace("#", ""));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    public static class Task {
        public final int id;
        public final String title;
        public final long createdAt;
        public boolean completed;

        public Task(int id, String title) {
            this.id = id;
            this.title = title;
            this.createdAt = System.currentTimeMillis();
            this.completed = false;
        }
    }
}
