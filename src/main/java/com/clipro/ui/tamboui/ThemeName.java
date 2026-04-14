package com.clipro.ui.tamboui;

/**
 * Theme name enumeration matching OpenClaude's theme.ts THEME_NAMES.
 */
public enum ThemeName {
    DARK("dark"),
    LIGHT("light"),
    DARK_ANSI("dark-ansi"),
    LIGHT_ANSI("light-ansi"),
    DARK_DALTONIZED("dark-daltonized"),
    LIGHT_DALTONIZED("light-daltonized"),
    AUTO("auto");

    private final String configValue;

    ThemeName(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }

    public static ThemeName fromConfigValue(String value) {
        for (ThemeName name : values()) {
            if (name.configValue.equalsIgnoreCase(value)) {
                return name;
            }
        }
        return DARK; // default
    }

    public boolean isAnsi() {
        return this == DARK_ANSI || this == LIGHT_ANSI;
    }

    public boolean isDaltonized() {
        return this == DARK_DALTONIZED || this == LIGHT_DALTONIZED;
    }

    public boolean isDark() {
        return this == DARK || this == DARK_ANSI || this == DARK_DALTONIZED;
    }
}
