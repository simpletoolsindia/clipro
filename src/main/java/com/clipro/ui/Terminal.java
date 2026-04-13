package com.clipro.ui;

/**
 * Terminal utilities for TUI.
 */
public class Terminal {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";

    public static int getColumns() {
        return 80;
    }

    public static int getRows() {
        return 24;
    }

    public static String red(String text) {
        return ANSI_RED + text + ANSI_RESET;
    }

    public static String green(String text) {
        return ANSI_GREEN + text + ANSI_RESET;
    }

    public static String yellow(String text) {
        return ANSI_YELLOW + text + ANSI_RESET;
    }

    public static String blue(String text) {
        return ANSI_BLUE + text + ANSI_RESET;
    }

    public static String cyan(String text) {
        return ANSI_CYAN + text + ANSI_RESET;
    }

    public static void clear() {
        System.out.print("\u001B[2J");
        System.out.print("\u001B[H");
    }

    public static void cursorHome() {
        System.out.print("\u001B[H");
    }

    public static void clearLine() {
        System.out.print("\u001B[2K");
    }

    /**
     * Create ANSI escape code.
     */
    public static String ansi(String code) {
        return "\u001B[" + code + "m";
    }

    /**
     * Bold text.
     */
    public static String bold(String text) {
        return "\u001B[1m" + text + ANSI_RESET;
    }

    /**
     * Enter alternate screen.
     */
    public static void enterAltScreen() {
        System.out.print("\u001B[?1049h");
    }

    /**
     * Exit alternate screen.
     */
    public static void exitAltScreen() {
        System.out.print("\u001B[?1049l");
    }

    /**
     * Dim text.
     */
    public static String dim(String text) {
        return "\u001B[2m" + text + ANSI_RESET;
    }
}
