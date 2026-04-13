package com.clipro.tools.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FileReadTool.
 */
class FileReadToolTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldHaveCorrectName() {
        FileReadTool tool = new FileReadTool();
        assertEquals("file_read", tool.getName());
    }

    @Test
    void shouldReturnErrorForMissingPath() {
        FileReadTool tool = new FileReadTool(tempDir.toString());
        String result = tool.execute(Map.of());
        assertTrue(result.startsWith("Error: path is required"));
    }

    @Test
    void shouldReadExistingFile() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        java.nio.file.Files.writeString(testFile, "Hello World");

        FileReadTool tool = new FileReadTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt"));

        assertTrue(result.contains("Hello World"));
    }

    @Test
    void shouldReturnErrorForNonexistentFile() {
        FileReadTool tool = new FileReadTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "nonexistent.txt"));
        assertTrue(result.startsWith("Error: File not found"));
    }

    @Test
    void shouldHandleLimit() throws Exception {
        Path testFile = tempDir.resolve("multi.txt");
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            sb.append("Line ").append(i).append("\n");
        }
        java.nio.file.Files.writeString(testFile, sb.toString());

        FileReadTool tool = new FileReadTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "multi.txt", "limit", 5));

        assertTrue(result.contains("Line 1"));
        assertTrue(result.contains("Line 5"));
        assertTrue(result.contains("more lines"));
    }
}