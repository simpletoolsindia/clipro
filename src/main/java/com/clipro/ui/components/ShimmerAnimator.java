package com.clipro.ui.components;

/**
 * Animated shimmer effect for rainbow colors.
 * Uses 120ms frame rate for smooth animation.
 *
 * Reference: openclaude/src/components/Spinner.tsx (shimmer logic)
 */
public class ShimmerAnimator {

    /** Frame rate in milliseconds */
    private static final int FRAME_RATE_MS = 120;

    /** Number of frames in a shimmer cycle */
    private static final int CYCLE_LENGTH = 2;

    private final RainbowRenderer renderer;
    private long frameCount = 0;
    private boolean enabled = false;
    private boolean paused = false;

    public ShimmerAnimator() {
        this.renderer = new RainbowRenderer();
    }

    public ShimmerAnimator(RainbowRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Advance one frame (call on each tick).
     */
    public void tick() {
        if (paused) return;

        frameCount++;
        renderer.tickShimmer();
    }

    /**
     * Get the frame rate in ms.
     */
    public int getFrameRateMs() {
        return FRAME_RATE_MS;
    }

    /**
     * Get current frame number.
     */
    public long getFrameCount() {
        return frameCount;
    }

    /**
     * Check if in shimmer phase (odd frame).
     */
    public boolean isShimmerPhase() {
        return frameCount % CYCLE_LENGTH == 1;
    }

    /**
     * Enable/disable shimmer animation.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        renderer.setShimmerEnabled(enabled);
    }

    /**
     * Check if shimmer is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Pause/resume animation.
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * Check if animation is paused.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Render text with shimmer animation.
     */
    public String renderShimmer(String text) {
        return renderer.renderRainbow(text, isShimmerPhase());
    }

    /**
     * Render text with shimmer animation (bold).
     */
    public String renderShimmerBold(String text) {
        return renderer.renderRainbowBold(text, isShimmerPhase());
    }

    /**
     * Render ultrathink keyword with shimmer.
     */
    public String renderUltrathink(String text) {
        return renderer.renderUltrathink(text, isShimmerPhase());
    }

    /**
     * Get the underlying renderer.
     */
    public RainbowRenderer getRenderer() {
        return renderer;
    }

    /**
     * Reset frame counter.
     */
    public void reset() {
        frameCount = 0;
    }

    /**
     * Create a timer-compatible runnable for animation loop.
     */
    public Runnable createTickRunnable() {
        return this::tick;
    }

    /**
     * Get shimmer state for serialization (e.g., save to config).
     */
    public ShimmerState getState() {
        return new ShimmerState(enabled, paused, frameCount);
    }

    /**
     * Restore shimmer state from serialization.
     */
    public void restoreState(ShimmerState state) {
        this.enabled = state.enabled();
        this.paused = state.paused();
        this.frameCount = state.frameCount();
        renderer.setShimmerEnabled(enabled);
    }

    /**
     * Serializable shimmer state record.
     */
    public record ShimmerState(boolean enabled, boolean paused, long frameCount) {}
}
