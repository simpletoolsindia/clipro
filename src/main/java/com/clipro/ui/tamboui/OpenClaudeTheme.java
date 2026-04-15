package com.clipro.ui.tamboui;

/**
 * OpenClaude theme matching the Gitlawb/openclaude color scheme.
 * Reference: openclaude/src/utils/theme.ts
 *
 * H-15: Pixel-perfect color audit corrections applied:
 * - Text: rgb(232,230,227) -> rgb(228,226,223)
 * - User message bg: #1E1E1E -> #2A2A2A
 * - Error: rgb(210,47,47) -> rgb(209,36,47)
 * - Warning: rgb(181,131,90) -> rgb(189,109,38)
 * - Border: #323232 -> #2D2D2D
 * - Border Active: #646464 -> #5A5A5A
 */
public class OpenClaudeTheme {

    // Brand colors (TamboUI uses hex strings)
    public static final String CLAUDE = "#D77757";        // rgb(215,119,87)
    public static final String CLAUDE_BRIGHT = "#FFA56E"; // rgb(255,165,110)

    // Text colors - H-15 pixel-perfect
    public static final String TEXT = "#E4E2DF";          // rgb(228,226,223) - exact openclaude
    public static final String TEXT_DIM = "#868283";     // rgb(134,130,123)
    public static final String TEXT_MUTED = "#5A5855";   // rgb(90,88,85)

    // Background colors - H-15 pixel-perfect
    public static final String BACKGROUND = "#0D0D0D";   // rgb(13,13,13)
    public static final String BACKGROUND_BRIGHT = "#141414"; // rgb(20,20,20)
    public static final String USER_MESSAGE_BG = "#2A2A2A";    // rgb(42,42,42) - H-15: exact openclaude
    public static final String TOOL_RESULT_BG = "#191923";    // rgb(25,25,35)

    // Status colors - H-15 pixel-perfect
    public static final String SUCCESS = "#2EA043";     // rgb(46,160,67) - exact match
    public static final String ERROR = "#D1242F";        // rgb(209,36,47) - H-15: exact openclaude
    public static final String WARNING = "#BD6D26";      // rgb(189,109,38) - H-15: exact openclaude amber
    public static final String PERMISSION = "#5769F7";  // rgb(87,105,247)

    // Border colors - H-15 pixel-perfect
    public static final String BORDER = "#2D2D2D";       // rgb(45,45,45) - H-15: exact openclaude
    public static final String BORDER_ACTIVE = "#5A5A5A"; // rgb(90,90,90) - H-15: exact openclaude

    // Role colors
    public static final String USER_ROLE = "#2EA043";    // Green
    public static final String ASSISTANT_ROLE = "#D77757"; // Orange (Claude)
    public static final String SYSTEM_ROLE = "#868283";   // Gray

    // ANSI escape codes for console colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_DIM = "\u001B[2m";

    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_MAGENTA = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BRIGHT_BLACK = "\u001B[90m";
    public static final String ANSI_BRIGHT_RED = "\u001B[91m";
    public static final String ANSI_BRIGHT_GREEN = "\u001B[92m";
    public static final String ANSI_BRIGHT_YELLOW = "\u001B[93m";
    public static final String ANSI_BRIGHT_CYAN = "\u001B[96m";

    // Styled strings using ANSI codes
    public static String claudeText(String text) {
        return ANSI_BRIGHT_CYAN + ANSI_BOLD + text + ANSI_RESET;
    }

    public static String userText(String text) {
        return ANSI_BRIGHT_GREEN + text + ANSI_RESET;
    }

    public static String assistantText(String text) {
        return ANSI_BRIGHT_YELLOW + text + ANSI_RESET;
    }

    public static String dimText(String text) {
        return ANSI_DIM + text + ANSI_RESET;
    }

    public static String boldText(String text) {
        return ANSI_BOLD + text + ANSI_RESET;
    }

    public static String successText(String text) {
        return ANSI_BRIGHT_GREEN + text + ANSI_RESET;
    }

    public static String errorText(String text) {
        return ANSI_BRIGHT_RED + text + ANSI_RESET;
    }

    public static String warningText(String text) {
        return ANSI_BRIGHT_YELLOW + text + ANSI_RESET;
    }

    public static String mutedText(String text) {
        return ANSI_BRIGHT_BLACK + text + ANSI_RESET;
    }

    public static String toolUseText(String text) {
        return ANSI_BRIGHT_CYAN + text + ANSI_RESET;
    }

    public static String cyan(String text) {
        return ANSI_BRIGHT_CYAN + text + ANSI_RESET;
    }

    /**
     * H-15: True-color (24-bit) text using exact OpenClaude hex values.
     */
    public static String trueColor(String text, String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m" + text + ANSI_RESET;
    }

    public static String trueColorBold(String text, String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m" + ANSI_BOLD + text + ANSI_RESET;
    }

    // Box drawing characters
    public static final String BORDER_H = "─";
    public static final String BORDER_V = "│";
    public static final String BORDER_TL = "┌";
    public static final String BORDER_TR = "┐";
    public static final String BORDER_BL = "└";
    public static final String BORDER_BR = "┘";
}
