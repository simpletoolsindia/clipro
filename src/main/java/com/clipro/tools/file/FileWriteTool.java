package com.clipro.tools.file;

import com.clipro.tools.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

/**
 * File write tool with atomic writes.
 * Creates parent directories if needed.
 */
public class FileWriteTool implements Tool {

    private final Path defaultDirectory;

    public FileWriteTool() {
        this.defaultDirectory = Path.of(System.getProperty("user.dir"));
    }

    public FileWriteTool(String defaultDir) {
        this.defaultDirectory = Path.of(defaultDir);
    }

    @Override
    public String getName() {
        return "file_write";
    }

    @Override
    public String getDescription() {
        return "Write content to files. Creates directories atomically. Uses temp file + rename for safety.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "path", Map.of(
                    "type", "string",
                    "description", "File path to write"
                ),
                "content", Map.of(
                    "type", "string",
                    "description", "Content to write"
                ),
                "append", Map.of(
                    "type", "boolean",
                    "description", "Append to existing file instead of overwriting",
                    "default", false
                )
            ),
            "required", List.of("path", "content")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String pathStr = (String) args.get("path");
        String content = (String) args.get("content");

        if (pathStr == null || pathStr.isEmpty()) {
            return "Error: path is required";
        }

        if (content == null) {
            return "Error: content is required";
        }

        boolean append = false;
        Object appendObj = args.get("append");
        if (appendObj instanceof Boolean) {
            append = (Boolean) appendObj;
        }

        try {
            Path path = resolvePath(pathStr);

            // Create parent directories
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            // Atomic write using temp file
            if (append) {
                Files.writeString(path, content, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            } else {
                Path tempFile = Files.createTempFile(path.getParent(), ".tmp", ".write");
                try {
                    Files.writeString(tempFile, content);
                    Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (Exception e) {
                    Files.deleteIfExists(tempFile);
                    throw e;
                }
            }

            long size = Files.size(path);
            return "Successfully wrote to " + path + " (" + size + " bytes)";

        } catch (Exception e) {
            return "Error writing file: " + e.getMessage();
        }
    }

    private Path resolvePath(String pathStr) {
        Path path = Path.of(pathStr);
        if (path.isAbsolute()) {
            return path;
        }
        return defaultDirectory.resolve(path);
    }
}