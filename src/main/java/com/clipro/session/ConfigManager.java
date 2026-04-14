package com.clipro.session;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Configuration manager for CLIPRO.
 * Handles API keys, model settings, and preferences.
 */
public class ConfigManager {

    private static final String DEFAULT_DIR = System.getProperty("user.home") + "/.clipro";
    private static final String CONFIG_FILE = "config.json";
    private static final String SECRETS_FILE = "secrets.properties";
    private static ConfigManager instance;

    private final Path configDir;
    private final Map<String, Object> config;
    private final Map<String, String> secrets;

    // Default configuration
    private static final Map<String, Object> DEFAULTS = new HashMap<>();

    static {
        DEFAULTS.put("ollama.url", "http://localhost:11434/v1");
        DEFAULTS.put("ollama.model", "qwen3-coder:32b");
        DEFAULTS.put("ollama.temperature", 0.7);
        DEFAULTS.put("ollama.num_predict", 4096);
        DEFAULTS.put("search.url", "https://search.sridharhomelab.in/search");
        DEFAULTS.put("ui.color", true);
        DEFAULTS.put("ui.vim_mode", false);
        DEFAULTS.put("history.max_messages", 1000);
        DEFAULTS.put("tokens.max", 20000);
    }

    public ConfigManager() {
        this(DEFAULT_DIR);
    }

    public ConfigManager(String configDir) {
        this.configDir = Paths.get(configDir);
        this.config = new HashMap<>(DEFAULTS);
        this.secrets = new HashMap<>();
        ensureDirectory();
        load();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void ensureDirectory() {
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Get a configuration value.
     */
    public String get(String key) {
        return get(key, null);
    }

    /**
     * Get a configuration value with default.
     */
    public String get(String key, String defaultValue) {
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Get a boolean configuration value.
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Get a boolean configuration value with default.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    /**
     * Get an integer configuration value.
     */
    public int getInt(String key, int defaultValue) {
        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Set a configuration value.
     */
    public void set(String key, Object value) {
        config.put(key, value);
    }

    /**
     * Get a secret (API key, etc).
     */
    public String getSecret(String key) {
        return secrets.get(key);
    }

    /**
     * Set a secret.
     */
    public void setSecret(String key, String value) {
        secrets.put(key, value);
        saveSecrets();
    }

    /**
     * Remove a secret.
     */
    public void removeSecret(String key) {
        secrets.remove(key);
        saveSecrets();
    }

    /**
     * Get all secret keys (not values).
     */
    public Set<String> getSecretKeys() {
        return new HashSet<>(secrets.keySet());
    }

    /**
     * Load configuration from file.
     */
    public void load() {
        loadConfig();
        loadSecrets();
    }

    private void loadConfig() {
        Path file = configDir.resolve(CONFIG_FILE);
        if (!Files.exists(file)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqPos = line.indexOf('=');
                if (eqPos > 0) {
                    String key = line.substring(0, eqPos).trim();
                    String value = line.substring(eqPos + 1).trim();
                    // Parse value
                    Object parsedValue = parseValue(value);
                    config.put(key, parsedValue);
                }
            }
        } catch (IOException e) {
            // Ignore - use defaults
        }
    }

    private void loadSecrets() {
        Path file = configDir.resolve(SECRETS_FILE);
        if (!Files.exists(file)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqPos = line.indexOf('=');
                if (eqPos > 0) {
                    String key = line.substring(0, eqPos).trim();
                    String value = line.substring(eqPos + 1).trim();
                    secrets.put(key, value);
                }
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Save configuration to file.
     */
    public void save() {
        saveConfig();
    }

    private void saveConfig() {
        Path file = configDir.resolve(CONFIG_FILE);
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.println("# CLIPRO Configuration");
            writer.println("# Auto-generated - manual edits welcome");
            writer.println();
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                if (!DEFAULTS.containsKey(entry.getKey()) || !entry.getValue().equals(DEFAULTS.get(entry.getKey()))) {
                    writer.println(entry.getKey() + "=" + entry.getValue());
                }
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    private void saveSecrets() {
        Path file = configDir.resolve(SECRETS_FILE);
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.println("# CLIPRO Secrets");
            writer.println("# DO NOT SHARE THIS FILE");
            writer.println();
            for (Map.Entry<String, String> entry : secrets.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    private Object parseValue(String value) {
        // Try boolean
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;

        // Try number
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            // String
        }

        return value;
    }

    /**
     * Reset to defaults.
     */
    public void reset() {
        config.clear();
        config.putAll(DEFAULTS);
        save();
    }

    /**
     * Get all keys.
     */
    public Set<String> getKeys() {
        return new HashSet<>(config.keySet());
    }

    /**
     * Get Ollama URL.
     */
    public String getOllamaUrl() {
        return get("ollama.url");
    }

    /**
     * Get Ollama model.
     */
    public String getOllamaModel() {
        return get("ollama.model");
    }

    /**
     * Get SearXNG URL.
     */
    public String getSearchUrl() {
        return get("search.url");
    }

    public Path getConfigDir() {
        return configDir;
    }
}
