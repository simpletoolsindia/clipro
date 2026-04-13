package com.clipro.ui.vim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VimKeyHandlerTest {

    @Test
    void shouldCreateKeyHandler() {
        VimMode vim = new VimMode();
        VimKeyHandler handler = new VimKeyHandler(vim);
        assertNotNull(handler);
    }

    @Test
    void shouldHandleNormalModeKeys() {
        VimMode vim = new VimMode();
        VimKeyHandler handler = new VimKeyHandler(vim);

        // i enters insert mode
        handler.handleKey("i");
        assertTrue(vim.getState().isInsert());
    }

    @Test
    void shouldEnterNormalModeOnEscape() {
        VimMode vim = new VimMode();
        VimKeyHandler handler = new VimKeyHandler(vim);

        vim.enterInsert();
        handler.handleModeKey("Escape");

        assertTrue(vim.getState().isNormal());
    }

    @Test
    void shouldEnterCommandMode() {
        VimMode vim = new VimMode();
        VimKeyHandler handler = new VimKeyHandler(vim);

        handler.handleModeKey(":");

        assertTrue(vim.getState().isCommand());
    }

    @Test
    void shouldEnterVisualMode() {
        VimMode vim = new VimMode();
        VimKeyHandler handler = new VimKeyHandler(vim);

        handler.handleKey("v");

        assertTrue(vim.getState().isVisual());
    }

    @Test
    void shouldEnterVisualLineMode() {
        VimMode vim = new VimMode();
        VimKeyHandler handler = new VimKeyHandler(vim);

        handler.handleKey("V");

        assertTrue(vim.getState().isVisual());
    }

    @Test
    void shouldToggleNormalInsert() {
        VimMode vim = new VimMode();
        VimKeyHandler handler = new VimKeyHandler(vim);

        assertTrue(vim.getState().isNormal());

        handler.handleModeKey("i");
        assertTrue(vim.getState().isInsert());

        handler.handleModeKey("Escape");
        assertTrue(vim.getState().isNormal());
    }

    @Test
    void shouldTrackLastAction() {
        VimMode vim = new VimMode();
        VimKeyHandler handler = new VimKeyHandler(vim);

        handler.handleKey("x"); // delete char
        assertEquals("x", vim.getLastAction());

        handler.handleKey("p"); // paste
        assertEquals("p", vim.getLastAction());
    }

    @Test
    void shouldReturnFalseForUnhandledKeys() {
        VimMode vim = new VimMode();
        VimKeyHandler handler = new VimKeyHandler(vim);

        // Keys without handlers should return false
        assertFalse(handler.handleKey("unknown"));
    }
}
