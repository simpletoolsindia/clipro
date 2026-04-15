package com.clipro.ui.components;

import java.util.concurrent.atomic.AtomicLong;

/**
 * H-13: Animated shimmer effect for the prompt cursor.
 * Creates a pulsing/shimmer animation at 120ms frame rate.
 */
public class ShimmerAnimator {

    private static final long FRAME_RATE_MS = 120;
    private static final int CYCLE_LENGTH = 8;

    private final AtomicLong frameCount = new AtomicLong(0);
    private volatile boolean enabled = false;
    private volatile boolean paused = false;
    private Thread animationThread;
    private final Object lock = new Object();

    public ShimmerAnimator() {}

    /**
     * Start the shimmer animation.
     */
    public void start() {
        if (enabled) return;
        enabled = true;
        paused = false;
        animationThread = new Thread(() -> {
            while (enabled) {
                try {
                    Thread.sleep(FRAME_RATE_MS);
                    if (!paused) {
                        frameCount.incrementAndGet();
                    }
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }, "shimmer-animator");
        animationThread.setDaemon(true);
        animationThread.start();
    }

    /**
     * Stop the shimmer animation.
     */
    public void stop() {
        enabled = false;
        if (animationThread != null) {
            animationThread.interrupt();
            animationThread = null;
        }
    }

    /**
     * Pause the animation (keeps thread alive).
     */
    public void pause() { paused = true; }

    /**
     * Resume the animation.
     */
    public void resume() { paused = false; }

    /**
     * Get the current frame for rendering.
     */
    public long getFrame() {
        return frameCount.get();
    }

    /**
     * Get shimmer frame for ANSI cursor animation.
     * Returns 0-7 for cycling through animation frames.
     */
    public int getShimmerFrame() {
        return (int) (frameCount.get() % CYCLE_LENGTH);
    }

    /**
     * Get a shimmer block cursor character based on current frame.
     */
    public String getShimmerCursor() {
        int frame = getShimmerFrame();
        return switch (frame) {
            case 0, 4 -> "\u2588";  // Full block
            case 1, 5 -> "\u2589";  // 7/8
            case 2, 6 -> "\u258A";  // 6/8
            case 3, 7 -> "\u258B";  // 5/8
            default -> "\u2588";
        };
    }

    /**
     * Get shimmer animation phase for color cycling.
     * Returns 0.0-1.0 cycling value.
     */
    public double getPhase() {
        return (frameCount.get() % (CYCLE_LENGTH * 4)) / (double) (CYCLE_LENGTH * 4);
    }

    public boolean isEnabled() { return enabled; }
    public boolean isPaused() { return paused; }
}
