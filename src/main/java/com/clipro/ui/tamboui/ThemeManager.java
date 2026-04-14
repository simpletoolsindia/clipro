package com.clipro.ui.tamboui;

import com.clipro.session.ConfigManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Theme manager providing all 6 OpenClaude themes with auto-detection.
 * Singleton pattern with thread-safe theme switching.
 *
 * Reference: openclaude/src/utils/theme.ts
 */
public class ThemeManager {

    private static ThemeManager instance;

    private ThemeName currentTheme = ThemeName.DARK;
    private Theme theme;
    private boolean shimmerEnabled = true;

    // All 6 themes
    private final Map<ThemeName, Theme> themes = new ConcurrentHashMap<>();

    private ThemeManager() {
        initializeThemes();
        theme = themes.get(currentTheme);
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Initialize all 6 themes from OpenClaude.
     */
    private void initializeThemes() {
        // Dark theme (default)
        themes.put(ThemeName.DARK, createDarkTheme());

        // Light theme
        themes.put(ThemeName.LIGHT, createLightTheme());

        // Dark ANSI (16-color fallback)
        themes.put(ThemeName.DARK_ANSI, createAnsiTheme(false));

        // Light ANSI
        themes.put(ThemeName.LIGHT_ANSI, createAnsiTheme(true));

        // Dark Daltonized (colorblind-friendly)
        themes.put(ThemeName.DARK_DALTONIZED, createDaltonizedTheme(false));

        // Light Daltonized
        themes.put(ThemeName.LIGHT_DALTONIZED, createDaltonizedTheme(true));
    }

    private Theme createDarkTheme() {
        return new Theme.Builder()
            .setClaude("\u001B[38;2;215;119;87m")
            .setText("\u001B[38;2;255;255;255m")
            .setSuccess("\u001B[38;2;78;186;101m")
            .setError("\u001B[38;2;255;107;128m")
            .setWarning("\u001B[38;2;255;193;7m")
            .setBackground("\u001B[38;2;0;204;204m")
            .setUserMessageBackground("\u001B[38;2;55;55;55m")
            .build();
    }

    private Theme createLightTheme() {
        return new Theme.Builder()
            .setClaude("\u001B[38;2;215;119;87m")
            .setText("\u001B[38;2;0;0;0m")
            .setSuccess("\u001B[38;2;44;122;57m")
            .setError("\u001B[38;2;171;43;63m")
            .setWarning("\u001B[38;2;150;108;30m")
            .setBackground("\u001B[38;2;0;153;153m")
            .setUserMessageBackground("\u001B[38;2;240;240;240m")
            .build();
    }

    private Theme createAnsiTheme(boolean light) {
        // Uses only 16 standard ANSI colors
        String reset = "\u001B[0m";
        String text = light ? "\u001B[30m" : "\u001B[37m";
        String success = "\u001B[32m";
        String error = "\u001B[31m";
        String warning = light ? "\u001B[33m" : "\u001B[33m";
        String suggestion = "\u001B[34m";
        String claude = "\u001B[31m"; // redBright

        return new Theme.Builder()
            .setClaude(claude)
            .setText(text)
            .setSuccess(success)
            .setError(error)
            .setWarning(warning)
            .setSuggestion(suggestion)
            .setUserMessageBackground(light ? "\u001B[47m" : "\u001B[90m")
            .build();
    }

    private Theme createDaltonizedTheme(boolean light) {
        // Colorblind-friendly variants (blue instead of green, etc.)
        return new Theme.Builder()
            .setClaude(light ? "\u001B[38;2;255;153;51m" : "\u001B[38;2;255;153;51m")
            .setText(light ? "\u001B[38;2;0;0;0m" : "\u001B[38;2;255;255;255m")
            .setSuccess(light ? "\u001B[38;2;0;102;153m" : "\u001B[38;2;0;68;102m")
            .setError(light ? "\u001B[38;2;204;0;0m" : "\u001B[38;2;255;102;102m")
            .setWarning(light ? "\u001B[38;2;255;153;0m" : "\u001B[38;2;255;204;0m")
            .setSuggestion("\u001B[38;2;51;102;255m")
            .setUserMessageBackground(light ? "\u001B[38;2;220;220;220m" : "\u001B[38;2;55;55;55m")
            .build();
    }

    /**
     * Set theme by name.
     */
    public void setTheme(ThemeName name) {
        Theme newTheme = themes.get(name);
        if (newTheme != null) {
            this.currentTheme = name;
            this.theme = newTheme;
            // Persist preference
            try {
                ConfigManager.getInstance().set("ui.theme", name.getConfigValue());
            } catch (Exception e) {
                // Config not available, ignore
            }
        }
    }

    /**
     * Set theme by config string.
     */
    public void setTheme(String configValue) {
        ThemeName name = ThemeName.fromConfigValue(configValue);
        setTheme(name);
    }

    /**
     * Auto-detect theme based on terminal settings.
     */
    public void autoDetect() {
        String term = System.getenv("COLORFGBG");
        String termProgram = System.getenv("TERM_PROGRAM");

        if (term != null && term.contains(";")) {
            // Terminal reports background color
            String[] parts = term.split(";");
            if (parts.length >= 2) {
                // If bg is light (7-15), use light theme
                int bg = Integer.parseInt(parts[1]);
                if (bg >= 7 && bg <= 15) {
                    setTheme(ThemeName.LIGHT);
                    return;
                }
            }
        }

        setTheme(ThemeName.DARK);
    }

    /**
     * Load saved theme preference from config.
     */
    public void loadFromConfig() {
        try {
            String saved = ConfigManager.getInstance().get("ui.theme", "dark");
            setTheme(saved);
        } catch (Exception e) {
            setTheme(ThemeName.DARK);
        }
    }

    /**
     * Get current theme.
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Get current theme name.
     */
    public ThemeName getThemeName() {
        return currentTheme;
    }

    /**
     * Toggle shimmer effect.
     */
    public void setShimmerEnabled(boolean enabled) {
        this.shimmerEnabled = enabled;
    }

    /**
     * Check if shimmer is enabled.
     */
    public boolean isShimmerEnabled() {
        return shimmerEnabled;
    }

    /**
     * Check if terminal supports true color.
     */
    public boolean supportsTrueColor() {
        String term = System.getenv("TERM");
        String noColor = System.getenv("NO_COLOR");

        if (noColor != null && !noColor.isEmpty()) {
            return false;
        }

        if (term != null) {
            // Known true color terminals
            String[] trueColorTerms = {
                "xterm-256color", "screen-256color", "tmux",
                "truecolor", "24bit"
            };
            for (String tc : trueColorTerms) {
                if (term.contains(tc)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if terminal is Apple Terminal (limited true color).
     */
    public boolean isAppleTerminal() {
        String termProgram = System.getenv("TERM_PROGRAM");
        return termProgram != null && termProgram.contains("Apple_Terminal");
    }

    /**
     * Get all available theme names.
     */
    public ThemeName[] getAvailableThemes() {
        return ThemeName.values();
    }

    /**
     * Check if theme requires ANSI fallback.
     */
    public boolean needsAnsiFallback() {
        return currentTheme.isAnsi() || !supportsTrueColor();
    }
}
