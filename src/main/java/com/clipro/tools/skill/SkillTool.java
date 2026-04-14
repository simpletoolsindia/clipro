package com.clipro.tools.skill;

import com.clipro.tools.Tool;
import com.clipro.session.ConfigManager;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * SkillTool - Load and execute skills from ~/.config/clipro/skills/
 * L-05: Skills system for extending agent capabilities
 *
 * Skills are YAML files with:
 * - name: skill identifier
 * - description: what the skill does
 * - parameters: input parameters
 * - prompt: prompt template with {{param}} placeholders
 *
 * Usage: /skill <name> [--param value]...
 */
public class SkillTool implements Tool {

    private static final String SKILLS_DIR = ".config/clipro/skills";
    private final Map<String, Skill> loadedSkills;

    public SkillTool() {
        this.loadedSkills = new HashMap<>();
        loadSkills();
    }

    @Override
    public String getName() {
        return "skill";
    }

    @Override
    public String getDescription() {
        return "Execute skill from ~/.config/clipro/skills/\n" +
               "Usage: /skill <name> [--param value]... | /skill list | /skill load <name>";
    }

    @Override
    public Object getParameters() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "action", Map.of(
                    "type", "string",
                    "description", "Action: execute, list, load, info",
                    "enum", List.of("execute", "list", "load", "info")
                ),
                "name", Map.of(
                    "type", "string",
                    "description", "Skill name to execute or load"
                ),
                "params", Map.of(
                    "type", "object",
                    "description", "Parameters to pass to the skill"
                )
            ),
            "required", List.of("action")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        String action = (String) args.getOrDefault("action", "list");

        switch (action) {
            case "execute":
            case "run":
                String name = (String) args.get("name");
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) args.get("params");
                return executeSkill(name, params != null ? params : Collections.emptyMap());
            case "list":
                return listSkills();
            case "load":
                String loadName = (String) args.get("name");
                return loadSkill(loadName);
            case "info":
                String infoName = (String) args.get("name");
                return getSkillInfo(infoName);
            default:
                return "Unknown action: " + action + "\n" +
                       "Usage: /skill list | /skill execute <name> [--param value]...";
        }
    }

    /**
     * Load all skills from ~/.config/clipro/skills/
     */
    public void loadSkills() {
        loadedSkills.clear();
        Path skillsPath = getSkillsDir();

        if (!Files.exists(skillsPath)) {
            try {
                Files.createDirectories(skillsPath);
            } catch (IOException e) {
                return; // Can't create, skip
            }
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(skillsPath, "*.{yaml,yml,json}")) {
            for (Path file : stream) {
                loadSkillFromFile(file);
            }
        } catch (IOException e) {
            // Directory doesn't exist or can't be read
        }
    }

    private Path getSkillsDir() {
        String home = System.getProperty("user.home");
        return Paths.get(home, SKILLS_DIR);
    }

    private void loadSkillFromFile(Path file) {
        try {
            String content = Files.readString(file);
            Skill skill = parseSkillFile(file.getFileName().toString(), content);
            if (skill != null) {
                loadedSkills.put(skill.name, skill);
            }
        } catch (IOException e) {
            // Skip invalid skill files
        }
    }

    /**
     * Parse a skill file (YAML or JSON format).
     */
    private Skill parseSkillFile(String filename, String content) {
        try {
            // Simple YAML/JSON parsing for skill files
            String name = extractValue(content, "name:");
            if (name == null) {
                name = filename.replace(".yaml", "").replace(".yml", "").replace(".json", "");
            }

            String description = extractValue(content, "description:");
            String prompt = extractValue(content, "prompt:");

            if (prompt == null && content.contains("prompt")) {
                // Multi-line prompt - extract everything after "prompt:" until next key
                prompt = extractMultiLineValue(content, "prompt:");
            }

            if (prompt == null) {
                return null; // Invalid skill
            }

            // Parse parameters
            List<SkillParam> params = new ArrayList<>();
            // Look for parameter blocks
            if (content.contains("parameters:")) {
                params = parseParameters(content);
            }

            return new Skill(name, description, prompt, params);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractValue(String content, String key) {
        int keyIndex = content.indexOf(key);
        if (keyIndex < 0) return null;

        int colonIndex = keyIndex + key.length();
        int newlineIndex = content.indexOf('\n', colonIndex);

        String value;
        if (newlineIndex < 0) {
            value = content.substring(colonIndex).trim();
        } else {
            value = content.substring(colonIndex, newlineIndex).trim();
        }

        // Remove quotes if present
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }

        return value.isEmpty() ? null : value;
    }

    private String extractMultiLineValue(String content, String key) {
        int keyIndex = content.indexOf(key);
        if (keyIndex < 0) return null;

        int colonIndex = keyIndex + key.length();
        int endIndex = content.indexOf('\n', colonIndex);

        // Check if it's a block (indented content follows)
        if (endIndex < 0) {
            return content.substring(colonIndex).trim();
        }

        String firstLine = content.substring(colonIndex, endIndex).trim();
        if (firstLine.isEmpty() || firstLine.equals("|") || firstLine.equals(">")) {
            // Block style - find indented content
            int start = endIndex + 1;
            int indent = -1;
            StringBuilder sb = new StringBuilder();

            for (int i = start; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '\n') {
                    sb.append('\n');
                } else if (c == ' ' || c == '\t') {
                    if (indent < 0 && i < content.length() - 1) {
                        indent = c == ' ' ? 4 : 1;
                    }
                    sb.append(c);
                } else {
                    break; // End of block
                }
            }

            return sb.toString().trim();
        }

        return firstLine;
    }

    private List<SkillParam> parseParameters(String content) {
        List<SkillParam> params = new ArrayList<>();
        // Simple param extraction - look for name/type patterns
        int paramStart = content.indexOf("parameters:");
        if (paramStart < 0) return params;

        int searchStart = paramStart + "parameters:".length();
        int paramEnd = content.indexOf('\n\n', searchStart);
        if (paramEnd < 0) paramEnd = content.length();

        String paramBlock = content.substring(searchStart, paramEnd);

        // Find each parameter
        int pos = 0;
        while (pos < paramBlock.length()) {
            int nameIndex = paramBlock.indexOf("name:", pos);
            if (nameIndex < 0) break;

            int colonIndex = nameIndex + "name:".length();
            int newlineIndex = paramBlock.indexOf('\n', colonIndex);
            String paramName = paramBlock.substring(colonIndex, newlineIndex).trim();

            int typeIndex = paramBlock.indexOf("type:", colonIndex);
            String paramType = "string";
            if (typeIndex > 0 && typeIndex < newlineIndex + 50) {
                int typeColon = typeIndex + "type:".length();
                int typeNewline = paramBlock.indexOf('\n', typeColon);
                paramType = paramBlock.substring(typeColon, typeNewline).trim();
            }

            int descIndex = paramBlock.indexOf("description:", colonIndex);
            String paramDesc = "";
            if (descIndex > 0 && descIndex < newlineIndex + 100) {
                int descColon = descIndex + "description:".length();
                int descNewline = paramBlock.indexOf('\n', descColon);
                if (descNewline < 0) descNewline = paramBlock.length();
                paramDesc = paramBlock.substring(descColon, descNewline).trim();
            }

            boolean required = paramBlock.contains("required") &&
                              paramBlock.substring(nameIndex, nameIndex + 100).contains("required: true");

            params.add(new SkillParam(paramName, paramType, paramDesc, required));
            pos = newlineIndex > 0 ? newlineIndex + 1 : nameIndex + 1;
        }

        return params;
    }

    /**
     * Execute a skill with the given parameters.
     */
    private String executeSkill(String name, Map<String, Object> params) {
        if (name == null || name.isEmpty()) {
            return "Usage: /skill execute <name> [--param value]...\n" +
                   "Available skills: " + String.join(", ", loadedSkills.keySet());
        }

        Skill skill = loadedSkills.get(name);
        if (skill == null) {
            return "Skill not found: " + name + "\n" +
                   "Use /skill list to see available skills";
        }

        // Substitute parameters in prompt
        String prompt = skill.prompt;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = String.valueOf(entry.getValue());
            prompt = prompt.replace(placeholder, value);
        }

        // Check for unreplaced placeholders
        int unreplaced = countOccurrences(prompt, "{{");
        if (unreplaced > 0) {
            return "Missing parameters for skill '" + name + "':\n" +
                   "Unreplaced placeholders: " + unreplaced + "\n" +
                   "Required params: " + skill.requiredParams();
        }

        return prompt;
    }

    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /**
     * List all available skills.
     */
    private String listSkills() {
        if (loadedSkills.isEmpty()) {
            return "No skills found.\n" +
                   "Create skills in ~/.config/clipro/skills/\n" +
                   "See example: code-review.yaml";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Available Skills (").append(loadedSkills.size()).append("):\n");
        sb.append("────────────────────────────────────────\n");

        for (Skill skill : loadedSkills.values()) {
            sb.append("• ").append(skill.name);
            if (skill.description != null) {
                sb.append(": ").append(skill.description);
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Load a specific skill (reload from disk).
     */
    private String loadSkill(String name) {
        if (name == null || name.isEmpty()) {
            return "Usage: /skill load <name>";
        }

        Path skillsDir = getSkillsDir();
        String[] extensions = {".yaml", ".yml", ".json"};

        for (String ext : extensions) {
            Path skillFile = skillsDir.resolve(name + ext);
            if (Files.exists(skillFile)) {
                loadSkillFromFile(skillFile);
                Skill skill = loadedSkills.get(name);
                if (skill != null) {
                    return "Loaded skill: " + name;
                }
            }
        }

        return "Skill not found: " + name + "\n" +
               "Check ~/.config/clipro/skills/ for skill files";
    }

    /**
     * Get detailed info about a skill.
     */
    private String getSkillInfo(String name) {
        if (name == null || name.isEmpty()) {
            return "Usage: /skill info <name>";
        }

        Skill skill = loadedSkills.get(name);
        if (skill == null) {
            return "Skill not found: " + name;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Skill: ").append(skill.name).append("\n");
        sb.append("────────────────────────────────────────\n");

        if (skill.description != null) {
            sb.append("Description: ").append(skill.description).append("\n");
        }

        if (!skill.params.isEmpty()) {
            sb.append("Parameters:\n");
            for (SkillParam param : skill.params) {
                String req = param.required ? " (required)" : " (optional)";
                sb.append("  • ").append(param.name)
                  .append(": ").append(param.type).append(req);
                if (!param.description.isEmpty()) {
                    sb.append("\n    ").append(param.description);
                }
                sb.append("\n");
            }
        }

        // Show prompt preview (first 200 chars)
        if (skill.prompt.length() > 200) {
            sb.append("\nPrompt preview:\n");
            sb.append(skill.prompt.substring(0, 200)).append("...");
        } else {
            sb.append("\nPrompt:\n").append(skill.prompt);
        }

        return sb.toString();
    }

    /**
     * Get all loaded skills.
     */
    public Collection<Skill> getLoadedSkills() {
        return loadedSkills.values();
    }

    /**
     * Skill data class.
     */
    public static class Skill {
        public final String name;
        public final String description;
        public final String prompt;
        public final List<SkillParam> params;

        public Skill(String name, String description, String prompt, List<SkillParam> params) {
            this.name = name;
            this.description = description;
            this.prompt = prompt;
            this.params = params;
        }

        public String requiredParams() {
            List<String> required = new ArrayList<>();
            for (SkillParam param : params) {
                if (param.required) {
                    required.add(param.name);
                }
            }
            return required.isEmpty() ? "none" : String.join(", ", required);
        }
    }

    /**
     * Skill parameter definition.
     */
    public static class SkillParam {
        public final String name;
        public final String type;
        public final String description;
        public final boolean required;

        public SkillParam(String name, String type, String description, boolean required) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.required = required;
        }
    }
}