package com.clipro.ui.tamboui;

/**
 * Complete theme data matching OpenClaude's theme.ts Theme type.
 * Contains all 80+ colors including rainbow, agent colors, diff colors, shimmer pairs.
 *
 * Reference: openclaude/src/utils/theme.ts
 */
public class Theme {

    // Brand colors
    private final String claude;
    private final String claudeShimmer;
    private final String claudeBlue;
    private final String claudeBlueShimmer;

    // Semantic colors
    private final String text;
    private final String inverseText;
    private final String subtle;
    private final String inactive;
    private final String background;

    // Status colors
    private final String success;
    private final String error;
    private final String warning;
    private final String merged;
    private final String suggestion;
    private final String permission;

    // Backgrounds
    private final String userMessageBackground;
    private final String userMessageBackgroundHover;
    private final String messageActionsBackground;
    private final String selectionBg;
    private final String bashMessageBackgroundColor;
    private final String memoryBackgroundColor;

    // Diff colors (6 variants)
    private final String diffAdded;
    private final String diffRemoved;
    private final String diffAddedDimmed;
    private final String diffRemovedDimmed;
    private final String diffAddedWord;
    private final String diffRemovedWord;

    // Rainbow colors (14 - base + shimmer)
    private final String rainbowRed;
    private final String rainbowOrange;
    private final String rainbowYellow;
    private final String rainbowGreen;
    private final String rainbowBlue;
    private final String rainbowIndigo;
    private final String rainbowViolet;
    private final String rainbowRedShimmer;
    private final String rainbowOrangeShimmer;
    private final String rainbowYellowShimmer;
    private final String rainbowGreenShimmer;
    private final String rainbowBlueShimmer;
    private final String rainbowIndigoShimmer;
    private final String rainbowVioletShimmer;

    // Agent colors (8 for sub-agents)
    private final String agentRed;
    private final String agentBlue;
    private final String agentGreen;
    private final String agentYellow;
    private final String agentPurple;
    private final String agentOrange;
    private final String agentPink;
    private final String agentCyan;

    // UI colors
    private final String planMode;
    private final String ide;
    private final String autoAccept;
    private final String bashBorder;
    private final String promptBorder;
    private final String promptBorderShimmer;

    // Special
    private final String fastMode;
    private final String fastModeShimmer;
    private final String rateLimitFill;
    private final String rateLimitEmpty;
    private final String briefLabelYou;
    private final String briefLabelClaude;
    private final String chromeYellow;
    private final String professionalBlue;

    // Constructor is private - use ThemeBuilder or ThemeFactory
    private Theme(Builder b) {
        this.claude = b.claude;
        this.claudeShimmer = b.claudeShimmer;
        this.claudeBlue = b.claudeBlue;
        this.claudeBlueShimmer = b.claudeBlueShimmer;
        this.text = b.text;
        this.inverseText = b.inverseText;
        this.subtle = b.subtle;
        this.inactive = b.inactive;
        this.background = b.background;
        this.success = b.success;
        this.error = b.error;
        this.warning = b.warning;
        this.merged = b.merged;
        this.suggestion = b.suggestion;
        this.permission = b.permission;
        this.userMessageBackground = b.userMessageBackground;
        this.userMessageBackgroundHover = b.userMessageBackgroundHover;
        this.messageActionsBackground = b.messageActionsBackground;
        this.selectionBg = b.selectionBg;
        this.bashMessageBackgroundColor = b.bashMessageBackgroundColor;
        this.memoryBackgroundColor = b.memoryBackgroundColor;
        this.diffAdded = b.diffAdded;
        this.diffRemoved = b.diffRemoved;
        this.diffAddedDimmed = b.diffAddedDimmed;
        this.diffRemovedDimmed = b.diffRemovedDimmed;
        this.diffAddedWord = b.diffAddedWord;
        this.diffRemovedWord = b.diffRemovedWord;
        this.rainbowRed = b.rainbowRed;
        this.rainbowOrange = b.rainbowOrange;
        this.rainbowYellow = b.rainbowYellow;
        this.rainbowGreen = b.rainbowGreen;
        this.rainbowBlue = b.rainbowBlue;
        this.rainbowIndigo = b.rainbowIndigo;
        this.rainbowViolet = b.rainbowViolet;
        this.rainbowRedShimmer = b.rainbowRedShimmer;
        this.rainbowOrangeShimmer = b.rainbowOrangeShimmer;
        this.rainbowYellowShimmer = b.rainbowYellowShimmer;
        this.rainbowGreenShimmer = b.rainbowGreenShimmer;
        this.rainbowBlueShimmer = b.rainbowBlueShimmer;
        this.rainbowIndigoShimmer = b.rainbowIndigoShimmer;
        this.rainbowVioletShimmer = b.rainbowVioletShimmer;
        this.agentRed = b.agentRed;
        this.agentBlue = b.agentBlue;
        this.agentGreen = b.agentGreen;
        this.agentYellow = b.agentYellow;
        this.agentPurple = b.agentPurple;
        this.agentOrange = b.agentOrange;
        this.agentPink = b.agentPink;
        this.agentCyan = b.agentCyan;
        this.planMode = b.planMode;
        this.ide = b.ide;
        this.autoAccept = b.autoAccept;
        this.bashBorder = b.bashBorder;
        this.promptBorder = b.promptBorder;
        this.promptBorderShimmer = b.promptBorderShimmer;
        this.fastMode = b.fastMode;
        this.fastModeShimmer = b.fastModeShimmer;
        this.rateLimitFill = b.rateLimitFill;
        this.rateLimitEmpty = b.rateLimitEmpty;
        this.briefLabelYou = b.briefLabelYou;
        this.briefLabelClaude = b.briefLabelClaude;
        this.chromeYellow = b.chromeYellow;
        this.professionalBlue = b.professionalBlue;
    }

    // Getters
    public String getClaude() { return claude; }
    public String getClaudeShimmer() { return claudeShimmer; }
    public String getClaudeBlue() { return claudeBlue; }
    public String getClaudeBlueShimmer() { return claudeBlueShimmer; }
    public String getText() { return text; }
    public String getInverseText() { return inverseText; }
    public String getSubtle() { return subtle; }
    public String getInactive() { return inactive; }
    public String getBackground() { return background; }
    public String getSuccess() { return success; }
    public String getError() { return error; }
    public String getWarning() { return warning; }
    public String getMerged() { return merged; }
    public String getSuggestion() { return suggestion; }
    public String getPermission() { return permission; }
    public String getUserMessageBackground() { return userMessageBackground; }
    public String getUserMessageBackgroundHover() { return userMessageBackgroundHover; }
    public String getMessageActionsBackground() { return messageActionsBackground; }
    public String getSelectionBg() { return selectionBg; }
    public String getBashMessageBackgroundColor() { return bashMessageBackgroundColor; }
    public String getMemoryBackgroundColor() { return memoryBackgroundColor; }
    public String getDiffAdded() { return diffAdded; }
    public String getDiffRemoved() { return diffRemoved; }
    public String getDiffAddedDimmed() { return diffAddedDimmed; }
    public String getDiffRemovedDimmed() { return diffRemovedDimmed; }
    public String getDiffAddedWord() { return diffAddedWord; }
    public String getDiffRemovedWord() { return diffRemovedWord; }
    public String getRainbowRed() { return rainbowRed; }
    public String getRainbowOrange() { return rainbowOrange; }
    public String getRainbowYellow() { return rainbowYellow; }
    public String getRainbowGreen() { return rainbowGreen; }
    public String getRainbowBlue() { return rainbowBlue; }
    public String getRainbowIndigo() { return rainbowIndigo; }
    public String getRainbowViolet() { return rainbowViolet; }
    public String getRainbowRedShimmer() { return rainbowRedShimmer; }
    public String getRainbowOrangeShimmer() { return rainbowOrangeShimmer; }
    public String getRainbowYellowShimmer() { return rainbowYellowShimmer; }
    public String getRainbowGreenShimmer() { return rainbowGreenShimmer; }
    public String getRainbowBlueShimmer() { return rainbowBlueShimmer; }
    public String getRainbowIndigoShimmer() { return rainbowIndigoShimmer; }
    public String getRainbowVioletShimmer() { return rainbowVioletShimmer; }
    public String getAgentRed() { return agentRed; }
    public String getAgentBlue() { return agentBlue; }
    public String getAgentGreen() { return agentGreen; }
    public String getAgentYellow() { return agentYellow; }
    public String getAgentPurple() { return agentPurple; }
    public String getAgentOrange() { return agentOrange; }
    public String getAgentPink() { return agentPink; }
    public String getAgentCyan() { return agentCyan; }
    public String getPlanMode() { return planMode; }
    public String getIde() { return ide; }
    public String getAutoAccept() { return autoAccept; }
    public String getBashBorder() { return bashBorder; }
    public String getPromptBorder() { return promptBorder; }
    public String getPromptBorderShimmer() { return promptBorderShimmer; }
    public String getFastMode() { return fastMode; }
    public String getFastModeShimmer() { return fastModeShimmer; }
    public String getRateLimitFill() { return rateLimitFill; }
    public String getRateLimitEmpty() { return rateLimitEmpty; }
    public String getBriefLabelYou() { return briefLabelYou; }
    public String getBriefLabelClaude() { return briefLabelClaude; }
    public String getChromeYellow() { return chromeYellow; }
    public String getProfessionalBlue() { return professionalBlue; }

    /**
     * Get rainbow color by index (0-6).
     */
    public String getRainbowColor(int index) {
        String[] colors = { rainbowRed, rainbowOrange, rainbowYellow, rainbowGreen,
                           rainbowBlue, rainbowIndigo, rainbowViolet };
        return colors[Math.abs(index) % 7];
    }

    /**
     * Get shimmer rainbow color by index (0-6).
     */
    public String getRainbowShimmerColor(int index) {
        String[] colors = { rainbowRedShimmer, rainbowOrangeShimmer, rainbowYellowShimmer,
                           rainbowGreenShimmer, rainbowBlueShimmer, rainbowIndigoShimmer,
                           rainbowVioletShimmer };
        return colors[Math.abs(index) % 7];
    }

    /**
     * Get agent color by index (0-7).
     */
    public String getAgentColor(int index) {
        String[] colors = { agentRed, agentBlue, agentGreen, agentYellow,
                           agentPurple, agentOrange, agentPink, agentCyan };
        return colors[Math.abs(index) % 8];
    }

    /**
     * Builder for Theme objects.
     */
    public static class Builder {
        // All fields with defaults matching dark theme
        private String claude = "\u001B[38;2;215;119;87m";
        private String claudeShimmer = "\u001B[38;2;235;159;127m";
        private String claudeBlue = "\u001B[38;2;147;165;255m";
        private String claudeBlueShimmer = "\u001B[38;2;177;195;255m";
        private String text = "\u001B[38;2;255;255;255m";
        private String inverseText = "\u001B[38;2;0;0;0m";
        private String subtle = "\u001B[38;2;80;80;80m";
        private String inactive = "\u001B[38;2;153;153;153m";
        private String background = "\u001B[38;2;0;204;204m";
        private String success = "\u001B[38;2;78;186;101m";
        private String error = "\u001B[38;2;255;107;128m";
        private String warning = "\u001B[38;2;255;193;7m";
        private String merged = "\u001B[38;2;175;135;255m";
        private String suggestion = "\u001B[38;2;177;185;249m";
        private String permission = "\u001B[38;2;177;185;249m";
        private String userMessageBackground = "\u001B[38;2;55;55;55m";
        private String userMessageBackgroundHover = "\u001B[38;2;70;70;70m";
        private String messageActionsBackground = "\u001B[38;2;44;50;62m";
        private String selectionBg = "\u001B[38;2;38;79;120m";
        private String bashMessageBackgroundColor = "\u001B[38;2;65;60;65m";
        private String memoryBackgroundColor = "\u001B[38;2;55;65;70m";
        private String diffAdded = "\u001B[38;2;34;92;43m";
        private String diffRemoved = "\u001B[38;2;122;41;54m";
        private String diffAddedDimmed = "\u001B[38;2;71;88;74m";
        private String diffRemovedDimmed = "\u001B[38;2;105;72;77m";
        private String diffAddedWord = "\u001B[38;2;56;166;96m";
        private String diffRemovedWord = "\u001B[38;2;179;89;107m";
        private String rainbowRed = "\u001B[38;2;235;95;87m";
        private String rainbowOrange = "\u001B[38;2;245;139;87m";
        private String rainbowYellow = "\u001B[38;2;250;195;95m";
        private String rainbowGreen = "\u001B[38;2;145;200;130m";
        private String rainbowBlue = "\u001B[38;2;130;170;220m";
        private String rainbowIndigo = "\u001B[38;2;155;130;200m";
        private String rainbowViolet = "\u001B[38;2;200;130;180m";
        private String rainbowRedShimmer = "\u001B[38;2;250;155;147m";
        private String rainbowOrangeShimmer = "\u001B[38;2;255;185;137m";
        private String rainbowYellowShimmer = "\u001B[38;2;255;225;155m";
        private String rainbowGreenShimmer = "\u001B[38;2;185;230;180m";
        private String rainbowBlueShimmer = "\u001B[38;2;180;205;240m";
        private String rainbowIndigoShimmer = "\u001B[38;2;195;180;230m";
        private String rainbowVioletShimmer = "\u001B[38;2;230;180;210m";
        private String agentRed = "\u001B[38;2;220;38;38m";
        private String agentBlue = "\u001B[38;2;37;99;235m";
        private String agentGreen = "\u001B[38;2;22;163;74m";
        private String agentYellow = "\u001B[38;2;202;138;4m";
        private String agentPurple = "\u001B[38;2;147;51;234m";
        private String agentOrange = "\u001B[38;2;234;88;12m";
        private String agentPink = "\u001B[38;2;219;39;119m";
        private String agentCyan = "\u001B[38;2;8;145;178m";
        private String planMode = "\u001B[38;2;72;150;140m";
        private String ide = "\u001B[38;2;71;130;200m";
        private String autoAccept = "\u001B[38;2;175;135;255m";
        private String bashBorder = "\u001B[38;2;253;93;177m";
        private String promptBorder = "\u001B[38;2;136;136;136m";
        private String promptBorderShimmer = "\u001B[38;2;166;166;166m";
        private String fastMode = "\u001B[38;2;255;120;20m";
        private String fastModeShimmer = "\u001B[38;2;255;165;70m";
        private String rateLimitFill = "\u001B[38;2;177;185;249m";
        private String rateLimitEmpty = "\u001B[38;2;80;83;112m";
        private String briefLabelYou = "\u001B[38;2;122;180;232m";
        private String briefLabelClaude = "\u001B[38;2;215;119;87m";
        private String chromeYellow = "\u001B[38;2;251;188;4m";
        private String professionalBlue = "\u001B[38;2;106;155;204m";

        public Builder setClaude(String v) { this.claude = v; return this; }
        public Builder setText(String v) { this.text = v; return this; }
        public Builder setSuccess(String v) { this.success = v; return this; }
        public Builder setError(String v) { this.error = v; return this; }
        public Builder setWarning(String v) { this.warning = v; return this; }
        public Builder setBackground(String v) { this.background = v; return this; }
        public Builder setUserMessageBackground(String v) { this.userMessageBackground = v; return this; }
        public Builder setSuggestion(String v) { this.suggestion = v; return this; }
        public Theme build() { return new Theme(this); }
    }
}
