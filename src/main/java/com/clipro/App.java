package com.clipro;

import com.clipro.logging.Logger;
import com.clipro.ui.Terminal;
import com.clipro.ui.components.*;
import com.clipro.ui.vim.VimMode;
import com.clipro.session.CommandRegistry;
import com.clipro.session.HistoryManager;

public class App {
    private static final Logger LOG = new Logger("App");

    public static void main(String[] args) {
        LOG.info("CLIPRO - Java AI Coding CLI");
        LOG.info("Version: 0.1.0");
        LOG.info("Terminal: " + Terminal.getColumns() + "x" + Terminal.getRows());

        // Demo all components
        demoComponents();
    }

    private static void demoComponents() {
        System.out.println("\n" + Terminal.bold("=== CLIPRO Component Demo ===\n"));

        // 1. Messages
        System.out.println(Terminal.bold("1. Message Components:"));
        MessageList messages = new MessageList();
        messages.addUser("Hello, can you help me?");
        messages.addAssistant("Of course! I'm here to help.");
        System.out.println(messages.render());

        // 2. Markdown
        System.out.println("\n" + Terminal.bold("2. Markdown Rendering:"));
        String md = "**Bold** and *italic* with `code`";
        System.out.println(MarkdownRenderer.render(md));

        // 3. Streaming
        System.out.println("\n" + Terminal.bold("3. Streaming Message:"));
        StreamingMessage streaming = new StreamingMessage(MessageRole.ASSISTANT);
        streaming.append("Typing response...");
        System.out.println(streaming.render());

        // 4. Vim Mode
        System.out.println("\n" + Terminal.bold("4. Vim Mode:"));
        VimMode vim = new VimMode();
        System.out.println("Normal: " + vim.renderIndicator());
        vim.enterInsert();
        System.out.println("Insert: " + vim.renderIndicator());
        vim.enterNormal();
        System.out.println("Normal: " + vim.renderIndicator());

        // 5. Commands
        System.out.println("\n" + Terminal.bold("5. Command Registry:"));
        CommandRegistry registry = new CommandRegistry();
        CommandRegistry.CommandContext ctx = new CommandRegistry.CommandContext(java.util.List.of());
        registry.execute("models", ctx);
        System.out.println(ctx.getOutput());

        // 6. Layout
        System.out.println("\n" + Terminal.bold("6. Full Layout:"));
        FullscreenLayout layout = new FullscreenLayout("qwen3-coder:32b");
        layout.setConnected(true);
        layout.addUserMessage("Show me the layout");
        layout.addAssistantMessage("Here's the full layout!");

        // Header
        System.out.println(layout.getHeader().render());

        System.out.println("\n" + Terminal.green("=== All components working! ==="));
    }
}
