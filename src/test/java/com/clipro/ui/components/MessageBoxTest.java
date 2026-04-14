package com.clipro.ui.components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageBoxTest {

    @Test
    void shouldRenderUserMessage() {
        Message msg = new Message(MessageRole.USER, "test content");
        String output = MessageBox.render(msg);

        assertTrue(output.contains("USER"), "Should contain USER");
        assertTrue(output.contains("test content"));
    }

    @Test
    void shouldRenderAssistantMessage() {
        Message msg = new Message(MessageRole.ASSISTANT, "assistant response");
        String output = MessageBox.render(msg);

        assertTrue(output.contains("CLAUDE"), "Should contain CLAUDE");
        assertTrue(output.contains("assistant response"));
    }

    @Test
    void shouldRenderStreamingMessage() {
        Message msg = new Message(MessageRole.ASSISTANT, "streaming", true);
        String output = MessageBox.render(msg);

        assertTrue(output.contains("streaming"));
    }

    @Test
    void shouldRenderUserContent() {
        String output = MessageBox.renderUser("user input", false);
        assertTrue(output.contains("USER"), "Should contain USER");
        assertTrue(output.contains("user input"));
    }

    @Test
    void shouldRenderAssistantContent() {
        String output = MessageBox.renderAssistant("assistant output", false);
        assertTrue(output.contains("CLAUDE"), "Should contain CLAUDE");
        assertTrue(output.contains("assistant output"));
    }

    @Test
    void shouldContainAnsiCodes() {
        String output = MessageBox.renderUser("test", false);
        assertTrue(output.contains("\u001B["));
    }
}