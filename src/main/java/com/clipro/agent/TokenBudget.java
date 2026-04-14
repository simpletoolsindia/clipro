package com.clipro.agent;

/**
 * Token budget management for context management.
 */
public class TokenBudget {

    private static final int SYSTEM_PROMPT_TOKENS = 2000;
    private static final int RESERVED_RESPONSE_TOKENS = 1000;

    private int promptTokens;
    private int completionTokens;
    private int maxTokens;

    public TokenBudget() {
        this.promptTokens = 0;
        this.completionTokens = 0;
        this.maxTokens = 20000;
    }

    public TokenBudget(int maxTokens) {
        this.promptTokens = 0;
        this.completionTokens = 0;
        this.maxTokens = maxTokens;
    }

    public void setMaxTokens(int max) {
        this.maxTokens = max;
    }

    public void addPrompt(int tokens) {
        this.promptTokens += tokens;
    }

    public void addCompletion(int tokens) {
        this.completionTokens += tokens;
    }

    public void addUsage(int tokens) {
        // Approximate: half to prompt, half to completion
        this.promptTokens += tokens / 2;
        this.completionTokens += tokens / 2;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }

    public int getRemainingTokens() {
        return maxTokens - getTotalTokens();
    }

    public boolean isOverBudget() {
        return isOverBudget(maxTokens);
    }

    public boolean isOverBudget(int limit) {
        return getTotalTokens() > limit;
    }

    public boolean canContinue() {
        // Leave room for system prompt and response
        int available = getRemainingTokens() - SYSTEM_PROMPT_TOKENS - RESERVED_RESPONSE_TOKENS;
        return available > 0;
    }

    public void reset() {
        this.promptTokens = 0;
        this.completionTokens = 0;
    }

    /**
     * Get available context window for messages.
     */
    public int getContextWindow() {
        int available = getRemainingTokens() - SYSTEM_PROMPT_TOKENS - RESERVED_RESPONSE_TOKENS;
        return Math.max(0, available);
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    @Override
    public String toString() {
        return String.format("TokenBudget[p=%d, c=%d, total=%d, remaining=%d]",
            promptTokens, completionTokens, getTotalTokens(), getRemainingTokens());
    }
}