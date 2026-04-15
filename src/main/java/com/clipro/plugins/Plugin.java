package com.clipro.plugins;

import com.clipro.cli.CommandRegistry;

/**
 * H-16: Plugin interface for extending CLIPRO functionality.
 * Implement this interface to create a plugin.
 */
public interface Plugin {
    /** Unique plugin identifier */
    String getId();

    /** Human-readable name */
    String getName();

    /** Plugin version */
    String getVersion();

    /** Plugin description */
    String getDescription();

    /** Initialize the plugin */
    void onLoad() throws Exception;

    /** Called when plugin is unloaded */
    void onUnload();

    /** Get commands provided by this plugin */
    default CommandRegistry.Command[] getCommands() {
        return new CommandRegistry.Command[0];
    }

    /** Get metadata (author, license, dependencies) */
    default PluginMetadata getMetadata() {
        return new PluginMetadata(getId(), getName(), getVersion(), getDescription(), "", "", new String[0]);
    }

    record PluginMetadata(String id, String name, String version, String description,
                         String author, String license, String[] dependencies) {}
}
