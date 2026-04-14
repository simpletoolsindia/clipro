package com.clipro.agent;

import java.util.*;
import java.util.concurrent.*;

/**
 * Agent Team for multi-agent coordination.
 * M-25: Proper teardown with interrupt and timeout handling.
 */
public class AgentTeam {

    private static final int TEARDOWN_TIMEOUT_SECONDS = 10;

    private final String id;
    private final List<SubAgent> agents = new ArrayList<>();
    private final Map<String, String> taskQueue = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    private final ExecutorService executor;

    public AgentTeam(String id) {
        this.id = id;
        this.executor = Executors.newCachedThreadPool();
    }

    public void addAgent(SubAgent agent) {
        agents.add(agent);
    }

    public void removeAgent(String agentId) {
        agents.removeIf(a -> a.getId().equals(agentId));
    }

    public void assignTask(String taskId, String task, String agentId) {
        taskQueue.put(taskId, task);
        for (SubAgent agent : agents) {
            if (agent.getId().equals(agentId)) {
                agent.assignTask(task);
                return;
            }
        }
    }

    public void assignTaskToFirstAvailable(String taskId, String task) {
        for (SubAgent agent : agents) {
            if (agent.getState() == SubAgent.AgentState.IDLE) {
                agent.assignTask(task);
                taskQueue.put(taskId, task);
                return;
            }
        }
    }

    public CompletableFuture<Map<String, String>> executeAllTasks() {
        running = true;
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> results = new ConcurrentHashMap<>();

            List<CompletableFuture<String>> futures = new ArrayList<>();
            for (SubAgent agent : agents) {
                futures.add(agent.getFuture());
            }

            // Wait for all agents
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Collect results
            for (SubAgent agent : agents) {
                results.put(agent.getId(), agent.getResult());
            }

            running = false;
            return results;
        }, executor);
    }

    public String aggregateResults(Map<String, String> results) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : results.entrySet()) {
            sb.append("=== Agent: ").append(entry.getKey()).append(" ===\n");
            sb.append(entry.getValue()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * M-25: Proper teardown with interrupt and timeout handling.
     * Cleans up all agent threads and resources.
     */
    public void teardown() {
        teardown(TEARDOWN_TIMEOUT_SECONDS);
    }

    /**
     * M-25: Teardown with custom timeout.
     */
    public void teardown(int timeoutSeconds) {
        running = false;

        // Interrupt all agent threads
        for (SubAgent agent : agents) {
            try {
                agent.interrupt();
            } catch (Exception e) {
                // Log but continue
            }
        }

        // Wait for completion with timeout
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        for (SubAgent agent : agents) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                // Force kill if timeout exceeded
                agent.forceKill();
                continue;
            }

            try {
                agent.awaitCompletion(remaining, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // Force kill on interrupt
                agent.forceKill();
                Thread.currentThread().interrupt();
            } catch (TimeoutException e) {
                // Timeout exceeded - force kill
                agent.forceKill();
            }
        }

        // Shutdown executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Clear agent list
        agents.clear();
        taskQueue.clear();
    }

    /**
     * M-25: Check if teardown is needed.
     */
    public boolean needsTeardown() {
        return running || !agents.isEmpty();
    }

    public List<SubAgent> getAgents() { return new ArrayList<>(agents); }
    public int getAgentCount() { return agents.size(); }
    public boolean isRunning() { return running; }
    public String getId() { return id; }
}
