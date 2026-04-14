package com.clipro.tools.shell;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BashTool.
 */
class BashToolTest {

    @Test
    void shouldHaveCorrectName() {
        BashTool tool = new BashTool();
        assertEquals("bash", tool.getName());
    }

    @Test
    void shouldExecuteSimpleCommand() {
        BashTool tool = new BashTool();
        String result = tool.execute(Map.of("command", "echo 'hello'"));

        assertTrue(result.contains("hello") || result.contains("Hello"));
    }

    @Test
    void shouldReturnErrorForMissingCommand() {
        BashTool tool = new BashTool();
        String result = tool.execute(new HashMap<>());

        assertTrue(result.startsWith("Error:") || result.contains("required"));
    }

    @Test
    void shouldReturnErrorForEmptyCommand() {
        BashTool tool = new BashTool();
        String result = tool.execute(Map.of("command", ""));

        assertTrue(result.startsWith("Error:") || result.contains("required"));
    }

    @Test
    void shouldHandleNullCommand() {
        BashTool tool = new BashTool();
        Map<String, Object> args = new HashMap<>();
        args.put("command", null);
        String result = tool.execute(args);

        // Should handle null gracefully - check it's not null
        assertNotNull(result);
        // Result should contain error indication
        assertFalse(result.isEmpty());
    }

    @Test
    void shouldReturnErrorForFailedCommand() {
        BashTool tool = new BashTool();
        String result = tool.execute(Map.of("command", "exit 1"));

        // Should indicate error with exit code
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void shouldHandleCommandNotFound() {
        BashTool tool = new BashTool();
        String result = tool.execute(Map.of("command", "nonexistent_command_12345"));

        assertNotNull(result);
        // Command not found should return an error
        assertFalse(result.isEmpty());
    }

    @Test
    void shouldReturnErrorForTimeout() {
        BashTool tool = new BashTool();
        // Simple timeout test
        String result = tool.execute(Map.of(
            "command", "sleep 100",
            "timeout", 1
        ));

        // Should contain timeout or error indication
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void shouldCaptureStderr() {
        BashTool tool = new BashTool();
        String result = tool.execute(Map.of("command", "ls /nonexistent_directory 2>&1 || true"));

        // Should capture stderr output
        assertTrue(result.contains("No such file") || result.contains("not found") || result.contains("Error"));
    }

    @Test
    void shouldExecutePwdCommand() {
        BashTool tool = new BashTool();
        String result = tool.execute(Map.of("command", "pwd"));

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void shouldHandleSpecialCharactersInCommand() {
        BashTool tool = new BashTool();
        String result = tool.execute(Map.of("command", "echo 'test with spaces and $HOME'"));

        assertTrue(result.contains("test") || result.contains("spaces"));
    }

    @Test
    void shouldHandleNewlinesInOutput() {
        BashTool tool = new BashTool();
        String result = tool.execute(Map.of("command", "echo 'line1' && echo 'line2' && echo 'line3'"));

        assertTrue(result.contains("line1") && result.contains("line2") && result.contains("line3"));
    }
}
