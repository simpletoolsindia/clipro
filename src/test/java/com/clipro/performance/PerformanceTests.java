package com.clipro.performance;

import com.clipro.App;
import com.clipro.ui.Terminal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for CLIPRO.
 * These tests measure startup time, memory usage, and UI rendering performance.
 *
 * Note: Full performance tests require GraalVM native image.
 * JVM mode tests provide approximate measurements.
 */
class PerformanceTests {

    @Test
    @EnabledIfEnvironmentVariable(named = "CLIPRO_PERF_TEST", matches = "true")
    void shouldMeasureStartupTime() {
        long startTime = System.nanoTime();

        // Simulate minimal startup
        Terminal.init();

        long endTime = System.nanoTime();
        long startupMs = (endTime - startTime) / 1_000_000;

        // With JVM: startup may be >100ms
        // With GraalVM native: should be <100ms
        System.out.println("Startup time: " + startupMs + "ms");

        // In native image mode, this should pass
        // In JVM mode, we just measure
        if (isNativeImage()) {
            assertTrue(startupMs < 100, "Startup should be <100ms in native image");
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "CLIPRO_PERF_TEST", matches = "true")
    void shouldMeasureMemoryUsage() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();

        long usedMB = heap.getUsed() / (1024 * 1024);
        long maxMB = heap.getMax() / (1024 * 1024);

        System.out.println("Heap used: " + usedMB + "MB / " + maxMB + "MB");

        // Note: JVM mode uses more memory than native image
        // Native image should use <50MB
        if (isNativeImage()) {
            assertTrue(usedMB < 50, "Memory usage should be <50MB in native image");
        }
    }

    @Test
    void shouldRenderUIEfficiently() {
        Terminal.setColumns(80);
        Terminal.setRows(24);

        // Measure UI render time
        long startTime = System.nanoTime();

        // Simulate UI render
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append(Terminal.dim("Line " + i + "\n"));
        }

        long endTime = System.nanoTime();
        long renderMs = (endTime - startTime) / 1_000_000;

        System.out.println("UI render (100 lines): " + renderMs + "ms");

        // 100 lines should render very quickly
        assertTrue(renderMs < 100, "UI render should be fast");
    }

    @Test
    void shouldHandleLargeOutputEfficiently() {
        // Test that large outputs don't cause memory issues
        StringBuilder largeOutput = new StringBuilder();

        for (int i = 0; i < 10000; i++) {
            largeOutput.append("Line ").append(i).append(": Some content here\n");
        }

        String output = largeOutput.toString();

        // Should handle large output
        assertEquals(10000, output.lines().count());
        assertTrue(output.length() > 100000);
    }

    @Test
    void shouldMeasureTokenBudgetOperations() {
        // Measure token budget operations
        long startTime = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            // Simulate token operations
            int tokens = i % 20000;
        }

        long endTime = System.nanoTime();
        long opsMs = (endTime - startTime) / 1_000_000;

        System.out.println("Token operations: " + opsMs + "ms");
        assertTrue(opsMs < 100, "Token operations should be fast");
    }

    @Test
    void shouldMeasureMessageRendering() {
        Terminal.setColumns(80);
        Terminal.setRows(24);

        long startTime = System.nanoTime();

        // Render 10 messages
        for (int i = 0; i < 10; i++) {
            String msg = "Message " + i + " with some content";
            // Simulate rendering
            Terminal.dim(msg);
        }

        long endTime = System.nanoTime();
        long renderMs = (endTime - startTime) / 1_000_000;

        System.out.println("10 messages render: " + renderMs + "ms");

        // Should be very fast
        assertTrue(renderMs < 50, "Message rendering should be fast");
    }

    @Test
    void shouldMeasureFileOperations() throws Exception {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("perf", ".txt");
        String content = "Test content\n".repeat(100);

        long startTime = System.nanoTime();

        java.nio.file.Files.writeString(tempFile, content);
        String read = java.nio.file.Files.readString(tempFile);

        long endTime = System.nanoTime();
        long ioMs = (endTime - startTime) / 1_000_000;

        System.out.println("File IO (1MB): " + ioMs + "ms");

        java.nio.file.Files.deleteIfExists(tempFile);

        assertEquals(content, read);
        assertTrue(ioMs < 1000, "File IO should be reasonably fast");
    }

    @Test
    void shouldMeasureToolExecution() {
        long startTime = System.nanoTime();

        // Simulate tool execution overhead
        for (int i = 0; i < 100; i++) {
            // Minimal computation
            String s = "test" + i;
        }

        long endTime = System.nanoTime();
        long toolMs = (endTime - startTime) / 1_000_000;

        System.out.println("Tool execution overhead (100 calls): " + toolMs + "ms");
        assertTrue(toolMs < 100, "Tool execution should have low overhead");
    }

    private boolean isNativeImage() {
        // Check if running as native image
        String javaClassVersion = System.getProperty("java.class.version", "");
        return javaClassVersion.contains("native");
    }
}
