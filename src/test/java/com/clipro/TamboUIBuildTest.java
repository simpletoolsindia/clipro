package com.clipro;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TamboUIBuildTest {

    @Test
    void should_have_tamboui_dependencies() {
        // Given: We now use TamboUI for TUI
        // When/Then: TamboUI classes should be on classpath
        assertDoesNotThrow(() -> Class.forName("dev.tamboui.tui.TuiRunner"));
        assertDoesNotThrow(() -> Class.forName("dev.tamboui.terminal.Frame"));
        assertDoesNotThrow(() -> Class.forName("dev.tamboui.tui.event.KeyEvent"));
    }

    @Test
    void should_have_tamboui_widgets() {
        // When/Then: TamboUI widgets should be on classpath
        assertDoesNotThrow(() -> Class.forName("dev.tamboui.widgets.paragraph.Paragraph"));
        assertDoesNotThrow(() -> Class.forName("dev.tamboui.text.Text"));
        assertDoesNotThrow(() -> Class.forName("dev.tamboui.layout.Rect"));
    }

    @Test
    void should_use_tamboui_adapter() {
        // When/Then: TamboUI adapter should exist and be loadable
        assertDoesNotThrow(() -> Class.forName("com.clipro.ui.tamboui.TamboUIAdapter"));
        assertDoesNotThrow(() -> Class.forName("com.clipro.ui.tamboui.TuiAdapter"));
        assertDoesNotThrow(() -> Class.forName("com.clipro.ui.tamboui.OpenClaudeTheme"));
    }
}
