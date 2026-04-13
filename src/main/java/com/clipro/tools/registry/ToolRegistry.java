package com.clipro.tools.registry;

import com.clipro.llm.models.ToolDefinition;
import com.clipro.tools.Tool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Tool registry with lazy loading and schema optimization.
 * Token-optimized: only loads tools when needed.
 */
public class ToolRegistry {

    private final Map<String, Tool> tools;
    private final Map<String, ToolDefinition> schemas;
    private final Map<String, Supplier<Tool>> lazyLoaders;

    public ToolRegistry() {
        this.tools = new ConcurrentHashMap<>();
        this.schemas = new ConcurrentHashMap<>();
        this.lazyLoaders = new ConcurrentHashMap<>();
    }

    /**
     * Register a tool directly.
     */
    public void register(Tool tool) {
        tools.put(tool.getName(), tool);
        schemas.put(tool.getName(), createSchema(tool));
    }

    /**
     * Register a tool lazily.
     */
    public void registerLazy(String name, Supplier<Tool> loader) {
        lazyLoaders.put(name, loader);
        // Create placeholder schema
        schemas.put(name, new ToolDefinition(name, "Loading...", Map.of("type", "object")));
    }

    /**
     * Get a tool (loads lazily if needed).
     */
    public Tool get(String name) {
        Tool tool = tools.get(name);
        if (tool == null && lazyLoaders.containsKey(name)) {
            tool = lazyLoaders.get(name).get();
            tools.put(name, tool);
            schemas.put(name, createSchema(tool));
            lazyLoaders.remove(name);
        }
        return tool;
    }

    /**
     * Check if tool exists (loaded or registered for lazy load).
     */
    public boolean has(String name) {
        return tools.containsKey(name) || lazyLoaders.containsKey(name);
    }

    /**
     * Unregister a tool.
     */
    public void unregister(String name) {
        tools.remove(name);
        schemas.remove(name);
        lazyLoaders.remove(name);
    }

    /**
     * Get all tool names.
     */
    public Set<String> getToolNames() {
        Set<String> all = new HashSet<>(tools.keySet());
        all.addAll(lazyLoaders.keySet());
        return all;
    }

    /**
     * Get optimized schemas for LLM.
     * Trims verbose descriptions to save tokens.
     */
    public List<ToolDefinition> getSchemas() {
        return new ArrayList<>(schemas.values());
    }

    /**
     * Get optimized schemas with lazy loading.
     */
    public List<ToolDefinition> getSchemas(boolean loadLazy) {
        if (loadLazy) {
            // Trigger lazy loading for all tools
            for (String name : lazyLoaders.keySet()) {
                get(name);
            }
        }
        return getSchemas();
    }

    /**
     * Clear all tools.
     */
    public void clear() {
        tools.clear();
        schemas.clear();
        lazyLoaders.clear();
    }

    private ToolDefinition createSchema(Tool tool) {
        return new ToolDefinition(
            tool.getName(),
            optimizeDescription(tool.getDescription()),
            tool.getParameters()
        );
    }

    /**
     * Optimize description for token savings.
     */
    private String optimizeDescription(String description) {
        if (description == null) return "";
        // Truncate to 100 chars for token savings
        if (description.length() > 100) {
            return description.substring(0, 97) + "...";
        }
        return description;
    }
}