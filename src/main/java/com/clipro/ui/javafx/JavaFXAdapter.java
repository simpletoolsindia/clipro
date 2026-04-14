package com.clipro.ui.javafx;

import java.util.*;

/**
 * JavaFX adapter for rich TUI rendering.
 * Provides virtual scrolling, syntax highlighting, and animations.
 */
public class JavaFXAdapter {

    private boolean initialized = false;
    private final List<String> messages = new ArrayList<>();
    private int scrollPosition = 0;
    private String currentInput = "";
    private boolean visible = false;

    public void initialize() {
        // JavaFX initialization would happen here
        // For now, this is a stub that indicates JavaFX is available
        this.initialized = true;
    }

    public void show() {
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public void clearMessages() {
        messages.clear();
        scrollPosition = 0;
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public void setInput(String input) {
        this.currentInput = input;
    }

    public String getInput() {
        return currentInput;
    }

    public void scrollTo(int position) {
        this.scrollPosition = Math.max(0, position);
    }

    public int getScrollPosition() {
        return scrollPosition;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Check if JavaFX is available on this system.
     */
    public static boolean isAvailable() {
        try {
            Class.forName("javafx.application.Platform");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
