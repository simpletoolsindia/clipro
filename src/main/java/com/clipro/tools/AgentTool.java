package com.clipro.tools;

import com.clipro.agent.AgentManager;
import com.clipro.agent.AgentManager.AgentSession;
import com.clipro.agent.AgentEngine;
import com.clipro.agent.SubAgent;
import com.clipro.agent.SubAgent.AgentState;
import com.clipro.llm.providers.OllamaProvider;
import com.clipro.tools.registry.ToolRegistry;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * AgentTool for spawning sub-agents in multi-agent workflows.
 * Enables parallel task execution and agent delegation.
 *
 * Reference: openclaude/src/tools/AgentTool.tsx
 */
public class AgentTool implements Tool {

    private static AgentTool instance;

    // Active sub-agents by ID
    private final Map<String, SubAgent> activeAgents = new ConcurrentHashMap<>();

    // Agent callback handlers
    private final Map<String, Consumer<String>> agentCallbacks = new ConcurrentHashMap<>();

    // Built-in agent definitions
    private static final Map<String, AgentDefinition> BUILT_IN_AGENTS = new HashMap<>();

    static {
        // General purpose agent
        BUILT_IN_AGENTS.put("general-purpose", new AgentDefinition(
            "general-purpose",
            "General Purpose Agent",
            "A versatile agent for any task",
            null
        ));

        // Verification agent
        BUILT_IN_AGENTS.put("verification", new AgentDefinition(
            "verification",
            "Verification Agent",
            "Verifies code changes and test results",
            "verification"
        ));

        // Plan agent
        BUILT_IN_AGENTS.put("plan", new AgentDefinition(
            "plan",
            "Planning Agent",
            "Creates detailed implementation plans",
            "plan"
        ));

        // Explore agent
        BUILT_IN_AGENTS.put("explore", new AgentDefinition(
            "explore",
            "Explore Agent",
            "Explores codebase and finds relevant files",
            "explore"
        ));
    }

    /**
     * Agent definition for agent types.
     */
    public record AgentDefinition(
        String type,
        String name,
        String description,
        String model
    ) {}

    /**
     * Result of an agent execution.
     */
    public record AgentResult(
        String agentId,
        String status,
        String output,
        String error
    ) {}

    public AgentTool() {}

    public static synchronized AgentTool getInstance() {
        if (instance == null) {
            instance = new AgentTool();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "agent";
    }

    @Override
    public String getDescription() {
        return "Spawn a sub-agent to perform a task in parallel. " +
               "Use this for complex tasks that can be broken into independent subtasks. " +
               "Returns agent ID for tracking and coordination.";
    }

    @Override
    public Object getParameters() {
        Map<String, Object> properties = new HashMap<>();

        // Description
        properties.put("description", Map.of(
            "type", "string",
            "description", "A short (3-5 word) description of the task"
        ));

        // Prompt (task)
        properties.put("prompt", Map.of(
            "type", "string",
            "description", "The task for the agent to perform"
        ));

        // Agent type
        properties.put("subagent_type", Map.of(
            "type", "string",
            "description", "Type of specialized agent (general-purpose, verification, plan, explore)"
        ));

        // Model override
        properties.put("model", Map.of(
            "type", "string",
            "description", "Model to use (sonnet, opus, haiku). Defaults to configured model."
        ));

        // Name for the agent
        properties.put("name", Map.of(
            "type", "string",
            "description", "Name for the spawned agent. Makes it addressable via SendMessage."
        ));

        // Background mode
        properties.put("run_in_background", Map.of(
            "type", "boolean",
            "description", "Set to true to run in background. You will be notified when complete."
        ));

        return Map.of(
            "type", "object",
            "properties", properties,
            "required", List.of("description", "prompt")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String description = getString(args, "description", "Sub-agent task");
        String prompt = getString(args, "prompt", "");
        String agentType = getString(args, "subagent_type", "general-purpose");
        String model = getString(args, "model", null);
        String name = getString(args, "name", null);
        boolean runInBackground = getBoolean(args, "run_in_background", false);

        if (prompt == null || prompt.isEmpty()) {
            return formatError("Missing required parameter: prompt");
        }

        try {
            // Generate agent ID
            String agentId = name != null ? name : "agent-" + System.currentTimeMillis();

            // Get agent definition
            AgentDefinition def = BUILT_IN_AGENTS.get(agentType);
            if (def == null) {
                def = BUILT_IN_AGENTS.get("general-purpose");
            }

            // Create sub-agent
            SubAgent agent = new SubAgent(agentId, def.name(), model);

            // Store in active agents
            activeAgents.put(agentId, agent);

            // Start execution
            if (runInBackground) {
                executeInBackground(agentId, agent, prompt, model);
                return formatAsyncResult(agentId, description, prompt);
            } else {
                String result = executeSync(agent, prompt, model);
                activeAgents.remove(agentId);
                return formatResult(agentId, "completed", result);
            }

        } catch (Exception e) {
            return formatError("Failed to spawn agent: " + e.getMessage());
        }
    }

    /**
     * Execute agent synchronously.
     */
    private String executeSync(SubAgent agent, String prompt, String model) {
        agent.assignTask(prompt);

        try {
            // Create agent engine for this sub-agent
            AgentEngine engine = createAgentEngine(model);
            String result = engine.run(prompt).join();
            agent.complete(result);
            return result;
        } catch (Exception e) {
            agent.fail(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute agent in background.
     */
    private void executeInBackground(String agentId, SubAgent agent, String prompt, String model) {
        CompletableFuture.runAsync(() -> {
            try {
                executeSync(agent, prompt, model);
                notifyCompletion(agentId);
            } catch (Exception e) {
                agent.fail(e.getMessage());
                notifyCompletion(agentId);
            }
        });
    }

    /**
     * Create an agent engine with the given model.
     */
    private AgentEngine createAgentEngine(String model) {
        if (model != null && !model.isEmpty()) {
            return new AgentEngine(model);
        }
        return new AgentEngine();
    }

    /**
     * Get an active agent by ID.
     */
    public SubAgent getAgent(String agentId) {
        return activeAgents.get(agentId);
    }

    /**
     * Get status of an agent.
     */
    public AgentResult getAgentStatus(String agentId) {
        SubAgent agent = activeAgents.get(agentId);
        if (agent == null) {
            return new AgentResult(agentId, "not_found", null, "Agent not found");
        }

        AgentState state = agent.getState();
        String status = switch (state) {
            case IDLE -> "idle";
            case RUNNING -> "running";
            case THINKING -> "thinking";
            case ACTING -> "acting";
            case COMPLETED -> "completed";
            case FAILED, ERROR -> "failed";
        };

        return new AgentResult(agentId, status, agent.getResult(), null);
    }

    /**
     * Kill an active agent.
     */
    public boolean killAgent(String agentId) {
        SubAgent agent = activeAgents.get(agentId);
        if (agent == null) {
            return false;
        }
        agent.forceKill();
        activeAgents.remove(agentId);
        return true;
    }

    /**
     * List all active agents.
     */
    public List<AgentResult> listAgents() {
        List<AgentResult> results = new ArrayList<>();
        for (String agentId : activeAgents.keySet()) {
            results.add(getAgentStatus(agentId));
        }
        return results;
    }

    /**
     * Register a callback for agent completion.
     */
    public void onAgentComplete(String agentId, Consumer<String> callback) {
        agentCallbacks.put(agentId, callback);
    }

    /**
     * Notify completion via callback.
     */
    private void notifyCompletion(String agentId) {
        Consumer<String> callback = agentCallbacks.remove(agentId);
        if (callback != null) {
            SubAgent agent = activeAgents.get(agentId);
            callback.accept(agent != null ? agent.getResult() : null);
        }
    }

    /**
     * Format result as JSON.
     */
    private String formatResult(String agentId, String status, String output) {
        return String.format(
            "{\n  \"agentId\": \"%s\",\n  \"status\": \"%s\",\n  \"output\": %s\n}",
            agentId,
            status,
            output != null ? "\"" + escapeJson(output) + "\"" : "null"
        );
    }

    /**
     * Format async launch result.
     */
    private String formatAsyncResult(String agentId, String description, String prompt) {
        return String.format(
            "{\n  \"status\": \"async_launched\",\n  \"agentId\": \"%s\",\n  \"description\": \"%s\",\n  \"prompt\": \"%s\",\n  \"message\": \"Agent started in background. You will be notified when complete.\"\n}",
            agentId,
            description,
            escapeJson(prompt)
        );
    }

    /**
     * Format error result.
     */
    private String formatError(String message) {
        return String.format(
            "{\n  \"status\": \"error\",\n  \"error\": \"%s\"\n}",
            escapeJson(message)
        );
    }

    /**
     * Escape string for JSON.
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Get string from args.
     */
    private String getString(Map<String, Object> args, String key, String defaultValue) {
        Object value = args.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Get boolean from args.
     */
    private boolean getBoolean(Map<String, Object> args, String key, boolean defaultValue) {
        Object value = args.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    // =====================
    // Tool Methods (non-executed)
    // =====================

    /**
     * Kill a running agent.
     */
    public String kill(Map<String, Object> args) {
        String agentId = getString(args, "agent_id", null);
        if (agentId == null) {
            return "{\"error\": \"Missing required parameter: agent_id\"}";
        }

        boolean killed = killAgent(agentId);
        return String.format("{\"agentId\": \"%s\", \"killed\": %s}", agentId, killed);
    }

    /**
     * Get agent status.
     */
    public String status(Map<String, Object> args) {
        String agentId = getString(args, "agent_id", null);
        if (agentId == null) {
            // List all agents
            List<AgentResult> agents = listAgents();
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < agents.size(); i++) {
                AgentResult r = agents.get(i);
                sb.append(String.format("  {\"agentId\": \"%s\", \"status\": \"%s\"}",
                    r.agentId(), r.status()));
                if (i < agents.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");
            return sb.toString();
        }

        AgentResult result = getAgentStatus(agentId);
        return String.format("{\"agentId\": \"%s\", \"status\": \"%s\", \"output\": \"%s\"}",
            result.agentId(),
            result.status(),
            escapeJson(result.output()));
    }

    /**
     * List available agent types.
     */
    public String listTypes() {
        StringBuilder sb = new StringBuilder("[\n");
        int i = 0;
        for (Map.Entry<String, AgentDefinition> entry : BUILT_IN_AGENTS.entrySet()) {
            AgentDefinition def = entry.getValue();
            sb.append(String.format("  {\"type\": \"%s\", \"name\": \"%s\", \"description\": \"%s\"}",
                def.type(),
                def.name(),
                def.description()));
            if (++i < BUILT_IN_AGENTS.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}
