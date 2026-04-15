package com.clipro.tools.web;

import com.clipro.llm.LlmHttpClient;
import com.clipro.tools.Tool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Web search tool using SearXNG (self-hosted search.sridharhomelab.in).
 * M-21: Only SearXNG - Tavily and Serper removed per user request.
 */
public class WebSearchTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DEFAULT_URL = "https://search.sridharhomelab.in/search";
    private static final int DEFAULT_LIMIT = 10;

    private final LlmHttpClient client;

    public WebSearchTool() {
        this.client = new LlmHttpClient(URI.create(DEFAULT_URL));
    }

    @Override
    public String getName() { return "web_search"; }

    @Override
    public String getDescription() {
        return "Search the web using SearXNG (search.sridharhomelab.in). Returns title, URL, and snippet for each result.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "query", Map.of("type", "string", "description", "Search query"),
                "limit", Map.of("type", "integer", "description", "Max results (default: 10)")
            ),
            "required", List.of("query")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String query = (String) args.get("query");
        if (query == null || query.isEmpty()) return "Error: query is required";

        int limit = DEFAULT_LIMIT;
        Object limitObj = args.get("limit");
        if (limitObj instanceof Number) limit = ((Number) limitObj).intValue();

        try {
            return search(query, limit);
        } catch (Exception e) {
            return "Error: Search failed - " + e.getMessage();
        }
    }

    public String search(String query, int limit) throws Exception {
        String url = DEFAULT_URL + "?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) +
                    "&format=json&engines=wikipedia,github,hackernews&safe_search=1&limit=" + limit;
        var response = client.getAsync(url).get();
        return formatResults(response.body(), limit);
    }

    private String formatResults(String json, int limit) {
        StringBuilder sb = new StringBuilder();
        sb.append("Search Results:\n\n");
        try {
            JsonNode root = MAPPER.readTree(json);
            JsonNode results = root.get("results");
            if (results != null && results.isArray()) {
                int count = 0;
                for (JsonNode result : results) {
                    if (count >= limit) break;
                    String title = safeGet(result, "title");
                    String url = safeGet(result, "url");
                    String content = safeGet(result, "content");
                    if (!title.isEmpty()) {
                        sb.append("[").append(++count).append("] ").append(title).append("\n");
                        if (!url.isEmpty()) sb.append("    ").append(url).append("\n");
                        if (!content.isEmpty()) sb.append("    ").append(truncate(content, 200)).append("\n");
                        sb.append("\n");
                    }
                }
                if (count == 0) sb.append("No results found.\n");
            } else {
                sb.append(formatFallback(json, limit));
            }
        } catch (Exception e) {
            sb.append(formatFallback(json, limit));
        }
        return sb.toString();
    }

    private String formatFallback(String json, int limit) {
        StringBuilder sb = new StringBuilder();
        sb.append("Search Results:\n\n");
        try {
            int count = 0;
            int titleIdx = json.indexOf("\"title\"");
            while (titleIdx >= 0 && count < limit) {
                int start = json.indexOf("\"", titleIdx + 7) + 1;
                int end = json.indexOf("\"", start);
                if (start > 0 && end > start) {
                    sb.append("[").append(++count).append("] ")
          .append(json.substring(start, end)).append("\n\n");
                }
                titleIdx = json.indexOf("\"title\"", titleIdx + 1);
            }
            if (count == 0) sb.append("Raw: ").append(truncate(json, 300)).append("\n");
        } catch (Exception e) {
            sb.append("Parse error.\n");
        }
        return sb.toString();
    }

    private String safeGet(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asText().trim() : "";
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }
}
