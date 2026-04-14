package com.clipro.ui.components;

/**
 * ReAct step for visualization.
 * Shows: Thought → Action → Observation
 */
public class ReActStep {

    public enum Type {
        THINK("🤔", "Thinking"),
        ACTION("⚡", "Action"),
        OBSERVE("👁", "Observe"),
        RESULT("✅", "Result");

        private final String icon;
        private final String label;

        Type(String icon, String label) {
            this.icon = icon;
            this.label = label;
        }

        public String getIcon() { return icon; }
        public String getLabel() { return label; }
    }

    private final Type type;
    private final String content;
    private final long timestamp;

    public ReActStep(Type type, String content) {
        this.type = type;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public Type getType() { return type; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }

    public String render() {
        return String.format("[%s] %s: %s", type.getIcon(), type.getLabel(), truncate(content, 200));
    }

    private String truncate(String s, int max) {
        if (s == null || s.isEmpty()) return "(empty)";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}