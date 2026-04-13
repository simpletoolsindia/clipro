package com.clipro.ui.components;

import com.clipro.ui.Terminal;

/**
 * Renders a message box in the terminal.
 * Reference: openclaude/src/components/Message.tsx
 */
public class MessageBox {
    private static final String USER_COLOR = "\033[36m";    // Cyan
    private static final String ASSISTANT_COLOR = "\033[32m"; // Green
    private static final String SYSTEM_COLOR = "\033[33m";   // Yellow
    private static final String RESET = "\033[0m";

    public static String render(Message message) {
        String roleLabel = getRoleLabel(message.getRole());
        String color = getColor(message.getRole());
        String rolePrefix = color + roleLabel + RESET + " ";

        StringBuilder sb = new StringBuilder();
        sb.append(rolePrefix);

        if (message.isStreaming()) {
            sb.append(renderStreamingContent(message.getContent()));
        } else {
            sb.append(message.getContent());
        }

        return sb.toString();
    }

    public static String renderUser(String content) {
        return render(new Message(MessageRole.USER, content));
    }

    public static String renderAssistant(String content) {
        return render(new Message(MessageRole.ASSISTANT, content));
    }

    public static String renderSystem(String content) {
        return render(new Message(MessageRole.SYSTEM, content));
    }

    private static String getRoleLabel(MessageRole role) {
        return switch (role) {
            case USER -> "[USER]";
            case ASSISTANT -> "[ASSISTANT]";
            case SYSTEM -> "[SYSTEM]";
            case TOOL -> "[TOOL]";
        };
    }

    private static String getColor(MessageRole role) {
        return switch (role) {
            case USER -> USER_COLOR;
            case ASSISTANT -> ASSISTANT_COLOR;
            case SYSTEM -> SYSTEM_COLOR;
            case TOOL -> ASSISTANT_COLOR;
        };
    }

    private static String renderStreamingContent(String content) {
        // Add cursor indicator for streaming
        return content + " \033[7m \033[0m";
    }
}
