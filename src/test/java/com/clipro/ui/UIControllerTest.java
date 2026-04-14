package com.clipro.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UIControllerTest {

    @Test
    void shouldCreateController() {
        UIController controller = new UIController();
        assertNotNull(controller);
        assertNotNull(controller.getLayout());
        assertNotNull(controller.getModel());
    }

    @Test
    void shouldCreateWithCustomModel() {
        UIController controller = new UIController("http://localhost:11434/v1", "llama3:70b");
        assertEquals("llama3:70b", controller.getModel());
    }

    @Test
    void shouldSetModel() {
        UIController controller = new UIController();
        controller.setModel("qwen3-coder:32b");
        assertEquals("qwen3-coder:32b", controller.getModel());
    }

    @Test
    void shouldRenderLayout() {
        UIController controller = new UIController();
        String output = controller.render();
        assertNotNull(output);
        assertTrue(output.length() > 0);
    }

    @Test
    void shouldRenderMinimal() {
        UIController controller = new UIController();
        String output = controller.renderMinimal();
        assertNotNull(output);
    }

    @Test
    void shouldClearMessages() {
        UIController controller = new UIController();
        controller.getLayout().addUserMessage("test");
        controller.clear();
        assertTrue(controller.getLayout().getMessages().isEmpty());
    }

    @Test
    void shouldGetTokenInfo() {
        UIController controller = new UIController();
        String info = controller.getTokenInfo();
        assertNotNull(info);
        assertTrue(info.contains("Tokens"));
    }
}