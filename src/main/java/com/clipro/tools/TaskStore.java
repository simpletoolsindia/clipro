package com.clipro.tools;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared task store for all task tools.
 * Provides thread-safe task management.
 */
public class TaskStore {

    private static TaskStore instance;

    private final Map<String, Task> tasks;
    private int nextId;

    private TaskStore() {
        this.tasks = new ConcurrentHashMap<>();
        this.nextId = 1;
    }

    public static synchronized TaskStore getInstance() {
        if (instance == null) {
            instance = new TaskStore();
        }
        return instance;
    }

    /**
     * Create a new task.
     */
    public Task create(String title, String notes, String priority, String status, String parentId) {
        String id = String.valueOf(nextId++);
        Task task = new Task(id, title, notes, priority, status);

        if (parentId != null && tasks.containsKey(parentId)) {
            task.parentId = parentId;
            tasks.get(parentId).children.add(id);
        }

        tasks.put(id, task);
        return task;
    }

    /**
     * Get a task by ID.
     */
    public Task get(String id) {
        return tasks.get(id);
    }

    /**
     * List all tasks.
     */
    public List<Task> list() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * List tasks by status.
     */
    public List<Task> listByStatus(String status) {
        return tasks.values().stream()
            .filter(t -> status.equals(t.status))
            .sorted(Comparator.comparingLong(t -> t.createdAt))
            .toList();
    }

    /**
     * Update a task.
     */
    public Task update(String id, String title, String notes, String priority, String status) {
        Task task = tasks.get(id);
        if (task == null) return null;

        if (title != null) task.title = title;
        if (notes != null) task.notes = notes;
        if (priority != null) task.priority = priority;
        if (status != null) task.status = status;
        task.updatedAt = System.currentTimeMillis();

        return task;
    }

    /**
     * Delete a task.
     */
    public boolean delete(String id) {
        Task task = tasks.remove(id);
        if (task != null) {
            // Remove from parent's children
            if (task.parentId != null) {
                Task parent = tasks.get(task.parentId);
                if (parent != null) {
                    parent.children.remove(id);
                }
            }
            // Delete children recursively
            for (String childId : task.children) {
                delete(childId);
            }
            return true;
        }
        return false;
    }

    /**
     * Stop/delete a running task.
     */
    public boolean stop(String id) {
        Task task = tasks.get(id);
        if (task != null) {
            task.status = "stopped";
            task.stoppedAt = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Task data class.
     */
    public static class Task {
        public final String id;
        public String title;
        public String notes;
        public String priority;
        public String status;
        public String parentId;
        public List<String> children;
        public final long createdAt;
        public long updatedAt;
        public long stoppedAt;

        public Task(String id, String title, String notes, String priority, String status) {
            this.id = id;
            this.title = title;
            this.notes = notes;
            this.priority = priority;
            this.status = status;
            this.parentId = null;
            this.children = new ArrayList<>();
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = createdAt;
        }

        public boolean isCompleted() {
            return "completed".equals(status);
        }

        public boolean isStopped() {
            return "stopped".equals(status);
        }
    }
}
