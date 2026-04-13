package com.clipro.tools.file;

import com.clipro.tools.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Grep tool with regex support.
 * Returns line numbers and context.
 */
public class GrepTool implements Tool {

    private static final int MAX_MATCHES = 200;
    private final Path defaultDirectory;

    public GrepTool() {
        this.defaultDirectory = Path.of(System.getProperty("user.dir"));
    }

    public GrepTool(String defaultDir) {
        this.defaultDirectory = Path.of(defaultDir);
    }

    @Override
    public String getName() {
        return "grep";
    }

    @Override
    public String getDescription() {
        return "Search for patterns in files. Supports regex. Returns line numbers and context.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "pattern", Map.of(
                    "type", "string",
                    "description", "Regex pattern to search"
                ),
                "path", Map.of(
                    "type", "string",
                    "description", "File or directory to search in"
                ),
                "case_sensitive", Map.of(
                    "type", "boolean",
                    "description", "Case sensitive search",
                    "default", true
                ),
                "max_results", Map.of(
                    "type", "integer",
                    "description", "Maximum results",
                    "default", MAX_MATCHES
                )
            ),
            "required", List.of("pattern", "path")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String patternStr = (String) args.get("pattern");
        String pathStr = (String) args.get("path");

        if (patternStr == null || patternStr.isEmpty()) {
            return "Error: pattern is required";
        }

        if (pathStr == null || pathStr.isEmpty()) {
            return "Error: path is required";
        }

        boolean caseSensitive = true;
        Object caseObj = args.get("case_sensitive");
        if (caseObj instanceof Boolean) {
            caseSensitive = (Boolean) caseObj;
        }

        int maxResults = MAX_MATCHES;
        Object maxObj = args.get("max_results");
        if (maxObj instanceof Number) {
            maxResults = ((Number) maxObj).intValue();
        }

        try {
            Pattern pattern = caseSensitive
                ? Pattern.compile(patternStr)
                : Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

            Path path = resolvePath(pathStr);
            List<String> results = new ArrayList<>();

            if (Files.isDirectory(path)) {
                searchDirectory(path, pattern, results, maxResults);
            } else if (Files.exists(path)) {
                searchFile(path, pattern, results, maxResults);
            } else {
                return "Error: Path not found: " + pathStr;
            }

            if (results.isEmpty()) {
                return "No matches found for: " + patternStr;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Found ").append(results.size()).append(" match(es):\n\n");
            for (String r : results) {
                sb.append(r).append("\n");
            }

            return sb.toString();

        } catch (PatternSyntaxException e) {
            return "Error: Invalid regex pattern - " + e.getMessage();
        } catch (Exception e) {
            return "Error searching: " + e.getMessage();
        }
    }

    private void searchFile(Path file, Pattern pattern, List<String> results, int maxResults) throws IOException {
        List<String> lines = Files.readAllLines(file);
        for (int i = 0; i < lines.size() && results.size() < maxResults; i++) {
            if (pattern.matcher(lines.get(i)).find()) {
                results.add(String.format("%s:%d: %s", file.getFileName(), i + 1, lines.get(i)));
            }
        }
    }

    private void searchDirectory(Path dir, Pattern pattern, List<String> results, int maxResults) throws IOException {
        try (var stream = Files.walk(dir)) {
            stream.filter(Files::isRegularFile)
                .filter(f -> !f.toString().contains(".git"))
                .limit(maxResults * 10)
                .forEach(f -> {
                    try {
                        searchFile(f, pattern, results, maxResults);
                    } catch (IOException ignored) {}
                });
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