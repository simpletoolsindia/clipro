package com.clipro.logging;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    @Test
    void shouldCreateLoggerWithName() {
        Logger logger = new Logger("test");
        assertNotNull(logger);
    }

    @Test
    void shouldSetAndGetLogLevel() {
        Logger.setLevel(LogLevel.DEBUG);
        assertEquals(LogLevel.DEBUG, Logger.getLevel());

        Logger.setLevel(LogLevel.ERROR);
        assertEquals(LogLevel.ERROR, Logger.getLevel());
    }

    @Test
    void shouldNotThrowOnAnyLogLevel() {
        Logger logger = new Logger("test");
        Logger.setLevel(LogLevel.DEBUG);

        assertDoesNotThrow(() -> logger.debug("debug message"));
        assertDoesNotThrow(() -> logger.info("info message"));
        assertDoesNotThrow(() -> logger.warn("warn message"));
        assertDoesNotThrow(() -> logger.error("error message"));
        assertDoesNotThrow(() -> logger.error("error with exception", new RuntimeException("test")));
    }
}
