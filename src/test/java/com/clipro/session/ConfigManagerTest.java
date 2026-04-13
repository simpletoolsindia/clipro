package com.clipro.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConfigManager.
 */
class ConfigManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGetDefaultOllamaUrl() {
        ConfigManager config = new ConfigManager(tempDir.toString());
        assertEquals("http://localhost:11434/v1", config.getOllamaUrl());
    }

    @Test
    void shouldGetDefaultOllamaModel() {
        ConfigManager config = new ConfigManager(tempDir.toString());
        assertEquals("qwen3-coder:32b", config.getOllamaModel());
    }

    @Test
    void shouldSetAndGetValue() {
        ConfigManager config = new ConfigManager(tempDir.toString());
        config.set("test.key", "test value");
        assertEquals("test value", config.get("test.key"));
    }

    @Test
    void shouldGetBooleanWithDefault() {
        ConfigManager config = new ConfigManager(tempDir.toString());
        // Default ui.color is true
        assertTrue(config.getBoolean("ui.color"));
        config.set("ui.color", false);
        assertFalse(config.getBoolean("ui.color"));
    }

    @Test
    void shouldGetIntWithDefault() {
        ConfigManager config = new ConfigManager(tempDir.toString());
        assertEquals(20000, config.getInt("tokens.max", 0));
    }

    @Test
    void shouldSetAndGetSecret() {
        ConfigManager config = new ConfigManager(tempDir.toString());
        config.setSecret("api_key", "secret123");
        assertEquals("secret123", config.getSecret("api_key"));
    }

    @Test
    void shouldRemoveSecret() {
        ConfigManager config = new ConfigManager(tempDir.toString());
        config.setSecret("temp_key", "temp_value");
        assertNotNull(config.getSecret("temp_key"));
        
        config.removeSecret("temp_key");
        assertNull(config.getSecret("temp_key"));
    }

    @Test
    void shouldGetSecretKeys() {
        ConfigManager config = new ConfigManager(tempDir.toString());
        config.setSecret("key1", "value1");
        config.setSecret("key2", "value2");
        
        assertTrue(config.getSecretKeys().contains("key1"));
        assertTrue(config.getSecretKeys().contains("key2"));
    }

    @Test
    void shouldSaveAndLoad() {
        ConfigManager config = new ConfigManager(tempDir.toString());
        config.set("custom.setting", "custom value");
        config.setSecret("my_key", "my_secret");
        config.save();
        
        ConfigManager loaded = new ConfigManager(tempDir.toString());
        assertEquals("custom value", loaded.get("custom.setting"));
        assertEquals("my_secret", loaded.getSecret("my_key"));
    }

    @Test
    void shouldGetSearchUrl() {
        ConfigManager config = new ConfigManager(tempDir.toString());
        assertTrue(config.getSearchUrl().contains("search"));
    }
}
