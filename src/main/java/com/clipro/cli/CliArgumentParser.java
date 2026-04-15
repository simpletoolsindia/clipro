package com.clipro.cli;

import com.clipro.session.ConfigManager;

import java.util.*;

/**
 * H-17: Full CLI argument parser supporting:
 * --model, --provider, --theme, --config, --workspace, --verbose, --help
 * Also handles workspace context variables like ${cwd}, ${git_root}.
 */
public class CliArgumentParser {

    public static class Args {
        public String model = null;
        public String provider = null;
        public String theme = null;
        public String configFile = null;
        public String workspace = null;
        public boolean verbose = false;
        public boolean help = false;
        public List<String> remaining = new ArrayList<>();
        public String workingDirectory = System.getProperty("user.dir");
        public String gitRoot = findGitRoot();
    }

    /**
     * H-17: Parse command-line arguments.
     */
    public static Args parse(String[] args) {
        Args result = new Args();
        if (args == null || args.length == 0) return result;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("--help") || arg.equals("-h") || arg.equals("-?")) {
                result.help = true;
            } else if (arg.equals("--verbose") || arg.equals("-v")) {
                result.verbose = true;
            } else if (arg.equals("--model") || arg.equals("-m")) {
                if (i + 1 < args.length) result.model = args[++i];
            } else if (arg.equals("--provider") || arg.equals("-p")) {
                if (i + 1 < args.length) result.provider = args[++i];
            } else if (arg.equals("--theme") || arg.equals("-t")) {
                if (i + 1 < args.length) result.theme = args[++i];
            } else if (arg.equals("--config") || arg.equals("-c")) {
                if (i + 1 < args.length) result.configFile = args[++i];
            } else if (arg.equals("--workspace") || arg.equals("-w")) {
                if (i + 1 < args.length) result.workspace = args[++i];
            } else if (arg.startsWith("--")) {
                // Long option without value
                result.remaining.add(arg);
            } else if (!arg.startsWith("-")) {
                result.remaining.add(arg);
            }
        }

        // Load config file if specified
        if (result.configFile != null) {
            loadConfigFile(result, result.configFile);
        }

        return result;
    }

    private static void loadConfigFile(Args args, String configFile) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(configFile);
            if (java.nio.file.Files.exists(path)) {
                java.nio.file.Files.lines(path).forEach(line -> {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) return;
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        String key = line.substring(0, eq).trim();
                        String value = line.substring(eq + 1).trim();
                        applyConfig(args, key, value);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load config file " + configFile);
        }
    }

    private static void applyConfig(Args args, String key, String value) {
        switch (key) {
            case "model" -> args.model = value;
            case "provider" -> args.provider = value;
            case "theme" -> args.theme = value;
            case "workspace" -> args.workspace = value;
        }
    }

    /**
     * H-17: Expand context variables in a string.
     * ${cwd} -> current working directory
     * ${git_root} -> git repository root
     */
    public static String expandContext(String input, Args args) {
        if (input == null) return null;
        String result = input;
        result = result.replace("${cwd}", args.workingDirectory);
        result = result.replace("${git_root}", args.gitRoot);
        result = result.replace("~", System.getProperty("user.home"));
        return result;
    }

    /**
     * H-17: Get usage string.
     */
    public static String getUsage() {
        return """
            CLIPRO - Local-first AI Coding CLI

            Usage: clipro [options] [initial-prompt]

            Options:
              -m, --model <name>    Set the LLM model (e.g., qwen3-coder:32b)
              -p, --provider <name> Set provider (ollama, openrouter, github, openai, gemini, bedrock)
              -t, --theme <name>   Set theme (dark, light, dark-ansi, light-ansi, auto)
              -c, --config <file> Load config from file
              -w, --workspace <dir> Set workspace directory
              -v, --verbose        Enable verbose output
              -h, --help           Show this help

            Context Variables:
              ${cwd}     - Current working directory
              ${git_root} - Git repository root

            Examples:
              clipro --model qwen3-coder:32b
              clipro --provider openrouter --model anthropic/claude-sonnet-4
              clipro --theme dark --workspace /projects/myapp
            """;
    }

    private static String findGitRoot() {
        try {
            java.nio.file.Path cwd = java.nio.file.Paths.get(System.getProperty("user.dir"));
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(.*)[/\\\\]\\.git$");
            java.nio.file.Path dir = cwd;
            for (int i = 0; i < 10 && dir != null; i++) {
                if (java.nio.file.Files.exists(dir.resolve(".git"))) {
                    return dir.toAbsolutePath().toString();
                }
                dir = dir.getParent();
            }
        } catch (Exception ignored) {}
        return System.getProperty("user.dir");
    }
}
