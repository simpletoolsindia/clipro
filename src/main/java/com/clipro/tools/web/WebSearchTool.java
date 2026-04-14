package com.clipro.tools.web;

import com.clipro.llm.LlmHttpClient;
import com.clipro.tools.Tool;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * Web search tool with multi-provider support.
 * M-21: SearchProvider interface with SearXNG, Tavily, Serper providers.
 */
public class WebSearchTool implements Tool {

    // M-21: Search provider interface
    public interface SearchProvider {
        String search(String query, int limit) throws Exception;
        String getName();
        default boolean isAvailable() { return true; }
    }

    // M-21: SearXNG provider (self-hosted)
    private static class SearXNGProvider implements SearchProvider {
        private static final String DEFAULT_URL = "https://search.sridharhomelab.in/search";
        private final LlmHttpClient client;

        SearXNGProvider() {
            this.client = new LlmHttpClient(URI.create(DEFAULT_URL));
        }

        @Override
        public String search(String query, int limit) throws Exception {
            String url = DEFAULT_URL + "?q=" + encode(query) +
                        "&format=json&engines=wikipedia,github,hackernews&safe_search=1&limit=" + limit;
            HttpResponse<String> response = client.getAsync(url).get();
            return formatSearXNGResults(response.body());
        }

        @Override
        public String getName() { return "SearXNG"; }

        private String formatSearXNGResults(String json) {
            StringBuilder sb = new StringBuilder();
            sb.append("Search Results (SearXNG):\n\n");
            try {
                // Simple JSON extraction
                int count = 0;
                String[] lines = json.split("\n");
                for (String line : lines) {
                    if (line.contains("\"title\"") && count < 10) {
                        sb.append("- ").append(line.trim()).append("\n");
                        count++;
                    }
                }
            } catch (Exception e) {
                return "Error parsing results: " + e.getMessage();
            }
            return sb.length() > 0 ? sb.toString() : "No results found";
        }
    }

    // M-21: Tavily provider
    private static class TavilyProvider implements SearchProvider {
        private final String apiKey;
        private final LlmHttpClient client;

        TavilyProvider(String apiKey) {
            this.apiKey = apiKey;
            this.client = new LlmHttpClient(URI.create("https://api.tavily.com"));
        }

        @Override
        public String search(String query, int limit) throws Exception {
            if (apiKey == null || apiKey.isEmpty()) {
                return "Tavily API key not configured";
            }
            // Simplified - would need proper API call
            return "Search Results (Tavily): " + query + "\n[Tavily integration pending]";
        }

        @Override
        public String getName() { return "Tavily"; }

        @Override
        public boolean isAvailable() { return apiKey != null && !apiKey.isEmpty(); }
    }

    // M-21: Serper provider
    private static class SerperProvider implements SearchProvider {
        private final String apiKey;
        private final LlmHttpClient client;

        SerperProvider(String apiKey) {
            this.apiKey = apiKey;
            this.client = new LlmHttpClient(URI.create("https://google.serper.dev/search"));
        }

        @Override
        public String search(String query, int limit) throws Exception {
            if (apiKey == null || apiKey.isEmpty()) {
                return "Serper API key not configured";
            }
            return "Search Results (Serper): " + query + "\n[Serper integration pending]";
        }

        @Override
        public String getName() { return "Serper"; }

        @Override
        public boolean isAvailable() { return apiKey != null && !apiKey.isEmpty(); }
    }

    // Provider management
    private final Map<String, SearchProvider> providers = new HashMap<>();
    private SearchProvider activeProvider;

    public WebSearchTool() {
        // Register default providers
        providers.put("searxng", new SearXNGProvider());
        // Try to load API keys from environment/config
        String tavilyKey = System.getenv("TAVILY_API_KEY");
        String serperKey = System.getenv("SERPER_API_KEY");

        if (tavilyKey != null) {
            providers.put("tavily", new TavilyProvider(tavilyKey));
        }
        if (serperKey != null) {
            providers.put("serper", new SerperProvider(serperKey));
        }

        // Default to SearXNG
        activeProvider = providers.get("searxng");
    }

    /**
     * M-21: Select provider by name.
     */
    public void setProvider(String name) {
        SearchProvider provider = providers.get(name.toLowerCase());
        if (provider != null && provider.isAvailable()) {
            activeProvider = provider;
        }
    }

    /**
     * M-21: List available providers.
     */
    public String listProviders() {
        StringBuilder sb = new StringBuilder("Available Search Providers:\n");
        for (Map.Entry<String, SearchProvider> entry : providers.entrySet()) {
            String marker = entry.getValue() == activeProvider ? " (active)" : "";
            String status = entry.getValue().isAvailable() ? "" : " [unavailable]";
            sb.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue().getName()).append(marker).append(status).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getName() {
        return "web_search";
    }

    @Override
    public String getDescription() {
        return "Search the web. Providers: SearXNG (default), Tavily, Serper. Use /provider <name> to switch.";
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
                    "description", "Maximum results (default: 10)"
                ),
                "provider", Map.of(
                    "type", "string",
                    "description", "Search provider: searxng, tavily, serper"
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

        // M-21: Provider selection
        Object providerObj = args.get("provider");
        if (providerObj instanceof String providerName) {
            setProvider(providerName);
        }

        if (activeProvider == null) {
            return "Error: No search provider available";
        }

        try {
            return activeProvider.search(query, limit);
        } catch (Exception e) {
            return "Error: Search failed - " + e.getMessage();
        }
    }

    private String encode(String s) {
        return s.replace(" ", "+").replace("&", "%26").replace("?", "%3F");
    }

    public LlmHttpClient getHttpClient() {
        return null;
    }
}
