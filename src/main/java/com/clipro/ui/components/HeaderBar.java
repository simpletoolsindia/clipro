package com.clipro.ui.components;

import com.clipro.ui.Terminal;

/**
 * Header bar - Pixel-perfect OpenClaude style.
 * Reference: ~/openclaude/src/components/Stats.tsx
 */
public class HeaderBar {
    private String modelName = "qwen3-coder:32b";
    private String status = "Ready";
    private boolean connected = false;
    private String vimMode = "";

    public HeaderBar() {}

    public HeaderBar(String modelName) {
        this.modelName = modelName;
    }

    public void setModel(String model) { this.modelName = model; }
    public String getModel() { return modelName; }
    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }
    public void setConnected(boolean connected) { this.connected = connected; }
    public boolean isConnected() { return connected; }
    public void setVimMode(String mode) { this.vimMode = mode; }

    public String render() {
        int width = Terminal.getColumns();
        StringBuilder sb = new StringBuilder();

        // Top border
        sb.append(Terminal.boxTop(width)).append("\n");

        // Title row
        String title = Terminal.bold(Terminal.brightCyan(" CLIPRO "));
        String connStatus = connected ? Terminal.green("●") : Terminal.red("○");
        String connText = connected ? "Connected" : "Disconnected";
        String modelText = Terminal.cyan(modelName);

        // Status row
        String row1 = title + "  " + connStatus + " " + connText;
        String row2 = modelText;
        if (!vimMode.isEmpty()) {
            row2 += " " + Terminal.dim("[" + vimMode + "]");
        }

        sb.append(Terminal.BORDER_V).append(" ").append(row1);
        int padding = width - row1.length() - row2.length() - 4;
        sb.append(" ".repeat(Math.max(1, padding)));
        sb.append(row2).append(" ").append(Terminal.BORDER_V).append("\n");

        // Bottom border
        sb.append(Terminal.boxBottom(width));

        return sb.toString();
    }

    public static String divider() {
        return Terminal.CYAN + Terminal.repeat(Terminal.BORDER_H, Terminal.getColumns()) + Terminal.RESET;
    }

    public String renderCompact() {
        String conn = connected ? Terminal.green("●") : Terminal.red("○");
        String mode = vimMode.isEmpty() ? "" : Terminal.dim("[" + vimMode + "]");
        return Terminal.bold(Terminal.brightCyan("[CLIPRO]")) + " " + conn + " " + modelName + " " + mode;
    }
}