package com.clipro.ui.components;

import com.clipro.ui.Terminal;

import java.time.format.DateTimeFormatter;

/**
 * Renders a message with timestamp - Pixel-perfect OpenClaude style.
 * Reference: openclaude/src/components/Message.tsx
 */
public class MessageRow {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String render(Message message) {
        String time = message.getTimestamp().format(TIME_FORMAT);
        String timePrefix = Terminal.dim(time + " ");

        return timePrefix + MessageBox.render(message);
    }

    public static String renderWithIndex(int index, Message message) {
        String indexPrefix = Terminal.dim("[" + index + "] ");
        return indexPrefix + MessageBox.render(message);
    }
}
