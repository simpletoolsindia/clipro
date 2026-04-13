package com.clipro.tools.file;

import com.clipro.tools.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * File edit tool with unified diff format.
 * Uses line-based editing with validation.
 */
public class FileEditTool implements Tool {

    private final Path defaultDirectory;

    public FileEditTool() {
        this.defaultDirectory = Path.of(System.getProperty("user.dir"));
    }

    @Override
    public String getName() {
        return "file_edit";
    }

    @Override
    public String getDescription() {
        return "Edit specific parts of files using unified diff format. Validates changes before writing.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "path", Map.of(
                    "type", "string",
                    "description", "File path to edit"
                ),
                "old_string", Map.of(
                    "type", "string",
                    "description", "Text to find and replace"
                ),
                "new_string", Map.of(
                    "type", "string",
                    "description", "Replacement text"
                )
            ),
            "required", List.of("path", "old_string", "new_string")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String pathStr = (String) args.get("path");
        String oldString = (String) args.get("old_string");
        String newString = (String) args.get("new_string");

        if (pathStr == null || pathStr.isEmpty()) {
            return "Error: path is required";
        }

        if (oldString == null) {
            return "Error: old_string is required";
        }

        if (newString == null) {
            return "Error: new_string is required";
        }

        try {
            Path path = resolvePath(pathStr);

            if (!Files.exists(path)) {
                return "Error: File not found: " + pathStr;
            }

            String content = Files.readString(path);

            if (!content.contains(oldString)) {
                return "Error: old_string not found in file";
            }

            // Generate diff preview
            String diff = generateDiff(pathStr, content, oldString, newString);

            // Perform replacement
            String newContent = content.replace(oldString, newString);
            Files.writeString(path, newContent);

            return diff + "\nEdit applied successfully.";

        } catch (Exception e) {
            return "Error editing file: " + e.getMessage();
        }
    }

    private String generateDiff(String path, String content, String oldStr, String newStr) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- a/").append(path).append("\n");
        sb.append("+++ b/").append(path).append("\n");

        String[] oldLines = content.split("\n");
        String[] newLines = content.replace(oldStr, newStr).split("\n");

        // Find the changed line
        int changeLine = 0;
        for (int i = 0; i < oldLines.length; i++) {
            if (oldLines[i].contains(oldStr.split("\n")[0])) {
                changeLine = i + 1;
                break;
            }
        }

        sb.append(String.format("@@ -%d,%d +%d,%d @@%n", changeLine, 1, changeLine, 1));
        sb.append("-").append(oldStr.split("\n")[0]).append("\n");
        sb.append("+").append(newStr.split("\n")[0]).append("\n");

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