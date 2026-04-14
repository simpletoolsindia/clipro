package com.clipro.tools;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tool for managing tasks/to-dos with nested support.
 * M-23: Supports parent-child task relationships and tree rendering.
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
        return "Task management with nested support. Create, list, update, complete tasks.\n" +
               "Usage: /task create <title> [--parent <id>] | /task list | /task done <id>";
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
                "parent", Map.of(
                    "type", "integer",
                    "description", "Parent task ID for nested tasks"
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
                Object parentObj = args.get("parent");
                String parentId = parentObj instanceof Number ?
                    String.valueOf(((Number) parentObj).intValue()) : null;
                return createTask(title, parentId);
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
                       "Usage: /task create <title> [--parent <id>] | /task list | /task done <id>";
        }
    }

    private String createTask(String title, String parentId) {
        if (title == null || title.isEmpty()) {
            return "Usage: /task create <task title> [--parent <parent-id>]";
        }

        int id = nextId++;
        Task task = new Task(id, title);

        // M-23: Set parent if specified
        if (parentId != null && tasks.containsKey(parentId)) {
            Task parent = tasks.get(parentId);
            parent.children.add(String.valueOf(id));
            task.parentId = parentId;
            task.depth = parent.depth + 1;
        }

        tasks.put(String.valueOf(id), task);
        String depthIndicator = task.depth > 0 ? " (child of #" + parentId + ")" : "";
        return "Task created: #" + id + " - " + title + depthIndicator;
    }

    private String listTasks() {
        if (tasks.isEmpty()) {
            return "Tasks: None\n\nUsage: /task create <title> [--parent <id>]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Tasks (").append(tasks.size()).append("):\n");
        sb.append("─────────────────────────────────────\n");

        // M-23: Render as tree
        List<Task> rootTasks = tasks.values().stream()
            .filter(t -> t.parentId == null)
            .sorted(Comparator.comparingInt(t -> t.id))
            .collect(Collectors.toList());

        for (Task task : rootTasks) {
            renderTaskTree(sb, task, "");
        }

        return sb.toString();
    }

    /**
     * M-23: Render task as tree with ├─ and └─ characters.
     */
    private void renderTaskTree(StringBuilder sb, Task task, String indent) {
        String status = task.completed ? "[x]" : "[ ]";
        String checkMark = task.completed ? "✓" : " ";

        // Tree characters
        String connector = indent.isEmpty() ? "" : "   ";
        String prefix = task.children.isEmpty() ? "└── " : "├── ";

        sb.append(connector).append(prefix)
          .append(status).append(" #").append(task.id)
          .append(" ").append(task.title);

        if (task.completed) {
            sb.append(" \033[90m(done)\033[0m");
        }
        sb.append("\n");

        // Render children
        List<Task> children = task.children.stream()
            .map(tasks::get)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt(t -> t.id))
            .collect(Collectors.toList());

        for (int i = 0; i < children.size(); i++) {
            boolean last = (i == children.size() - 1);
            String childIndent = indent + (task.depth == 0 ? "    " : "│   ") + (last ? "    " : "│   ");
            renderTaskTree(sb, children.get(i), childIndent);
        }
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
        Task task = tasks.remove(String.valueOf(id));
        if (task == null) {
            return "Task not found: #" + id;
        }
        // M-23: Remove from parent's children list
        if (task.parentId != null) {
            Task parent = tasks.get(task.parentId);
            if (parent != null) {
                parent.children.remove(String.valueOf(id));
            }
        }
        // M-23: Delete children recursively
        for (String childId : task.children) {
            deleteTask(Integer.parseInt(childId));
        }
        return "Task deleted: #" + id;
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

    /**
     * M-23: Task with nested support.
     */
    public static class Task {
        public final int id;
        public final String title;
        public final long createdAt;
        public boolean completed;
        public String parentId;  // M-23: Parent task ID
        public List<String> children;  // M-23: Child task IDs
        public int depth;  // M-23: Tree depth

        public Task(int id, String title) {
            this.id = id;
            this.title = title;
            this.createdAt = System.currentTimeMillis();
            this.completed = false;
            this.parentId = null;
            this.children = new ArrayList<>();
            this.depth = 0;
        }
    }
}
