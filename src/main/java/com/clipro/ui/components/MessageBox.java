package com.clipro.ui.components;

import com.clipro.ui.Terminal;
import com.clipro.llm.ThinkingParser;

import static com.clipro.ui.Terminal.RESET;

/**
 * Renders a message box in the terminal - Pixel-perfect OpenClaude style.
 * Reference: ~/openclaude/src/components/Message.tsx
 *
 * Features:
 * - Thinking blocks with rainbow colors and collapsible UI
 * - Tool result display with formatting
 * - User/Assistant/System/Tool message rendering
 * - Block count indicators for thinking blocks
 */
public class MessageBox {

    private static final ThinkingBlock thinkingBlock = new ThinkingBlock();
    private static final ThinkingParser thinkingParser = new ThinkingParser();
    private static final MarkdownRenderer markdownRenderer = new MarkdownRenderer();

    public static String render(Message message) {
        return switch (message.getRole()) {
            case USER -> renderUser(message.getContent(), message.isStreaming());
            case ASSISTANT -> renderAssistant(message.getContent(), message.isStreaming());
            case SYSTEM -> renderSystem(message.getContent());
            case TOOL -> renderTool(message.getContent());
        };
    }

    /**
     * Render user message with dark grey background.
     * Reference: openclaude/src/components/Message.tsx (UserTextMessage)
     */
    public static String renderUser(String content, boolean streaming) {
        StringBuilder sb = new StringBuilder();
        sb.append(Terminal.boxTop(Terminal.getColumns()));
        sb.append("\n");
        sb.append(Terminal.boxRow(Terminal.user("USER") + " " + Terminal.dim("[message]"), Terminal.getColumns()));
        sb.append("\n");
        sb.append(Terminal.boxRow("", Terminal.getColumns()));
        sb.append("\n");

        // Content with word wrap (safe for plain text)
        String wrapped = wordWrap(content, Terminal.getColumns() - 4);
        for (String line : wrapped.split("\n")) {
            sb.append(Terminal.BORDER_V + " " + line + padRight("", Terminal.getColumns() - line.length() - 3) + Terminal.BORDER_V + "\n");
        }

        sb.append(Terminal.boxRow("", Terminal.getColumns()));
        sb.append("\n");
        sb.append(Terminal.boxBottom(Terminal.getColumns()));
        if (streaming) sb.append(Terminal.brightCyan(" ▌")); // Streaming cursor
        return sb.toString();
    }

    /**
     * Render assistant message with thinking block integration.
     * - Parses thinking blocks from content
     * - Renders thinking blocks with rainbow colors (collapsible)
     * - Renders remaining content as markdown
     * - Shows block count indicator
     * Reference: openclaude/src/components/Message.tsx
     */
    public static String renderAssistant(String content, boolean streaming) {
        StringBuilder sb = new StringBuilder();

        // Count thinking blocks for indicator
        int blockCount = thinkingParser.countThinkingBlocks(content);

        // Build header with block count indicator
        sb.append(Terminal.boxTop(Terminal.getColumns()));
        sb.append("\n");

        String headerLabel = Terminal.assistant("CLAUDE");
        if (blockCount > 0) {
            String blockIndicator = Terminal.dim("[💭 " + blockCount + " thinking block" + (blockCount > 1 ? "s" : "") + "]");
            sb.append(Terminal.boxRow(headerLabel + " " + blockIndicator, Terminal.getColumns()));
        } else {
            sb.append(Terminal.boxRow(headerLabel + " " + Terminal.dim("[assistant]"), Terminal.getColumns()));
        }
        sb.append("\n");
        sb.append(Terminal.boxRow("", Terminal.getColumns()));
        sb.append("\n");

        // Render content: thinking blocks + markdown
        String rendered = renderContentWithThinking(content, streaming);
        String[] lines = rendered.split("\n");
        for (String line : lines) {
            // Don't word-wrap already-formatted content (ANSI codes)
            sb.append(Terminal.BORDER_V + " " + line + padRight("", Terminal.getColumns() - line.length() - 3) + Terminal.BORDER_V + "\n");
        }

        sb.append(Terminal.boxRow("", Terminal.getColumns()));
        sb.append("\n");
        sb.append(Terminal.boxBottom(Terminal.getColumns()));
        if (streaming) sb.append(Terminal.brightCyan(" ▌"));
        return sb.toString();
    }

    /**
     * Render content with thinking blocks integrated.
     * - Extracts thinking blocks and renders with rainbow colors
     * - Renders text between thinking blocks as markdown
     * - Does NOT word-wrap (ANSI codes must stay intact)
     */
    private static String renderContentWithThinking(String content, boolean streaming) {
        if (content == null || content.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        // Parse thinking blocks
        var blocks = thinkingParser.parseBlocks(content);

        if (blocks.isEmpty()) {
            // No thinking blocks — render as markdown
            String markdown = markdownRenderer.render(content);
            sb.append(markdown);
            return sb.toString();
        }

        // Render: text before first block → thinking block → text → ... → last block → text after
        int lastEnd = 0;

        for (var block : blocks) {
            // Text before this thinking block — render as markdown
            if (block.getStartIndex() > lastEnd) {
                String textBefore = content.substring(lastEnd, block.getStartIndex());
                String markdown = markdownRenderer.render(textBefore);
                sb.append(markdown);
                if (!markdown.isEmpty() && !markdown.endsWith("\n")) {
                    sb.append("\n");
                }
            }

            // Render thinking block with rainbow styling
            sb.append(renderThinkingBlock(block, content, streaming));

            lastEnd = block.getEndIndex();
        }

        // Text after last block — render as markdown
        if (lastEnd < content.length()) {
            String textAfter = content.substring(lastEnd);
            String markdown = markdownRenderer.render(textAfter);
            sb.append(markdown);
        }

        return sb.toString();
    }

    /**
     * Render a single thinking block with collapsible rainbow styling.
     * Reference: openclaude/src/components/Message.tsx (AssistantThinkingMessage)
     */
    private static String renderThinkingBlock(ThinkingParser.ThinkingBlock block, String fullContent, boolean isStreaming) {
        StringBuilder sb = new StringBuilder();

        String content = block.getContent();
        boolean isUltrathink = block.isUltrathink();
        boolean isCollapsed = block.isCollapsed() && !thinkingBlock.isExpanded();

        // Rainbow colors for the thinking block
        String rainbow = getThinkingRainbow(0);
        String dimmed = Terminal.dim("");

        // Block header with indicator
        if (isUltrathink) {
            sb.append(rainbow).append("▸ ").append(Terminal.yellow("[ultrathink]")).append("\n");
        } else {
            sb.append(rainbow).append("◇ ").append(Terminal.dim("[thinking]")).append("\n");
        }

        if (isCollapsed) {
            // Collapsed: show indicator + preview
            sb.append(dimmed).append("  ");
            sb.append("[collapsed: ").append(content.length()).append(" chars]");
            if (!content.isEmpty()) {
                String firstLine = content.split("\n")[0];
                if (firstLine != null && firstLine.length() > 0) {
                    sb.append(" — ").append(truncate(firstLine, 60));
                }
            }
            sb.append(" ").append(Terminal.brightCyan("[click to expand]")).append("\n");
        } else {
            // Expanded: show full content with rainbow colors
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.trim().isEmpty()) {
                    sb.append("\n");
                    continue;
                }

                // Apply rainbow to first N words, then dim the rest
                String[] words = line.split("\\s+");
                sb.append(dimmed).append("  ");

                int rainbowIndex = 0;
                int wordsShown = Math.min(words.length, isUltrathink ? words.length : 10);

                for (int w = 0; w < words.length; w++) {
                    if (w < wordsShown) {
                        String color = getThinkingRainbow(rainbowIndex % 7);
                        sb.append(color).append(words[w]).append(RESET);
                        rainbowIndex++;
                    } else {
                        sb.append(Terminal.dim(words[w]));
                    }
                    sb.append(" ");
                }

                if (isStreaming && i == lines.length - 1) {
                    sb.append(Terminal.brightCyan("▌"));
                }
                sb.append(RESET).append("\n");
            }
        }

        // Streaming cursor
        if (isStreaming && content.isEmpty()) {
            sb.append(Terminal.brightCyan("  ▌"));
        }

        return sb.toString();
    }

    /**
     * Get rainbow color for thinking block at given index.
     */
    private static String getThinkingRainbow(int index) {
        // 7-color rainbow matching OpenClaude's thinking block colors
        String[] rainbow = {
            "\033[91m",  // Red
            "\033[93m",  // Yellow
            "\033[92m",  // Green
            "\033[96m",  // Cyan
            "\033[94m",  // Blue
            "\033[95m",  // Magenta
            "\033[33m",  // Orange
        };
        return rainbow[index % rainbow.length];
    }

    /**
     * Render assistant message with explicit thinking block processing.
     * Use this for messages that definitely contain thinking tags.
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

    /**
     * Render system message.
     */
    public static String renderSystem(String content) {
        StringBuilder sb = new StringBuilder();
        sb.append(Terminal.dim("[SYSTEM] " + content));
        return sb.toString();
    }

    /**
     * Render tool result with indigo background tint.
     * Reference: openclaude/src/components/Message.tsx (UserToolResultMessage)
     */
    public static String renderTool(String content) {
        StringBuilder sb = new StringBuilder();
        String wrapped = wordWrap(content, Terminal.getColumns() - 6);
        for (String line : wrapped.split("\n")) {
            // Indigo tint for tool results: dark blue-grey background
            sb.append(Terminal.dim("  │ " + line + "\n"));
        }
        return sb.toString();
    }

    /**
     * Simple render for compact display.
     */
    public static String renderSimple(String content, MessageRole role) {
        String prefix = switch (role) {
            case USER -> Terminal.user("▶ ");
            case ASSISTANT -> Terminal.assistant("◀ ");
            case TOOL -> Terminal.dim("└─ ");
            default -> "";
        };
        return prefix + content;
    }

    /**
     * Word wrap plain text (no ANSI codes).
     */
    private static String wordWrap(String text, int maxWidth) {
        if (text == null || text.isEmpty()) return "";
        if (maxWidth <= 0) maxWidth = 80;

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

    /**
     * Right-pad string to given width.
     */
    private static String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return "";
        return " ".repeat(width - s.length());
    }

    /**
     * Truncate string to max length with ellipsis.
     */
    private static String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) return text;
        return text.substring(0, Math.max(0, maxLen - 3)) + "...";
    }
}
