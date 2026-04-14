package com.clipro.ui.components;

import com.clipro.ui.Terminal;

/**
 * Tool result message display.
 * Shows bash output, tool results, and command outputs.
 */
public class ToolResultMessage {

    private final String toolName;
    private final String output;
    private final long timestamp;
    private boolean truncated;

    public ToolResultMessage(String toolName, String output) {
        this.toolName = toolName;
        this.output = output;
        this.timestamp = System.currentTimeMillis();
        this.truncated = output != null && output.length() > 5000;
    }

    /**
     * Render as ASCII art.
     */
    public String render() {
        StringBuilder sb = new StringBuilder();

        String label = "[" + toolName + "]";
        if (truncated) {
            label += " (truncated)";
        }

        sb.append(Terminal.dim("─── ")).append(label).append(" ───\n");

        String displayOutput = output;
        if (truncated) {
            displayOutput = output.substring(0, 5000) + "\n\n[Output truncated - click to expand]";
        }

        sb.append(displayOutput);
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Render compact version.
     */
    public String renderCompact(int maxLines) {
        if (output == null) return "";

        String[] lines = output.split("\n");
        if (lines.length <= maxLines) {
            return output;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLines; i++) {
            sb.append(lines[i]).append("\n");
        }
        sb.append(Terminal.dim("... [" + (lines.length - maxLines) + " more lines]"));
        return sb.toString();
    }

    public String getToolName() { return toolName; }
    public String getOutput() { return output; }
    public boolean isTruncated() { return truncated; }
}
