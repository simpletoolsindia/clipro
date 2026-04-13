package com.clipro.tools.web;

import com.clipro.tools.Tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Quick fetch tool for fast URL content retrieval.
 * Simpler and faster than WebFetchTool with shorter timeout.
 */
public class QuickFetchTool implements Tool {

    private static final int TIMEOUT_SECONDS = 10;
    private static final int MAX_CHARS = 2000;
    private final HttpClient httpClient;

    public QuickFetchTool() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();
    }

    @Override
    public String getName() {
        return "quick_fetch";
    }

    @Override
    public String getDescription() {
        return "Quick fetch of URL content with short timeout. Max 2000 chars.";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "url", Map.of(
                    "type", "string",
                    "description", "URL to fetch"
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

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String content = stripHtml(response.body());
                if (content.length() > MAX_CHARS) {
                    return content.substring(0, MAX_CHARS) + "\n\n[Truncated]";
                }
                return content;
            } else {
                return "Error: Fetch failed with status " + response.statusCode();
            }
        } catch (Exception e) {
            return "Error: Fetch failed - " + e.getMessage();
        }
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html
            .replaceAll("<[^>]+>", "")
            .replaceAll("&nbsp;", " ")
            .replaceAll("&amp;", "&")
            .replaceAll("\\s+", " ")
            .trim();
    }
}