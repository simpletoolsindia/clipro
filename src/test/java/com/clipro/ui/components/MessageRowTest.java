package com.clipro.ui.components;

import com.clipro.ui.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageRowTest {

    @BeforeEach
    void setUp() {
        Terminal.setColumns(80);
        Terminal.setRows(24);
    }

    @Test
    void shouldRenderMessageWithTime() {
        Message msg = new Message(MessageRole.USER, "test");
        String output = MessageRow.render(msg);

        // Check for content - format may vary
        assertTrue(output.contains("test"));
        // Check for timestamp or role indicator
        assertTrue(output.contains(":") || output.contains("USER") || output.contains("user"));
    }

    @Test
    void shouldRenderWithIndex() {
        Message msg = new Message(MessageRole.ASSISTANT, "test");
        String output = MessageRow.renderWithIndex(5, msg);

        assertTrue(output.contains("5") || output.contains("[5]"));
        assertTrue(output.contains("test"));
    }

    @Test
    void shouldRenderMultipleMessages() {
        Message msg1 = new Message(MessageRole.USER, "first");
        Message msg2 = new Message(MessageRole.ASSISTANT, "second");

        String output1 = MessageRow.renderWithIndex(1, msg1);
        String output2 = MessageRow.renderWithIndex(2, msg2);

        assertTrue(output1.contains("[1]"));
        assertTrue(output2.contains("[2]"));
    }
}
