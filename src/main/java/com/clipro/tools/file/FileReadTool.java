package com.clipro.tools.file;

import com.clipro.tools.Tool;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * File read tool using Java NIO.2.
 * Token-optimized: truncates files > 100KB.
 * M-19: Image processing for PNG, JPG, GIF, WebP with dimension extraction.
 */
public class FileReadTool implements Tool {

    private static final int MAX_FILE_SIZE = 100 * 1024; // 100KB
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp");
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
        return "Read file contents. Truncates files > 100KB. Returns with line numbers. Images show dimensions.";
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

            // M-19: Check for image file
            if (isImageFile(pathStr)) {
                return readImageFile(path);
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

    /**
     * M-19: Check if file is an image based on extension.
     */
    private boolean isImageFile(String pathStr) {
        String lower = pathStr.toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * M-19: Read image file and extract dimensions.
     */
    private String readImageFile(Path path) throws IOException {
        StringBuilder sb = new StringBuilder();

        long fileSize = Files.size(path);
        String ext = getExtension(path.getFileName().toString()).toUpperCase().replace(".", "");

        try {
            BufferedImage img = ImageIO.read(path.toFile());
            if (img != null) {
                int width = img.getWidth();
                int height = img.getHeight();
                sb.append("[").append(ext).append(" ").append(width).append("x").append(height).append("] ");
                sb.append(path.getFileName()).append("\n\n");
                sb.append("Dimensions: ").append(width).append(" x ").append(height).append(" pixels\n");
                sb.append("File size: ").append(formatFileSize(fileSize)).append("\n");
                sb.append("\n[Binary image data - use /bash img2txt or similar for preview]");
            } else {
                sb.append("[").append(ext).append("] ").append(path.getFileName()).append("\n");
                sb.append("File size: ").append(formatFileSize(fileSize)).append("\n");
                sb.append("\n[Unable to read image dimensions]");
            }
        } catch (Exception e) {
            sb.append("[").append(ext).append("] ").append(path.getFileName()).append("\n");
            sb.append("File size: ").append(formatFileSize(fileSize)).append("\n");
            sb.append("\n[Image preview not available]");
        }

        return sb.toString();
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
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
        sb.append("File size: ").append(formatFileSize(fileSize)).append("\n\n");

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
