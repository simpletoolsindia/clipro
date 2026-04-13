package com.clipro.ui.components;

import com.clipro.ui.Terminal;

/**
 * Status bar showing tokens, latency, etc.
 * Reference: openclaude/src/components/Stats.tsx
 */
public class StatusBar {
    private int inputTokens = 0;
    private int outputTokens = 0;
    private long latencyMs = 0;
    private String vimMode = "";

    public StatusBar() {}

    public void setTokens(int input, int output) {
        this.inputTokens = input;
        this.outputTokens = output;
    }

    public int getInputTokens() {
        return inputTokens;
    }

    public int getOutputTokens() {
        return outputTokens;
    }

    public int getTotalTokens() {
        return inputTokens + outputTokens;
    }

    public void setLatency(long ms) {
        this.latencyMs = ms;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setVimMode(String mode) {
        this.vimMode = mode;
    }

    public String getVimMode() {
        return vimMode;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();

        // Token count
        sb.append("Tokens: ").append(Terminal.cyan(inputTokens + "/" + outputTokens));

        // Latency
        if (latencyMs > 0) {
            sb.append(" | Latency: ").append(Terminal.green(latencyMs + "ms"));
        }

        // Vim mode
        if (!vimMode.isEmpty()) {
            sb.append(" | ").append(Terminal.yellow("VIM:")).append(vimMode);
        }

        return sb.toString();
    }

    public String renderFull() {
        int width = Terminal.getColumns();
        String content = render();
        int padding = Math.max(0, width - content.length() - 2);

        return "├" + " ".repeat(width - 2) + "┤\r" + content + " ".repeat(padding);
    }
}
