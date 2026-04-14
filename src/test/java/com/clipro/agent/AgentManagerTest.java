package com.clipro.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AgentManager.
 */
class AgentManagerTest {

    private AgentManager manager;

    @BeforeEach
    void setUp() {
        manager = new AgentManager(3);
    }

    @Test
    void shouldCreateMainAgent() {
        assertEquals("main", manager.getCurrentAgentId());
        assertNotNull(manager.getCurrentAgent());
    }

    @Test
    void shouldCreateNewAgent() {
        String id = manager.createAgent("test-agent", "qwen3-coder:32b", "You are helpful");
        assertNotNull(id);
        assertEquals(2, manager.getActiveAgentCount());
    }

    @Test
    void shouldNotExceedMaxAgents() {
        manager.createAgent("agent1", "model1", "prompt1");
        manager.createAgent("agent2", "model2", "prompt2");
        String id = manager.createAgent("agent3", "model3", "prompt3");
        assertNull(id); // Should fail - max is 3
    }

    @Test
    void shouldListAgents() {
        manager.createAgent("test", "model", "prompt");
        var agents = manager.listAgents();
        assertEquals(2, agents.size()); // main + test
    }

    @Test
    void shouldSwitchAgent() {
        String id = manager.createAgent("switch-test", "model", "prompt");
        assertTrue(manager.switchAgent(id));
        assertEquals(id, manager.getCurrentAgentId());
    }

    @Test
    void shouldRemoveAgent() {
        String id = manager.createAgent("removable", "model", "prompt");
        assertTrue(manager.removeAgent(id));
        assertFalse(manager.removeAgent("main")); // Can't remove main
    }

    @Test
    void shouldGetAgent() {
        String id = manager.createAgent("get-test", "model", "prompt");
        var agent = manager.getAgent(id);
        assertNotNull(agent);
        assertEquals(id, agent.getId());
    }

    @Test
    void shouldRenderAgentStatus() {
        manager.createAgent("status-test", "model", "prompt");
        String status = manager.renderAgentStatus();
        assertNotNull(status);
        assertTrue(status.contains("Agent Manager"));
        assertTrue(status.contains("status-test"));
    }
}
