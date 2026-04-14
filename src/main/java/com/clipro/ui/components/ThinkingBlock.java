package com.clipro.ui.components;

/**
 * Thinking block renderer with collapsible UI.
 * Renders thinking content with rainbow colors and shimmer animation.
 *
 * Reference: openclaude/src/components/Message.tsx (thinking blocks)
 */
public class ThinkingBlock {

    private final ThinkingParser parser;
    private final RainbowRenderer renderer;
    private final ShimmerAnimator animator;

    private boolean expanded = false;
    private boolean showCollapsedIndicator = true;
    private int indentSize = 2;

    // ANSI styling for thinking blocks
    private static final String RESET = "\u001B[0m";
    private static final String DIM = "\u001B[2m";
    private static final String ITALIC = "\u001B[3m";

    public ThinkingBlock() {
        this.parser = new ThinkingParser();
        this.renderer = parser.getRenderer();
        this.animator = new ShimmerAnimator(renderer);
    }

    public ThinkingBlock(ThinkingParser parser) {
        this.parser = parser;
        this.renderer = parser.getRenderer();
        this.animator = new ShimmerAnimator(renderer);
    }

    /**
     * Render thinking content from a message.
     * Automatically detects thinking tags and applies appropriate styling.
     */
    public String render(String content, boolean isStreaming) {
        if (content == null || content.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        // Check for thinking blocks
        var blocks = parser.parseBlocks(content);

        if (blocks.isEmpty()) {
            // No thinking blocks, just render normally
            return renderInlineThinking(content);
        }

        // Render each block
        int lastEnd = 0;
        for (var block : blocks) {
            // Render text before this block
            if (block.getStartIndex() > lastEnd) {
                String before = content.substring(lastEnd, block.getStartIndex());
                sb.append(renderInlineThinking(before));
            }

            // Render the thinking block
            sb.append(renderThinkingBlock(block, isStreaming));

            lastEnd = block.getEndIndex();
        }

        // Render text after last block
        if (lastEnd < content.length()) {
            String after = content.substring(lastEnd);
            sb.append(renderInlineThinking(after));
        }

        return sb.toString();
    }

    /**
     * Render thinking blocks with streaming support.
     * Shows partial content as it streams in.
     */
    public String renderStreaming(String partialContent) {
        return render(partialContent, true);
    }

    /**
     * Render a single thinking block with formatting.
     */
    private String renderThinkingBlock(ThinkingParser.ThinkingBlock block, boolean isStreaming) {
        StringBuilder sb = new StringBuilder();

        String prefix = block.isUltrathink() ? "▸ " : "◇ ";
        sb.append(renderer.renderWord(prefix, 0, animator.isShimmerPhase()));

        String content = block.getContent();

        // Handle collapsed display
        if (block.isCollapsed() && !expanded) {
            sb.append(renderCollapsed(content, block.isUltrathink()));
        } else {
            // Expanded: show full content with dimmed tags
            sb.append(renderExpanded(content, block.isUltrathink()));
        }

        // Streaming indicator
        if (isStreaming) {
            sb.append(renderer.renderWord("▌", 0, animator.isShimmerPhase()));
        }

        sb.append("\n");
        return sb.toString();
    }

    /**
     * Render collapsed thinking block.
     */
    private String renderCollapsed(String content, boolean isUltrathink) {
        StringBuilder sb = new StringBuilder();

        // Collapsed indicator line
        String indicator = isUltrathink
            ? "[+ ultrathink: " + content.length() + " chars]"
            : "[+ thinking: " + content.length() + " chars]";

        sb.append(DIM).append(ITALIC);
        sb.append(" ".repeat(indentSize));
        sb.append(renderer.renderWord(indicator, 2, animator.isShimmerPhase()));
        sb.append(RESET);
        sb.append("\n");

        // Preview first line
        String firstLine = content.split("\n")[0];
        if (firstLine != null && firstLine.length() > 0) {
            sb.append(" ".repeat(indentSize));
            sb.append(DIM).append(ITALIC);
            sb.append(truncate(firstLine, 50));
            sb.append(RESET);
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Render expanded thinking block with rainbow coloring.
     */
    private String renderExpanded(String content, boolean isUltrathink) {
        StringBuilder sb = new StringBuilder();

        sb.append(" ".repeat(indentSize));
        sb.append(DIM).append(ITALIC);

        // Apply rainbow to first few words
        String[] words = content.split("\\s+");
        for (int i = 0; i < Math.min(words.length, 20); i++) {
            if (isUltrathink) {
                // Full rainbow for ultrathink
                sb.append(renderer.renderWord(words[i], i, animator.isShimmerPhase()));
            } else {
                // Dim for regular thinking
                sb.append(words[i]);
            }
            sb.append(" ");
        }

        // If content is longer, show summary
        if (words.length > 20) {
            sb.append(DIM).append("... (").append(words.length - 20).append(" more words)").append(RESET);
        }

        sb.append(RESET);
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Render inline thinking (without block tags).
     */
    private String renderInlineThinking(String text) {
        var triggers = parser.findTriggers(text);

        if (triggers.isEmpty()) {
            return DIM + text;
        }

        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        for (var trigger : triggers) {
            // Text before trigger
            if (trigger.getStart() > lastEnd) {
                sb.append(DIM);
                sb.append(text, lastEnd, trigger.getStart());
                sb.append(RESET);
            }

            // The trigger with rainbow
            sb.append(renderer.renderWord(trigger.getWord(), 0, animator.isShimmerPhase()));
            lastEnd = trigger.getEnd();
        }

        // Text after last trigger
        if (lastEnd < text.length()) {
            sb.append(DIM);
            sb.append(text.substring(lastEnd));
            sb.append(RESET);
        }

        return sb.toString();
    }

    /**
     * Render just the thinking summary line (compact mode).
     */
    public String renderSummary(String content) {
        int count = parser.countThinkingBlocks(content);
        int length = parser.getTotalThinkingLength(content);

        if (count == 0) return "";

        String summary = count == 1
            ? "[1 thinking block, " + length + " chars]"
            : "[" + count + " thinking blocks, " + length + " chars]";

        return DIM + ITALIC + summary + RESET;
    }

    /**
     * Render ultrathink keyword with full rainbow animation.
     */
    public String renderUltrathinkKeyword(String keyword) {
        return renderer.renderUltrathink(keyword, animator.isShimmerPhase());
    }

    /**
     * Toggle expanded/collapsed state.
     */
    public void toggleExpanded() {
        expanded = !expanded;
    }

    /**
     * Set expanded state.
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * Check if expanded.
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Enable/disable collapse indicators.
     */
    public void setShowCollapsedIndicator(boolean show) {
        this.showCollapsedIndicator = show;
    }

    /**
     * Set indent size for nested thinking.
     */
    public void setIndentSize(int size) {
        this.indentSize = Math.max(0, size);
    }

    /**
     * Advance animation frame.
     */
    public void tick() {
        animator.tick();
    }

    /**
     * Enable/disable shimmer animation.
     */
    public void setShimmerEnabled(boolean enabled) {
        animator.setEnabled(enabled);
    }

    /**
     * Get the parser instance.
     */
    public ThinkingParser getParser() {
        return parser;
    }

    /**
     * Get the animator instance.
     */
    public ShimmerAnimator getAnimator() {
        return animator;
    }

    /**
     * Get the renderer instance.
     */
    public RainbowRenderer getRenderer() {
        return renderer;
    }

    private String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) return text;
        return text.substring(0, Math.max(0, maxLen - 3)) + "...";
    }
}
