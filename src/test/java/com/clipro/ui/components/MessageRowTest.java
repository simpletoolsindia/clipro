package com.clipro.ui.components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageRowTest {

    @Test
    void shouldRenderMessageWithTime() {
        Message msg = new Message(MessageRole.USER, "test");
        String output = MessageRow.render(msg);

        assertTrue(output.contains("[USER]"));
        assertTrue(output.contains("test"));
        // Should contain timestamp pattern (HH:mm:ss)
        assertTrue(output.matches(".*\\d{2}:\\d{2}:\\d{2}.*"));
    }

    @Test
    void shouldRenderWithIndex() {
        Message msg = new Message(MessageRole.ASSISTANT, "test");
        String output = MessageRow.renderWithIndex(5, msg);

        assertTrue(output.contains("[5]"));
        assertTrue(output.contains("[ASSISTANT]"));
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
