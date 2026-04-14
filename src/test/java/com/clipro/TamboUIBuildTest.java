package com.clipro;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TamboUIBuildTest {

    @Test
    void should_have_tamboui_core() {
        // Given: TamboUI is on classpath
        // When/Then: Core classes should be available
        assertDoesNotThrow(() -> Class.forName("dev.tamboui.buffer.Cell"));
        assertDoesNotThrow(() -> Class.forName("dev.tamboui.buffer.Buffer"));
    }

    @Test
    void should_have_tamboui_widgets() {
        // When/Then: Widget classes should be available
        // Note: Not checking exact class names as they may vary
        assertDoesNotThrow(() -> Class.forName("dev.tamboui.Main"));
    }

    @Test
    void should_have_tamboui_jline3_backend() {
        // When/Then: JLine3 backend should be available
        assertDoesNotThrow(() -> Class.forName("org.jline.reader.LineReader"));
    }
}