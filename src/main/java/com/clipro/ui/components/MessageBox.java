package com.clipro.ui.components;

import com.clipro.ui.Terminal;

/**
 * Renders a message box in the terminal - Pixel-perfect OpenClaude style.
 * Reference: ~/openclaude/src/components/Message.tsx
 */
public class MessageBox {

    private static final ThinkingBlock thinkingBlock = new ThinkingBlock();

    public static String render(Message message) {
        return switch (message.getRole()) {
            case USER -> renderUser(message.getContent(), message.isStreaming());
            case ASSISTANT -> renderAssistant(message.getContent(), message.isStreaming());
            case SYSTEM -> renderSystem(message.getContent());
            case TOOL -> renderTool(message.getContent());
        };
    }

    public static String renderUser(String content, boolean streaming) {
        StringBuilder sb = new StringBuilder();
        sb.append(Terminal.boxTop(Terminal.getColumns()));
        sb.append("\n");
        sb.append(Terminal.boxRow(Terminal.user("USER") + " " + Terminal.dim("[message]"), Terminal.getColumns()));
        sb.append("\n");
        sb.append(Terminal.boxRow("", Terminal.getColumns()));
        sb.append("\n");

        // Content with word wrap
        String wrapped = wordWrap(content, Terminal.getColumns() - 4);
        for (String line : wrapped.split("\n")) {
            sb.append(Terminal.BORDER_V + " " + line + Terminal.padRight("", Terminal.getColumns() - line.length() - 3) + Terminal.BORDER_V + "\n");
        }

        sb.append(Terminal.boxRow("", Terminal.getColumns()));
        sb.append("\n");
        sb.append(Terminal.boxBottom(Terminal.getColumns()));
        if (streaming) sb.append(Terminal.brightCyan(" ▌")); // Streaming cursor
        return sb.toString();
    }

    public static String renderAssistant(String content, boolean streaming) {
        StringBuilder sb = new StringBuilder();
        sb.append(Terminal.boxTop(Terminal.getColumns()));
        sb.append("\n");
        sb.append(Terminal.boxRow(Terminal.assistant("CLAUDE") + " " + Terminal.dim("[assistant]"), Terminal.getColumns()));
        sb.append("\n");
        sb.append(Terminal.boxRow("", Terminal.getColumns()));
        sb.append("\n");

        // Process content for thinking blocks with rainbow rendering
        String processed = thinkingBlock.render(content, streaming);
        String wrapped = wordWrap(processed, Terminal.getColumns() - 4);
        for (String line : wrapped.split("\n")) {
            sb.append(Terminal.BORDER_V + " " + line + Terminal.padRight("", Terminal.getColumns() - line.length() - 3) + Terminal.BORDER_V + "\n");
        }

        sb.append(Terminal.boxRow("", Terminal.getColumns()));
        sb.append("\n");
        sb.append(Terminal.boxBottom(Terminal.getColumns()));
        if (streaming) sb.append(Terminal.brightCyan(" ▌"));
        return sb.toString();
    }

    /**
     * Render assistant message with thinking block processing.
     * Use this for messages that may contain thinking tags.
     */
    public static String renderAssistantWithThinking(String content, boolean streaming) {
        return renderAssistant(content, streaming);
    }

    /**
     * Toggle thinking block expansion.
     */
    public static void toggleThinkingExpanded() {
        thinkingBlock.toggleExpanded();
    }

    /**
     * Enable/disable shimmer animation for thinking blocks.
     */
    public static void setThinkingShimmerEnabled(boolean enabled) {
        thinkingBlock.setShimmerEnabled(enabled);
    }

    /**
     * Advance thinking block animation frame.
     */
    public static void tickThinkingAnimation() {
        thinkingBlock.tick();
    }

    public static String renderSystem(String content) {
        StringBuilder sb = new StringBuilder();
        sb.append(Terminal.dim("[SYSTEM] " + content));
        return sb.toString();
    }

    public static String renderTool(String content) {
        StringBuilder sb = new StringBuilder();
        String wrapped = wordWrap(content, Terminal.getColumns() - 6);
        for (String line : wrapped.split("\n")) {
            sb.append(Terminal.dim("  │ " + line + "\n"));
        }
        return sb.toString();
    }

    public static String renderSimple(String content, MessageRole role) {
        String prefix = switch (role) {
            case USER -> Terminal.user("▶ ");
            case ASSISTANT -> Terminal.assistant("◀ ");
            case TOOL -> Terminal.dim("└─ ");
            default -> "";
        };
        return prefix + content;
    }

    private static String wordWrap(String text, int maxWidth) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() + word.length() + 1 > maxWidth) {
                if (current.length() > 0) {
                    result.append(current.toString().trim());
                    result.append("\n");
                    current = new StringBuilder();
                }
            }
            current.append(word).append(" ");
        }
        if (current.length() > 0) {
            result.append(current.toString().trim());
        }
        return result.toString();
    }
}