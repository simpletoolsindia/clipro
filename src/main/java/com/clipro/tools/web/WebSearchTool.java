package com.clipro.tools.web;

import com.clipro.llm.LlmHttpClient;
import com.clipro.tools.Tool;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web search tool using SearXNG.
 * Default endpoint: https://search.sridharhomelab.in
 */
public class WebSearchTool implements Tool {

    private static final String DEFAULT_SEARXNG_URL = "https://search.sridharhomelab.in/search";
    private final LlmHttpClient httpClient;

    public WebSearchTool() {
        this(DEFAULT_SEARXNG_URL);
    }

    public WebSearchTool(String searxngUrl) {
        this.httpClient = new LlmHttpClient(URI.create(searxngUrl));
    }

    @Override
    public String getName() {
        return "web_search";
    }

    @Override
    public String getDescription() {
        return "Search the web using SearXNG. Returns title, URL, and snippet.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "query", Map.of(
                    "type", "string",
                    "description", "Search query"
                ),
                "limit", Map.of(
                    "type", "integer",
                    "description", "Maximum results (default: 10)",
                    "default", 10
                )
            ),
            "required", List.of("query")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String query = (String) args.get("query");
        if (query == null || query.isEmpty()) {
            return "Error: query is required";
        }

        int limit = 10;
        Object limitObj = args.get("limit");
        if (limitObj instanceof Number) {
            limit = ((Number) limitObj).intValue();
        }

        try {
            String url = DEFAULT_SEARXNG_URL + "?q=" + encode(query) +
                        "&format=json&engines=wikipedia,github,hackernews&safe_search=1&limit=" + limit;

            HttpResponse<String> response = httpClient.getAsync(url)
                .get();

            if (response.statusCode() == 200) {
                return formatResults(response.body());
            } else {
                return "Error: Search failed with status " + response.statusCode();
            }
        } catch (Exception e) {
            return "Error: Search failed - " + e.getMessage();
        }
    }

    private String formatResults(String json) {
        // Simple formatting - in production, parse JSON properly
        StringBuilder sb = new StringBuilder();
        sb.append("Search Results:\n\n");

        try {
            // Basic JSON parsing for results
            // This is a simplified version
            String[] lines = json.split("\n");
            int count = 0;
            for (String line : lines) {
                if (line.contains("\"url\"") && count < 10) {
                    sb.append("- ").append(line).append("\n");
                    count++;
                }
            }
        } catch (Exception e) {
            return "Error parsing results: " + e.getMessage();
        }

        return sb.length() > 0 ? sb.toString() : "No results found";
    }

    private String encode(String s) {
        return s.replace(" ", "+").replace("&", "%26").replace("?", "%3F");
    }

    public LlmHttpClient getHttpClient() {
        return httpClient;
    }
}