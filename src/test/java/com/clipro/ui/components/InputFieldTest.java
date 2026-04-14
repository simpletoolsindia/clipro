package com.clipro.ui.components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InputFieldTest {

    @Test
    void shouldCreateEmptyInputField() {
        InputField input = new InputField();
        assertTrue(input.isEmpty());
        assertEquals(0, input.getLength());
    }

    @Test
    void shouldInsertCharacters() {
        InputField input = new InputField();
        input.insert('h');
        input.insert('i');

        assertEquals("hi", input.getText());
        assertEquals(2, input.getLength());
        assertFalse(input.isEmpty());
    }

    @Test
    void shouldInsertString() {
        InputField input = new InputField();
        input.insert("hello");

        assertEquals("hello", input.getText());
    }

    @Test
    void shouldBackspace() {
        InputField input = new InputField();
        input.insert("hello");
        input.backspace();

        assertEquals("hell", input.getText());
        assertEquals(4, input.getLength());
    }

    @Test
    void shouldDeleteCharacter() {
        InputField input = new InputField();
        input.insert("hello");
        input.moveCursorToStart(); // cursor at 0
        input.moveCursorRight();   // cursor at 1
        input.moveCursorRight();   // cursor at 2
        input.delete();            // deletes 'l' at position 2

        assertEquals("helo", input.getText());
    }

    @Test
    void shouldMoveCursor() {
        InputField input = new InputField();
        input.insert("hello");

        assertEquals(5, input.getCursorPosition());

        input.moveCursorLeft();
        assertEquals(4, input.getCursorPosition());

        input.moveCursorLeft();
        assertEquals(3, input.getCursorPosition());

        input.moveCursorRight();
        assertEquals(4, input.getCursorPosition());
    }

    @Test
    void shouldMoveCursorToStartAndEnd() {
        InputField input = new InputField();
        input.insert("hello");

        input.moveCursorToStart();
        assertEquals(0, input.getCursorPosition());

        input.moveCursorToEnd();
        assertEquals(5, input.getCursorPosition());
    }

    @Test
    void shouldSubmit() {
        InputField input = new InputField();
        input.insert("test command");

        String result = input.submit();

        assertEquals("test command", result);
        assertTrue(input.isEmpty());
        assertEquals(1, input.getHistory().size());
    }

    @Test
    void shouldClear() {
        InputField input = new InputField();
        input.insert("hello");
        input.clear();

        assertTrue(input.isEmpty());
    }

    @Test
    void shouldNotAddEmptyToHistory() {
        InputField input = new InputField();
        input.submit(); // Empty submission

        assertEquals(0, input.getHistory().size());
    }

    @Test
    void shouldRender() {
        InputField input = new InputField();
        input.insert("hi");

        String output = input.render();
        assertTrue(output.contains("hi"));
        assertTrue(output.contains("▶"));
        assertTrue(output.contains("\033[7m")); // Cursor
    }

    @Test
    void shouldMaskPassword() {
        InputField input = new InputField();
        input.setMasked(true);
        input.insert("secret");

        String output = input.render();
        assertTrue(output.contains("******"));
    }

    @Test
    void shouldUseCustomPrompt() {
        InputField input = new InputField("$ ");
        input.insert("test");

        String output = input.render();
        assertTrue(output.contains("$ "));
    }

    @Test
    void shouldNavigateHistory() {
        InputField input = new InputField();
        input.insert("cmd1");
        input.submit();
        input.insert("cmd2");
        input.submit();

        input.historyUp();
        assertEquals("cmd2", input.getText());

        input.historyUp();
        assertEquals("cmd1", input.getText());

        input.historyDown();
        assertEquals("cmd2", input.getText());
    }
}
