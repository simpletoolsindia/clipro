package com.clipro.session;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {

    @Test
    void shouldCreateRegistry() {
        CommandRegistry registry = new CommandRegistry();
        assertNotNull(registry);
    }

    @Test
    void shouldHaveDefaultCommands() {
        CommandRegistry registry = new CommandRegistry();
        assertTrue(registry.exists("help"));
        assertTrue(registry.exists("clear"));
        assertTrue(registry.exists("exit"));
        assertTrue(registry.exists("quit"));
        assertTrue(registry.exists("model"));
        assertTrue(registry.exists("models"));
    }

    @Test
    void shouldRegisterCommand() {
        CommandRegistry registry = new CommandRegistry();
        registry.register("test", new CommandRegistry.Command("test", "Test command", ctx -> {
            ctx.appendOutput("Test executed");
        }));

        assertTrue(registry.exists("test"));
    }

    @Test
    void shouldUnregisterCommand() {
        CommandRegistry registry = new CommandRegistry();
        registry.register("temp", new CommandRegistry.Command("temp", "Temp", ctx -> {}));
        assertTrue(registry.exists("temp"));

        registry.unregister("temp");
        assertFalse(registry.exists("temp"));
    }

    @Test
    void shouldExecuteCommand() {
        CommandRegistry registry = new CommandRegistry();
        registry.register("greet", new CommandRegistry.Command("greet", "Greet", ctx -> {
            ctx.appendOutput("Hello, " + (ctx.getArgs().isEmpty() ? "World" : ctx.getArgs().get(0)));
        }));

        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext(java.util.List.of("User"));
        boolean executed = registry.execute("greet", ctx);

        assertTrue(executed);
        assertTrue(ctx.getOutput().contains("Hello"));
        assertTrue(ctx.getOutput().contains("User"));
    }

    @Test
    void shouldReturnFalseForNonExistentCommand() {
        CommandRegistry registry = new CommandRegistry();
        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext(java.util.List.of());

        boolean executed = registry.execute("nonexistent", ctx);
        assertFalse(executed);
    }

    @Test
    void shouldExecuteHelp() {
        CommandRegistry registry = new CommandRegistry();
        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext(java.util.List.of());

        registry.execute("help", ctx);

        assertTrue(ctx.getOutput().contains("Available commands"));
    }

    @Test
    void shouldHandleExitCommand() {
        CommandRegistry registry = new CommandRegistry();
        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext(java.util.List.of());

        assertFalse(ctx.isExit());
        registry.execute("exit", ctx);
        assertTrue(ctx.isExit());
    }

    @Test
    void shouldHandleClearCommand() {
        CommandRegistry registry = new CommandRegistry();
        boolean[] cleared = {false};
        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext(java.util.List.of());
        ctx.setClearMessages(() -> cleared[0] = true);

        registry.execute("clear", ctx);

        assertTrue(cleared[0]);
        assertTrue(ctx.getOutput().contains("cleared"));
    }

    @Test
    void shouldHandleModelCommand() {
        CommandRegistry registry = new CommandRegistry();
        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext(java.util.List.of("qwen3-coder:32b"));
        ctx.setCurrentModel("llama3:70b");

        registry.execute("model", ctx);

        assertEquals("qwen3-coder:32b", ctx.getSelectedModel());
    }

    @Test
    void shouldListModels() {
        CommandRegistry registry = new CommandRegistry();
        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext(java.util.List.of());

        registry.execute("models", ctx);

        assertTrue(ctx.getOutput().contains("qwen3-coder:32b"));
        assertTrue(ctx.getOutput().contains("Available models"));
    }

    @Test
    void shouldGetAllCommands() {
        CommandRegistry registry = new CommandRegistry();
        var all = registry.getAll();

        assertFalse(all.isEmpty());
        assertTrue(all.containsKey("help"));
        assertTrue(all.containsKey("model"));
    }
}
