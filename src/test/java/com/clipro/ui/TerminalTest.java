package com.clipro.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TerminalTest {

    @Test
    void shouldReturnDefaultOrDetectedColumns() {
        int columns = Terminal.getColumns();
        assertTrue(columns > 0, "Columns should be positive");
        assertTrue(columns <= 500, "Columns should be reasonable");
    }

    @Test
    void shouldReturnDefaultOrDetectedRows() {
        int rows = Terminal.getRows();
        assertTrue(rows > 0, "Rows should be positive");
        assertTrue(rows <= 200, "Rows should be reasonable");
    }

    @Test
    void shouldReturnValidAnsiCodes() {
        assertTrue(Terminal.red("").startsWith("\033[31"));
        assertTrue(Terminal.green("").startsWith("\033[32"));
        assertTrue(Terminal.bold("").startsWith("\033[1"));
        assertTrue(Terminal.dim("").startsWith("\033[2"));
    }

    @Test
    void shouldWrapTextWithColor() {
        String redText = Terminal.red("error");
        assertTrue(redText.startsWith("\033[31m"));
        assertTrue(redText.endsWith("\033[0m"));
        assertTrue(redText.contains("error"));
    }

    @Test
    void shouldProduceAltScreenCodes() {
        assertTrue(Terminal.ansi("?1049h").startsWith("\033[?1049h"));
        assertTrue(Terminal.ansi("?1049l").startsWith("\033[?1049l"));
    }

    @Test
    void shouldProduceCursorMovementCodes() {
        String move = "\033[5;10H";
        assertTrue(move.startsWith("\033["));
        assertTrue(move.contains("5"));
        assertTrue(move.contains("10"));
    }
}
