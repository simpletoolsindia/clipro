package com.clipro.ui.components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarkdownRendererTest {

    @Test
    void shouldReturnEmptyForNull() {
        assertEquals("", MarkdownRenderer.render(null));
    }

    @Test
    void shouldReturnEmptyForBlank() {
        assertEquals("", MarkdownRenderer.render(""));
    }

    @Test
    void shouldHandleBlankInput() {
        // Blank/whitespace input returns as-is (trimming is optional)
        String result = MarkdownRenderer.render("   ");
        assertNotNull(result);
        assertFalse(result.contains("**")); // No markdown processing needed
    }

    @Test
    void shouldRenderBold() {
        String result = MarkdownRenderer.render("Hello **world**");
        assertTrue(result.contains("world"));
        assertTrue(result.contains("\033[1m"));
    }

    @Test
    void shouldRenderItalic() {
        String result = MarkdownRenderer.render("Hello *world*");
        assertTrue(result.contains("world"));
        assertTrue(result.contains("\033[3m"));
    }

    @Test
    void shouldRenderInlineCode() {
        String result = MarkdownRenderer.render("Use `println` for output");
        assertTrue(result.contains("println"));
        assertTrue(result.contains("\033[40m")); // Code background
    }

    @Test
    void shouldRenderLinks() {
        String result = MarkdownRenderer.render("Check [this](https://example.com)");
        assertTrue(result.contains("this"));
        assertTrue(result.contains("https://example.com"));
    }

    @Test
    void shouldRenderHeaders() {
        String result = MarkdownRenderer.render("# Header");
        assertTrue(result.contains("Header"));
        assertTrue(result.contains("\033[1m"));
    }

    @Test
    void shouldRenderCodeBlock() {
        String code = "def hello():\n    print('hi')";
        String result = MarkdownRenderer.renderCodeBlock(code);
        assertTrue(result.contains("def hello()"));
        assertTrue(result.contains("┌"));
        assertTrue(result.contains("└"));
    }

    @Test
    void shouldRenderCodeBlockWithLanguage() {
        String code = "function hi() {}";
        String result = MarkdownRenderer.renderCodeBlock(code, "javascript");
        assertTrue(result.contains("javascript"));
    }

    @Test
    void shouldWrapInBox() {
        String content = "Hello\nWorld";
        String result = MarkdownRenderer.wrapInBox(content, "Test");
        assertTrue(result.contains("┌"));
        assertTrue(result.contains("┐"));
        assertTrue(result.contains("└"));
        assertTrue(result.contains("┘"));
        assertTrue(result.contains("Test"));
    }

    @Test
    void shouldPreservePlainText() {
        String plain = "Hello world, no formatting here";
        assertEquals(plain, MarkdownRenderer.render(plain));
    }
}
