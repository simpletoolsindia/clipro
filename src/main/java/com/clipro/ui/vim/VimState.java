package com.clipro.ui.vim;

/**
 * Vim state enum for modal editing.
 * Reference: openclaude vim keybindings
 */
public enum VimState {
    NORMAL(""),
    INSERT("INSERT"),
    VISUAL("VISUAL"),
    VISUAL_LINE("VISUAL"),
    COMMAND(":"),
    REPLACE("REPLACE");

    private final String display;

    VimState(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public boolean isInsert() {
        return this == INSERT;
    }

    public boolean isNormal() {
        return this == NORMAL;
    }

    public boolean isVisual() {
        return this == VISUAL || this == VISUAL_LINE;
    }

    public boolean isCommand() {
        return this == COMMAND;
    }

    public boolean isReplace() {
        return this == REPLACE;
    }
}
