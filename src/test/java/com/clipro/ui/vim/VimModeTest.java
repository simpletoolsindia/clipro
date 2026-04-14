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

    @Test
    void shouldRecordMacro() {
        VimMode vim = new VimMode();

        vim.startRecording('a');
        assertTrue(vim.isRecording());
        assertEquals(Character.valueOf('a'), vim.getRecordingRegister());

        vim.recordKeystroke("hello");
        vim.recordKeystroke("world");
        vim.stopRecording();

        assertFalse(vim.isRecording());
        assertEquals("helloworld", vim.getMacro('a'));
    }

    @Test
    void shouldPlaybackMacro() {
        VimMode vim = new VimMode();

        vim.startRecording('b');
        vim.recordKeystroke("test");
        vim.stopRecording();

        String macro = vim.playbackMacro('b');
        assertEquals("test", macro);
    }

    @Test
    void shouldCheckMacroExists() {
        VimMode vim = new VimMode();

        assertFalse(vim.hasMacro('x'));

        vim.startRecording('x');
        vim.recordKeystroke("macro");
        vim.stopRecording();

        assertTrue(vim.hasMacro('x'));
    }

    @Test
    void shouldHandleMultipleMacros() {
        VimMode vim = new VimMode();

        vim.startRecording('a');
        vim.recordKeystroke("foo");
        vim.stopRecording();

        vim.startRecording('b');
        vim.recordKeystroke("bar");
        vim.stopRecording();

        assertEquals("foo", vim.getMacro('a'));
        assertEquals("bar", vim.getMacro('b'));
    }
}
