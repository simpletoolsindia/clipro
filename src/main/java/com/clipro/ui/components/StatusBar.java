package com.clipro.ui.components;

import com.clipro.ui.Terminal;

/**
 * Status bar - Pixel-perfect OpenClaude style.
 * Reference: ~/openclaude/src/components/Stats.tsx
 *
 * Features:
 * - Token display (input/output)
 * - Session cost tracking
 * - Rate limit display (5h and 7d windows)
 * - Latency and vim mode indicators
 */
public class StatusBar {
    private int inputTokens = 0;
    private int outputTokens = 0;
    private double sessionCost = 0.0;
    private long latencyMs = 0;
    private String vimMode = "";
    private String statusText = "Ready";

    // Rate limit tracking
    private int rateLimit5hUsed = 0;
    private int rateLimit5hTotal = 100;
    private int rateLimit7dUsed = 0;
    private int rateLimit7dTotal = 1000;

    public StatusBar() {}

    // Token methods
    public void setTokens(int input, int output) { this.inputTokens = input; this.outputTokens = output; }
    public int getInputTokens() { return inputTokens; }
    public int getOutputTokens() { return outputTokens; }
    public int getTotalTokens() { return inputTokens + outputTokens; }

    // Latency methods
    public void setLatency(long ms) { this.latencyMs = ms; }
    public long getLatencyMs() { return latencyMs; }

    // Vim mode methods
    public void setVimMode(String mode) { this.vimMode = mode; }
    public String getVimMode() { return vimMode; }

    // Status text methods
    public void setStatusText(String text) { this.statusText = text; }
    public String getStatusText() { return statusText; }

    // Cost tracking methods (M-07)
    public void recordCost(double cost) { this.sessionCost += cost; }
    public void setSessionCost(double cost) { this.sessionCost = cost; }
    public double getSessionCost() { return sessionCost; }

    // Rate limit methods (M-08)
    public void setRateLimit5h(int used, int total) {
        this.rateLimit5hUsed = used;
        this.rateLimit5hTotal = total;
    }
    public void setRateLimit7d(int used, int total) {
        this.rateLimit7dUsed = used;
        this.rateLimit7dTotal = total;
    }
    public void recordRequest() {
        this.rateLimit5hUsed++;
        this.rateLimit7dUsed++;
    }

    public String render() {
        int width = Terminal.getColumns();
        StringBuilder sb = new StringBuilder();

        // Token display
        String tokens = Terminal.dim("Tokens: ") + Terminal.cyan(inputTokens + "/" + outputTokens);

        // Cost display (M-07)
        String cost = Terminal.dim(" │ $") + formatCost(sessionCost);

        // Rate limit display (M-08)
        String rateLimit = Terminal.dim(" │ 5h: ") + formatRateLimit(rateLimit5hUsed, rateLimit5hTotal) +
                           Terminal.dim(" │ 7d: ") + formatRateLimit(rateLimit7dUsed, rateLimit7dTotal);

        // Latency
        String latency = "";
        if (latencyMs > 0) {
            latency = Terminal.dim(" │ ") + Terminal.green(latencyMs + "ms");
        }

        // Vim mode
        String vim = vimMode.isEmpty() ? "" : Terminal.yellow(" │ " + vimMode);

        // Status text
        String status = statusText.equals("Ready") ? Terminal.green(statusText) : Terminal.yellow(statusText);

        String content = tokens + cost + rateLimit + latency + vim + Terminal.dim(" │ ") + status;

        // Box style
        return Terminal.BORDER_BL + Terminal.repeat(Terminal.BORDER_H, width - 2) + Terminal.BORDER_BR + "\r" +
               Terminal.BORDER_V + " " + content + Terminal.padRight("", width - content.length() - 3) + Terminal.BORDER_V;
    }

    public String renderCompact() {
        String tokens = Terminal.dim("[" + inputTokens + "/" + outputTokens + "]");
        String cost = sessionCost > 0 ? Terminal.dim(" [$") + formatCost(sessionCost) + "]" : "";
        String latency = latencyMs > 0 ? Terminal.green(" " + latencyMs + "ms") : "";
        String vim = vimMode.isEmpty() ? "" : Terminal.yellow(" " + vimMode);
        return tokens + cost + latency + vim;
    }

    /**
     * Format cost with appropriate precision.
     */
    private String formatCost(double cost) {
        if (cost < 0.01) {
            return Terminal.dim("<$0.01");
        } else if (cost < 1.0) {
            return String.format("%.4f", cost);
        } else if (cost < 100.0) {
            return String.format("%.2f", cost);
        } else {
            return String.format("%.0f", cost);
        }
    }

    /**
     * Format rate limit with warning color when >80% used.
     */
    private String formatRateLimit(int used, int total) {
        double percentage = (double) used / total;
        String display = used + "/" + total;

        if (percentage > 0.8) {
            return Terminal.red(display);
        } else if (percentage > 0.5) {
            return Terminal.yellow(display);
        } else {
            return Terminal.green(display);
        }
    }
}