package com.clipro.tools.git;

import org.junit.jupiter.api.Test;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Git tools using JGit.
 * Tests work with the clipro repo since GitTool uses System.getProperty("user.dir")
 */
class GitToolsTest {

    @Test
    void gitStatusToolShouldHaveCorrectName() {
        GitStatusTool tool = new GitStatusTool();
        assertEquals("git_status", tool.getName());
    }

    @Test
    void gitStatusToolShouldReturnStatus() {
        GitStatusTool tool = new GitStatusTool();
        String result = tool.execute(Map.of());

        // Should return some status result (working dir may have changes)
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void gitLogToolShouldHaveCorrectName() {
        GitLogTool tool = new GitLogTool();
        assertEquals("git_log", tool.getName());
    }

    @Test
    void gitLogToolShouldReturnLog() {
        GitLogTool tool = new GitLogTool();
        String result = tool.execute(Map.of());

        // Should return some log result
        assertNotNull(result);
    }

    @Test
    void gitDiffToolShouldHaveCorrectName() {
        GitDiffTool tool = new GitDiffTool();
        assertEquals("git_diff", tool.getName());
    }

    @Test
    void gitDiffToolShouldReturnDiff() {
        GitDiffTool tool = new GitDiffTool();
        String result = tool.execute(Map.of());

        // Should return some diff result
        assertNotNull(result);
    }

    @Test
    void gitCommitToolShouldHaveCorrectName() {
        GitCommitTool tool = new GitCommitTool();
        assertEquals("git_commit", tool.getName());
    }

    @Test
    void gitCommitToolShouldRequireMessage() {
        GitCommitTool tool = new GitCommitTool();
        String result = tool.execute(Map.of());

        assertTrue(result.startsWith("Error:") || result.contains("required"));
    }
}
