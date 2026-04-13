package com.clipro.tools.web;

import com.clipro.llm.LlmHttpClient;
import com.clipro.tools.Tool;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Web fetch tool for retrieving and extracting content from URLs.
 */
public class WebFetchTool implements Tool {

    private static final int MAX_TOKENS = 4000;
    private static final int TIMEOUT_SECONDS = 30;
    private final LlmHttpClient httpClient;

    public WebFetchTool() {
        this.httpClient = new LlmHttpClient(URI.create("https://example.com"));
        this.httpClient.setTimeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS));
    }

    @Override
    public String getName() {
        return "web_fetch";
    }

    @Override
    public String getDescription() {
        return "Fetch and extract content from URLs. Returns markdown with max 4000 tokens.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "url", Map.of(
                    "type", "string",
                    "description", "URL to fetch"
                ),
                "max_tokens", Map.of(
                    "type", "integer",
                    "description", "Maximum tokens to return",
                    "default", MAX_TOKENS
                )
            ),
            "required", List.of("url")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String url = (String) args.get("url");
        if (url == null || url.isEmpty()) {
            return "Error: url is required";
        }

        int maxTokens = MAX_TOKENS;
        Object maxTokensObj = args.get("max_tokens");
        if (maxTokensObj instanceof Number) {
            maxTokens = ((Number) maxTokensObj).intValue();
        }

        try {
            // Use the base URL for the fetch
            URI fetchUri = URI.create(url);
            LlmHttpClient fetchClient = new LlmHttpClient(URI.create(fetchUri.getScheme() + "://" + fetchUri.getHost()));
            fetchClient.setTimeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS));

            HttpResponse<String> response = fetchClient.getAsync("/" + (fetchUri.getPath().isEmpty() ? "" : fetchUri.getPath()))
                .get();

            if (response.statusCode() == 200) {
                String content = stripHtml(response.body());
                return truncate(content, maxTokens);
            } else {
                return "Error: Fetch failed with status " + response.statusCode();
            }
        } catch (Exception e) {
            return "Error: Fetch failed - " + e.getMessage();
        }
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        // Basic HTML stripping
        return html
            .replaceAll("(?s)<script[^>]*>.*?</script>", "")
            .replaceAll("(?s)<style[^>]*>.*?</style>", "")
            .replaceAll("(?s)<nav[^>]*>.*?</nav>", "")
            .replaceAll("(?s)<footer[^>]*>.*?</footer>", "")
            .replaceAll("(?s)<header[^>]*>.*?</header>", "")
            .replaceAll("<[^>]+>", " ")
            .replaceAll("&nbsp;", " ")
            .replaceAll("&lt;", "<")
            .replaceAll("&gt;", ">")
            .replaceAll("&amp;", "&")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private String truncate(String content, int maxTokens) {
        if (content == null) return "";
        // Approximate: 4 chars per token
        int maxChars = maxTokens * 4;
        if (content.length() <= maxChars) {
            return content;
        }
        return content.substring(0, maxChars) + "\n\n[Truncated - content exceeds max tokens]";
    }
}