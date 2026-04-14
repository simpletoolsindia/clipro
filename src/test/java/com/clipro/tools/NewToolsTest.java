package com.clipro.tools;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for new tools.
 */
class NewToolsTest {

    @Test
    void shouldExecuteMCPToolList() {
        MCPTool tool = new MCPTool();
        String result = tool.execute(Map.of("action", "list"));
        assertNotNull(result);
        assertTrue(result.contains("MCP Resources"));
    }

    @Test
    void shouldExecuteMCPToolAdd() {
        MCPTool tool = new MCPTool();
        String result = tool.execute(Map.of("action", "add", "server", "test-server"));
        assertNotNull(result);
        assertTrue(result.contains("test-server"));
    }

    @Test
    void shouldExecuteAskUserQuestionTool() {
        AskUserQuestionTool tool = new AskUserQuestionTool();
        String result = tool.execute(Map.of("question", "What is your name?"));
        assertNotNull(result);
        assertTrue(result.contains("What is your name?"));
    }

    @Test
    void shouldExecuteScheduleCronTool() {
        ScheduleCronTool tool = new ScheduleCronTool();
        String result = tool.execute(Map.of("task", "Reminder task"));
        assertNotNull(result);
        assertTrue(result.contains("Scheduled task"));
    }

    @Test
    void shouldExecuteTaskToolCreate() {
        TaskTool tool = new TaskTool();
        String result = tool.execute(Map.of("action", "create", "title", "Test task"));
        assertNotNull(result);
        assertTrue(result.contains("Task created"));
    }

    @Test
    void shouldExecuteTaskToolList() {
        TaskTool tool = new TaskTool();
        tool.execute(Map.of("action", "create", "title", "Task 1"));
        String result = tool.execute(Map.of("action", "list"));
        assertNotNull(result);
        assertTrue(result.contains("Tasks"));
    }

    @Test
    void shouldExecuteTaskToolDone() {
        TaskTool tool = new TaskTool();
        tool.execute(Map.of("action", "create", "title", "Task to complete"));
        String result = tool.execute(Map.of("action", "done", "id", 1));
        assertNotNull(result);
        assertTrue(result.contains("completed"));
    }
}
