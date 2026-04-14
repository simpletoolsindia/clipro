package com.clipro;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TamboUIBuildTest {

    @Test
    void should_not_have_tamboui_dependencies() {
        // Given: We use custom Terminal.java instead of TamboUI
        // When/Then: TamboUI classes should NOT be on classpath
        assertThrows(ClassNotFoundException.class, () -> Class.forName("dev.tamboui.buffer.Cell"));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("dev.tamboui.buffer.Buffer"));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("dev.tamboui.Main"));
    }

    @Test
    void should_not_have_jline3_tamboui_backend() {
        // When/Then: TamboUI's jline3 backend should NOT be on classpath
        assertThrows(ClassNotFoundException.class, () -> Class.forName("dev.tamboui.jline3.JLine3Backend"));
    }

    @Test
    void should_use_custom_terminal() {
        // When/Then: Our custom Terminal.java handles TUI
        // This test passes if no TamboUI classes are found
        try {
            Class.forName("dev.tamboui.Main");
            fail("Tamboui should not be on classpath");
        } catch (ClassNotFoundException e) {
            // Expected - we use custom Terminal.java
        }
    }
}