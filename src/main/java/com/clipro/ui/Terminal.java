package com.clipro.ui;

import java.io.Console;
import java.io.IOException;

/**
 * Terminal utilities for size detection and ANSI output.
 * Reference: openclaude/src/ink/terminal.ts
 */
public class Terminal {
    private static int columns = 80;
    private static int rows = 24;

    static {
        detectSize();
    }

    public static int getColumns() {
        return columns;
    }

    public static int getRows() {
        return rows;
    }

    public static void detectSize() {
        try {
            // Use stty for Unix systems
            ProcessBuilder pb = new ProcessBuilder("stty", "size");
            pb.redirectErrorStream(true);
            Process p = pb.start();

            String output = new String(p.getInputStream().readAllBytes()).trim();
            p.waitFor();

            if (!output.isEmpty()) {
                String[] parts = output.split("\\s+");
                if (parts.length == 2) {
                    rows = Integer.parseInt(parts[0]);
                    columns = Integer.parseInt(parts[1]);
                }
            }
        } catch (IOException | InterruptedException | NumberFormatException e) {
            // Fall back to defaults
        }

        // Try environment as fallback
        try {
            String cols = System.getenv("COLUMNS");
            String lines = System.getenv("LINES");
            if (cols != null) columns = Integer.parseInt(cols);
            if (lines != null) rows = Integer.parseInt(lines);
        } catch (NumberFormatException e) {
            // Use defaults
        }
    }

    public static void clear() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    public static void moveCursor(int row, int col) {
        System.out.print("\033[" + row + ";" + col + "H");
        System.out.flush();
    }

    public static void hideCursor() {
        System.out.print("\033[?25l");
        System.out.flush();
    }

    public static void showCursor() {
        System.out.print("\033[?25h");
        System.out.flush();
    }

    public static void enterAltScreen() {
        System.out.print("\033[?1049h");
        System.out.flush();
    }

    public static void exitAltScreen() {
        System.out.print("\033[?1049l");
        System.out.flush();
    }

    public static void resetAttributes() {
        System.out.print("\033[0m");
        System.out.flush();
    }

    public static String ansi(String code) {
        return "\033[" + code + "m";
    }

    public static String bold(String text) {
        return ansi("1") + text + ansi("0");
    }

    public static String dim(String text) {
        return ansi("2") + text + ansi("0");
    }

    public static String red(String text) {
        return ansi("31") + text + ansi("0");
    }

    public static String green(String text) {
        return ansi("32") + text + ansi("0");
    }

    public static String yellow(String text) {
        return ansi("33") + text + ansi("0");
    }

    public static String blue(String text) {
        return ansi("34") + text + ansi("0");
    }

    public static String cyan(String text) {
        return ansi("36") + text + ansi("0");
    }

    public static String boldRed(String text) {
        return ansi("1;31") + text + ansi("0");
    }

    public static String boldGreen(String text) {
        return ansi("1;32") + text + ansi("0");
    }
}
