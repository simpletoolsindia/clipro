package com.clipro.ui.vim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VimModeTest {

    @Test
    void shouldStartInNormalMode() {
        VimMode vim = new VimMode();
        assertEquals(VimState.NORMAL, vim.getState());
    }

    @Test
    void shouldEnterInsertMode() {
        VimMode vim = new VimMode();
        vim.enterInsert();
        assertEquals(VimState.INSERT, vim.getState());
        assertTrue(vim.getState().isInsert());
    }

    @Test
    void shouldEnterNormalMode() {
        VimMode vim = new VimMode();
        vim.enterInsert();
        vim.enterNormal();
        assertEquals(VimState.NORMAL, vim.getState());
    }

    @Test
    void shouldToggleMode() {
        VimMode vim = new VimMode();

        assertTrue(vim.getState().isNormal());
        vim.toggleMode();
        assertTrue(vim.getState().isInsert());

        vim.toggleMode();
        assertTrue(vim.getState().isNormal());
    }

    @Test
    void shouldTrackLastAction() {
        VimMode vim = new VimMode();
        vim.setLastAction("dd");

        assertEquals("dd", vim.getLastAction());
        assertTrue(vim.isDotRepeat());
    }

    @Test
    void shouldSaveAndRetrieveRegisters() {
        VimMode vim = new VimMode();
        vim.saveToRegister("a", "clipboard content");

        assertEquals("clipboard content", vim.getFromRegister("a"));
        assertEquals("", vim.getFromRegister("nonexistent"));
    }

    @Test
    void shouldRenderMode() {
        VimMode vim = new VimMode();
        assertEquals("", vim.renderIndicator());

        vim.enterInsert();
        assertEquals(" INSERT ", vim.renderIndicator());
    }

    @Test
    void shouldEnterVisualModes() {
        VimMode vim = new VimMode();

        vim.enterVisual();
        assertTrue(vim.getState().isVisual());

        vim.enterVisualLine();
        assertTrue(vim.getState().isVisual());
    }

    @Test
    void shouldEnterCommandMode() {
        VimMode vim = new VimMode();
        vim.enterCommand();
        assertTrue(vim.getState().isCommand());
    }
}
