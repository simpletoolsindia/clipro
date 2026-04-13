package com.clipro.tools.file;

import com.clipro.tools.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * File read tool using Java NIO.2.
 * Token-optimized: truncates files > 100KB.
 */
public class FileReadTool implements Tool {

    private static final int MAX_FILE_SIZE = 100 * 1024; // 100KB
    private final Path defaultDirectory;

    public FileReadTool() {
        this.defaultDirectory = Path.of(System.getProperty("user.dir"));
    }

    public FileReadTool(String defaultDir) {
        this.defaultDirectory = Path.of(defaultDir);
    }

    @Override
    public String getName() {
        return "file_read";
    }

    @Override
    public String getDescription() {
        return "Read file contents. Truncates files > 100KB. Returns with line numbers.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "path", Map.of(
                    "type", "string",
                    "description", "File path to read"
                ),
                "limit", Map.of(
                    "type", "integer",
                    "description", "Maximum lines to read"
                ),
                "offset", Map.of(
                    "type", "integer",
                    "description", "Line offset to start reading"
                )
            ),
            "required", List.of("path")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String pathStr = (String) args.get("path");
        if (pathStr == null || pathStr.isEmpty()) {
            return "Error: path is required";
        }

        Integer limit = null;
        Integer offset = null;

        Object limitObj = args.get("limit");
        if (limitObj instanceof Number) {
            limit = ((Number) limitObj).intValue();
        }

        Object offsetObj = args.get("offset");
        if (offsetObj instanceof Number) {
            offset = ((Number) offsetObj).intValue();
        }

        try {
            Path path = resolvePath(pathStr);

            if (!Files.exists(path)) {
                return "Error: File not found: " + pathStr;
            }

            if (Files.isDirectory(path)) {
                return "Error: Path is a directory: " + pathStr;
            }

            long fileSize = Files.size(path);
            if (fileSize > MAX_FILE_SIZE) {
                return readLargeFile(path, limit, offset);
            }

            return readFile(path, limit, offset);

        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    private String readFile(Path path, Integer limit, Integer offset) throws IOException {
        StringBuilder sb = new StringBuilder();
        List<String> lines = Files.readAllLines(path);

        int start = (offset != null) ? Math.min(offset, lines.size()) : 0;
        int end = (limit != null) ? Math.min(start + limit, lines.size()) : lines.size();

        for (int i = start; i < end; i++) {
            sb.append(String.format("%6d  %s%n", i + 1, lines.get(i)));
        }

        if (end < lines.size()) {
            sb.append("\n... (").append(lines.size() - end).append(" more lines)");
        }

        return sb.toString();
    }

    private String readLargeFile(Path path, Integer limit, Integer offset) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[File truncated - exceeds 100KB limit]\n\n");

        long fileSize = Files.size(path);
        sb.append("File size: ").append(fileSize / 1024).append("KB\n\n");

        try (var reader = Files.newBufferedReader(path)) {
            String line;
            int lineNum = 0;
            int start = (offset != null) ? offset : 0;
            int count = 0;
            int maxLines = (limit != null) ? limit : 100;

            while ((line = reader.readLine()) != null) {
                if (lineNum >= start && count < maxLines) {
                    sb.append(String.format("%6d  %s%n", lineNum + 1, line));
                    count++;
                }
                lineNum++;
                if (count >= maxLines) break;
            }

            if (lineNum > start + count) {
                sb.append("\n... (").append(lineNum - (start + count)).append(" more lines)");
            }
        }

        return sb.toString();
    }

    private Path resolvePath(String pathStr) {
        Path path = Path.of(pathStr);
        if (path.isAbsolute()) {
            return path;
        }
        return defaultDirectory.resolve(path);
    }
}