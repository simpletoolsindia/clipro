package com.clipro.tools.file;

import com.clipro.tools.Tool;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Glob tool for finding files matching patterns.
 * Token-optimized: limits to 100 files max.
 */
public class GlobTool implements Tool {

    private static final int MAX_FILES = 100;
    private final Path defaultDirectory;

    public GlobTool() {
        this.defaultDirectory = Path.of(System.getProperty("user.dir"));
    }

    public GlobTool(String defaultDir) {
        this.defaultDirectory = Path.of(defaultDir);
    }

    @Override
    public String getName() {
        return "glob";
    }

    @Override
    public String getDescription() {
        return "Find files matching patterns. Glob: **/*.java, *.ts, etc. Max 100 files.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "pattern", Map.of(
                    "type", "string",
                    "description", "Glob pattern (e.g., **/*.java, src/**/*.ts)"
                ),
                "base_dir", Map.of(
                    "type", "string",
                    "description", "Base directory to search in"
                )
            ),
            "required", List.of("pattern")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String pattern = (String) args.get("pattern");
        if (pattern == null || pattern.isEmpty()) {
            return "Error: pattern is required";
        }

        Path baseDir = defaultDirectory;
        Object baseDirObj = args.get("base_dir");
        if (baseDirObj instanceof String) {
            baseDir = resolvePath((String) baseDirObj);
        }

        try {
            List<Path> matches = new ArrayList<>();

            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            final Path searchDir = baseDir;

            Files.walkFileTree(baseDir, new SimpleFileVisitor<>() {
                @Override
                public java.nio.file.FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (matcher.matches(file.getFileName()) || matcher.matches(searchDir.relativize(file))) {
                        matches.add(file);
                        if (matches.size() >= MAX_FILES) {
                            return java.nio.file.FileVisitResult.TERMINATE;
                        }
                    }
                    return java.nio.file.FileVisitResult.CONTINUE;
                }
            });

            if (matches.isEmpty()) {
                return "No files matching pattern: " + pattern;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Found ").append(matches.size()).append(" file(s):\n\n");
            for (Path p : matches) {
                sb.append("- ").append(baseDir.relativize(p).toString().replace("\\", "/")).append("\n");
            }

            if (matches.size() >= MAX_FILES) {
                sb.append("\n[Max ").append(MAX_FILES).append(" files shown]");
            }

            return sb.toString();

        } catch (Exception e) {
            return "Error searching files: " + e.getMessage();
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