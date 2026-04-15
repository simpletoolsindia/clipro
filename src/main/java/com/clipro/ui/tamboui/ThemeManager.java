package com.clipro.ui.tamboui;

import com.clipro.session.ConfigManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Theme manager providing all 6 OpenClaude themes with auto-detection.
 * Singleton pattern with thread-safe theme switching.
 *
 * Reference: openclaude/src/utils/theme.ts - ALL themes (dark, light, ansi variants, daltonized)
 */
public class ThemeManager {

    private static ThemeManager instance;

    private ThemeName currentTheme = ThemeName.DARK;
    private Theme theme;
    private boolean shimmerEnabled = true;

    // All 6 themes
    private final Map<ThemeName, Theme> themes = new ConcurrentHashMap<>();

    // ANSI color code prefixes
    private static final String RGB = "\u001B[38;2;";
    private static final String RESET = "\u001B[0m";

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
        themes.put(ThemeName.DARK, createDarkTheme());
        themes.put(ThemeName.LIGHT, createLightTheme());
        themes.put(ThemeName.DARK_ANSI, createAnsiTheme(false));
        themes.put(ThemeName.LIGHT_ANSI, createAnsiTheme(true));
        themes.put(ThemeName.DARK_DALTONIZED, createDaltonizedTheme(false));
        themes.put(ThemeName.LIGHT_DALTONIZED, createDaltonizedTheme(true));
    }

    /**
     * Create dark theme - exact match openclaude/src/utils/theme.ts darkTheme
     */
    private Theme createDarkTheme() {
        return new Theme.Builder()
            // Brand colors
            .setClaude(RGB + "215;119;87m")
            .setClaudeShimmer(RGB + "235;159;127m")
            .setClaudeBlue(RGB + "147;165;255m")
            .setClaudeBlueShimmer(RGB + "177;195;255m")
            // Text colors
            .setText(RGB + "255;255;255m")
            .setInverseText(RGB + "0;0;0m")
            .setSubtle(RGB + "80;80;80m")
            .setInactive(RGB + "153;153;153m")
            // Background
            .setBackground(RGB + "0;204;204m")
            .setUserMessageBackground(RGB + "55;55;55m")
            .setUserMessageBackgroundHover(RGB + "70;70;70m")
            .setMessageActionsBackground(RGB + "44;50;62m")
            .setSelectionBg(RGB + "38;79;120m")
            .setBashMessageBackgroundColor(RGB + "65;60;65m")
            .setToolResultBackground("\u001B[48;2;25;25;35m")
            .setMemoryBackgroundColor(RGB + "55;65;70m")
            // Status colors
            .setSuccess(RGB + "78;186;101m")
            .setError(RGB + "255;107;128m")
            .setWarning(RGB + "255;193;7m")
            .setMerged(RGB + "175;135;255m")
            .setSuggestion(RGB + "177;185;249m")
            .setPermission(RGB + "177;185;249m")
            .setAutoAccept(RGB + "175;135;255m")
            // Borders
            .setBashBorder(RGB + "253;93;177m")
            .setPromptBorder(RGB + "136;136;136m")
            .setPromptBorderShimmer(RGB + "166;166;166m")
            // UI elements
            .setPlanMode(RGB + "72;150;140m")
            .setIde(RGB + "71;130;200m")
            .setFastMode(RGB + "255;120;20m")
            .setFastModeShimmer(RGB + "255;165;70m")
            .setRateLimitFill(RGB + "177;185;249m")
            .setRateLimitEmpty(RGB + "80;83;112m")
            .setBriefLabelYou(RGB + "122;180;232m")
            .setBriefLabelClaude(RGB + "215;119;87m")
            .setChromeYellow(RGB + "251;188;4m")
            .setProfessionalBlue(RGB + "106;155;204m")
            // Diff colors
            .setDiffAdded(RGB + "34;92;43m")
            .setDiffRemoved(RGB + "122;41;54m")
            .setDiffAddedDimmed(RGB + "71;88;74m")
            .setDiffRemovedDimmed(RGB + "105;72;77m")
            .setDiffAddedWord(RGB + "56;166;96m")
            .setDiffRemovedWord(RGB + "179;89;107m")
            // Agent colors (8)
            .setAgentRed(RGB + "220;38;38m")
            .setAgentBlue(RGB + "37;99;235m")
            .setAgentGreen(RGB + "22;163;74m")
            .setAgentYellow(RGB + "202;138;4m")
            .setAgentPurple(RGB + "147;51;234m")
            .setAgentOrange(RGB + "234;88;12m")
            .setAgentPink(RGB + "219;39;119m")
            .setAgentCyan(RGB + "8;145;178m")
            // Rainbow colors (base)
            .setRainbowRed(RGB + "235;95;87m")
            .setRainbowOrange(RGB + "245;139;87m")
            .setRainbowYellow(RGB + "250;195;95m")
            .setRainbowGreen(RGB + "145;200;130m")
            .setRainbowBlue(RGB + "130;170;220m")
            .setRainbowIndigo(RGB + "155;130;200m")
            .setRainbowViolet(RGB + "200;130;180m")
            // Rainbow shimmer
            .setRainbowRedShimmer(RGB + "250;155;147m")
            .setRainbowOrangeShimmer(RGB + "255;185;137m")
            .setRainbowYellowShimmer(RGB + "255;225;155m")
            .setRainbowGreenShimmer(RGB + "185;230;180m")
            .setRainbowBlueShimmer(RGB + "180;205;240m")
            .setRainbowIndigoShimmer(RGB + "195;180;230m")
            .setRainbowVioletShimmer(RGB + "230;180;210m")
            .build();
    }

    /**
     * Create light theme - exact match openclaude/src/utils/theme.ts lightTheme
     */
    private Theme createLightTheme() {
        return new Theme.Builder()
            // Brand colors (same as dark for claude)
            .setClaude(RGB + "215;119;87m")
            .setClaudeShimmer(RGB + "245;149;117m")
            .setClaudeBlue(RGB + "87;105;247m")
            .setClaudeBlueShimmer(RGB + "117;135;255m")
            // Text colors
            .setText(RGB + "0;0;0m")
            .setInverseText(RGB + "255;255;255m")
            .setSubtle(RGB + "175;175;175m")
            .setInactive(RGB + "102;102;102m")
            // Background
            .setBackground(RGB + "0;153;153m")
            .setUserMessageBackground(RGB + "240;240;240m")
            .setUserMessageBackgroundHover(RGB + "252;252;252m")
            .setMessageActionsBackground(RGB + "232;236;244m")
            .setSelectionBg(RGB + "180;213;255m")
            .setBashMessageBackgroundColor(RGB + "250;245;250m")
            .setToolResultBackground("\u001B[48;2;250;245;250m")
            .setMemoryBackgroundColor(RGB + "230;245;250m")
            // Status colors
            .setSuccess(RGB + "44;122;57m")
            .setError(RGB + "171;43;63m")
            .setWarning(RGB + "150;108;30m")
            .setMerged(RGB + "135;0;255m")
            .setSuggestion(RGB + "87;105;247m")
            .setPermission(RGB + "87;105;247m")
            .setAutoAccept(RGB + "135;0;255m")
            // Borders
            .setBashBorder(RGB + "255;0;135m")
            .setPromptBorder(RGB + "153;153;153m")
            .setPromptBorderShimmer(RGB + "183;183;183m")
            // UI elements
            .setPlanMode(RGB + "0;102;102m")
            .setIde(RGB + "71;130;200m")
            .setFastMode(RGB + "255;106;0m")
            .setFastModeShimmer(RGB + "255;150;50m")
            .setRateLimitFill(RGB + "87;105;247m")
            .setRateLimitEmpty(RGB + "39;47;111m")
            .setBriefLabelYou(RGB + "37;99;235m")
            .setBriefLabelClaude(RGB + "215;119;87m")
            .setChromeYellow(RGB + "251;188;4m")
            .setProfessionalBlue(RGB + "106;155;204m")
            // Diff colors
            .setDiffAdded(RGB + "105;219;124m")
            .setDiffRemoved(RGB + "255;168;180m")
            .setDiffAddedDimmed(RGB + "199;225;203m")
            .setDiffRemovedDimmed(RGB + "253;210;216m")
            .setDiffAddedWord(RGB + "47;157;68m")
            .setDiffRemovedWord(RGB + "209;69;75m")
            // Agent colors (same as dark)
            .setAgentRed(RGB + "220;38;38m")
            .setAgentBlue(RGB + "37;99;235m")
            .setAgentGreen(RGB + "22;163;74m")
            .setAgentYellow(RGB + "202;138;4m")
            .setAgentPurple(RGB + "147;51;234m")
            .setAgentOrange(RGB + "234;88;12m")
            .setAgentPink(RGB + "219;39;119m")
            .setAgentCyan(RGB + "8;145;178m")
            // Rainbow colors (same as dark)
            .setRainbowRed(RGB + "235;95;87m")
            .setRainbowOrange(RGB + "245;139;87m")
            .setRainbowYellow(RGB + "250;195;95m")
            .setRainbowGreen(RGB + "145;200;130m")
            .setRainbowBlue(RGB + "130;170;220m")
            .setRainbowIndigo(RGB + "155;130;200m")
            .setRainbowViolet(RGB + "200;130;180m")
            // Rainbow shimmer (same as dark)
            .setRainbowRedShimmer(RGB + "250;155;147m")
            .setRainbowOrangeShimmer(RGB + "255;185;137m")
            .setRainbowYellowShimmer(RGB + "255;225;155m")
            .setRainbowGreenShimmer(RGB + "185;230;180m")
            .setRainbowBlueShimmer(RGB + "180;205;240m")
            .setRainbowIndigoShimmer(RGB + "195;180;230m")
            .setRainbowVioletShimmer(RGB + "230;180;210m")
            .build();
    }

    /**
     * Create ANSI theme (16-color fallback) - exact match openclaude lightAnsiTheme/darkAnsiTheme
     */
    private Theme createAnsiTheme(boolean light) {
        // 16 standard ANSI colors
        String text = light ? "\u001B[30m" : "\u001B[37m";
        String claude = light ? "\u001B[31m" : "\u001B[91m";
        String success = "\u001B[32m";
        String error = "\u001B[31m";
        String warning = "\u001B[33m";
        String cyan = "\u001B[36m";
        String blue = "\u001B[34m";
        String magenta = "\u001B[35m";
        String white = light ? "\u001B[37m" : "\u001B[90m";
        String bgWhite = light ? "\u001B[47m" : "\u001B[90m";
        String bgBlack = light ? "\u001B[47m" : "\u001B[40m";

        return new Theme.Builder()
            .setClaude(claude)
            .setClaudeShimmer(light ? "\u001B[33m" : "\u001B[93m")
            .setClaudeBlue(blue)
            .setClaudeBlueShimmer(light ? "\u001B[34m" : "\u001B[94m")
            .setText(text)
            .setInverseText(white)
            .setSubtle(text)
            .setInactive(text)
            .setBackground(cyan)
            .setSuccess(success)
            .setError(error)
            .setWarning(warning)
            .setMerged(magenta)
            .setSuggestion(blue)
            .setPermission(blue)
            .setAutoAccept(magenta)
            .setBashBorder(magenta)
            .setPromptBorder(white)
            .setPromptBorderShimmer(white)
            .setPlanMode(cyan)
            .setIde(blue)
            .setFastMode(warning)
            .setFastModeShimmer(warning)
            .setRateLimitFill(blue)
            .setRateLimitEmpty(text)
            .setBriefLabelYou(blue)
            .setBriefLabelClaude(claude)
            .setChromeYellow(warning)
            .setProfessionalBlue(blue)
            .setDiffAdded(success)
            .setDiffRemoved(error)
            .setDiffAddedDimmed(success)
            .setDiffRemovedDimmed(error)
            .setDiffAddedWord(success)
            .setDiffRemovedWord(error)
            .setAgentRed(error)
            .setAgentBlue(blue)
            .setAgentGreen(success)
            .setAgentYellow(warning)
            .setAgentPurple(magenta)
            .setAgentOrange(error)
            .setAgentPink(magenta)
            .setAgentCyan(cyan)
            .setRainbowRed(error)
            .setRainbowOrange(warning)
            .setRainbowYellow(warning)
            .setRainbowGreen(success)
            .setRainbowBlue(blue)
            .setRainbowIndigo(magenta)
            .setRainbowViolet(magenta)
            .setRainbowRedShimmer(light ? "\u001B[91m" : "\u001B[31m")
            .setRainbowOrangeShimmer(warning)
            .setRainbowYellowShimmer(warning)
            .setRainbowGreenShimmer(success)
            .setRainbowBlueShimmer(light ? "\u001B[94m" : "\u001B[34m")
            .setRainbowIndigoShimmer(magenta)
            .setRainbowVioletShimmer(magenta)
            .setUserMessageBackground(bgWhite)
            .setUserMessageBackgroundHover(bgWhite)
            .setMessageActionsBackground(bgWhite)
            .setSelectionBg(cyan)
            .setBashMessageBackgroundColor(bgWhite)
            .setToolResultBackground(bgWhite)
            .setMemoryBackgroundColor(bgWhite)
            .build();
    }

    /**
     * Create daltonized theme (colorblind-friendly) - exact match openclaude darkDaltonizedTheme/lightDaltonizedTheme
     */
    private Theme createDaltonizedTheme(boolean light) {
        return new Theme.Builder()
            // Brand colors (adjusted for colorblind)
            .setClaude(RGB + "255;153;51m")
            .setClaudeShimmer(RGB + "255;183;101m")
            .setClaudeBlue(RGB + "153;204;255m")
            .setClaudeBlueShimmer(RGB + "183;224;255m")
            // Text colors
            .setText(light ? RGB + "0;0;0m" : RGB + "255;255;255m")
            .setInverseText(light ? RGB + "255;255;255m" : RGB + "0;0;0m")
            .setSubtle(light ? RGB + "175;175;175m" : RGB + "80;80;80m")
            .setInactive(light ? RGB + "102;102;102m" : RGB + "153;153;153m")
            // Background
            .setBackground(RGB + "0;204;204m")
            .setUserMessageBackground(RGB + "240;240;240m")
            .setUserMessageBackgroundHover(RGB + "252;252;252m")
            .setMessageActionsBackground(RGB + "232;236;244m")
            .setSelectionBg(RGB + "180;213;255m")
            .setBashMessageBackgroundColor(RGB + "250;245;250m")
            .setToolResultBackground("\u001B[48;2;250;245;250m")
            .setMemoryBackgroundColor(RGB + "230;245;250m")
            // Status colors (daltonized - blue instead of green, etc.)
            .setSuccess(light ? RGB + "0;102;153m" : RGB + "0;68;102m")
            .setError(light ? RGB + "204;0;0m" : RGB + "255;102;102m")
            .setWarning(light ? RGB + "255;153;0m" : RGB + "255;204;0m")
            .setMerged(RGB + "175;135;255m")
            .setSuggestion(RGB + "153;204;255m")
            .setPermission(RGB + "153;204;255m")
            .setAutoAccept(RGB + "175;135;255m")
            // Borders
            .setBashBorder(light ? RGB + "255;0;135m" : RGB + "51;153;255m")
            .setPromptBorder(RGB + "136;136;136m")
            .setPromptBorderShimmer(RGB + "166;166;166m")
            // UI elements
            .setPlanMode(RGB + "102;153;153m")
            .setIde(RGB + "71;130;200m")
            .setFastMode(RGB + "255;120;20m")
            .setFastModeShimmer(RGB + "255;165;70m")
            .setRateLimitFill(RGB + "153;204;255m")
            .setRateLimitEmpty(RGB + "69;92;115m")
            .setBriefLabelYou(RGB + "153;204;255m")
            .setBriefLabelClaude(RGB + "255;153;51m")
            .setChromeYellow(RGB + "251;188;4m")
            .setProfessionalBlue(RGB + "106;155;204m")
            // Diff colors (daltonized)
            .setDiffAdded(RGB + "0;68;102m")
            .setDiffRemoved(RGB + "102;0;0m")
            .setDiffAddedDimmed(RGB + "62;81;91m")
            .setDiffRemovedDimmed(RGB + "62;44;44m")
            .setDiffAddedWord(RGB + "0;119;179m")
            .setDiffRemovedWord(RGB + "179;0;0m")
            // Agent colors (daltonized - brighter)
            .setAgentRed(RGB + "255;102;102m")
            .setAgentBlue(RGB + "102;178;255m")
            .setAgentGreen(RGB + "102;255;102m")
            .setAgentYellow(RGB + "255;255;102m")
            .setAgentPurple(RGB + "178;102;255m")
            .setAgentOrange(RGB + "255;178;102m")
            .setAgentPink(RGB + "255;153;204m")
            .setAgentCyan(RGB + "102;204;204m")
            // Rainbow colors (same)
            .setRainbowRed(RGB + "235;95;87m")
            .setRainbowOrange(RGB + "245;139;87m")
            .setRainbowYellow(RGB + "250;195;95m")
            .setRainbowGreen(RGB + "145;200;130m")
            .setRainbowBlue(RGB + "130;170;220m")
            .setRainbowIndigo(RGB + "155;130;200m")
            .setRainbowViolet(RGB + "200;130;180m")
            // Rainbow shimmer (same)
            .setRainbowRedShimmer(RGB + "250;155;147m")
            .setRainbowOrangeShimmer(RGB + "255;185;137m")
            .setRainbowYellowShimmer(RGB + "255;225;155m")
            .setRainbowGreenShimmer(RGB + "185;230;180m")
            .setRainbowBlueShimmer(RGB + "180;205;240m")
            .setRainbowIndigoShimmer(RGB + "195;180;230m")
            .setRainbowVioletShimmer(RGB + "230;180;210m")
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
        if (term != null && term.contains(";")) {
            String[] parts = term.split(";");
            if (parts.length >= 2) {
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
