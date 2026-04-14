package com.clipro.tools.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FileWriteTool.
 */
class FileWriteToolTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldHaveCorrectName() {
        FileWriteTool tool = new FileWriteTool(tempDir.toString());
        assertEquals("file_write", tool.getName());
    }

    @Test
    void shouldWriteNewFile() throws Exception {
        FileWriteTool tool = new FileWriteTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt", "content", "Hello"));

        assertTrue(result.contains("wrote") || result.contains("successfully") || result.contains("bytes"));
        assertTrue(java.nio.file.Files.exists(tempDir.resolve("test.txt")));
        assertEquals("Hello", java.nio.file.Files.readString(tempDir.resolve("test.txt")));
    }

    @Test
    void shouldOverwriteExistingFile() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        java.nio.file.Files.writeString(testFile, "Old content");

        FileWriteTool tool = new FileWriteTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt", "content", "New content"));

        assertEquals("New content", java.nio.file.Files.readString(testFile));
    }

    @Test
    void shouldAppendToFile() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        java.nio.file.Files.writeString(testFile, "Line 1\n");

        FileWriteTool tool = new FileWriteTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt", "content", "Line 2\n", "append", true));

        String content = java.nio.file.Files.readString(testFile);
        assertTrue(content.contains("Line 1"));
        assertTrue(content.contains("Line 2"));
    }

    @Test
    void shouldCreateParentDirectories() throws Exception {
        FileWriteTool tool = new FileWriteTool(tempDir.toString());
        String result = tool.execute(Map.of(
            "path", "subdir/nested/test.txt",
            "content", "nested content"
        ));

        assertTrue(java.nio.file.Files.exists(tempDir.resolve("subdir/nested/test.txt")));
    }

    @Test
    void shouldReturnErrorForMissingPath() {
        FileWriteTool tool = new FileWriteTool(tempDir.toString());
        String result = tool.execute(Map.of("content", "test"));

        assertTrue(result.startsWith("Error:") || result.contains("required"));
    }

    @Test
    void shouldReturnErrorForMissingContent() {
        FileWriteTool tool = new FileWriteTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "test.txt"));

        assertTrue(result.startsWith("Error:") || result.contains("required"));
    }

    @Test
    void shouldHandleEmptyContent() throws Exception {
        FileWriteTool tool = new FileWriteTool(tempDir.toString());
        String result = tool.execute(Map.of("path", "empty.txt", "content", ""));

        assertTrue(result.contains("wrote") || result.contains("successfully") || result.contains("bytes"));
        assertEquals("", java.nio.file.Files.readString(tempDir.resolve("empty.txt")));
    }

    @Test
    void shouldWriteMultilineContent() throws Exception {
        FileWriteTool tool = new FileWriteTool(tempDir.toString());
        String content = "Line 1\nLine 2\nLine 3";
        String result = tool.execute(Map.of("path", "multiline.txt", "content", content));

        assertEquals(content, java.nio.file.Files.readString(tempDir.resolve("multiline.txt")));
    }

    @Test
    void shouldWriteSpecialCharacters() throws Exception {
        FileWriteTool tool = new FileWriteTool(tempDir.toString());
        String content = "Unicode: 你好 🎉\nSpecial: <>&\"'\nNewlines:\n\n\n";
        String result = tool.execute(Map.of("path", "special.txt", "content", content));

        assertEquals(content, java.nio.file.Files.readString(tempDir.resolve("special.txt")));
    }
}
