package com.clipro.ui.components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageRoleTest {

    @Test
    void shouldHaveCorrectValues() {
        assertEquals("user", MessageRole.USER.getValue());
        assertEquals("assistant", MessageRole.ASSISTANT.getValue());
        assertEquals("system", MessageRole.SYSTEM.getValue());
        assertEquals("tool", MessageRole.TOOL.getValue());
    }
}
