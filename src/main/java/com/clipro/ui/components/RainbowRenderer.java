package com.clipro.ui.components;

/**
 * Rainbow color renderer for thinking blocks and ultrathink keywords.
 * Matches OpenClaude's theme.ts rainbow colors.
 *
 * Reference: openclaude/src/utils/theme.ts
 * Colors: rainbow_red through rainbow_violet (7 colors)
 */
public class RainbowRenderer {

    // Rainbow colors (RGB true color ANSI escapes)
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    // Base rainbow colors
    private static final String RAINBOW_RED = "\u001B[38;2;235;95;87m";
    private static final String RAINBOW_ORANGE = "\u001B[38;2;245;139;87m";
    private static final String RAINBOW_YELLOW = "\u001B[38;2;250;195;95m";
    private static final String RAINBOW_GREEN = "\u001B[38;2;145;200;130m";
    private static final String RAINBOW_BLUE = "\u001B[38;2;130;170;220m";
    private static final String RAINBOW_INDIGO = "\u001B[38;2;155;130;200m";
    private static final String RAINBOW_VIOLET = "\u001B[38;2;200;130;180m";

    // Shimmer (lighter) rainbow colors
    private static final String RAINBOW_RED_SHIMMER = "\u001B[38;2;250;155;147m";
    private static final String RAINBOW_ORANGE_SHIMMER = "\u001B[38;2;255;185;137m";
    private static final String RAINBOW_YELLOW_SHIMMER = "\u001B[38;2;255;225;155m";
    private static final String RAINBOW_GREEN_SHIMMER = "\u001B[38;2;185;230;180m";
    private static final String RAINBOW_BLUE_SHIMMER = "\u001B[38;2;180;205;240m";
    private static final String RAINBOW_INDIGO_SHIMMER = "\u001B[38;2;195;180;230m";
    private static final String RAINBOW_VIOLET_SHIMMER = "\u001B[38;2;230;180;210m";

    // Agent colors (8 colors for sub-agents)
    public static final String AGENT_RED = "\u001B[38;2;220;38;38m";
    public static final String AGENT_BLUE = "\u001B[38;2;37;99;235m";
    public static final String AGENT_GREEN = "\u001B[38;2;22;163;74m";
    public static final String AGENT_YELLOW = "\u001B[38;2;202;138;4m";
    public static final String AGENT_PURPLE = "\u001B[38;2;147;51;234m";
    public static final String AGENT_ORANGE = "\u001B[38;2;234;88;12m";
    public static final String AGENT_PINK = "\u001B[38;2;219;39;119m";
    public static final String AGENT_CYAN = "\u001B[38;2;8;145;178m";

    private final String[] baseColors;
    private final String[] shimmerColors;
    private boolean shimmerEnabled = false;
    private long shimmerPhase = 0;

    public RainbowRenderer() {
        this.baseColors = new String[] {
            RAINBOW_RED, RAINBOW_ORANGE, RAINBOW_YELLOW, RAINBOW_GREEN,
            RAINBOW_BLUE, RAINBOW_INDIGO, RAINBOW_VIOLET
        };
        this.shimmerColors = new String[] {
            RAINBOW_RED_SHIMMER, RAINBOW_ORANGE_SHIMMER, RAINBOW_YELLOW_SHIMMER,
            RAINBOW_GREEN_SHIMMER, RAINBOW_BLUE_SHIMMER, RAINBOW_INDIGO_SHIMMER,
            RAINBOW_VIOLET_SHIMMER
        };
    }

    /**
     * Render text with rainbow coloring, cycling through colors per character.
     */
    public String renderRainbow(String text, boolean shimmer) {
        if (text == null || text.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        String[] colors = shimmer ? shimmerColors : baseColors;

        for (int i = 0; i < text.length(); i++) {
            String color = colors[i % colors.length];
            sb.append(color).append(text.charAt(i));
        }

        sb.append(RESET);
        return sb.toString();
    }

    /**
     * Render text with bold rainbow coloring.
     */
    public String renderRainbowBold(String text, boolean shimmer) {
        if (text == null || text.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        String[] colors = shimmer ? shimmerColors : baseColors;

        sb.append(BOLD);
        for (int i = 0; i < text.length(); i++) {
            String color = colors[i % colors.length];
            sb.append(color).append(text.charAt(i));
        }
        sb.append(RESET);

        return sb.toString();
    }

    /**
     * Render a single word with a single rainbow color.
     * Colors cycle per word.
     */
    public String renderWord(String word, int wordIndex, boolean shimmer) {
        if (word == null || word.isEmpty()) return "";

        String color;
        if (shimmer && shimmerEnabled) {
            color = shimmerColors[wordIndex % shimmerColors.length];
        } else {
            color = baseColors[wordIndex % baseColors.length];
        }

        return color + BOLD + word + RESET;
    }

    /**
     * Render thinking tags (like &lt;thinking&gt;) with rainbow colors.
     * The tag itself gets rainbow coloring, content is dimmed.
     */
    public String renderThinkingTag(String tagContent, boolean shimmer) {
        return BOLD + renderRainbow(tagContent, shimmer) + RESET;
    }

    /**
     * Render ultrathink keyword with per-character rainbow (as per OpenClaude).
     */
    public String renderUltrathink(String text, boolean shimmer) {
        return renderRainbow(text, shimmer);
    }

    /**
     * Enable shimmer animation phase (for animation tick).
     */
    public void tickShimmer() {
        shimmerPhase = (shimmerPhase + 1) % 2;
    }

    /**
     * Check if currently in shimmer phase.
     */
    public boolean isShimmerPhase() {
        return shimmerPhase == 1;
    }

    /**
     * Toggle shimmer effect.
     */
    public void setShimmerEnabled(boolean enabled) {
        this.shimmerEnabled = enabled;
    }

    /**
     * Get agent color by index (0-7).
     */
    public String getAgentColor(int index) {
        String[] agentColors = {
            AGENT_RED, AGENT_BLUE, AGENT_GREEN, AGENT_YELLOW,
            AGENT_PURPLE, AGENT_ORANGE, AGENT_PINK, AGENT_CYAN
        };
        return agentColors[Math.abs(index) % agentColors.length];
    }

    /**
     * Get a single rainbow color by index.
     */
    public String getRainbowColor(int index, boolean shimmer) {
        String[] colors = shimmer ? shimmerColors : baseColors;
        return colors[Math.abs(index) % colors.length];
    }

    /**
     * Render diff added text (green).
     */
    public String renderDiffAdded(String text) {
        return "\u001B[38;2;34;92;43m" + text + RESET; // diffAdded dark theme
    }

    /**
     * Render diff removed text (red).
     */
    public String renderDiffRemoved(String text) {
        return "\u001B[38;2;122;41;54m" + text + RESET; // diffRemoved dark theme
    }

    /**
     * Render diff added word (bright green).
     */
    public String renderDiffAddedWord(String text) {
        return "\u001B[38;2;56;166;96m" + text + RESET;
    }

    /**
     * Render diff removed word (bright red).
     */
    public String renderDiffRemovedWord(String text) {
        return "\u001B[38;2;179;89;107m" + text + RESET;
    }

    /**
     * Static utility: get rainbow color for character index.
     */
    public static String getRainbowForChar(int charIndex, boolean shimmer) {
        String[][] colors = {
            { RAINBOW_RED, RAINBOW_RED_SHIMMER },
            { RAINBOW_ORANGE, RAINBOW_ORANGE_SHIMMER },
            { RAINBOW_YELLOW, RAINBOW_YELLOW_SHIMMER },
            { RAINBOW_GREEN, RAINBOW_GREEN_SHIMMER },
            { RAINBOW_BLUE, RAINBOW_BLUE_SHIMMER },
            { RAINBOW_INDIGO, RAINBOW_INDIGO_SHIMMER },
            { RAINBOW_VIOLET, RAINBOW_VIOLET_SHIMMER }
        };
        int idx = Math.abs(charIndex) % 7;
        return colors[idx][shimmer ? 1 : 0];
    }

    /**
     * Render a range of characters with rainbow, suitable for shimmer animation.
     */
    public String renderRainbowRange(String text, int startOffset, int length, boolean shimmer) {
        if (text == null || text.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        String[] colors = shimmer ? shimmerColors : baseColors;
        int end = Math.min(startOffset + length, text.length());

        for (int i = startOffset; i < end; i++) {
            String color = colors[i % colors.length];
            sb.append(color).append(text.charAt(i));
        }

        sb.append(RESET);
        return sb.toString();
    }
}
