package com.clipro.ui;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Handles terminal resize events.
 * Reference: openclaude/src/ink/events/
 */
public class TerminalResizeHandler {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread monitorThread;
    private int lastColumns = 0;
    private int lastRows = 0;

    public interface ResizeListener {
        void onResize(int columns, int rows);
    }

    public void start(ResizeListener listener) {
        if (running.get()) return;

        running.set(true);
        lastColumns = Terminal.getColumns();
        lastRows = Terminal.getRows();

        monitorThread = new Thread(() -> {
            while (running.get()) {
                int cols = Terminal.getColumns();
                int rows = Terminal.getRows();

                if (cols != lastColumns || rows != lastRows) {
                    lastColumns = cols;
                    lastRows = rows;
                    listener.onResize(cols, rows);
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "TerminalResizeMonitor");
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    public void stop() {
        running.set(false);
        if (monitorThread != null) {
            monitorThread.interrupt();
        }
    }

    public boolean isRunning() {
        return running.get();
    }
}
