package com.clipro.agent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent spawning system for sub-agent support.
 * Manages multiple agent instances and their coordination.
 */
public class AgentManager {

    private final Map<String, AgentSession> agents;
    private String currentAgentId;
    private final int maxAgents;

    public AgentManager() {
        this(5); // Default max 5 agents
    }

    public AgentManager(int maxAgents) {
        this.agents = new ConcurrentHashMap<>();
        this.currentAgentId = "main";
        this.maxAgents = maxAgents;
        // Initialize main agent
        this.agents.put("main", new AgentSession("main", "system", "Main agent session"));
    }

    /**
     * Create a new agent session.
     */
    public String createAgent(String name, String model, String prompt) {
        if (agents.size() >= maxAgents) {
            return null; // Max agents reached
        }

        String agentId = name != null ? name : "agent-" + agents.size();
        if (agents.containsKey(agentId)) {
            agentId = agentId + "-" + System.currentTimeMillis();
        }

        AgentSession session = new AgentSession(agentId, model, prompt);
        agents.put(agentId, session);

        return agentId;
    }

    /**
     * Get an agent by ID.
     */
    public AgentSession getAgent(String agentId) {
        return agents.get(agentId);
    }

    /**
     * List all agents.
     */
    public List<AgentSession> listAgents() {
        return new ArrayList<>(agents.values());
    }

    /**
     * Remove an agent.
     */
    public boolean removeAgent(String agentId) {
        if ("main".equals(agentId)) {
            return false; // Cannot remove main agent
        }
        return agents.remove(agentId) != null;
    }

    /**
     * Switch to a different agent.
     */
    public boolean switchAgent(String agentId) {
        if (agents.containsKey(agentId)) {
            currentAgentId = agentId;
            return true;
        }
        return false;
    }

    /**
     * Get the current agent.
     */
    public AgentSession getCurrentAgent() {
        return agents.get(currentAgentId);
    }

    /**
     * Get the current agent ID.
     */
    public String getCurrentAgentId() {
        return currentAgentId;
    }

    /**
     * Get active agent count.
     */
    public int getActiveAgentCount() {
        return agents.size();
    }

    /**
     * Agent session state.
     */
    public static class AgentSession {
        private final String id;
        private final String model;
        private final String systemPrompt;
        private final Map<String, Object> context;
        private final List<String> history;
        private long createdAt;
        private long lastActivity;
        private String status;

        public AgentSession(String id, String model, String systemPrompt) {
            this.id = id;
            this.model = model;
            this.systemPrompt = systemPrompt;
            this.context = new ConcurrentHashMap<>();
            this.history = new ArrayList<>();
            this.createdAt = System.currentTimeMillis();
            this.lastActivity = createdAt;
            this.status = "active";
        }

        public String getId() { return id; }
        public String getModel() { return model; }
        public String getSystemPrompt() { return systemPrompt; }
        public List<String> getHistory() { return new ArrayList<>(history); }
        public long getCreatedAt() { return createdAt; }
        public long getLastActivity() { return lastActivity; }
        public String getStatus() { return status; }

        public void setStatus(String status) {
            this.status = status;
            this.lastActivity = System.currentTimeMillis();
        }

        public void addToHistory(String message) {
            history.add(message);
            lastActivity = System.currentTimeMillis();
        }

        public void setContext(String key, Object value) {
            context.put(key, value);
        }

        public Object getContext(String key) {
            return context.get(key);
        }

        public Map<String, Object> getAllContext() {
            return new HashMap<>(context);
        }
    }

    /**
     * Render agent status as ASCII art.
     */
    public String renderAgentStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║                     Agent Manager                             ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        List<AgentSession> agents = listAgents();
        for (AgentSession agent : agents) {
            String marker = agent.getId().equals(currentAgentId) ? "▶" : " ";
            sb.append(String.format("║ %s %-12s %-20s %-10s          ║\n",
                marker,
                truncate(agent.getId(), 12),
                truncate(agent.getModel(), 20),
                truncate(agent.getStatus(), 10)));
        }

        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Active: %d/%d | Current: %s                        ║\n",
            agents.size(), maxAgents, currentAgentId));
        sb.append("╚══════════════════════════════════════════════════════════════╝\n");
        sb.append("\nCommands: /agent create <name> | /agent switch <id> | /agent list");

        return sb.toString();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 2) + "..";
    }
}
