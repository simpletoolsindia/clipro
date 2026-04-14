package com.clipro.ui.components;

import com.clipro.ui.Terminal;

import java.util.HashMap;
import java.util.Map;

/**
 * Stats display component for CLIPRO.
 * Shows token usage, model stats, and session statistics.
 */
public class StatsComponent {

    private long sessionStartTime;
    private int totalTokens;
    private int promptTokens;
    private int completionTokens;
    private int messagesCount;
    private int toolCalls;
    private Map<String, Integer> modelUsage;

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
    public String estimateCost() {
        // Rough estimates per 1M tokens for OpenRouter
        Map<String, Double> rates = Map.of(
            "qwen/qwen3.6-plus", 0.001,
            "anthropic/claude-sonnet-4", 3.0,
            "openai/gpt-4o", 5.0,
            "google/gemini-2.0-flash-thinking-exp", 0.1
        );

        double totalCost = 0;
        for (Map.Entry<String, Integer> entry : modelUsage.entrySet()) {
            String model = entry.getKey();
            int count = entry.getValue();
            double rate = rates.getOrDefault(model, 0.5);
            double cost = (totalTokens / 1_000_000.0) * rate;
            totalCost += cost;
        }

        if (totalCost < 0.01) {
            return "< $0.01";
        }
        return String.format("$%.4f", totalCost);
    }

    /**
     * Render stats as ASCII art.
     */
    public String render() {
        StringBuilder sb = new StringBuilder();
        int width = Math.min(Terminal.getColumns(), 60);

        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║                      Session Statistics                       ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Session info
        sb.append(String.format("║ Session:   %-45s║\n", getSessionDuration()));
        sb.append(String.format("║ Messages:  %-45s║\n", messagesCount + " sent"));
        sb.append(String.format("║ Tool Calls: %-44s║\n", toolCalls + " executed"));

        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Token info
        sb.append(String.format("║ Prompt Tokens:    %-37s║\n", formatNumber(promptTokens)));
        sb.append(String.format("║ Completion Tokens: %-37s║\n", formatNumber(completionTokens)));
        sb.append(String.format("║ Total Tokens:      %-37s║\n", formatNumber(totalTokens)));

        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Model usage
        if (!modelUsage.isEmpty()) {
            sb.append("║ Model Usage:                                               ║\n");
            for (Map.Entry<String, Integer> entry : modelUsage.entrySet()) {
                String model = truncate(entry.getKey(), 45);
                sb.append(String.format("║   %-45s %4d║\n", model, entry.getValue()));
            }
            sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        }

        // Cost estimate
        sb.append(String.format("║ Est. Cost: %-47s║\n", estimateCost()));
        sb.append("╚══════════════════════════════════════════════════════════════╝\n");

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

    /**
     * Render token heatmap (visual representation of token usage over time).
     */
    public String renderTokenHeatmap() {
        StringBuilder sb = new StringBuilder();
        sb.append("Token Usage Heatmap:\n");

        int buckets = 10;
        int bucketSize = Math.max(1, totalTokens / buckets);

        for (int i = 0; i < buckets; i++) {
            int start = i * bucketSize;
            int end = Math.min((i + 1) * bucketSize, totalTokens);
            int usage = end - start;

            int intensity = Math.min(9, usage / (bucketSize / 10 + 1));
            String block = String.valueOf(intensity);

            // Color based on intensity (simple ANSI)
            if (intensity < 3) {
                sb.append("\033[32m").append(block).append("\033[0m"); // Green
            } else if (intensity < 6) {
                sb.append("\033[33m").append(block).append("\033[0m"); // Yellow
            } else {
                sb.append("\033[31m").append(block).append("\033[0m"); // Red
            }
        }
        sb.append("\n");
        sb.append("Low \033[32m■\033[0m → High \033[31m■\033[0m\n");

        return sb.toString();
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
