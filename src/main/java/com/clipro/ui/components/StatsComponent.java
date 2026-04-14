package com.clipro.ui.components;

import com.clipro.ui.Terminal;

import java.util.HashMap;
import java.util.Map;

/**
 * Stats display component for CLIPRO (M-01).
 * Shows token usage, model stats, and session statistics with ASCII charts.
 * Reference: ~/openclaude/src/components/Stats.tsx
 */
public class StatsComponent {

    private long sessionStartTime;
    private int totalTokens;
    private int promptTokens;
    private int completionTokens;
    private int messagesCount;
    private int toolCalls;
    private Map<String, Integer> modelUsage;
    private int currentTab = 0;

    // Tab names
    private static final String[] TAB_NAMES = {"Overview", "Tokens", "Cost", "Session"};

    public StatsComponent() {
        this.sessionStartTime = System.currentTimeMillis();
        this.totalTokens = 0;
        this.promptTokens = 0;
        this.completionTokens = 0;
        this.messagesCount = 0;
        this.toolCalls = 0;
        this.modelUsage = new HashMap<>();
    }

    /**
     * Record token usage.
     */
    public void recordTokens(int prompt, int completion) {
        this.promptTokens += prompt;
        this.completionTokens += completion;
        this.totalTokens = promptTokens + completionTokens;
    }

    /**
     * Record a message.
     */
    public void recordMessage() {
        messagesCount++;
    }

    /**
     * Record a tool call.
     */
    public void recordToolCall() {
        toolCalls++;
    }

    /**
     * Record model usage.
     */
    public void recordModelUsage(String model) {
        modelUsage.merge(model, 1, Integer::sum);
    }

    /**
     * Set current tab (0=Overview, 1=Tokens, 2=Cost, 3=Session).
     */
    public void setTab(int tab) {
        if (tab >= 0 && tab < TAB_NAMES.length) {
            this.currentTab = tab;
        }
    }

    public int getTab() { return currentTab; }

    /**
     * Get session duration in human-readable format.
     */
    public String getSessionDuration() {
        long duration = System.currentTimeMillis() - sessionStartTime;
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Estimate cost for OpenRouter models.
     */
    public double getEstimatedCost() {
        Map<String, Double> rates = Map.of(
            "qwen/qwen3.6-plus", 0.001,
            "anthropic/claude-sonnet-4", 3.0,
            "openai/gpt-4o", 5.0,
            "google/gemini-2.0-flash-thinking-exp", 0.1
        );

        double totalCost = 0;
        for (Map.Entry<String, Integer> entry : modelUsage.entrySet()) {
            String model = entry.getKey();
            double rate = rates.getOrDefault(model, 0.5);
            double cost = (totalTokens / 1_000_000.0) * rate;
            totalCost += cost;
        }
        return totalCost;
    }

    public String estimateCost() {
        double cost = getEstimatedCost();
        if (cost < 0.01) {
            return "< $0.01";
        }
        return String.format("$%.4f", cost);
    }

    /**
     * Render stats with ASCII charts and tab layout (M-01).
     */
    public String render() {
        StringBuilder sb = new StringBuilder();
        int width = Math.min(Terminal.getColumns(), 70);

        // Tab header
        sb.append("┌─ Statistics ─");
        for (int i = 0; i < TAB_NAMES.length; i++) {
            String tab = " " + TAB_NAMES[i] + " ";
            if (i == currentTab) {
                sb.append(Terminal.brightCyan(tab));
            } else {
                sb.append(Terminal.dim(tab));
            }
        }
        sb.append("─".repeat(Math.max(0, width - 18 - TAB_NAMES[0].length() * TAB_NAMES.length)));
        sb.append("─┐\n");

        // Tab content
        switch (currentTab) {
            case 1 -> sb.append(renderTokensTab(width));
            case 2 -> sb.append(renderCostTab(width));
            case 3 -> sb.append(renderSessionTab(width));
            default -> sb.append(renderOverviewTab(width));
        }

        return sb.toString();
    }

    /**
     * Render Overview tab.
     */
    private String renderOverviewTab(int width) {
        StringBuilder sb = new StringBuilder();

        // ASCII bar chart for message composition
        sb.append("├─ Messages ────────────────────────────────────────────────────────┤\n");
        sb.append("│ Messages: ").append(renderAsciiBar(messagesCount, 100, 20)).append(" ").append(messagesCount).append("\n");
        sb.append("│ Tool Calls: ").append(renderAsciiBar(toolCalls, 50, 20)).append(" ").append(toolCalls).append("\n");
        sb.append("├─ Tokens ──────────────────────────────────────────────────────────┤\n");
        sb.append("│ Total: ").append(renderAsciiBar(totalTokens, 100000, 20)).append(" ").append(formatNumber(totalTokens)).append("\n");
        sb.append("├─ Cost ─────────────────────────────────────────────────────────────┤\n");
        sb.append("│ Session: ").append(renderAsciiBar((int)(getEstimatedCost() * 1000), 100, 20)).append(" ").append(estimateCost()).append("\n");
        sb.append("└───────────────────────────────────────────────────────────────────┘\n");

        return sb.toString();
    }

    /**
     * Render Tokens tab with detailed breakdown.
     */
    private String renderTokensTab(int width) {
        StringBuilder sb = new StringBuilder();
        int maxTokens = Math.max(totalTokens, 1);

        sb.append("├─ Token Usage ──────────────────────────────────────────────────────┤\n");
        sb.append("│ Input Tokens:  ").append(renderAsciiBar(promptTokens, maxTokens, 40)).append(" ").append(formatNumber(promptTokens)).append("\n");
        sb.append("│ Output Tokens: ").append(renderAsciiBar(completionTokens, maxTokens, 40)).append(" ").append(formatNumber(completionTokens)).append("\n");
        sb.append("│ Total Tokens:  ").append(renderAsciiBar(totalTokens, maxTokens, 40)).append(" ").append(formatNumber(totalTokens)).append("\n");

        // Ratio visualization
        double ratio = totalTokens > 0 ? (double) completionTokens / totalTokens * 100 : 0;
        sb.append("├─ I/O Ratio ───────────────────────────────────────────────────────┤\n");
        sb.append("│ Input:Completion = ").append(formatNumber(promptTokens)).append(" : ").append(formatNumber(completionTokens)).append(" (").append(String.format("%.1f%%", ratio)).append(" completion)\n");

        sb.append("└───────────────────────────────────────────────────────────────────┘\n");
        return sb.toString();
    }

    /**
     * Render Cost tab with model breakdown.
     */
    private String renderCostTab(int width) {
        StringBuilder sb = new StringBuilder();

        sb.append("├─ API Cost Estimate ──────────────────────────────────────────────┤\n");
        sb.append("│ Total Session Cost: ").append(Terminal.green(estimateCost())).append("\n");

        if (!modelUsage.isEmpty()) {
            sb.append("├─ Model Breakdown ─────────────────────────────────────────────────┤\n");
            Map<String, Double> rates = Map.of(
                "qwen/qwen3.6-plus", 0.001,
                "anthropic/claude-sonnet-4", 3.0,
                "openai/gpt-4o", 5.0,
                "google/gemini-2.0-flash-thinking-exp", 0.1
            );

            for (Map.Entry<String, Integer> entry : modelUsage.entrySet()) {
                String model = truncate(entry.getKey(), 35);
                double rate = rates.getOrDefault(entry.getKey(), 0.5);
                double modelCost = (totalTokens / 1_000_000.0) * rate;
                sb.append("│ ").append(truncate(model, 20)).append(": ").append(String.format("$%.4f", modelCost)).append(" (").append(entry.getValue()).append(" calls)\n");
            }
        }

        sb.append("└───────────────────────────────────────────────────────────────────┘\n");
        return sb.toString();
    }

    /**
     * Render Session tab.
     */
    private String renderSessionTab(int width) {
        StringBuilder sb = new StringBuilder();

        sb.append("├─ Session Info ───────────────────────────────────────────────────┤\n");
        sb.append("│ Duration: ").append(getSessionDuration()).append("\n");
        sb.append("│ Messages: ").append(messagesCount).append("\n");
        sb.append("│ Tool Calls: ").append(toolCalls).append("\n");

        if (!modelUsage.isEmpty()) {
            sb.append("├─ Models Used ─────────────────────────────────────────────────────┤\n");
            for (Map.Entry<String, Integer> entry : modelUsage.entrySet()) {
                sb.append("│ ").append(truncate(entry.getKey(), 50)).append(": ").append(entry.getValue()).append(" requests\n");
            }
        }

        sb.append("└───────────────────────────────────────────────────────────────────┘\n");
        return sb.toString();
    }

    /**
     * Render ASCII bar chart using Unicode blocks: ░▒▓█
     */
    private String renderAsciiBar(int value, int max, int width) {
        if (max <= 0) max = 1;
        int filled = (int) ((double) value / max * width);
        filled = Math.min(filled, width);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) {
            if (i < filled) {
                double ratio = (double) i / width;
                if (ratio < 0.5) {
                    sb.append("\033[32m▓\033[0m"); // Green
                } else if (ratio < 0.8) {
                    sb.append("\033[33m▓\033[0m"); // Yellow
                } else {
                    sb.append("\033[31m▓\033[0m"); // Red
                }
            } else {
                sb.append("\033[90m░\033[0m"); // Dim gray
            }
        }
        return sb.toString();
    }

    /**
     * Render compact stats line for status bar.
     */
    public String renderCompact() {
        return String.format("[%s] [Msgs: %d] [Tokens: %s]",
            getSessionDuration(),
            messagesCount,
            formatNumberCompact(totalTokens)
        );
    }

    private String formatNumber(int num) {
        if (num < 1000) return String.valueOf(num);
        if (num < 1_000_000) return String.format("%,d", num);
        return String.format("%.1fM", num / 1_000_000.0);
    }

    private String formatNumberCompact(int num) {
        if (num < 1000) return String.valueOf(num);
        if (num < 1_000_000) return (num / 1000) + "K";
        return String.format("%.1fM", num / 1_000_000.0);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 2) + "..";
    }

    // Getters for external access
    public int getTotalTokens() { return totalTokens; }
    public int getMessagesCount() { return messagesCount; }
    public int getToolCalls() { return toolCalls; }
    public long getSessionStartTime() { return sessionStartTime; }
}
