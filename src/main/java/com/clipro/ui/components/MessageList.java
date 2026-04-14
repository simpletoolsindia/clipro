package com.clipro.ui.components;

import com.clipro.ui.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Scrollable message list with virtualization, height caching, and smooth scroll.
 * Pixel-perfect OpenClaude style.
 *
 * Reference: openclaude/src/components/VirtualMessageList.tsx (1,082 lines)
 */
public class MessageList {
    private final List<Message> messages = new ArrayList<>();
    private final Map<Integer, Integer> heightCache = new HashMap<>();

    private int scrollOffset = 0;         // How many messages scrolled from bottom
    private int viewportHeight = 20;      // Number of visible rows
    private int defaultItemHeight = 5;    // Estimated height for unmeasured items
    private int overscanRows = 3;
    private int currentIndex = 0;         // Focused message index
    private boolean atBottom = true;      // True when scrolled to bottom

    // Smooth scroll state
    private volatile boolean animating = false;
    private ScheduledExecutorService scrollAnimator;
    private int scrollAnimationSteps = 12;

    // New messages pill state
    private int newMessageCount = 0;
    private boolean showNewMessagesPill = false;

    // Sticky header state
    private String stickyHeaderText = null;

    public MessageList() {
        this(100);
    }

    public MessageList(int maxVisible) {
        this.viewportHeight = Math.min(maxVisible, 50);
        this.scrollAnimator = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "scroll-animator");
            t.setDaemon(true);
            return t;
        });
    }

    // -------------------------------------------------------------------------
    // Message Management
    // -------------------------------------------------------------------------

    public void add(Message message) {
        messages.add(message);
        measureAndCacheHeight(messages.size() - 1);

        // If at bottom, auto-scroll to show new message
        if (atBottom) {
            scrollOffset = Math.max(0, messages.size() - 1);
        } else {
            // User has scrolled up — increment new message count for pill
            newMessageCount++;
            showNewMessagesPill = true;
        }
    }

    public void addUser(String content) {
        messages.add(new Message(MessageRole.USER, content));
        measureAndCacheHeight(messages.size() - 1);
    }

    public void addAssistant(String content) {
        messages.add(new Message(MessageRole.ASSISTANT, content));
        measureAndCacheHeight(messages.size() - 1);
    }

    public void addSystem(String content) {
        messages.add(new Message(MessageRole.SYSTEM, content));
        measureAndCacheHeight(messages.size() - 1);
    }

    public Message get(int index) {
        if (index >= 0 && index < messages.size()) {
            return messages.get(index);
        }
        return null;
    }

    public int size() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public void clear() {
        messages.clear();
        heightCache.clear();
        scrollOffset = 0;
        currentIndex = 0;
        atBottom = true;
        newMessageCount = 0;
        showNewMessagesPill = false;
        stickyHeaderText = null;
        stopAnimation();
    }

    // -------------------------------------------------------------------------
    // Height Caching
    // -------------------------------------------------------------------------

    /**
     * Measure rendered height of a message at index.
     */
    private int measureHeight(int index) {
        if (index < 0 || index >= messages.size()) return defaultItemHeight;
        Message msg = messages.get(index);
        String rendered = MessageRow.render(msg);
        int lines = rendered.split("\n").length;
        return Math.max(lines, 1);
    }

    /**
     * Measure and cache height for a message.
     */
    private void measureAndCacheHeight(int index) {
        if (index >= 0 && index < messages.size()) {
            int height = measureHeight(index);
            heightCache.put(index, height);
        }
    }

    /**
     * Get cached height for message at index.
     */
    private int getItemHeight(int index) {
        if (index < 0 || index >= messages.size()) return defaultItemHeight;
        if (heightCache.containsKey(index)) {
            return heightCache.get(index);
        }
        int height = measureHeight(index);
        heightCache.put(index, height);
        return height;
    }

    /**
     * Invalidate height cache for a message.
     */
    public void invalidateHeight(int index) {
        heightCache.remove(index);
    }

    /**
     * Invalidate all height cache.
     */
    public void invalidateAllHeights() {
        heightCache.clear();
    }

    // -------------------------------------------------------------------------
    // Scroll Methods
    // -------------------------------------------------------------------------

    public void scrollUp(int lines) {
        scrollByImmediate(lines);
    }

    public void scrollDown(int lines) {
        scrollByImmediate(-lines);
    }

    /**
     * Scroll up (show older messages).
     */
    public void scrollByImmediate(int lines) {
        int maxOffset = Math.max(0, messages.size() - 1);
        scrollOffset = Math.max(0, Math.min(scrollOffset + lines, maxOffset));
        updateAtBottom();
        updateStickyHeader();
    }

    /**
     * Scroll up with smooth animation.
     */
    public void scrollUpSmooth(int lines) {
        smoothScroll(scrollOffset + lines);
    }

    /**
     * Scroll with animation to target offset.
     */
    public void smoothScroll(int targetOffset) {
        if (messages.isEmpty()) return;
        stopAnimation();
        int maxOffset = Math.max(0, messages.size() - 1);
        targetOffset = Math.max(0, Math.min(targetOffset, maxOffset));
        final int target = targetOffset;
        animating = true;

        int delta = target - scrollOffset;
        if (delta == 0) return;

        final int stepSize = Math.max(1, Math.abs(delta) / scrollAnimationSteps);
        final int[] currentOffset = {scrollOffset};

        scrollAnimator.scheduleAtFixedRate(() -> {
            try {
                if (!animating) return;
                int remaining = target - currentOffset[0];
                if (remaining == 0) {
                    stopAnimation();
                    return;
                }
                int step = Math.abs(remaining) <= stepSize ? remaining : (remaining > 0 ? stepSize : -stepSize);
                currentOffset[0] = Math.max(0, Math.min(currentOffset[0] + step, maxOffset));
                final int newOffset = currentOffset[0];
                scrollOffset = newOffset;
                updateAtBottom();
                updateStickyHeader();
            } catch (Exception ignored) {}
        }, 0, 20, TimeUnit.MILLISECONDS);

        scrollAnimator.schedule(() -> {
            scrollOffset = target;
            stopAnimation();
            updateAtBottom();
            updateStickyHeader();
        }, scrollAnimationSteps * 25, TimeUnit.MILLISECONDS);
    }

    public void scrollToTop() {
        stopAnimation();
        scrollOffset = 0;
        currentIndex = 0;
        atBottom = false;
        stickyHeaderText = null;
    }

    public void scrollToBottom() {
        stopAnimation();
        scrollOffset = Math.max(0, messages.size() - 1);
        currentIndex = messages.size() - 1;
        atBottom = true;
        stickyHeaderText = null;
    }

    /**
     * Smooth scroll to bottom.
     */
    public void scrollToBottomSmooth() {
        smoothScroll(Math.max(0, messages.size() - 1));
        atBottom = true;
    }

    public void scrollToIndex(int index) {
        if (index < 0 || index >= messages.size()) return;
        stopAnimation();
        int maxOffset = Math.max(0, messages.size() - 1);
        scrollOffset = Math.max(0, Math.min(index, maxOffset));
        currentIndex = index;
        updateAtBottom();
        updateStickyHeader();
    }

    /**
     * Smooth scroll to message index.
     */
    public void scrollToIndexSmooth(int index, int frames) {
        if (index < 0 || index >= messages.size()) return;
        int maxOffset = Math.max(0, messages.size() - 1);
        int target = Math.max(0, Math.min(index, maxOffset));
        smoothScroll(target);
        currentIndex = index;
    }

    public void scrollPageUp() {
        scrollByImmediate(-(viewportHeight - 2));
    }

    public void scrollPageDown() {
        scrollByImmediate(viewportHeight - 2);
    }

    public void stopAnimation() {
        animating = false;
    }

    private void updateAtBottom() {
        atBottom = (scrollOffset >= messages.size() - 1);
        if (atBottom) {
            newMessageCount = 0;
            showNewMessagesPill = false;
        }
    }

    private void updateStickyHeader() {
        if (scrollOffset > 0 && messages.size() > 0) {
            int bottomIndex = messages.size() - 1 - scrollOffset;
            if (bottomIndex > 0 && bottomIndex < messages.size()) {
                Message msg = messages.get(bottomIndex);
                if (msg.getRole() == MessageRole.USER) {
                    String content = msg.getContent();
                    stickyHeaderText = "User: " + truncate(content, 50);
                }
            }
        } else {
            stickyHeaderText = null;
        }
    }

    // -------------------------------------------------------------------------
    // Keyboard Navigation
    // -------------------------------------------------------------------------

    /**
     * Handle keyboard key for scroll navigation.
     * @param key the key string
     * @return true if handled
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
                scrollToBottom();
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

    public void navigateDown(int count) {
        if (messages.isEmpty()) return;
        int bottomIndex = messages.size() - 1 - scrollOffset;
        int newIndex = Math.min(bottomIndex + count, messages.size() - 1);
        if (newIndex != bottomIndex) {
            scrollToIndex(messages.size() - 1 - newIndex + scrollOffset);
            currentIndex = newIndex;
        }
    }

    public void navigateUp(int count) {
        if (messages.isEmpty()) return;
        int bottomIndex = messages.size() - 1 - scrollOffset;
        int newIndex = Math.max(bottomIndex - count, 0);
        if (newIndex != bottomIndex) {
            scrollToIndex(messages.size() - 1 - newIndex + scrollOffset);
            currentIndex = newIndex;
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int getScrollOffset() {
        return scrollOffset;
    }

    public boolean hasMoreAbove() {
        return scrollOffset > 0;
    }

    public boolean hasMoreBelow() {
        return scrollOffset < Math.max(0, messages.size() - 1);
    }

    public boolean isAtBottom() {
        return atBottom;
    }

    public boolean isAnimating() {
        return animating;
    }

    public int getNewMessageCount() {
        return newMessageCount;
    }

    public boolean shouldShowNewMessagesPill() {
        return showNewMessagesPill && newMessageCount > 0;
    }

    /**
     * Dismiss new messages pill and scroll to bottom.
     */
    public void dismissNewMessagesPill() {
        scrollToBottom();
        newMessageCount = 0;
        showNewMessagesPill = false;
    }

    public String getStickyHeaderText() {
        return stickyHeaderText;
    }

    public boolean hasStickyHeader() {
        return stickyHeaderText != null;
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    public List<Message> getVisibleMessages() {
        int start = Math.max(0, messages.size() - viewportHeight - scrollOffset);
        int end = messages.size() - scrollOffset;
        if (end > start) {
            return new ArrayList<>(messages.subList(start, Math.min(end, messages.size())));
        }
        return new ArrayList<>();
    }

    public String render() {
        StringBuilder sb = new StringBuilder();

        // Sticky header (when scrolled up)
        if (hasStickyHeader()) {
            sb.append(Terminal.dim("┌─ ")).append(stickyHeaderText)
              .append(" ─").append(Terminal.repeat("─", Math.max(0, Terminal.getColumns() - stickyHeaderText.length() - 7)))
              .append("┐\n");
        }

        // Scroll indicator at top
        if (hasMoreAbove()) {
            int count = getScrollIndicatorCount();
            sb.append(Terminal.dim("  ↑ " + count + " more ↑\n"));
        }

        // Visible messages
        List<Message> visible = getVisibleMessages();
        for (int i = 0; i < visible.size(); i++) {
            Message msg = visible.get(i);
            int index = messages.indexOf(msg);
            sb.append(MessageRow.renderWithIndex(index + 1, msg));
            if (i < visible.size() - 1) {
                sb.append("\n");
            }
        }

        // Scroll indicator at bottom
        if (hasMoreBelow()) {
            sb.append("\n").append(Terminal.dim("  ↓ scroll up to see more ↓"));
        }

        // New messages pill
        if (shouldShowNewMessagesPill()) {
            sb.append("\n");
            int pillWidth = Math.min(40, Terminal.getColumns() - 4);
            sb.append(Terminal.BORDER_BL);
            for (int i = 0; i < pillWidth; i++) sb.append(Terminal.BORDER_H);
            sb.append(Terminal.BORDER_BR).append("\n");
            sb.append(Terminal.BORDER_V).append(" ");
            sb.append(Terminal.brightCyan("↓ " + newMessageCount + " new message" + (newMessageCount > 1 ? "s" : "")));
            sb.append(Terminal.dim(" [click or j to scroll]"));
            sb.append(Terminal.padRight("", pillWidth - 22 - String.valueOf(newMessageCount).length()));
            sb.append(" ").append(Terminal.BORDER_V).append("\n");
            sb.append(Terminal.BORDER_BL);
            for (int i = 0; i < pillWidth; i++) sb.append(Terminal.BORDER_H);
            sb.append(Terminal.BORDER_BR);
        }

        return sb.toString();
    }

    private int getScrollIndicatorCount() {
        return Math.max(0, scrollOffset);
    }

    public String renderAll() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            sb.append(MessageRow.renderWithIndex(i + 1, messages.get(i)));
            if (i < messages.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setViewportHeight(int height) {
        this.viewportHeight = Math.max(1, height);
    }

    public void setScrollOffset(int offset) {
        this.scrollOffset = Math.max(0, offset);
        updateAtBottom();
        updateStickyHeader();
    }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    public void shutdown() {
        stopAnimation();
        scrollAnimator.shutdown();
        try {
            scrollAnimator.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) return text;
        return text.substring(0, Math.max(0, maxLen - 3)) + "...";
    }
}
