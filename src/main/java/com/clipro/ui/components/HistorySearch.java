package com.clipro.ui.components;

import java.util.*;

/**
 * History search mode for input field.
 * Supports Ctrl+R reverse search with incremental matching.
 */
public class HistorySearch {

    private boolean searchMode = false;
    private String currentQuery = "";
    private int currentResultIndex = -1;
    private List<String> searchHistory = new ArrayList<>();

    public void enterSearchMode() {
        searchMode = true;
        currentQuery = "";
        currentResultIndex = -1;
    }

    public void exitSearchMode() {
        searchMode = false;
        currentQuery = "";
        currentResultIndex = -1;
    }

    public void setSearchHistory(List<String> history) {
        this.searchHistory = new ArrayList<>(history);
        Collections.reverse(this.searchHistory); // Reverse for newest-first
    }

    public List<String> search(String query, List<String> history) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }

        currentQuery = query.toLowerCase();
        List<String> results = new ArrayList<>();

        for (String entry : history) {
            if (entry.toLowerCase().contains(currentQuery)) {
                results.add(entry);
            }
        }

        return results;
    }

    public String getNext(String query, List<String> history) {
        List<String> results = search(query, history);
        if (results.isEmpty()) return null;

        currentResultIndex = (currentResultIndex + 1) % results.size();
        return results.get(currentResultIndex);
    }

    public String getPrevious(String query, List<String> history) {
        List<String> results = search(query, history);
        if (results.isEmpty()) return null;

        currentResultIndex = (currentResultIndex - 1 + results.size()) % results.size();
        return results.get(currentResultIndex);
    }

    public boolean isSearchMode() { return searchMode; }
    public String getCurrentQuery() { return currentQuery; }
    public int getCurrentResultIndex() { return currentResultIndex; }
}
