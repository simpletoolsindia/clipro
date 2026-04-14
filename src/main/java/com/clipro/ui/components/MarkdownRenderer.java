package com.clipro.ui.components;

import com.clipro.ui.Terminal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Simple markdown renderer for terminal output with syntax highlighting.
 * Reference: openclaude/src/components/Markdown.tsx
 *
 * Features:
 * - Syntax highlighting for 18+ languages
 * - Code blocks with line numbers (M-10)
 * - Markdown tables with box-drawing characters (M-09)
 * - Inline code, bold, italic, links
 */
public class MarkdownRenderer {
    private static final String RESET = Terminal.ansi("0");
    private static final String BOLD_START = Terminal.ansi("1");
    private static final String ITALIC_START = Terminal.ansi("3");
    private static final String CODE_BG = Terminal.ansi("40");
    private static final String LINK_COLOR = Terminal.ansi("34");
    private static final String DIM = Terminal.ansi("2");
    private static final String RESET_DIM = Terminal.ansi("22");
    private static final String CYAN = Terminal.ansi("36");

    // Syntax highlighter instance
    private static final SyntaxHighlighter syntaxHighlighter = new SyntaxHighlighter();

    // Patterns
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)");
    private static final Pattern CODE_PATTERN = Pattern.compile("`([^`]+)`");
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w*)\\n([\\s\\S]*?)```");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
    private static final Pattern TABLE_FULL_PATTERN = Pattern.compile("(?m)((?:^\\|.*\\|$\\n?)+)");

    public static String render(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        String result = markdown;

        // Process code blocks first (highest priority)
        final String[] holder = {result};
        result = CODE_BLOCK_PATTERN.matcher(result).replaceAll(match -> {
            String lang = match.group(1) != null ? match.group(1) : "";
            String code = match.group(2);
            return renderCodeBlock(code.trim(), lang);
        });

        // Inline code
        result = CODE_PATTERN.matcher(result).replaceAll(match -> {
            return renderInlineCode(match.group(1));
        });

        // Bold
        result = BOLD_PATTERN.matcher(result).replaceAll(match -> {
            return BOLD_START + match.group(1) + RESET;
        });

        // Italic
        result = ITALIC_PATTERN.matcher(result).replaceAll(match -> {
            return ITALIC_START + match.group(1) + RESET;
        });

        // Links
        result = LINK_PATTERN.matcher(result).replaceAll(match -> {
            return LINK_COLOR + match.group(1) + RESET + " (" + match.group(2) + ")";
        });

        // Headers to plain text with decoration
        result = result.replaceAll("(?m)^### (.+)$", BOLD_START + "$1" + RESET);
        result = result.replaceAll("(?m)^## (.+)$", BOLD_START + "$1" + RESET);
        result = result.replaceAll("(?m)^# (.+)$", BOLD_START + BOLD_START + "$1" + RESET);

        // Lists: - item -> • item
        result = result.replaceAll("(?m)^\\s*[-*]\\s+", "  • ");

        // Blockquotes: > text -> ▌ text
        result = result.replaceAll("(?m)^>\\s*(.+)$", DIM + "▌ $1" + RESET_DIM);

        // Tables
        result = renderTables(result);

        return result;
    }

    /**
     * Render a code block with syntax highlighting.
     * Integrates SyntaxHighlighter for 18+ language support.
     */
    public static String renderCodeBlock(String code) {
        return renderCodeBlock(code, "");
    }

    public static String renderCodeBlock(String code, String language) {
        if (code == null || code.isEmpty()) {
            return Terminal.BORDER_TL + Terminal.BORDER_H + Terminal.BORDER_H + "\n" +
                   Terminal.BORDER_BL + Terminal.BORDER_H + Terminal.BORDER_H + "\n";
        }

        // Apply syntax highlighting if language is known
        String highlighted = syntaxHighlighter.highlight(code, language);

        // Render with line numbers
        String[] lines = highlighted.split("\n");
        int gutterWidth = String.valueOf(lines.length).length();

        StringBuilder sb = new StringBuilder();
        sb.append(Terminal.BORDER_TL);

        // Header with language
        if (language != null && !language.isEmpty()) {
            sb.append(Terminal.BORDER_H).append(" ").append(language).append(" ");
            for (int i = language.length() + 2; i < Terminal.getColumns() - 2; i++) {
                sb.append(Terminal.BORDER_H);
            }
        } else {
            for (int i = 0; i < Terminal.getColumns() - 2; i++) {
                sb.append(Terminal.BORDER_H);
            }
        }
        sb.append(Terminal.BORDER_TR).append("\n");

        // Code lines with line numbers
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum];
            String lineNumStr = String.format("%" + gutterWidth + "d", lineNum + 1);
            sb.append(Terminal.BORDER_V)
              .append("\u001B[2m")  // DIM
              .append(lineNumStr)
              .append(" ")
              .append(RESET)
              .append(" ")
              .append(line);
            // Pad to column width
            int lineLen = lineNumStr.length() + 1 + line.length() + 1;
            int padLen = Terminal.getColumns() - 2 - lineLen;
            if (padLen > 0) {
                for (int i = 0; i < padLen; i++) sb.append(" ");
            }
            sb.append(" ").append(Terminal.BORDER_V).append("\n");
        }

        sb.append(Terminal.BORDER_BL);
        for (int i = 0; i < Terminal.getColumns() - 2; i++) {
            sb.append(Terminal.BORDER_H);
        }
        sb.append(Terminal.BORDER_BR);

        return sb.toString();
    }

    /**
     * Render inline code.
     */
    public static String renderInlineCode(String code) {
        return CODE_BG + code + RESET;
    }

    /**
     * Syntax highlight code (delegates to SyntaxHighlighter).
     */
    public static String highlightCode(String code) {
        return syntaxHighlighter.highlight(code, "");
    }

    /**
     * Syntax highlight code with language.
     */
    public static String highlightCode(String code, String language) {
        return syntaxHighlighter.highlight(code, language);
    }

    /**
     * Render markdown tables with box-drawing characters (M-09).
     * Supports: | col1 | col2 | with | --- | --- | separator.
     * Alignment: :--- (left), :---: (center), ---: (right).
     */
    private static String renderTables(String text) {
        StringBuffer result = new StringBuffer();
        Matcher tableMatcher = TABLE_FULL_PATTERN.matcher(text);

        while (tableMatcher.find()) {
            String tableBlock = tableMatcher.group(1);
            String renderedTable = renderTableBlock(tableBlock);
            tableMatcher.appendReplacement(result, Matcher.quoteReplacement(renderedTable));
        }
        tableMatcher.appendTail(result);

        return result.toString();
    }

    /**
     * Render a single table block with proper alignment.
     */
    private static String renderTableBlock(String tableBlock) {
        String[] lines = tableBlock.trim().split("\n");
        if (lines.length < 2) return tableBlock;

        List<String[]> rows = new ArrayList<>();
        List<int[]> alignments = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            // Skip separator line
            if (line.matches("^\\|[-:\\s]+\\|.*$")) {
                alignments.add(parseAlignments(line));
                continue;
            }

            // Parse cells
            String[] cells = line.split("\\|");
            List<String> cellList = new ArrayList<>();
            for (int j = 0; j < cells.length; j++) {
                String cell = cells[j].trim();
                if (j == 0 && cell.isEmpty()) continue; // Skip leading empty
                if (j == cells.length - 1 && cell.isEmpty()) continue; // Skip trailing empty
                cellList.add(cell);
            }
            if (!cellList.isEmpty()) {
                rows.add(cellList.toArray(new String[0]));
            }
        }

        if (rows.isEmpty()) return tableBlock;

        // Determine column widths
        int colCount = rows.get(0).length;
        int[] colWidths = new int[colCount];
        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, colCount); i++) {
                colWidths[i] = Math.max(colWidths[i], stripAnsi(row[i]).length());
            }
        }

        // Default alignments
        int[] colAligns = alignments.isEmpty() ? new int[colCount] : alignments.get(0);
        for (int i = 0; i < colAligns.length; i++) {
            if (colAligns[i] == 0) colAligns[i] = -1; // Default: left
        }

        StringBuilder sb = new StringBuilder();

        // Top border
        sb.append("┌");
        for (int i = 0; i < colCount; i++) {
            if (i > 0) sb.append("┬");
            sb.append("─".repeat(colWidths[i] + 2));
        }
        sb.append("┐\n");

        // Header row
        sb.append("│");
        for (int i = 0; i < colCount; i++) {
            if (i > 0) sb.append("│");
            String cell = rows.get(0)[i];
            int width = colWidths[i];
            sb.append(" ").append(formatCell(cell, width, colAligns[i])).append(" ");
        }
        sb.append("│\n");

        // Header separator
        sb.append("├");
        for (int i = 0; i < colCount; i++) {
            if (i > 0) sb.append("┼");
            sb.append("─".repeat(colWidths[i] + 2));
        }
        sb.append("┤\n");

        // Data rows
        for (int r = 1; r < rows.size(); r++) {
            sb.append("│");
            for (int i = 0; i < colCount; i++) {
                if (i > 0) sb.append("│");
                String cell = i < rows.get(r).length ? rows.get(r)[i] : "";
                int width = colWidths[i];
                sb.append(" ").append(formatCell(cell, width, colAligns[i])).append(" ");
            }
            sb.append("│\n");
        }

        // Bottom border
        sb.append("└");
        for (int i = 0; i < colCount; i++) {
            if (i > 0) sb.append("┴");
            sb.append("─".repeat(colWidths[i] + 2));
        }
        sb.append("┘");

        return sb.toString();
    }

    /**
     * Parse alignment from separator row.
     * -1 = left, 0 = center, 1 = right
     */
    private static int[] parseAlignments(String separator) {
        String[] cells = separator.split("\\|");
        int[] aligns = new int[cells.length];
        for (int i = 0; i < cells.length; i++) {
            String cell = cells[i].trim();
            if (cell.startsWith(":") && cell.endsWith(":")) {
                aligns[i] = 0; // center
            } else if (cell.endsWith(":")) {
                aligns[i] = 1; // right
            } else {
                aligns[i] = -1; // left (default)
            }
        }
        return aligns;
    }

    /**
     * Format cell content with alignment and padding.
     */
    private static String formatCell(String content, int width, int align) {
        String stripped = stripAnsi(content);
        int padding = width - stripped.length();
        if (padding <= 0) {
            return content;
        }

        switch (align) {
            case 0: // center
                int leftPad = padding / 2;
                int rightPad = padding - leftPad;
                return " ".repeat(leftPad) + content + " ".repeat(rightPad);
            case 1: // right
                return " ".repeat(padding) + content;
            default: // left
                return content + " ".repeat(padding);
        }
    }

    /**
     * Wrap content in a box with optional title.
     */
    public static String wrapInBox(String content, String title) {
        String[] lines = content.split("\n");
        int maxLen = 0;
        for (String line : lines) {
            // Strip ANSI codes for length calculation
            String stripped = stripAnsi(line);
            maxLen = Math.max(maxLen, stripped.length());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(Terminal.BORDER_TL);
        if (title != null) {
            sb.append(Terminal.BORDER_H).append(" ").append(title).append(" ");
            for (int i = title.length() + 2; i < maxLen + 2; i++) sb.append(Terminal.BORDER_H);
        } else {
            for (int i = 0; i < maxLen + 2; i++) sb.append(Terminal.BORDER_H);
        }
        sb.append(Terminal.BORDER_TR).append("\n");

        for (String line : lines) {
            String stripped = stripAnsi(line);
            sb.append(Terminal.BORDER_V).append(" ").append(line);
            for (int i = stripped.length(); i < maxLen; i++) sb.append(" ");
            sb.append(" ").append(Terminal.BORDER_V).append("\n");
        }

        sb.append(Terminal.BORDER_BL);
        for (int i = 0; i < maxLen + 2; i++) sb.append(Terminal.BORDER_H);
        sb.append(Terminal.BORDER_BR);

        return sb.toString();
    }

    /**
     * Strip ANSI escape codes from string.
     */
    private static String stripAnsi(String text) {
        return text.replaceAll("\u001B\\[[0-9;]*[a-zA-Z]", "");
    }
}
