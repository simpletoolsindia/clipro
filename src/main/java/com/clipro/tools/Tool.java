package com.clipro.tools;

import com.clipro.llm.models.ToolDefinition;

import java.util.List;
import java.util.Map;

/**
 * Tool interface for clipro native tools.
 */
public interface Tool {
    String getName();
    String getDescription();
    Object getParameters();
    String execute(Map<String, Object> args);
}