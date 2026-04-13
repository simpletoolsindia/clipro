package com.clipro.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static LogLevel currentLevel = LogLevel.INFO;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String name;

    public Logger(String name) {
        this.name = name;
    }

    public static void setLevel(LogLevel level) {
        currentLevel = level;
    }

    public static LogLevel getLevel() {
        return currentLevel;
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message + " - " + throwable.getMessage());
    }

    private void log(LogLevel level, String message) {
        if (level.isEnabled(currentLevel)) {
            String timestamp = LocalDateTime.now().format(FORMATTER);
            System.out.println(timestamp + " [" + level.getName() + "] " + name + ": " + message);
        }
    }
}
