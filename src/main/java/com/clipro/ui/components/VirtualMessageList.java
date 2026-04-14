package com.clipro.ui.components;

import java.util.*;
import java.util.concurrent.*;

/**
 * Virtual scrolling message list with height caching and smooth scroll.
 * Handles large conversation history with O(1) random access.
 *
 * Reference: openclaude/src/components/VirtualMessageList.tsx (1,082 lines)
 */
public class VirtualMessageList {

    private final List<Message> messages = new ArrayList<>();
    private final Map<String, Integer> heightCacheById = new HashMap<>();
    private final Map<Integer, Integer> heightCacheByIndex = new HashMap<>();
    private final Map<String, String> searchIndex = new HashMap<>();

    private int viewportStart = 0;         // Index of first visible message
    private int viewportHeight = 20;      // Number of visible rows
    private int defaultItemHeight = 5;     // Estimated height for unmeasured items
    private int overscanRows = 3;
    private int currentIndex = 0;         // Current focused message (for j/k nav)
    private boolean atBottom = true;      // True when scrolled to bottom

    // Smooth scroll state
    private volatile int targetScrollY = 0;
    private volatile int currentScrollY = 0;
    private volatile boolean animating = false;
    private ScheduledExecutorService scrollAnimator;
    private int scrollAnimationSteps = 12;  // ~240ms at 50ms intervals

    // Keyboard navigation
    private int navigationMode = NAV_NONE;
    private static final int NAV_NONE = 0;
    private static final int NAV_J_K = 1;

    public VirtualMessageList() {
        this.scrollAnimator = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "scroll-animator");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Add a message and measure its height.
     */
    public void addMessage(Message message) {
        messages.add(message);
        searchIndex.put(message.getId(), message.getContent().toLowerCase());

        // Measure and cache height for the new message
        int index = messages.size() - 1;
        int height = measureHeight(index);
        heightCacheByIndex.put(index, height);
        heightCacheById.put(message.getId(), height);

        // Auto-scroll to bottom when message is added
        scrollToEnd();
    }

    /**
     * Measure the rendered height of a message at given index.
     * Counts newlines in the rendered output.
     */
    public int measureHeight(int index) {
        if (index < 0 || index >= messages.size()) {
            return defaultItemHeight;
        }
        Message msg = messages.get(index);
        String rendered = MessageRow.render(msg);
        int lines = rendered.split("\n").length;
        return Math.max(lines, 1);
    }

    /**
     * Measure height and cache it.
     */
    public void measureAndCacheHeight(int index) {
        if (index >= 0 && index < messages.size()) {
            int height = measureHeight(index);
            Message msg = messages.get(index);
            heightCacheByIndex.put(index, height);
            heightCacheById.put(msg.getId(), height);
        }
    }

    /**
     * Invalidate height cache when a message changes.
     */
    public void invalidateHeight(int index) {
        if (index >= 0 && index < messages.size()) {
            Message msg = messages.get(index);
            heightCacheById.remove(msg.getId());
            heightCacheByIndex.remove(index);
        }
    }

    /**
     * Invalidate all height cache (e.g., after theme change).
     */
    public void invalidateAllHeights() {
        heightCacheById.clear();
        heightCacheByIndex.clear();
    }

    public void clear() {
        messages.clear();
        searchIndex.clear();
        heightCacheById.clear();
        heightCacheByIndex.clear();
        viewportStart = 0;
        currentIndex = 0;
        atBottom = true;
        stopAnimation();
    }

    public Message getMessage(int index) {
        if (index < 0 || index >= messages.size()) return null;
        return messages.get(index);
    }

    public int size() {
        return messages.size();
    }

    // -------------------------------------------------------------------------
    // Scroll Methods
    // -------------------------------------------------------------------------

    /**
     * Get visible range of message indices.
     */
    public int[] getVisibleRange() {
        int start = Math.max(0, viewportStart - overscanRows);
        int end = Math.min(messages.size(), start + viewportHeight + overscanRows * 2);
        return new int[]{start, end};
    }

    /**
     * Get visible messages.
     */
    public List<Message> getVisibleItems() {
        int[] range = getVisibleRange();
        if (range[0] >= range[1]) return Collections.emptyList();
        return new ArrayList<>(messages.subList(range[0], range[1]));
    }

    /**
     * Get visible range as message indices (0-based).
     */
    public int getVisibleStartIndex() {
        return Math.max(0, viewportStart - overscanRows);
    }

    public int getVisibleEndIndex() {
        return Math.min(messages.size(), viewportStart + viewportHeight + overscanRows);
    }

    /**
     * Scroll to specific message index.
     */
    public void scrollToIndex(int index) {
        if (index < 0 || index >= messages.size()) return;
        stopAnimation();
        viewportStart = Math.max(0, index - overscanRows);
        currentIndex = index;
        updateAtBottom();
    }

    /**
     * Scroll to specific message with smooth animation.
     * @param index target message index
     * @param frames number of animation frames (default: 12)
     */
    public void scrollToIndexSmooth(int index, int frames) {
        if (index < 0 || index >= messages.size()) return;
        if (messages.size() == 0) return;
        stopAnimation();

        int target = Math.max(0, index - overscanRows);
        targetScrollY = target;
        animating = true;

        int steps = frames > 0 ? frames : scrollAnimationSteps;
        int delta = target - viewportStart;
        int stepSize = Math.max(1, Math.abs(delta) / steps);

        scrollAnimator.scheduleAtFixedRate(() -> {
            try {
                if (!animating) return;

                int remaining = targetScrollY - viewportStart;
                if (remaining == 0) {
                    stopAnimation();
                    return;
                }

                if (Math.abs(remaining) <= stepSize) {
                    viewportStart = targetScrollY;
                } else {
                    viewportStart += (remaining > 0 ? stepSize : -stepSize);
                }

                viewportStart = Math.max(0, Math.min(viewportStart, messages.size() - 1));
                updateAtBottom();
            } catch (Exception ignored) {}
        }, 0, 20, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Auto-stop after max duration
        scrollAnimator.schedule(() -> stopAnimation(), steps * 25, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Smooth scroll to end (latest messages).
     */
    public void scrollToBottomSmooth() {
        scrollToIndexSmooth(Math.max(0, messages.size() - 1), scrollAnimationSteps);
        atBottom = true;
    }

    /**
     * Instant scroll to end.
     */
    public void scrollToEnd() {
        stopAnimation();
        viewportStart = Math.max(0, messages.size() - 1);
        currentIndex = messages.size() - 1;
        atBottom = true;
    }

    /**
     * Scroll by delta (positive = down, negative = up).
     * Smooth version animates the scroll.
     */
    public void scrollBy(int delta) {
        stopAnimation();
        scrollByImmediate(delta);
    }

    /**
     * Immediate scroll by delta (no animation).
     */
    public void scrollByImmediate(int delta) {
        viewportStart = Math.max(0, Math.min(viewportStart + delta, messages.size() - 1));
        updateAtBottom();
    }

    /**
     * Scroll up one screen height.
     */
    public void scrollPageUp() {
        scrollByImmediate(-(viewportHeight - 2));
    }

    /**
     * Scroll down one screen height.
     */
    public void scrollPageDown() {
        scrollByImmediate(viewportHeight - 2);
    }

    /**
     * Stop any ongoing scroll animation.
     */
    public void stopAnimation() {
        animating = false;
    }

    /**
     * Update whether we're at the bottom.
     */
    private void updateAtBottom() {
        atBottom = (viewportStart >= messages.size() - viewportHeight);
    }

    // -------------------------------------------------------------------------
    // Keyboard Navigation (j/k)
    // -------------------------------------------------------------------------

    /**
     * Handle keyboard navigation.
     * @param key the key pressed
     * @return true if the key was handled, false otherwise
     */
    public boolean handleKey(String key) {
        switch (key) {
            case "j":
            case "KEY_DOWN":
                navigateDown(1);
                return true;
            case "k":
            case "KEY_UP":
                navigateUp(1);
                return true;
            case "g":
                scrollToTop();
                return true;
            case "G":
                scrollToEnd();
                return true;
            case " ":
            case "PAGE_DOWN":
                scrollPageDown();
                return true;
            case "PAGE_UP":
                scrollPageUp();
                return true;
            default:
                return false;
        }
    }

    /**
     * Navigate down to newer messages.
     */
    public void navigateDown(int count) {
        if (messages.isEmpty()) return;
        int newIndex = Math.min(currentIndex + count, messages.size() - 1);
        if (newIndex != currentIndex) {
            currentIndex = newIndex;
            // If navigating past visible area, scroll
            if (currentIndex > viewportStart + viewportHeight - 2) {
                scrollToIndex(currentIndex);
            }
        }
    }

    /**
     * Navigate up to older messages.
     */
    public void navigateUp(int count) {
        if (messages.isEmpty()) return;
        int newIndex = Math.max(currentIndex - count, 0);
        if (newIndex != currentIndex) {
            currentIndex = newIndex;
            // If navigating past visible area, scroll
            if (currentIndex < viewportStart) {
                scrollToIndex(currentIndex);
            }
        }
    }

    /**
     * Scroll to top.
     */
    public void scrollToTop() {
        stopAnimation();
        viewportStart = 0;
        currentIndex = 0;
        atBottom = false;
    }

    // -------------------------------------------------------------------------
    // Height Accessors
    // -------------------------------------------------------------------------

    /**
     * Get cached height for message at index.
     */
    public int getItemHeight(int index) {
        if (heightCacheByIndex.containsKey(index)) {
            return heightCacheByIndex.get(index);
        }
        return defaultItemHeight;
    }

    /**
     * Get cached height for message by ID.
     */
    public int getItemHeightById(String id) {
        if (heightCacheById.containsKey(id)) {
            return heightCacheById.get(id);
        }
        return defaultItemHeight;
    }

    /**
     * Calculate total scrollable height.
     */
    public int getTotalHeight() {
        int total = 0;
        for (int i = 0; i < messages.size(); i++) {
            total += getItemHeight(i);
        }
        return total;
    }

    /**
     * Get height from index to end.
     */
    public int getHeightFromIndex(int index) {
        int total = 0;
        for (int i = index; i < messages.size(); i++) {
            total += getItemHeight(i);
        }
        return total;
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    /**
     * Search messages by content.
     */
    public List<Integer> search(String query) {
        List<Integer> results = new ArrayList<>();
        String lower = query.toLowerCase();
        for (int i = 0; i < messages.size(); i++) {
            String content = searchIndex.get(messages.get(i).getId());
            if (content != null && content.contains(lower)) {
                results.add(i);
            }
        }
        return results;
    }

    /**
     * Jump to search result.
     */
    public void jumpToSearchResult(int resultIndex, List<Integer> results) {
        if (resultIndex >= 0 && resultIndex < results.size()) {
            scrollToIndexSmooth(results.get(resultIndex), scrollAnimationSteps);
        }
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setViewportHeight(int height) {
        this.viewportHeight = Math.max(1, height);
    }

    public void setOverscanRows(int rows) {
        this.overscanRows = Math.max(1, rows);
    }

    public void setDefaultItemHeight(int height) {
        this.defaultItemHeight = Math.max(1, height);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int getViewportStart() {
        return viewportStart;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isAtBottom() {
        return atBottom;
    }

    public boolean isAnimating() {
        return animating;
    }

    /**
     * Get navigation mode.
     */
    public int getNavigationMode() {
        return navigationMode;
    }

    /**
     * Set navigation mode.
     */
    public void setNavigationMode(int mode) {
        this.navigationMode = mode;
    }

    /**
     * Get message index at given pixel offset from top.
     */
    public int getIndexAtOffset(int pixelOffset) {
        int offset = 0;
        for (int i = 0; i < messages.size(); i++) {
            offset += getItemHeight(i);
            if (offset > pixelOffset) {
                return i;
            }
        }
        return Math.max(0, messages.size() - 1);
    }

    /**
     * Shutdown the scroll animator thread pool.
     */
    public void shutdown() {
        stopAnimation();
        scrollAnimator.shutdown();
        try {
            scrollAnimator.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
    }
}
