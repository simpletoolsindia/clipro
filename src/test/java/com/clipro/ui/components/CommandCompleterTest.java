package com.clipro.ui.components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandCompleterTest {

    @Test
    void shouldCreateCompleter() {
        CommandCompleter completer = new CommandCompleter();
        assertNotNull(completer);
    }

    @Test
    void shouldHaveDefaultCommands() {
        CommandCompleter completer = new CommandCompleter();
        assertFalse(completer.getAllCommandNames().isEmpty());
    }

    @Test
    void shouldCompletePartialInput() {
        CommandCompleter completer = new CommandCompleter();

        var completions = completer.complete("/m");
        assertFalse(completions.isEmpty());
        assertTrue(completions.stream().anyMatch(c -> c.startsWith("/m")));
    }

    @Test
    void shouldReturnExactMatch() {
        CommandCompleter completer = new CommandCompleter();

        String match = completer.getCompletion("/help");
        assertEquals("/help", match);
    }

    @Test
    void shouldReturnNullForNoMatch() {
        CommandCompleter completer = new CommandCompleter();

        String match = completer.getCompletion("/nonexistent");
        assertNull(match);
    }

    @Test
    void shouldReturnAllCommandsForEmptyInput() {
        CommandCompleter completer = new CommandCompleter();

        var completions = completer.complete("");
        assertEquals(completer.getAllCommandNames().size(), completions.size());
    }

    @Test
    void shouldAddCommands() {
        CommandCompleter completer = new CommandCompleter();
        int initialSize = completer.getAllCommandNames().size();

        completer.addCommand("/custom", "Custom command");

        assertEquals(initialSize + 1, completer.getAllCommandNames().size());
        assertNotNull(completer.getCompletion("/custom"));
    }

    @Test
    void shouldRemoveCommands() {
        CommandCompleter completer = new CommandCompleter();
        completer.addCommand("/temp", "Temp");

        completer.removeCommand("/temp");

        assertNull(completer.getCompletion("/temp"));
    }

    @Test
    void shouldIdentifyCommands() {
        CommandCompleter completer = new CommandCompleter();

        assertTrue(completer.isCommand("/help"));
        assertTrue(completer.isCommand("/model"));
        assertFalse(completer.isCommand("regular text"));
    }

    @Test
    void shouldIdentifyPartialCommands() {
        CommandCompleter completer = new CommandCompleter();

        assertTrue(completer.isPartialCommand("/m"));
        assertTrue(completer.isPartialCommand("/model"));
        assertFalse(completer.isPartialCommand("/help with args"));
        assertFalse(completer.isPartialCommand("regular text"));
    }

    @Test
    void shouldGetDescription() {
        CommandCompleter completer = new CommandCompleter();

        String desc = completer.getDescription("/help");
        assertNotNull(desc);
        assertFalse(desc.isEmpty());
    }

    @Test
    void shouldReturnNullDescriptionForUnknown() {
        CommandCompleter completer = new CommandCompleter();

        String desc = completer.getDescription("/unknown");
        assertNull(desc);
    }

    @Test
    void shouldCompleteCaseInsensitive() {
        CommandCompleter completer = new CommandCompleter();

        var completions = completer.complete("/MODEL");
        assertFalse(completions.isEmpty());
    }

    @Test
    void shouldNotMatchPartialInMiddle() {
        CommandCompleter completer = new CommandCompleter();

        var completions = completer.complete("/el");
        // Should not match since it doesn't start with /el
        assertTrue(completions.isEmpty());
    }
}
