package com.clipro.tools;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Tool for asking user questions during execution.
 * Used by agent to clarify requirements or get user input.
 */
public class AskUserQuestionTool implements Tool {

    private static final int MAX_OPTIONS = 4;

    @Override
    public String getName() {
        return "ask_user";
    }

    @Override
    public String getDescription() {
        return "Ask a question to the user with optional options. " +
               "Returns user response. Usage: /ask <question> | /ask <question> [opt1] [opt2]...";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "question", Map.of(
                    "type", "string",
                    "description", "The question to ask"
                ),
                "options", Map.of(
                    "type", "array",
                    "items", Map.of("type", "string"),
                    "description", "Optional multiple choice options (max 4)"
                )
            ),
            "required", List.of("question")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String question = (String) args.get("question");
        if (question == null || question.isEmpty()) {
            return "Usage: /ask <question> [option1] [option2]...";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("QUESTION: ").append(question).append("\n");

        Object optionsObj = args.get("options");
        if (optionsObj instanceof java.util.List<?> options) {
            sb.append("Options:\n");
            for (int i = 0; i < Math.min(options.size(), MAX_OPTIONS); i++) {
                Object opt = options.get(i);
                sb.append("  ").append(i + 1).append(". ").append(opt).append("\n");
            }
        }

        sb.append("\n[Waiting for user response...]");
        return sb.toString();
    }
}
