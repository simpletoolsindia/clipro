package com.clipro.ui.tamboui;

/**
 * OpenClaude theme matching the Gitlawb/openclaude color scheme.
 * Reference: openclaude/src/utils/theme.ts
 *
 * Colors:
 * - Claude brand: rgb(215,119,87) - warm orange
 * - Text: rgb(232,230,227) - off-white
 * - Background: rgb(13,13,13) - near-black
 * - User messages: rgb(30,30,30)
 * - Success: rgb(46,160,67)
 * - Error: rgb(210,47,47)
 * - Warning: rgb(181,131,90)
 * - Subtle: rgb(134,130,123)
 */
public class OpenClaudeTheme {

    // Brand colors (TamboUI uses hex strings)
    public static final String CLAUDE = "#D77757";        // rgb(215,119,87)
    public static final String CLAUDE_BRIGHT = "#FFA56E"; // rgb(255,165,110)

    // Text colors
    public static final String TEXT = "#E8E6E3";          // rgb(232,230,227)
    public static final String TEXT_DIM = "#868283";     // rgb(134,130,123)
    public static final String TEXT_MUTED = "#5A5855";   // rgb(90,88,85)

    // Background colors
    public static final String BACKGROUND = "#0D0D0D";   // rgb(13,13,13)
    public static final String BACKGROUND_BRIGHT = "#141414"; // rgb(20,20,20)
    public static final String USER_MESSAGE_BG = "#1E1E1E";    // rgb(30,30,30)
    public static final String TOOL_RESULT_BG = "#191923";    // rgb(25,25,35)

    // Status colors
    public static final String SUCCESS = "#2EA043";     // rgb(46,160,67)
    public static final String ERROR = "#D22F2F";        // rgb(210,47,47)
    public static final String WARNING = "#B5835A";      // rgb(181,131,90)
    public static final String PERMISSION = "#5769F7";  // rgb(87,105,247)

    // Border colors
    public static final String BORDER = "#323232";       // rgb(50,50,50)
    public static final String BORDER_ACTIVE = "#646464"; // rgb(100,100,100)

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

    // Box drawing characters
    public static final String BORDER_H = "─";
    public static final String BORDER_V = "│";
    public static final String BORDER_TL = "┌";
    public static final String BORDER_TR = "┐";
    public static final String BORDER_BL = "└";
    public static final String BORDER_BR = "┘";
}
