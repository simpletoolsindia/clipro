package com.clipro.ui.vim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VimStateTest {

    @Test
    void shouldHaveCorrectDisplayNames() {
        assertEquals("", VimState.NORMAL.getDisplay());
        assertEquals("INSERT", VimState.INSERT.getDisplay());
        assertEquals("VISUAL", VimState.VISUAL.getDisplay());
        assertEquals("VISUAL", VimState.VISUAL_LINE.getDisplay());
        assertEquals(":", VimState.COMMAND.getDisplay());
        assertEquals("REPLACE", VimState.REPLACE.getDisplay());
    }

    @Test
    void shouldIdentifyInsertMode() {
        assertTrue(VimState.INSERT.isInsert());
        assertFalse(VimState.NORMAL.isInsert());
    }

    @Test
    void shouldIdentifyNormalMode() {
        assertTrue(VimState.NORMAL.isNormal());
        assertFalse(VimState.INSERT.isNormal());
    }

    @Test
    void shouldIdentifyVisualModes() {
        assertTrue(VimState.VISUAL.isVisual());
        assertTrue(VimState.VISUAL_LINE.isVisual());
        assertFalse(VimState.NORMAL.isVisual());
    }

    @Test
    void shouldIdentifyCommandMode() {
        assertTrue(VimState.COMMAND.isCommand());
        assertFalse(VimState.NORMAL.isCommand());
    }

    @Test
    void shouldIdentifyReplaceMode() {
        assertTrue(VimState.REPLACE.isReplace());
        assertFalse(VimState.NORMAL.isReplace());
    }
}
