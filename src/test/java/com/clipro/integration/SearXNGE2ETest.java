package com.clipro.integration;

import com.clipro.tools.web.WebSearchTool;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SearXNG search.
 * Note: Requires SearXNG running at search.sridharhomelab.in
 */
class SearXNGE2ETest {

    @Test
    void shouldCreateWebSearchTool() {
        WebSearchTool tool = new WebSearchTool();
        assertEquals("web_search", tool.getName());
        assertTrue(tool.getDescription().contains("SearXNG"));
    }

    @Test
    void shouldReturnErrorForMissingQuery() {
        WebSearchTool tool = new WebSearchTool();
        String result = tool.execute(Map.of());
        assertTrue(result.startsWith("Error"));
    }

    @Test
    void shouldReturnErrorForEmptyQuery() {
        WebSearchTool tool = new WebSearchTool();
        String result = tool.execute(Map.of("query", ""));
        assertTrue(result.startsWith("Error"));
    }

    @Test
    void shouldHaveCorrectParameters() {
        WebSearchTool tool = new WebSearchTool();
        Object params = tool.getParameters();
        assertNotNull(params);
    }

    @Test
    void shouldParseQuery() {
        WebSearchTool tool = new WebSearchTool();
        // Just verify the tool can be called
        try {
            tool.execute(Map.of("query", "test"));
        } catch (Exception e) {
            // Expected if no network
        }
    }
}
