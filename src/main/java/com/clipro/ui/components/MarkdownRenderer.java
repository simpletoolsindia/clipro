package com.clipro.ui.components;

import com.clipro.ui.Terminal;
import java.util.regex.Pattern;

/**
 * Simple markdown renderer for terminal output.
 * Reference: openclaude/src/components/Message.tsx
 */
public class MarkdownRenderer {
    private static final String BOLD_START = Terminal.ansi("1");
    private static final String ITALIC_START = Terminal.ansi("3");
    private static final String CODE_BG = Terminal.ansi("40");
    private static final String LINK_COLOR = Terminal.ansi("34");
    private static final String RESET = Terminal.ansi("0");

    // Patterns
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)");
    private static final Pattern CODE_PATTERN = Pattern.compile("`([^`]+)`");
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```[\\s\\S]*?```");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");

    public static String render(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        String result = markdown;

        // Process code blocks first (highest priority)
        result = CODE_BLOCK_PATTERN.matcher(result).replaceAll(match -> {
            String code = match.group().replaceAll("```\\w*", "").trim();
            return renderCodeBlock(code);
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
        result = result.replaceAll("^### (.+)$", BOLD_START + "$1" + RESET);
        result = result.replaceAll("^## (.+)$", BOLD_START + "$1" + RESET);
        result = result.replaceAll("^# (.+)$", BOLD_START + BOLD_START + "$1" + RESET);

        return result;
    }

    public static String renderInlineCode(String code) {
        return CODE_BG + code + RESET;
    }

    public static String renderCodeBlock(String code) {
        return "\n┌─\n" +
               "│ " + CODE_BG + code.replace("\n", "\n│ ") + RESET + "\n" +
               "└─\n";
    }

    public static String renderCodeBlock(String code, String language) {
        return "┌─ " + language + "\n" +
               "│ " + CODE_BG + code.replace("\n", "\n│ ") + RESET + "\n" +
               "└─\n";
    }

    public static String wrapInBox(String content, String title) {
        String[] lines = content.split("\n");
        int maxLen = lines[0].length();
        for (String line : lines) {
            maxLen = Math.max(maxLen, line.length());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("┌");
        if (title != null) {
            sb.append("─ ").append(title).append(" ");
            for (int i = title.length() + 2; i < maxLen; i++) sb.append("─");
        } else {
            for (int i = 0; i < maxLen + 2; i++) sb.append("─");
        }
        sb.append("┐\n");

        for (String line : lines) {
            sb.append("│ ").append(line);
            for (int i = line.length(); i < maxLen; i++) sb.append(" ");
            sb.append(" │\n");
        }

        sb.append("└");
        for (int i = 0; i < maxLen + 2; i++) sb.append("─");
        sb.append("┘");

        return sb.toString();
    }
}
