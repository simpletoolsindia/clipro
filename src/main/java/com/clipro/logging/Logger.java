package com.clipro.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple logging utility with levels.
 */
public class Logger {

    public enum LogLevel { DEBUG, INFO, WARN, ERROR }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static LogLevel level = LogLevel.INFO;

    private final String name;

    public Logger(String name) {
        this.name = name;
    }

    public static void setLevel(LogLevel l) {
        level = l;
    }

    public static LogLevel getLevel() {
        return level;
    }

    public void info(String message) {
        if (level.ordinal() <= LogLevel.INFO.ordinal()) {
            System.out.println(format("INFO", message));
        }
    }

    public void error(String message) {
        if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
            System.err.println(format("ERROR", message));
        }
    }

    public void error(String message, Throwable t) {
        if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
            System.err.println(format("ERROR", message + " - " + t.getMessage()));
        }
    }

    public void warn(String message) {
        if (level.ordinal() <= LogLevel.WARN.ordinal()) {
            System.out.println(format("WARN", message));
        }
    }

    public void debug(String message) {
        if (level.ordinal() <= LogLevel.DEBUG.ordinal()) {
            System.out.println(format("DEBUG", message));
        }
    }

    private String format(String level, String message) {
        return String.format("%s [%s] %s: %s",
            LocalDateTime.now().format(FORMATTER),
            level,
            name,
            message
        );
    }
}