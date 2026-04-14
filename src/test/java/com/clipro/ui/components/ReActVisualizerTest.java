package com.clipro.ui.components;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReActVisualizerTest {

    @Test
    void shouldCreateVisualizer() {
        ReActVisualizer viz = new ReActVisualizer();
        assertNotNull(viz);
        assertTrue(viz.getSteps().isEmpty());
    }

    @Test
    void shouldAddSteps() {
        ReActVisualizer viz = new ReActVisualizer();
        viz.addThink("I should read the file first");
        viz.addAction("file_read(path=/home/test.txt)");
        viz.addObserve("File contains 100 lines");

        assertEquals(3, viz.getSteps().size());
    }

    @Test
    void shouldRenderSteps() {
        ReActVisualizer viz = new ReActVisualizer();
        viz.addThink("Test thought");
        viz.addAction("test_action()");

        String output = viz.render();
        assertNotNull(output);
        assertTrue(output.contains("Test thought"));
        assertTrue(output.contains("🤔"));
        assertTrue(output.contains("⚡"));
    }

    @Test
    void shouldRenderCompact() {
        ReActVisualizer viz = new ReActVisualizer();
        viz.addThink("A");
        viz.addAction("B");
        viz.addObserve("C");

        String compact = viz.renderCompact();
        assertNotNull(compact);
        assertTrue(compact.contains("🤔"));
        assertTrue(compact.contains("⚡"));
        assertTrue(compact.contains("👁"));
    }

    @Test
    void shouldClear() {
        ReActVisualizer viz = new ReActVisualizer();
        viz.addThink("test");
        assertFalse(viz.getSteps().isEmpty());

        viz.clear();
        assertTrue(viz.getSteps().isEmpty());
    }

    @Test
    void shouldTruncateLongContent() {
        ReActVisualizer viz = new ReActVisualizer();
        String longText = "A".repeat(300);
        viz.addThink(longText);

        String output = viz.render();
        assertTrue(output.contains("..."));
    }
}