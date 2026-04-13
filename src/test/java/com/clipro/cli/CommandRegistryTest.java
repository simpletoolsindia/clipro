package com.clipro.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CommandRegistry.
 */
class CommandRegistryTest {

    @Test
    void shouldCreateRegistryWithDefaultCommands() {
        CommandRegistry registry = new CommandRegistry();
        assertTrue(registry.hasCommand("help"));
        assertTrue(registry.hasCommand("clear"));
        assertTrue(registry.hasCommand("exit"));
        assertTrue(registry.hasCommand("quit"));
        assertTrue(registry.hasCommand("model"));
    }

    @Test
    void shouldExecuteHelpCommand() {
        CommandRegistry registry = new CommandRegistry();
        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext();
        
        String result = registry.execute("/help", ctx);
        assertNotNull(result);
        assertTrue(result.contains("Available Commands"));
    }

    @Test
    void shouldReturnNullForNonCommand() {
        CommandRegistry registry = new CommandRegistry();
        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext();
        
        String result = registry.execute("Hello world", ctx);
        assertNull(result); // Not a command
    }

    @Test
    void shouldReturnErrorForUnknownCommand() {
        CommandRegistry registry = new CommandRegistry();
        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext();
        
        String result = registry.execute("/unknown", ctx);
        assertNotNull(result);
        assertTrue(result.contains("Unknown command"));
    }

    @Test
    void shouldRegisterCustomCommand() {
        CommandRegistry registry = new CommandRegistry();
        registry.register(new CommandRegistry.Command(
            "test", "A test command", ctx -> "test executed"
        ));
        
        assertTrue(registry.hasCommand("test"));
        String result = registry.execute("/test", new CommandRegistry.CommandContext());
        assertEquals("test executed", result);
    }

    @Test
    void shouldUnregisterCommand() {
        CommandRegistry registry = new CommandRegistry();
        registry.register(new CommandRegistry.Command("temp", "temp", ctx -> ""));
        assertTrue(registry.hasCommand("temp"));
        
        registry.unregister("temp");
        assertFalse(registry.hasCommand("temp"));
    }

    @Test
    void shouldGetAllCommandNames() {
        CommandRegistry registry = new CommandRegistry();
        assertTrue(registry.getCommandNames().size() >= 4);
    }

    @Test
    void shouldPassArgsToCommand() {
        CommandRegistry registry = new CommandRegistry();
        registry.register(new CommandRegistry.Command(
            "echo", "Echo args", ctx -> "args: " + ctx.getArgs()
        ));
        
        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext();
        String result = registry.execute("/echo hello world", ctx);
        assertEquals("args: hello world", result);
    }
}
