package com.clipro.tools;

import java.util.List;
import java.util.Map;

/**
 * Tool for running scheduled cron jobs.
 * Allows scheduling reminders and recurring tasks.
 */
public class ScheduleCronTool implements Tool {

    private final Map<String, ScheduledTask> tasks;

    public ScheduleCronTool() {
        this.tasks = new java.util.HashMap<>();
    }

    @Override
    public String getName() {
        return "schedule";
    }

    @Override
    public String getDescription() {
        return "Schedule a cron job or reminder. Usage: /schedule <cron> <task>\n" +
               "Examples: /schedule * * * * * hourly task | /schedule 0 9 * * * daily at 9am";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "cron", Map.of(
                    "type", "string",
                    "description", "Cron expression (min hour day month weekday)"
                ),
                "task", Map.of(
                    "type", "string",
                    "description", "Task description or command"
                )
            ),
            "required", List.of("task")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String cron = (String) args.getOrDefault("cron", "* * * * *");
        String task = (String) args.get("task");

        if (task == null || task.isEmpty()) {
            return listScheduledTasks();
        }

        String taskId = "task-" + System.currentTimeMillis();
        tasks.put(taskId, new ScheduledTask(taskId, cron, task));

        return "Scheduled task: " + taskId + "\n" +
               "  Cron: " + cron + "\n" +
               "  Task: " + task + "\n" +
               "NOTE: Full cron execution requires server process";
    }

    private String listScheduledTasks() {
        if (tasks.isEmpty()) {
            return "Scheduled tasks: None\n\n" +
                   "Usage: /schedule <cron> <task>\n" +
                   "Example: /schedule * * * * * reminder every minute";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Scheduled tasks (").append(tasks.size()).append("):\n");
        for (ScheduledTask t : tasks.values()) {
            sb.append("  - ").append(t.id).append(": ").append(t.task)
              .append(" (").append(t.cron).append(")\n");
        }
        return sb.toString();
    }

    public static class ScheduledTask {
        public final String id;
        public final String cron;
        public final String task;
        public final long createdAt;
        public int executionCount;

        public ScheduledTask(String id, String cron, String task) {
            this.id = id;
            this.cron = cron;
            this.task = task;
            this.createdAt = System.currentTimeMillis();
            this.executionCount = 0;
        }
    }
}
