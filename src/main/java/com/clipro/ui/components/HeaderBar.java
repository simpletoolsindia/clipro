package com.clipro.ui.components;

import com.clipro.ui.Terminal;

/**
 * Header bar showing model info and status.
 * Reference: openclaude/src/components/Stats.tsx
 */
public class HeaderBar {
    private String modelName = "qwen3-coder:32b";
    private String status = "Ready";
    private boolean connected = false;

    public HeaderBar() {}

    public HeaderBar(String modelName) {
        this.modelName = modelName;
    }

    public void setModel(String model) {
        this.modelName = model;
    }

    public String getModel() {
        return modelName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

    public String render() {
        String modelDisplay = Terminal.cyan(modelName);
        String statusIcon = connected ? Terminal.green("●") : Terminal.red("○");
        String statusText = connected ? "Connected" : "Disconnected";

        int width = Terminal.getColumns();
        String title = " CLIPRO ";
        int padding = width - title.length() - modelDisplay.length() - statusText.length() - 10;

        StringBuilder sb = new StringBuilder();
        sb.append(Terminal.ansi("48;5;235")); // Dark background
        sb.append(Terminal.bold(title));
        sb.append(" ".repeat(Math.max(1, padding)));
        sb.append(statusIcon).append(" ").append(statusText).append(" | ");
        sb.append(modelDisplay);
        sb.append(Terminal.ansi("0m"));

        return sb.toString();
    }

    public String renderCompact() {
        return Terminal.bold("[CLIPRO] ") +
               (connected ? Terminal.green("●") : Terminal.red("○")) +
               " " + modelName +
               " | " + status;
    }

    public static String divider() {
        int width = Terminal.getColumns();
        return Terminal.ansi("90m") + "─".repeat(Math.max(1, width)) + Terminal.ansi("0m");
    }
}
