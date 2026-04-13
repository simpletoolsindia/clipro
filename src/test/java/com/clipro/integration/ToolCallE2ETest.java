package com.clipro.integration;

import com.clipro.agent.AgentEngine;
import com.clipro.tools.Tool;
import com.clipro.tools.registry.ToolRegistry;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for tool calling.
 */
class ToolCallE2ETest {

    static class EchoTool implements Tool {
        @Override
        public String getName() { return "echo"; }
        @Override
        public String getDescription() { return "Echo the input back"; }
        @Override
        public Object getParameters() {
            return Map.of("type", "object", "properties", Map.of("message", Map.of("type", "string")));
        }
        @Override
        public String execute(Map<String, Object> args) {
            return "Echo: " + args.get("message");
        }
    }

    @Test
    void shouldRegisterTool() {
        AgentEngine agent = new AgentEngine();
        agent.registerTool(new EchoTool());
        assertEquals(1, agent.getToolRegistry().getToolNames().size());
    }

    @Test
    void shouldGetToolSchemas() {
        AgentEngine agent = new AgentEngine();
        agent.registerTool(new EchoTool());
        
        var schemas = agent.getToolRegistry().getSchemas();
        assertTrue(schemas.size() >= 1);
    }

    @Test
    void shouldHaveToolWithName() {
        AgentEngine agent = new AgentEngine();
        agent.registerTool(new EchoTool());
        
        Tool tool = agent.getToolRegistry().get("echo");
        assertNotNull(tool);
        assertEquals("echo", tool.getName());
    }

    @Test
    void shouldExecuteTool() {
        ToolRegistry registry = new ToolRegistry();
        registry.register(new EchoTool());
        
        Tool tool = registry.get("echo");
        assertNotNull(tool);
        
        String result = tool.execute(Map.of("message", "Hello"));
        assertEquals("Echo: Hello", result);
    }

    @Test
    void shouldUnregisterTool() {
        AgentEngine agent = new AgentEngine();
        agent.registerTool(new EchoTool());
        assertTrue(agent.getToolRegistry().has("echo"));
        
        agent.getToolRegistry().unregister("echo");
        assertFalse(agent.getToolRegistry().has("echo"));
    }

    @Test
    void shouldHandleToolNotFound() {
        ToolRegistry registry = new ToolRegistry();
        Tool tool = registry.get("nonexistent");
        assertNull(tool);
    }

    @Test
    void shouldHandleToolError() {
        ToolRegistry registry = new ToolRegistry();
        Tool badTool = new Tool() {
            @Override
            public String getName() { return "bad"; }
            @Override
            public String getDescription() { return "bad"; }
            @Override
            public Object getParameters() { return Map.of(); }
            @Override
            public String execute(Map<String, Object> args) {
                throw new RuntimeException("Intentional error");
            }
        };
        registry.register(badTool);
        
        Tool tool = registry.get("bad");
        assertNotNull(tool);
        
        // Tool throws when executed - this is expected behavior
        assertThrows(RuntimeException.class, () -> tool.execute(Map.of()));
    }
}
