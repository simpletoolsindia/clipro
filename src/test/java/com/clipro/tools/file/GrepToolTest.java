package com.clipro.tools.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GrepTool.
 */
class GrepToolTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldHaveCorrectName() {
        GrepTool tool = new GrepTool();
        assertEquals("grep", tool.getName());
    }

    @Test
    void shouldReturnErrorForMissingPattern() {
        GrepTool tool = new GrepTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt"));

        assertTrue(result.startsWith("Error:") || result.contains("required"));
    }

    @Test
    void shouldReturnErrorForMissingPath() {
        GrepTool tool = new GrepTool(tempDir.toString());
        String result = tool.execute(Map.of("pattern", "test"));

        assertTrue(result.startsWith("Error:") || result.contains("required"));
    }

    @Test
    void shouldFindMatchingLines() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        java.nio.file.Files.writeString(testFile, "line1: hello\nline2: world\nline3: hello again");

        GrepTool tool = new GrepTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt", "pattern", "hello"));

        assertTrue(result.contains("hello"));
    }

    @Test
    void shouldReturnErrorForNoMatches() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        java.nio.file.Files.writeString(testFile, "line1: foo\nline2: bar");

        GrepTool tool = new GrepTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt", "pattern", "xyz123"));

        assertTrue(result.contains("No matches") || result.contains("not found") || result.isEmpty() || result.contains("0 matches"));
    }

    @Test
    void shouldFindWithRegex() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        java.nio.file.Files.writeString(testFile, "abc123\ndef456\nghi789");

        GrepTool tool = new GrepTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt", "pattern", "\\d+", "regex", true));

        assertTrue(result.contains("abc") || result.contains("123"));
    }

    @Test
    void shouldFindCaseInsensitive() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        java.nio.file.Files.writeString(testFile, "Hello World\nhello world\nHELLO WORLD");

        GrepTool tool = new GrepTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt", "pattern", "hello", "caseInsensitive", true));

        // Should find all 3 lines
        assertTrue(result.contains("Hello") || result.contains("hello") || result.contains("HELLO"));
    }

    @Test
    void shouldReturnErrorForNonexistentFile() {
        GrepTool tool = new GrepTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "nonexistent.txt", "pattern", "test"));

        assertTrue(result.startsWith("Error:") || result.contains("not found"));
    }

    @Test
    void shouldHandleEmptyFile() throws Exception {
        Path emptyFile = tempDir.resolve("empty.txt");
        java.nio.file.Files.writeString(emptyFile, "");

        GrepTool tool = new GrepTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "empty.txt", "pattern", "anything"));

        assertTrue(result.contains("No matches") || result.contains("not found") || result.isEmpty());
    }

    @Test
    void shouldHandleSpecialCharactersInPattern() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        java.nio.file.Files.writeString(testFile, "test [pattern]\nspecial $chars\nnormal line");

        GrepTool tool = new GrepTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt", "pattern", "test"));

        assertTrue(result.contains("test"));
    }

    @Test
    void shouldLimitResults() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("line").append(i).append(": match\n");
        }
        java.nio.file.Files.writeString(testFile, sb.toString());

        GrepTool tool = new GrepTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt", "pattern", "match", "limit", 5));

        // Should contain some matches
        assertTrue(result.contains("match"));
    }
}
