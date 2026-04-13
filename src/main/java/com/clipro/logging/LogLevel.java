package com.clipro.logging;

public enum LogLevel {
    DEBUG(0, "DEBUG"),
    INFO(1, "INFO"),
    WARN(2, "WARN"),
    ERROR(3, "ERROR");

    private final int priority;
    private final String name;

    LogLevel(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled(LogLevel threshold) {
        return this.priority >= threshold.priority;
    }
}
