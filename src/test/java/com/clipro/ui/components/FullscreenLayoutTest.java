package com.clipro.ui.components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FullscreenLayoutTest {

    @Test
    void shouldCreateLayout() {
        FullscreenLayout layout = new FullscreenLayout();
        assertNotNull(layout);
        assertNotNull(layout.getHeader());
        assertNotNull(layout.getMessages());
        assertNotNull(layout.getStatus());
        assertNotNull(layout.getInput());
    }

    @Test
    void shouldCreateWithModel() {
        FullscreenLayout layout = new FullscreenLayout("qwen3-coder:32b");
        assertEquals("qwen3-coder:32b", layout.getHeader().getModel());
    }

    @Test
    void shouldSetModel() {
        FullscreenLayout layout = new FullscreenLayout();
        layout.setModel("llama3:70b");
        assertEquals("llama3:70b", layout.getHeader().getModel());
    }

    @Test
    void shouldSetConnected() {
        FullscreenLayout layout = new FullscreenLayout();
        layout.setConnected(true);
        assertTrue(layout.getHeader().isConnected());

        layout.setConnected(false);
        assertFalse(layout.getHeader().isConnected());
    }

    @Test
    void shouldAddMessages() {
        FullscreenLayout layout = new FullscreenLayout();
        layout.addUserMessage("Hello");
        layout.addAssistantMessage("Hi there!");

        assertEquals(2, layout.getMessages().size());
    }

    @Test
    void shouldClearMessages() {
        FullscreenLayout layout = new FullscreenLayout();
        layout.addUserMessage("test");
        layout.clearMessages();

        assertTrue(layout.getMessages().isEmpty());
    }

    @Test
    void shouldClearAll() {
        FullscreenLayout layout = new FullscreenLayout();
        layout.addUserMessage("test");
        layout.getInput().insert("input");

        layout.clearAll();

        assertTrue(layout.getMessages().isEmpty());
        assertTrue(layout.getInput().isEmpty());
    }

    @Test
    void shouldRender() {
        FullscreenLayout layout = new FullscreenLayout();
        String output = layout.render();

        assertNotNull(output);
        assertTrue(output.contains("CLIPRO"));
    }

    @Test
    void shouldRenderMinimal() {
        FullscreenLayout layout = new FullscreenLayout();
        layout.getInput().insert("test");

        String output = layout.renderMinimal();
        assertNotNull(output);
    }
}
