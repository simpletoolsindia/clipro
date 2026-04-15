package com.clipro.plugins;

import com.clipro.cli.CommandRegistry;
import com.clipro.logging.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * H-16: Plugin loader for discovering and managing CLIPRO plugins.
 * Scans ~/.clipro/plugins/ for JAR/class files and loads Plugin implementations.
 */
public class PluginLoader {

    private static final Logger LOG = new Logger("PluginLoader");
    private static final String PLUGIN_DIR = ".clipro/plugins";

    private final List<Plugin> loadedPlugins = new CopyOnWriteArrayList<>();
    private final Map<String, Plugin> pluginMap = new java.util.concurrent.ConcurrentHashMap<>();
    private final CommandRegistry registry;

    public PluginLoader(CommandRegistry registry) {
        this.registry = registry;
    }

    /**
     * H-16: Discover and load all plugins from the plugin directory.
     */
    public void loadAll() {
        Path pluginPath = Paths.get(System.getProperty("user.home"), PLUGIN_DIR);
        if (!Files.exists(pluginPath)) {
            try { Files.createDirectories(pluginPath); } catch (IOException ignored) {}
            LOG.info("Created plugin directory: " + pluginPath);
            return;
        }

        LOG.info("Loading plugins from: " + pluginPath);
        List<Path> jars = new ArrayList<>();

        // Find all JAR files in plugin directory
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pluginPath, "*.jar")) {
            for (Path jar : stream) jars.add(jar);
        } catch (IOException e) {
            LOG.debug("No JAR files found in plugin directory");
        }

        // Try to load each JAR
        for (Path jar : jars) {
            try {
                loadPlugin(jar);
            } catch (Exception e) {
                LOG.warn("Failed to load plugin from " + jar + ": " + e.getMessage());
            }
        }

        LOG.info("Loaded " + loadedPlugins.size() + " plugins");
    }

    /**
     * H-16: Load a single plugin JAR.
     */
    public void loadPlugin(Path jarPath) throws Exception {
        // Create isolated classloader for plugin
        URLClassLoader classLoader = new URLClassLoader(
            new URL[]{jarPath.toUri().toURL()},
            getClass().getClassLoader()
        );

        // Scan JAR for Plugin implementations
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                        .replace('/', '.')
                        .substring(0, entry.getName().length() - 6);

                    try {
                        Class<?> cls = classLoader.loadClass(className);
                        if (Plugin.class.isAssignableFrom(cls) && !cls.isInterface()) {
                            Plugin plugin = (Plugin) cls.getDeclaredConstructor().newInstance();
                            register(plugin);
                            LOG.info("Loaded plugin: " + plugin.getName() + " v" + plugin.getVersion());
                        }
                    } catch (Exception ignored) {
                        // Not a Plugin or couldn't instantiate
                    }
                }
            }
        }
    }

    /**
     * H-16: Register a plugin with the command registry.
     */
    public void register(Plugin plugin) {
        loadedPlugins.add(plugin);
        pluginMap.put(plugin.getId(), plugin);

        try {
            plugin.onLoad();
        } catch (Exception e) {
            LOG.error("Plugin " + plugin.getName() + " onLoad failed: " + e.getMessage());
            pluginMap.remove(plugin.getId());
            loadedPlugins.remove(plugin);
            return;
        }

        // Register plugin commands
        for (CommandRegistry.Command cmd : plugin.getCommands()) {
            registry.register(cmd);
            LOG.debug("Registered command: /" + cmd.getName());
        }
    }

    /**
     * H-16: Unload a plugin.
     */
    public void unload(String pluginId) {
        Plugin plugin = pluginMap.remove(pluginId);
        if (plugin != null) {
            loadedPlugins.remove(plugin);
            plugin.onUnload();
            LOG.info("Unloaded plugin: " + plugin.getName());
        }
    }

    /**
     * H-16: Reload all plugins.
     */
    public void reloadAll() {
        LOG.info("Reloading all plugins...");
        for (Plugin p : new ArrayList<>(loadedPlugins)) {
            unload(p.getId());
        }
        loadAll();
    }

    /** Get all loaded plugins */
    public List<Plugin> getLoadedPlugins() { return new ArrayList<>(loadedPlugins); }

    /** Get plugin by ID */
    public Plugin getPlugin(String id) { return pluginMap.get(id); }

    /** Get number of loaded plugins */
    public int getPluginCount() { return loadedPlugins.size(); }

    /** Render plugin status as string */
    public String renderStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Plugin Status:\n");
        if (loadedPlugins.isEmpty()) {
            sb.append("  No plugins loaded.\n");
            sb.append("  Add JAR files to ~/.clipro/plugins/\n");
        } else {
            for (Plugin p : loadedPlugins) {
                sb.append("  ✓ ").append(p.getName()).append(" v").append(p.getVersion());
                sb.append(" (").append(p.getId()).append(")\n");
            }
        }
        sb.append("\n  Total: ").append(getPluginCount()).append(" plugins");
        return sb.toString();
    }
}
