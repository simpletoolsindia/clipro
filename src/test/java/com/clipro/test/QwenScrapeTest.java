package com.clipro.test;

import com.clipro.agent.AgentEngine;
import com.clipro.llm.providers.OllamaProvider;
import com.clipro.tools.Tool;
import com.clipro.tools.web.WebFetchTool;
import com.clipro.tools.web.WebSearchTool;
import java.util.*;

/**
 * Direct test: CLIPRO Agent with qwen3.5:35b-a3b-coding-nvfp4 model
 * Tests: Web scraping, tool calling, UI components
 */
public class QwenScrapeTest {

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════════════╗");
        System.out.println("║         CLIPRO + QWEN3.5:35B SCRAPE TEST                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════╝\n");

        String model = args.length > 0 ? args[0] : "qwen3.5:35b-a3b-coding-nvfp4";
        var results = new StringBuilder();

        // 1. Test Ollama Connection
        System.out.println("┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("│ 1. OLLAMA CONNECTION                                           │");
        System.out.println("└─────────────────────────────────────────────────────────────────┘");
        try {
            var provider = new OllamaProvider();
            provider.setCurrentModel(model);
            var health = provider.healthCheck().get();
            if (health) {
                System.out.println("  ✓ Health Check: PASS");
                System.out.println("  ✓ Model: " + model);
                System.out.println("  Status: ✓ PASS\n");
                results.append("1. Ollama Connection: PASS\n");
            }
        } catch (Exception e) {
            System.out.println("  ✗ Health Check FAIL: " + e.getMessage());
            results.append("1. Ollama Connection: FAIL\n");
        }

        // 2. Test Web Fetch Tool
        System.out.println("┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("│ 2. WEB FETCH TOOL TEST                                          │");
        System.out.println("└─────────────────────────────────────────────────────────────────┘");
        try {
            var tool = new WebFetchTool();
            System.out.println("  Tool: " + tool.getName());
            System.out.println("  Desc: " + tool.getDescription());

            long start = System.currentTimeMillis();
            String result = tool.execute(Map.of("url", "https://sridharkaruppusamy.in"));
            long elapsed = System.currentTimeMillis() - start;

            System.out.println("  Latency: " + elapsed + "ms");
            System.out.println("  Content length: " + result.length() + " chars");
            System.out.println("  Preview: " + result.substring(0, Math.min(200, result.length())).replace("\n", " ") + "...");

            boolean ok = result.length() > 50 && !result.startsWith("Error:");
            System.out.println("\n  Status: " + (ok ? "✓ PASS" : "✗ FAIL"));
            results.append("2. Web Fetch Tool: " + (ok ? "PASS" : "FAIL") + "\n");
        } catch (Exception e) {
            System.out.println("  ✗ Error: " + e.getMessage());
            results.append("2. Web Fetch Tool: FAIL\n");
        }

        // 3. Test Web Search Tool
        System.out.println("\n┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("│ 3. WEB SEARCH TOOL TEST                                         │");
        System.out.println("└─────────────────────────────────────────────────────────────────┘");
        try {
            var tool = new WebSearchTool();
            System.out.println("  Tool: " + tool.getName());
            System.out.println("  Desc: " + tool.getDescription());

            long start = System.currentTimeMillis();
            String result = tool.execute(Map.of("query", "Sridhar Karuppusamy"));
            long elapsed = System.currentTimeMillis() - start;

            System.out.println("  Latency: " + elapsed + "ms");
            System.out.println("  Results length: " + result.length() + " chars");
            System.out.println("  Preview: " + result.substring(0, Math.min(200, result.length())).replace("\n", " ") + "...");

            boolean ok = result.length() > 20 && !result.startsWith("Error:");
            System.out.println("\n  Status: " + (ok ? "✓ PASS" : "⚠ PARTIAL"));
            results.append("3. Web Search Tool: " + (ok ? "PASS" : "PARTIAL") + "\n");
        } catch (Exception e) {
            System.out.println("  ✗ Error: " + e.getMessage());
            results.append("3. Web Search Tool: FAIL\n");
        }

        // 4. Test Tool Registry
        System.out.println("\n┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("│ 4. TOOL REGISTRY TEST                                           │");
        System.out.println("└─────────────────────────────────────────────────────────────────┘");
        try {
            var engine = new AgentEngine(model);
            engine.registerTools(List.of(
                new WebFetchTool(),
                new WebSearchTool()
            ));

            var schemas = engine.getToolRegistry().getSchemas();
            System.out.println("  Registered tools: " + schemas.size());
            for (var s : schemas) {
                var fn = s.getFunction();
                System.out.println("    • " + fn.getName() + " - " + fn.getDescription());
            }
            System.out.println("\n  Status: ✓ PASS");
            results.append("4. Tool Registry: PASS\n");
        } catch (Exception e) {
            System.out.println("  ✗ Error: " + e.getMessage());
            results.append("4. Tool Registry: FAIL\n");
        }

        // 5. Test Ollama Chat with Scraping Request
        System.out.println("\n┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("│ 5. OLLAMA CHAT (SCRAPE REQUEST)                                 │");
        System.out.println("└─────────────────────────────────────────────────────────────────┘");
        System.out.println("  Model: " + model);
        System.out.println("  Request: Scrape and summarize sridharkaruppusamy.in");
        System.out.println("  ⚠ This test requires local Ollama with " + model + " loaded");
        System.out.println("  Note: The 35B model takes 60-120s per response on CPU\n");
        results.append("5. Ollama Chat (Skipped - model too slow for auto-test)\n");

        // 6. UI Theme Test
        System.out.println("┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("│ 6. UI THEME TEST (OpenClaude Theme)                             │");
        System.out.println("└─────────────────────────────────────────────────────────────────┘");
        try {
            var theme = Class.forName("com.clipro.ui.tamboui.OpenClaudeTheme").newInstance();
            System.out.println("  ✓ OpenClaudeTheme loaded");

            // Test ANSI colors
            var methods = theme.getClass().getMethods();
            long colorMethods = Arrays.stream(methods).filter(m ->
                m.getName().contains("Text") || m.getName().contains("Color")
            ).count();
            System.out.println("  ✓ Color/text methods: " + colorMethods);

            // Test rainbow colors
            var rainbowClass = Class.forName("com.clipro.ui.tamboui.OpenClaudeTheme");
            var rainbowMethod = rainbowClass.getMethod("getRainbowColor", int.class);
            String r0 = (String) rainbowMethod.invoke(null, 0);
            String r1 = (String) rainbowMethod.invoke(null, 1);
            System.out.println("  ✓ Rainbow[0]: " + r0 + "RED" + "\u001B[0m");
            System.out.println("  ✓ Rainbow[1]: " + r1 + "ORANGE" + "\u001B[0m");

            System.out.println("\n  Status: ✓ PASS");
            results.append("6. UI Theme: PASS\n");
        } catch (Exception e) {
            System.out.println("  ✗ Error: " + e.getMessage());
            results.append("6. UI Theme: FAIL\n");
        }

        // Summary
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TEST SUMMARY                                  ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════╣");
        for (String line : results.toString().split("\n")) {
            System.out.println("║ " + line);
        }
        System.out.println("╠═══════════════════════════════════════════════════════════════════╣");
        System.out.println("║ Model: " + model + " (21.9GB, nvfp4 quantized)         ║");
        System.out.println("║ Tools: web_fetch, web_search, bash, grep, git, file ops        ║");
        System.out.println("║ UI:    TamboUI + OpenClaude theme, dark mode, box-drawing chars ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    }
}
