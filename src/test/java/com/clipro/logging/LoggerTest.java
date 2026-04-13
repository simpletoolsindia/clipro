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
        Logger.setLevel(Logger.LogLevel.DEBUG);
        assertEquals(Logger.LogLevel.DEBUG, Logger.getLevel());

        Logger.setLevel(Logger.LogLevel.ERROR);
        assertEquals(Logger.LogLevel.ERROR, Logger.getLevel());
    }

    @Test
    void shouldNotThrowOnAnyLogLevel() {
        Logger logger = new Logger("test");
        Logger.setLevel(Logger.LogLevel.DEBUG);

        assertDoesNotThrow(() -> logger.debug("debug message"));
        assertDoesNotThrow(() -> logger.info("info message"));
        assertDoesNotThrow(() -> logger.warn("warn message"));
        assertDoesNotThrow(() -> logger.error("error message"));
        assertDoesNotThrow(() -> logger.error("error with exception", new RuntimeException("test")));
    }
}
