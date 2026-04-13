package com.clipro.ui;

import com.clipro.ui.TerminalResizeHandler.ResizeListener;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TerminalResizeHandlerTest {

    @Test
    void shouldCreateResizeHandler() {
        TerminalResizeHandler handler = new TerminalResizeHandler();
        assertNotNull(handler);
        assertFalse(handler.isRunning());
    }

    @Test
    void shouldStartAndStop() {
        TerminalResizeHandler handler = new TerminalResizeHandler();

        handler.start((cols, rows) -> {});
        assertTrue(handler.isRunning());

        handler.stop();
        assertFalse(handler.isRunning());
    }

    @Test
    void shouldStopSafelyWhenNotStarted() {
        TerminalResizeHandler handler = new TerminalResizeHandler();
        assertDoesNotThrow(handler::stop);
    }
}
