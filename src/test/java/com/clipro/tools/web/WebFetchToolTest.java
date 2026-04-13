package com.clipro.tools.web;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WebFetchTool.
 */
class WebFetchToolTest {

    @Test
    void shouldHaveCorrectName() {
        WebFetchTool tool = new WebFetchTool();
        assertEquals("web_fetch", tool.getName());
    }

    @Test
    void shouldHaveCorrectDescription() {
        WebFetchTool tool = new WebFetchTool();
        assertNotNull(tool.getDescription());
        assertTrue(tool.getDescription().contains("markdown"));
    }

    @Test
    void shouldReturnErrorForMissingUrl() {
        WebFetchTool tool = new WebFetchTool();
        String result = tool.execute(Map.of());
        assertTrue(result.startsWith("Error: url is required"));
    }

    @Test
    void shouldReturnErrorForEmptyUrl() {
        WebFetchTool tool = new WebFetchTool();
        String result = tool.execute(Map.of("url", ""));
        assertTrue(result.startsWith("Error: url is required"));
    }
}