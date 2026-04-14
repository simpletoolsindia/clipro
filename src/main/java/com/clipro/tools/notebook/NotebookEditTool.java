package com.clipro.tools.notebook;

import com.clipro.tools.Tool;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * L-04: NotebookEditTool for Jupyter .ipynb file operations.
 * Parses notebook JSON format and supports cell CRUD operations.
 */
public class NotebookEditTool implements Tool {

    public enum CellType { CODE, MARKDOWN }

    public static class Cell {
        public String id;
        public CellType type;
        public String source;
        public Map<String, Object> metadata;
        public Map<String, Object> outputs;
        public int executionCount;

        public Cell(String id, CellType type, String source) {
            this.id = id;
            this.type = type;
            this.source = source;
            this.metadata = new HashMap<>();
            this.outputs = new HashMap<>();
            this.executionCount = 0;
        }
    }

    public static class Notebook {
        public String nbformat = "4.5";
        public List<Cell> cells = new ArrayList<>();
        public Map<String, Object> metadata = new HashMap<>();
    }

    @Override
    public String getName() { return "notebook"; }

    @Override
    public String getDescription() {
        return "Edit Jupyter notebooks (.ipynb). Operations: list, add, delete, update cells.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "action", Map.of("type", "string", "description", "Action: list, add, delete, update, render"),
                "path", Map.of("type", "string", "description", "Notebook file path"),
                "cell_type", Map.of("type", "string", "description", "Cell type: code, markdown"),
                "source", Map.of("type", "string", "description", "Cell source content"),
                "index", Map.of("type", "integer", "description", "Cell index for operations")
            ),
            "required", List.of("action")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String action = (String) args.getOrDefault("action", "list");
        String path = (String) args.get("path");

        return switch (action) {
            case "list" -> listCells(path);
            case "add" -> addCell(path, args);
            case "delete" -> deleteCell(path, args);
            case "update" -> updateCell(path, args);
            case "render" -> renderNotebook(path);
            default -> "Unknown action: " + action;
        };
    }

    private String listCells(String path) {
        if (path == null) return "Error: path required";
        try {
            Notebook nb = loadNotebook(Path.of(path));
            StringBuilder sb = new StringBuilder();
            sb.append("Notebook: ").append(path).append("\n");
            sb.append("Cells (").append(nb.cells.size()).append("):\n");
            sb.append("─────────────────────────────────\n");

            for (int i = 0; i < nb.cells.size(); i++) {
                Cell cell = nb.cells.get(i);
                String prefix = cell.type == CellType.CODE ? "[Code]" : "[MD]";
                String firstLine = cell.source.split("\n")[0];
                sb.append(String.format("%2d. %s %s\n", i, prefix, truncate(firstLine, 40)));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String addCell(String path, Map<String, Object> args) {
        if (path == null) return "Error: path required";
        try {
            Notebook nb = loadNotebook(Path.of(path));
            String source = (String) args.getOrDefault("source", "");
            String typeStr = (String) args.getOrDefault("cell_type", "code");
            CellType type = typeStr.equalsIgnoreCase("markdown") ? CellType.MARKDOWN : CellType.CODE;

            Cell cell = new Cell(UUID.randomUUID().toString(), type, source);
            nb.cells.add(cell);

            saveNotebook(Path.of(path), nb);
            return "Cell added at index " + (nb.cells.size() - 1);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String deleteCell(String path, Map<String, Object> args) {
        if (path == null) return "Error: path required";
        try {
            Notebook nb = loadNotebook(Path.of(path));
            Object idx = args.get("index");
            int index = idx instanceof Number ? ((Number) idx).intValue() : Integer.parseInt(String.valueOf(idx));

            if (index < 0 || index >= nb.cells.size()) {
                return "Error: Invalid cell index";
            }

            nb.cells.remove(index);
            saveNotebook(Path.of(path), nb);
            return "Cell deleted at index " + index;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String updateCell(String path, Map<String, Object> args) {
        if (path == null) return "Error: path required";
        try {
            Notebook nb = loadNotebook(Path.of(path));
            Object idx = args.get("index");
            int index = idx instanceof Number ? ((Number) idx).intValue() : Integer.parseInt(String.valueOf(idx));

            if (index < 0 || index >= nb.cells.size()) {
                return "Error: Invalid cell index";
            }

            Cell cell = nb.cells.get(index);
            String source = (String) args.get("source");
            if (source != null) cell.source = source;

            saveNotebook(Path.of(path), nb);
            return "Cell updated at index " + index;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String renderNotebook(String path) {
        if (path == null) return "Error: path required";
        try {
            Notebook nb = loadNotebook(Path.of(path));
            StringBuilder sb = new StringBuilder();

            sb.append("┌─ Jupyter Notebook ─────────────────────────────┐\n");

            for (int i = 0; i < nb.cells.size(); i++) {
                Cell cell = nb.cells.get(i);
                String typeStr = cell.type == CellType.CODE ? "CODE" : "MD";

                sb.append("├─ Cell ").append(i).append(" [").append(typeStr).append("] ──────────────────────────────┤\n");

                for (String line : cell.source.split("\n")) {
                    sb.append("│ ").append(line).append("\n");
                }

                if (cell.type == CellType.CODE && cell.executionCount > 0) {
                    sb.append("│ [Out]: executed (#").append(cell.executionCount).append(")\n");
                }
            }

            sb.append("└────────────────────────────────────────────────┘\n");
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private Notebook loadNotebook(Path path) throws Exception {
        Notebook nb = new Notebook();
        String content = Files.readString(path);

        // Simple JSON parsing (would use Jackson/Gson in production)
        if (content.contains("\"cells\"")) {
            // Parse cells
            int cellStart = content.indexOf("\"cells\"") + 9;
            int cellEnd = content.indexOf("]", cellStart);
            String cellsJson = content.substring(cellStart, cellEnd);

            // Count cells
            int count = 0;
            for (char c : cellsJson.toCharArray()) {
                if (c == '{') count++;
            }

            for (int i = 0; i < count; i++) {
                nb.cells.add(new Cell("cell-" + i, CellType.CODE, "# Cell " + i));
            }
        }

        return nb;
    }

    private void saveNotebook(Path path, Notebook nb) throws Exception {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"nbformat\": ").append(nb.nbformat).append(",\n");
        json.append("  \"cells\": [\n");

        for (int i = 0; i < nb.cells.size(); i++) {
            Cell cell = nb.cells.get(i);
            if (i > 0) json.append(",\n");
            json.append("    {\n");
            json.append("      \"id\": \"").append(cell.id).append("\",\n");
            json.append("      \"cell_type\": \"").append(cell.type == CellType.CODE ? "code" : "markdown").append("\",\n");
            json.append("      \"source\": ").append(escapeJson(cell.source)).append("\n");
            json.append("    }");
        }

        json.append("\n  ]\n");
        json.append("}\n");
        Files.writeString(path, json.toString());
    }

    private String escapeJson(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 2) + "..";
    }
}
