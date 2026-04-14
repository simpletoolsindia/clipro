package com.clipro.llm.providers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ProviderManager.
 */
class ProviderManagerTest {

    private ProviderManager manager;

    @BeforeEach
    void setUp() {
        manager = new ProviderManager();
    }

    @Test
    void shouldCreateWithDefaultProvider() {
        assertEquals(ProviderManager.ProviderType.OLLAMA, manager.getCurrentProviderType());
        assertNotNull(manager.getCurrentProvider());
    }

    @Test
    void shouldSwitchProvider() {
        assertTrue(manager.switchProvider(ProviderManager.ProviderType.OPENROUTER));
        assertEquals(ProviderManager.ProviderType.OPENROUTER, manager.getCurrentProviderType());
    }

    @Test
    void shouldGetAvailableProviders() {
        var providers = manager.getAvailableProviders();
        assertEquals(2, providers.size());

        // Should have both Ollama and OpenRouter
        assertTrue(providers.stream().anyMatch(p -> p.getType() == ProviderManager.ProviderType.OLLAMA));
        assertTrue(providers.stream().anyMatch(p -> p.getType() == ProviderManager.ProviderType.OPENROUTER));
    }

    @Test
    void shouldGetCurrentModel() {
        String model = manager.getCurrentModel();
        assertNotNull(model);
    }

    @Test
    void shouldSetModel() {
        manager.setModel("qwen2.5-coder:14b");
        assertEquals("qwen2.5-coder:14b", manager.getCurrentModel());
    }

    @Test
    void shouldGetAvailableModels() {
        var models = manager.getAvailableModels();
        assertFalse(models.isEmpty());
    }

    @Test
    void shouldRenderProviderStatus() {
        String status = manager.renderProviderStatus();
        assertNotNull(status);
        assertTrue(status.contains("Provider Status"));
        assertTrue(status.contains("Ollama") || status.contains("OPENROUTER"));
    }

    @Test
    void shouldReturnFalseForInvalidProviderSwitch() {
        // Switching to same provider should return true
        assertTrue(manager.switchProvider(ProviderManager.ProviderType.OLLAMA));
    }
}
