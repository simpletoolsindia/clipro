package com.clipro.logging;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogLevelTest {

    @Test
    void shouldHaveCorrectPriorityOrder() {
        assertEquals(0, LogLevel.DEBUG.getPriority());
        assertEquals(1, LogLevel.INFO.getPriority());
        assertEquals(2, LogLevel.WARN.getPriority());
        assertEquals(3, LogLevel.ERROR.getPriority());
    }

    @Test
    void shouldReturnCorrectName() {
        assertEquals("DEBUG", LogLevel.DEBUG.getName());
        assertEquals("INFO", LogLevel.INFO.getName());
        assertEquals("WARN", LogLevel.WARN.getName());
        assertEquals("ERROR", LogLevel.ERROR.getName());
    }

    @Test
    void shouldEnableHigherOrEqualPriority() {
        LogLevel threshold = LogLevel.INFO;

        assertTrue(LogLevel.INFO.isEnabled(threshold));
        assertTrue(LogLevel.WARN.isEnabled(threshold));
        assertTrue(LogLevel.ERROR.isEnabled(threshold));
        assertFalse(LogLevel.DEBUG.isEnabled(threshold));
    }

    @Test
    void shouldEnableSamePriority() {
        LogLevel threshold = LogLevel.ERROR;
        assertTrue(LogLevel.ERROR.isEnabled(threshold));
    }
}
