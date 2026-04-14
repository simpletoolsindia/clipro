package com.clipro.ui.components;

import com.clipro.ui.Terminal;

/**
 * Status bar - Pixel-perfect OpenClaude style.
 * Reference: ~/openclaude/src/components/Stats.tsx
 */
public class StatusBar {
    private int inputTokens = 0;
    private int outputTokens = 0;
    private long latencyMs = 0;
    private String vimMode = "";

    public StatusBar() {}

    public void setTokens(int input, int output) { this.inputTokens = input; this.outputTokens = output; }
    public int getInputTokens() { return inputTokens; }
    public int getOutputTokens() { return outputTokens; }
    public int getTotalTokens() { return inputTokens + outputTokens; }
    public void setLatency(long ms) { this.latencyMs = ms; }
    public long getLatencyMs() { return latencyMs; }
    public void setVimMode(String mode) { this.vimMode = mode; }
    public String getVimMode() { return vimMode; }

    public String render() {
        int width = Terminal.getColumns();
        StringBuilder sb = new StringBuilder();

        // Token display
        String tokens = Terminal.dim("Tokens: ") + Terminal.cyan(inputTokens + "/" + outputTokens);

        // Latency
        String latency = "";
        if (latencyMs > 0) {
            latency = Terminal.dim(" | ") + Terminal.green(latencyMs + "ms");
        }

        // Vim mode
        String vim = vimMode.isEmpty() ? "" : Terminal.yellow(" | " + vimMode);

        String content = tokens + latency + vim;

        // Box style
        return Terminal.BORDER_BL + Terminal.repeat(Terminal.BORDER_H, width - 2) + Terminal.BORDER_BR + "\r" +
               Terminal.BORDER_V + " " + content + Terminal.padRight("", width - content.length() - 3) + Terminal.BORDER_V;
    }

    public String renderCompact() {
        String tokens = Terminal.dim("[" + inputTokens + "/" + outputTokens + "]");
        String latency = latencyMs > 0 ? Terminal.green(" " + latencyMs + "ms") : "";
        String vim = vimMode.isEmpty() ? "" : Terminal.yellow(" " + vimMode);
        return tokens + latency + vim;
    }
}