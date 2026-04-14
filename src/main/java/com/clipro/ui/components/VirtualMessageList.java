package com.clipro.ui.components;

import java.util.*;

/**
 * Virtual scrolling message list.
 * Handles large conversation history with O(1) random access.
 */
public class VirtualMessageList {

    private final List<Message> messages = new ArrayList<>();
    private final Map<String, Integer> heightCache = new HashMap<>();
    private final Map<String, String> searchIndex = new HashMap<>();
    private final Map<String, Float> offsets = new HashMap<>();

    private int viewportStart = 0;
    private int viewportHeight = 20;
    private int defaultItemHeight = 3;
    private int overscanRows = 3;

    public VirtualMessageList() {}

    public void addMessage(Message message) {
        messages.add(message);
        searchIndex.put(message.getId(), message.getContent().toLowerCase());
        updateOffsets();
    }

    public void clear() {
        messages.clear();
        searchIndex.clear();
        offsets.clear();
        viewportStart = 0;
    }

    public Message getMessage(int index) {
        if (index < 0 || index >= messages.size()) return null;
        return messages.get(index);
    }

    public int size() { return messages.size(); }

    /**
     * Get visible range indices.
     */
    public int[] getVisibleRange() {
        int start = Math.max(0, viewportStart - overscanRows);
        int visibleCount = viewportHeight + (overscanRows * 2);
        int end = Math.min(messages.size(), start + visibleCount);
        return new int[]{start, end};
    }

    /**
     * Get items for visible viewport.
     */
    public List<Message> getVisibleItems() {
        int[] range = getVisibleRange();
        return messages.subList(range[0], range[1]);
    }

    /**
     * Scroll to specific message index.
     */
    public void scrollToIndex(int index) {
        if (index < 0 || index >= messages.size()) return;
        viewportStart = Math.max(0, index - overscanRows);
    }

    /**
     * Scroll to end (latest messages).
     */
    public void scrollToEnd() {
        viewportStart = Math.max(0, messages.size() - viewportHeight);
    }

    /**
     * Scroll by delta (positive = down, negative = up).
     */
    public void scrollBy(int delta) {
        viewportStart = Math.max(0, viewportStart + delta);
    }

    /**
     * Update item height after rendering.
     */
    public void updateItemHeight(String id, int height) {
        heightCache.put(id, height);
    }

    /**
     * Get cached height for item.
     */
    public int getItemHeight(String id) {
        return heightCache.getOrDefault(id, defaultItemHeight);
    }

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
    public void jumpToSearchResult(int resultIndex) {
        List<Integer> results = searchIndex.values().stream()
            .toList()
            .stream()
            .map(s -> messages.indexOf(messages.stream().filter(m -> m.getContent().toLowerCase().contains(s)).findFirst().orElse(null)))
            .filter(i -> i >= 0)
            .toList();
        if (resultIndex < results.size()) {
            scrollToIndex(results.get(resultIndex));
        }
    }

    private void updateOffsets() {
        float offset = 0;
        for (int i = 0; i < messages.size(); i++) {
            offsets.put(messages.get(i).getId(), offset);
            offset += getItemHeight(messages.get(i).getId());
        }
    }

    public void setViewportHeight(int height) { this.viewportHeight = height; }
    public void setOverscanRows(int rows) { this.overscanRows = rows; }
    public int getViewportStart() { return viewportStart; }
}
