package com.clipro.ui.components;

import com.clipro.ui.Terminal;

/**
 * Thinking/thinking block display.
 * Shows reasoning steps from the agent's ReAct loop.
 */
public class ThinkingMessage {

    private final String content;
    private boolean expanded;
    private long timestamp;

    public ThinkingMessage(String content) {
        this.content = content;
        this.expanded = false;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Render as collapsible thinking block.
     */
    public String render() {
        StringBuilder sb = new StringBuilder();

        if (expanded) {
            // Show full thinking
            sb.append(Terminal.dim("┌─ Thinking ─┐\n"));
            sb.append(content);
            sb.append("\n");
            sb.append(Terminal.dim("└────────────┘\n"));
        } else {
            // Show preview
            String preview = getPreview();
            sb.append(Terminal.dim("[Thinking: ")).append(preview).append("]");
        }

        return sb.toString();
    }

    /**
     * Get a short preview of the thinking content.
     */
    public String getPreview() {
        if (content == null) return "";

        String[] lines = content.split("\n");
        if (lines.length == 0) return "";

        // Get first meaningful line
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                return truncate(trimmed, 50);
            }
        }
        return "...";
    }

    /**
     * Toggle expanded state.
     */
    public void toggle() {
        expanded = !expanded;
    }

    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
    public String getContent() { return content; }

    private String truncate(String s, int max) {
        if (s.length() <= max) return s;
        return s.substring(0, max - 2) + "..";
    }
}
