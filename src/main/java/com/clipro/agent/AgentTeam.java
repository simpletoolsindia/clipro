package com.clipro.agent;

import java.util.*;
import java.util.concurrent.*;

/**
 * Agent Team for multi-agent coordination.
 * Enables parallel task execution and result aggregation.
 */
public class AgentTeam {

    private final String id;
    private final List<SubAgent> agents = new ArrayList<>();
    private final Map<String, String> taskQueue = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    public AgentTeam(String id) {
        this.id = id;
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
        });
    }

    public String aggregateResults(Map<String, String> results) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : results.entrySet()) {
            sb.append("=== Agent: ").append(entry.getKey()).append(" ===\n");
            sb.append(entry.getValue()).append("\n\n");
        }
        return sb.toString();
    }

    public List<SubAgent> getAgents() { return new ArrayList<>(agents); }
    public int getAgentCount() { return agents.size(); }
    public boolean isRunning() { return running; }
}
