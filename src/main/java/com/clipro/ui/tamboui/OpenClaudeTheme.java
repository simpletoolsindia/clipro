package com.clipro.ui.tamboui;

/**
 * OpenClaude theme constants matching Gitlawb/openclaude darkTheme exactly.
 * Reference: openclaude/src/utils/theme.ts - darkTheme object
 *
 * ALL colors match openclaude's darkTheme RGB values pixel-for-pixel.
 * H-15: Fixed to use exact openclaude RGB values (not "corrected" wrong values).
 */
public class OpenClaudeTheme {

    // Brand colors - exact match darkTheme
    public static final String CLAUDE = "#D77757";              // rgb(215,119,87)
    public static final String CLAUDE_SHIMMER = "#EBA17F";     // rgb(235,159,127)
    public static final String CLAUDE_BLUE = "#93A5FF";        // rgb(147,165,255) - system spinner
    public static final String CLAUDE_BLUE_SHIMMER = "#B1C3FF"; // rgb(177,195,255)

    // Text colors - exact match darkTheme
    public static final String TEXT = "#FFFFFF";               // rgb(255,255,255)
    public static final String INVERSE_TEXT = "#000000";        // rgb(0,0,0)
    public static final String SUBTLE = "#505050";             // rgb(80,80,80)
    public static final String INACTIVE = "#999999";           // rgb(153,153,153)
    public static final String INACTIVE_SHIMMER = "#C1C1C1";   // rgb(193,193,193)
    public static final String TEXT_DIM = "#505050";           // rgb(80,80,80)
    public static final String TEXT_MUTED = "#505050";         // rgb(80,80,80)

    // Background colors - exact match darkTheme
    public static final String BACKGROUND = "#0f0f0f";         // Dark background for terminal
    public static final String BACKGROUND_BRIGHT = "#1a1a1a";   // Slightly brighter for hover/select
    public static final String USER_MESSAGE_BG = "#373737";     // rgb(55,55,55)
    public static final String USER_MESSAGE_HOVER = "#464646";  // rgb(70,70,70)
    public static final String TOOL_RESULT_BG = "#191923";      // rgb(25,25,35)
    public static final String MESSAGE_ACTIONS_BG = "#2C323E";  // rgb(44,50,62)
    public static final String SELECTION_BG = "#264F78";        // rgb(38,79,120)
    public static final String BASH_MSG_BG = "#413C41";        // rgb(65,60,65)
    public static final String MEMORY_BG = "#374146";          // rgb(55,65,70)

    // Status colors - exact match darkTheme
    public static final String SUCCESS = "#4EBA65";             // rgb(78,186,101)
    public static final String ERROR = "#FF6B80";               // rgb(255,107,128)
    public static final String WARNING = "#FFC107";             // rgb(255,193,7)
    public static final String WARNING_SHIMMER = "#FFDF39";     // rgb(255,223,57)
    public static final String MERGED = "#AF87FF";              // rgb(175,135,255)
    public static final String AUTO_ACCEPT = "#AF87FF";        // rgb(175,135,255)
    public static final String PERMISSION = "#B1B9F9";         // rgb(177,185,249)
    public static final String PERMISSION_SHIMMER = "#CFD7FF"; // rgb(207,215,255)
    public static final String SUGGESTION = "#B1B9F9";         // rgb(177,185,249)
    public static final String REMEMBER = "#B1B9F9";           // rgb(177,185,249)

    // Border colors - exact match darkTheme
    public static final String BASH_BORDER = "#FD5DB1";        // rgb(253,93,177) - bright pink
    public static final String PROMPT_BORDER = "#888888";       // rgb(136,136,136)
    public static final String PROMPT_BORDER_SHIMMER = "#A6A6A6"; // rgb(166,166,166)
    public static final String BORDER = "#888888";              // rgb(136,136,136)
    public static final String BORDER_ACTIVE = "#A6A6A6";       // rgb(166,166,166)

    // Role colors
    public static final String USER_ROLE = "#7AB4E8";           // rgb(122,180,232) - briefLabelYou
    public static final String ASSISTANT_ROLE = "#D77757";      // rgb(215,119,87) - Claude orange
    public static final String SYSTEM_ROLE = "#999999";        // rgb(153,153,153) - inactive

    // UI element colors - exact match darkTheme
    public static final String PLAN_MODE = "#48968C";           // rgb(72,150,140)
    public static final String IDE = "#4782C8";                  // rgb(71,130,200)
    public static final String FAST_MODE = "#FF7814";           // rgb(255,120,20)
    public static final String FAST_MODE_SHIMMER = "#FFA546";   // rgb(255,165,70)

    // Rate limit colors - exact match darkTheme
    public static final String RATE_LIMIT_FILL = "#B1B9F9";    // rgb(177,185,249)
    public static final String RATE_LIMIT_EMPTY = "#505370";    // rgb(80,83,112)

    // Brief mode labels - exact match darkTheme
    public static final String BRIEF_LABEL_YOU = "#7AB4E8";     // rgb(122,180,232)
    public static final String BRIEF_LABEL_CLAUDE = "#D77757";  // rgb(215,119,87)

    // Chrome colors - exact match darkTheme
    public static final String CHROME_YELLOW = "#FBBC04";       // rgb(251,188,4)

    // Grove colors - exact match darkTheme
    public static final String PROFESSIONAL_BLUE = "#6A9BCC";   // rgb(106,155,204)

    // CLAWD TUI colors - exact match darkTheme
    public static final String CLAWD_BODY = "#D77757";           // rgb(215,119,87)
    public static final String CLAWD_BACKGROUND = "#000000";      // rgb(0,0,0)

    // Diff colors - exact match darkTheme
    public static final String DIFF_ADDED = "#225C2B";          // rgb(34,92,43)
    public static final String DIFF_REMOVED = "#7A2936";          // rgb(122,41,54)
    public static final String DIFF_ADDED_DIMMED = "#47584A";   // rgb(71,88,74)
    public static final String DIFF_REMOVED_DIMMED = "#69484D";  // rgb(105,72,77)
    public static final String DIFF_ADDED_WORD = "#38A660";     // rgb(56,166,96)
    public static final String DIFF_REMOVED_WORD = "#B3596B";   // rgb(179,89,107)

    // Agent colors (8 sub-agent colors) - exact match darkTheme
    public static final String AGENT_RED = "#DC2626";            // rgb(220,38,38)
    public static final String AGENT_BLUE = "#2563EB";           // rgb(37,99,235)
    public static final String AGENT_GREEN = "#16A34A";          // rgb(22,163,74)
    public static final String AGENT_YELLOW = "#CA8A04";         // rgb(202,138,4)
    public static final String AGENT_PURPLE = "#9333EA";         // rgb(147,51,234)
    public static final String AGENT_ORANGE = "#EA580C";         // rgb(234,88,12)
    public static final String AGENT_PINK = "#DB2777";            // rgb(219,39,119)
    public static final String AGENT_CYAN = "#0891B2";           // rgb(8,145,178)

    // Rainbow colors for ultrathink - exact match darkTheme
    public static final String RAINBOW_RED = "#EB5F57";          // rgb(235,95,87)
    public static final String RAINBOW_ORANGE = "#F58B57";        // rgb(245,139,87)
    public static final String RAINBOW_YELLOW = "#FAC35F";        // rgb(250,195,95)
    public static final String RAINBOW_GREEN = "#91C882";        // rgb(145,200,130)
    public static final String RAINBOW_BLUE = "#82AAE0";          // rgb(130,170,220)
    public static final String RAINBOW_INDIGO = "#9B82C8";         // rgb(155,130,200)
    public static final String RAINBOW_VIOLET = "#C882B4";        // rgb(200,130,180)

    // Rainbow shimmer variants - exact match darkTheme
    public static final String RAINBOW_RED_SHIMMER = "#FA9B93";     // rgb(250,155,147)
    public static final String RAINBOW_ORANGE_SHIMMER = "#FFB989";  // rgb(255,185,137)
    public static final String RAINBOW_YELLOW_SHIMMER = "#FFE19B";  // rgb(255,225,155)
    public static final String RAINBOW_GREEN_SHIMMER = "#B9E6B4";  // rgb(185,230,180)
    public static final String RAINBOW_BLUE_SHIMMER = "#B4CDF0";    // rgb(180,205,240)
    public static final String RAINBOW_INDIGO_SHIMMER = "#C3B4E6";  // rgb(195,180,230)
    public static final String RAINBOW_VIOLET_SHIMMER = "#E6B4D2";  // rgb(230,180,210)

    // ANSI escape codes (16-color fallback)
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

    // Styled strings using true-color (24-bit) ANSI
    public static String claudeText(String text) {
        return trueColor(text, CLAUDE);
    }

    public static String userText(String text) {
        return trueColor(text, BRIEF_LABEL_YOU);
    }

    public static String assistantText(String text) {
        return trueColor(text, CLAUDE);
    }

    public static String dimText(String text) {
        return trueColor(text, SUBTLE);
    }

    public static String boldText(String text) {
        return ANSI_BOLD + text + ANSI_RESET;
    }

    public static String successText(String text) {
        return trueColor(text, SUCCESS);
    }

    public static String errorText(String text) {
        return trueColor(text, ERROR);
    }

    public static String warningText(String text) {
        return trueColor(text, WARNING);
    }

    public static String mutedText(String text) {
        return trueColor(text, INACTIVE);
    }

    public static String toolUseText(String text) {
        return trueColor(text, PERMISSION);
    }

    public static String cyan(String text) {
        return trueColor(text, "#00CCCC");
    }

    /**
     * True-color (24-bit) text using RGB values.
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

    /**
     * Get rainbow color by index (0-6).
     */
    public static String getRainbowColor(int index) {
        String[] colors = {
            RAINBOW_RED, RAINBOW_ORANGE, RAINBOW_YELLOW, RAINBOW_GREEN,
            RAINBOW_BLUE, RAINBOW_INDIGO, RAINBOW_VIOLET
        };
        return colors[Math.abs(index) % 7];
    }

    /**
     * Get rainbow shimmer color by index (0-6).
     */
    public static String getRainbowShimmerColor(int index) {
        String[] colors = {
            RAINBOW_RED_SHIMMER, RAINBOW_ORANGE_SHIMMER, RAINBOW_YELLOW_SHIMMER,
            RAINBOW_GREEN_SHIMMER, RAINBOW_BLUE_SHIMMER, RAINBOW_INDIGO_SHIMMER,
            RAINBOW_VIOLET_SHIMMER
        };
        return colors[Math.abs(index) % 7];
    }

    /**
     * Get agent color by index (0-7).
     */
    public static String getAgentColor(int index) {
        String[] colors = {
            AGENT_RED, AGENT_BLUE, AGENT_GREEN, AGENT_YELLOW,
            AGENT_PURPLE, AGENT_ORANGE, AGENT_PINK, AGENT_CYAN
        };
        return colors[Math.abs(index) % 8];
    }

    // Box drawing characters
    public static final String BORDER_H = "─";
    public static final String BORDER_V = "│";
    public static final String BORDER_TL = "┌";
    public static final String BORDER_TR = "┐";
    public static final String BORDER_BL = "└";
    public static final String BORDER_BR = "┘";
}
