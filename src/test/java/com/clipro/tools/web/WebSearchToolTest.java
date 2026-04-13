package com.clipro.tools.web;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WebSearchTool.
 */
class WebSearchToolTest {

    @Test
    void shouldHaveCorrectName() {
        WebSearchTool tool = new WebSearchTool();
        assertEquals("web_search", tool.getName());
    }

    @Test
    void shouldHaveCorrectDescription() {
        WebSearchTool tool = new WebSearchTool();
        assertNotNull(tool.getDescription());
        assertTrue(tool.getDescription().contains("SearXNG"));
    }

    @Test
    void shouldHaveParameters() {
        WebSearchTool tool = new WebSearchTool();
        Object params = tool.getParameters();
        assertNotNull(params);
    }

    @Test
    void shouldReturnErrorForMissingQuery() {
        WebSearchTool tool = new WebSearchTool();
        String result = tool.execute(Map.of());
        assertTrue(result.startsWith("Error: query is required"));
    }

    @Test
    void shouldReturnErrorForEmptyQuery() {
        WebSearchTool tool = new WebSearchTool();
        String result = tool.execute(Map.of("query", ""));
        assertTrue(result.startsWith("Error: query is required"));
    }
}