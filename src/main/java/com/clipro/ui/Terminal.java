package com.clipro.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Terminal utilities for TUI - Pixel-perfect OpenClaude styling.
 */
public class Terminal {

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";

    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String GRAY = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_CYAN = "\u001B[96m";

    public static final String BORDER_H = "─";
    public static final String BORDER_V = "│";
    public static final String BORDER_TL = "┌";
    public static final String BORDER_TR = "┐";
    public static final String BORDER_BL = "└";
    public static final String BORDER_BR = "┘";

    public static final String CLAUDE_COLOR = BRIGHT_CYAN;
    public static final String USER_COLOR = BRIGHT_GREEN;

    private static int columns = 80;
    private static int rows = 24;

    public static void init() {
        detectTerminalSize();
    }

    public static void detectTerminalSize() {
        try {
            Process p = Runtime.getRuntime().exec("tput cols");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s = br.readLine();
            if (s != null) columns = Integer.parseInt(s.trim());
            br.close();
            p = Runtime.getRuntime().exec("tput lines");
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            s = br.readLine();
            if (s != null) rows = Integer.parseInt(s.trim());
            br.close();
        } catch (Exception e) {}
    }

    public static int getColumns() { init(); return columns; }
    public static int getRows() { init(); return rows; }

    public static String red(String t) { return RED + t + RESET; }
    public static String green(String t) { return GREEN + t + RESET; }
    public static String yellow(String t) { return YELLOW + t + RESET; }
    public static String cyan(String t) { return CYAN + t + RESET; }
    public static String gray(String t) { return GRAY + t + RESET; }
    public static String bold(String t) { return BOLD + t + RESET; }
    public static String dim(String t) { return DIM + t + RESET; }
    public static String user(String t) { return USER_COLOR + bold(t) + RESET; }
    public static String assistant(String t) { return CLAUDE_COLOR + t + RESET; }

    public static void clear() { System.out.print("\u001B[2J\u001B[H"); }
    public static void clearLine() { System.out.print("\u001B[2K"); }
    public static void cursorHome() { System.out.print("\u001B[H"); }
    public static void cursorUp(int n) { System.out.print("\u001B[" + n + "A"); }
    public static void cursorDown(int n) { System.out.print("\u001B[" + n + "B"); }
    public static void hideCursor() { System.out.print("\u001B[?25l"); }
    public static void showCursor() { System.out.print("\u001B[?25h"); }
    public static void enterAltScreen() { System.out.print("\u001B[?1049h"); }
    public static void exitAltScreen() { System.out.print("\u001B[?1049l"); }

    public static String ansi(String code) { return "\u001B[" + code + "m"; }

    public static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }

    public static String divider() { return repeat(BORDER_H, getColumns()); }

    public static String boxTop(int w) { return BORDER_TL + repeat(BORDER_H, w-2) + BORDER_TR; }
    public static String boxBottom(int w) { return BORDER_BL + repeat(BORDER_H, w-2) + BORDER_BR; }
    public static String boxRow(String content, int w) {
        return BORDER_V + content + repeat(" ", w - content.length() - 2) + BORDER_V;
    }

    public static String padRight(String s, int n) {
        if (n <= 0) return "";
        return repeat(" ", n);
    }
}