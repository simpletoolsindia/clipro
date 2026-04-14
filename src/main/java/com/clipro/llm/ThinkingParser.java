package com.clipro.llm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses thinking/thinking tags from LLM responses.
 * Supports: <thinking>, </thinking>, <ultrathink>, [thinking] markers.
 *
 * Reference: openclaude/src/utils/thinking.ts
 */
public class ThinkingParser {

    // Rainbow colors for rendering (ANSI escape codes)
    private static final String[] RAINBOW = {
        "\u001B[38;2;235;95;87m",   // red
        "\u001B[38;2;245;139;87m",  // orange
        "\u001B[38;2;250;195;95m",  // yellow
        "\u001B[38;2;145;200;130m", // green
        "\u001B[38;2;130;170;220m", // blue
        "\u001B[38;2;155;130;200m", // indigo
        "\u001B[38;2;200;130;180m", // violet
    };
    private static final String RESET = "\u001B[0m";

    // Pattern for <thinking>...</thinking> blocks
    private static final Pattern THINKING_BLOCK_PATTERN =
        Pattern.compile("(?i)<thinking>(.*?)</thinking>", Pattern.DOTALL);

    // Pattern for <thinking> without closing tag
    private static final Pattern THINKING_OPEN_PATTERN =
        Pattern.compile("(?i)<thinking>(.*?)$", Pattern.DOTALL);

    // Pattern for ultrathink keyword
    private static final Pattern ULTRATHINK_PATTERN =
        Pattern.compile("(?i)\\bultrathink\\b");

    // Pattern for thinking keyword (standalone)
    private static final Pattern THINKING_KEYWORD_PATTERN =
        Pattern.compile("(?i)\\bthinking\\b");

    // Pattern for [thinking] bracket notation
    private static final Pattern BRACKET_THINKING_PATTERN =
        Pattern.compile("(?i)\\[thinking\\]");

    /**
     * Represents a parsed thinking block.
     */
    public static class ThinkingBlock {
        private final String content;
        private final int startIndex;
        private final int endIndex;
        private final boolean isUltrathink;
        private final boolean isCollapsed;

        public ThinkingBlock(String content, int startIndex, int endIndex,
                            boolean isUltrathink, boolean isCollapsed) {
            this.content = content;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.isUltrathink = isUltrathink;
            this.isCollapsed = isCollapsed;
        }

        public String getContent() { return content; }
        public int getStartIndex() { return startIndex; }
        public int getEndIndex() { return endIndex; }
        public boolean isUltrathink() { return isUltrathink; }
        public boolean isCollapsed() { return isCollapsed; }
        public int getLength() { return endIndex - startIndex; }
    }

    /**
     * Represents a thinking keyword trigger position.
     */
    public static class ThinkingTrigger {
        private final String word;
        private final int start;
        private final int end;
        private final boolean isUltrathink;

        public ThinkingTrigger(String word, int start, int end, boolean isUltrathink) {
            this.word = word;
            this.start = start;
            this.end = end;
            this.isUltrathink = isUltrathink;
        }

        public String getWord() { return word; }
        public int getStart() { return start; }
        public int getEnd() { return end; }
        public boolean isUltrathink() { return isUltrathink; }
    }

    private boolean collapseEnabled = true;
    private int maxCollapsedLength = 100;

    public ThinkingParser() {
        // Use built-in rainbow rendering - no external RainbowRenderer needed
    }

    /**
     * Render a word with rainbow coloring.
     */
    public String renderRainbowWord(String word, int index) {
        return RAINBOW[index % RAINBOW.length] + word + RESET;
    }

    /**
     * Render text with rainbow spectrum.
     */
    public String renderRainbow(String text) {
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            sb.append(renderRainbowWord(words[i], i));
            if (i < words.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Parse all thinking blocks from text.
     */
    public List<ThinkingBlock> parseBlocks(String text) {
        List<ThinkingBlock> blocks = new ArrayList<>();

        // Match <thinking>...</thinking> blocks
        Matcher blockMatcher = THINKING_BLOCK_PATTERN.matcher(text);
        while (blockMatcher.find()) {
            String content = blockMatcher.group(1);
            boolean isUltrathink = isUltrathinkContent(content);
            blocks.add(new ThinkingBlock(
                content,
                blockMatcher.start(),
                blockMatcher.end(),
                isUltrathink,
                shouldCollapse(content)
            ));
        }

        // Match <thinking> without closing (still streaming)
        Matcher openMatcher = THINKING_OPEN_PATTERN.matcher(text);
        while (openMatcher.find()) {
            // Skip if already matched as a block
            if (isInsideBlock(blocks, openMatcher.start())) continue;

            String content = openMatcher.group(1);
            boolean isUltrathink = content.contains("ultrathink");
            blocks.add(new ThinkingBlock(
                content,
                openMatcher.start(),
                text.length(),
                isUltrathink,
                shouldCollapse(content)
            ));
        }

        return blocks;
    }

    /**
     * Find thinking keyword triggers (for highlighting).
     */
    public List<ThinkingTrigger> findTriggers(String text) {
        List<ThinkingTrigger> triggers = new ArrayList<>();

        // Find ultrathink keywords
        Matcher ultrathinkMatcher = ULTRATHINK_PATTERN.matcher(text);
        while (ultrathinkMatcher.find()) {
            triggers.add(new ThinkingTrigger(
                ultrathinkMatcher.group(),
                ultrathinkMatcher.start(),
                ultrathinkMatcher.end(),
                true
            ));
        }

        // Get existing blocks to check overlap
        List<ThinkingBlock> existingBlocks = parseBlocks(text);

        // Find standalone thinking keywords (not inside blocks)
        Matcher thinkingMatcher = THINKING_KEYWORD_PATTERN.matcher(text);
        while (thinkingMatcher.find()) {
            // Skip if inside a thinking block or ultrathink
            if (isInsideBlock(existingBlocks, thinkingMatcher.start())) continue;
            if (isInsideTrigger(triggers, thinkingMatcher.start())) continue;

            triggers.add(new ThinkingTrigger(
                thinkingMatcher.group(),
                thinkingMatcher.start(),
                thinkingMatcher.end(),
                false
            ));
        }

        return triggers;
    }

    /**
     * Check if model supports thinking (based on model name).
     * Reference: openclaude checks for Sonnet 4+, Opus 4+
     */
    public boolean modelSupportsThinking(String model) {
        if (model == null) return false;

        String lower = model.toLowerCase();

        // Most local models with extended context support thinking
        if (lower.contains("qwen") || lower.contains("deepseek") ||
            lower.contains("llama") || lower.contains("mistral") ||
            lower.contains("codellama")) {
            return true;
        }

        // Disable for older models
        if (lower.contains("claude-3") || lower.contains("gpt-3") ||
            lower.contains("gpt-4-0")) {
            return false;
        }

        return true;
    }

    /**
     * Extract thinking content from a full response.
     * Removes thinking tags but preserves the thinking text.
     */
    public String extractThinkingContent(String response) {
        return response
            .replaceAll("(?i)<thinking>", "")
            .replaceAll("(?i)</thinking>", "");
    }

    /**
     * Strip all thinking blocks from text (for clean display).
     */
    public String stripThinkingBlocks(String text) {
        String result = THINKING_BLOCK_PATTERN.matcher(text).replaceAll("");
        result = BRACKET_THINKING_PATTERN.matcher(result).replaceAll("");
        return result.trim();
    }

    /**
     * Check if content contains ultrathink keyword.
     */
    public boolean isUltrathinkContent(String content) {
        return content != null && ULTRATHINK_PATTERN.matcher(content).find();
    }

    /**
     * Check if thinking should be collapsed by default.
     */
    public boolean shouldCollapse(String content) {
        if (!collapseEnabled) return false;
        return content != null && content.length() > maxCollapsedLength;
    }

    /**
     * Set whether to collapse long thinking blocks.
     */
    public void setCollapseEnabled(boolean enabled) {
        this.collapseEnabled = enabled;
    }

    /**
     * Set maximum length before collapsing.
     */
    public void setMaxCollapsedLength(int maxLength) {
        this.maxCollapsedLength = maxLength;
    }

    /**
     * Get rainbow color for index (for compatibility).
     */
    public String getRainbowColor(int index) {
        return RAINBOW[index % RAINBOW.length];
    }

    public String getResetCode() {
        return RESET;
    }

    // Helper methods

    private boolean isInsideBlock(List<ThinkingBlock> blocks, int position) {
        for (ThinkingBlock block : blocks) {
            if (position >= block.getStartIndex() && position < block.getEndIndex()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideTrigger(List<ThinkingTrigger> triggers, int position) {
        for (ThinkingTrigger trigger : triggers) {
            if (position >= trigger.getStart() && position < trigger.getEnd()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideBlock(List<ThinkingBlock> blocks, int start, int end) {
        for (ThinkingBlock block : blocks) {
            if (start >= block.getStartIndex() && end <= block.getEndIndex()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count total thinking blocks in text.
     */
    public int countThinkingBlocks(String text) {
        return parseBlocks(text).size();
    }

    /**
     * Get total thinking characters (for display).
     */
    public int getTotalThinkingLength(String text) {
        int total = 0;
        for (ThinkingBlock block : parseBlocks(text)) {
            total += block.getContent().length();
        }
        return total;
    }
}
