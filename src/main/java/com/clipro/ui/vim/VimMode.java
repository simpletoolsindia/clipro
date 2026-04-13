package com.clipro.ui.vim;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages Vim state transitions.
 */
public class VimMode {
    private VimState state = VimState.NORMAL;
    private boolean dotRepeat = false;
    private String lastAction = "";
    private final Map<String, String> registers = new HashMap<>();

    public VimMode() {}

    public VimState getState() {
        return state;
    }

    public void setState(VimState state) {
        this.state = state;
    }

    public void enterInsert() {
        this.state = VimState.INSERT;
    }

    public void enterNormal() {
        this.state = VimState.NORMAL;
    }

    public void enterVisual() {
        this.state = VimState.VISUAL;
    }

    public void enterVisualLine() {
        this.state = VimState.VISUAL_LINE;
    }

    public void enterCommand() {
        this.state = VimState.COMMAND;
    }

    public void enterReplace() {
        this.state = VimState.REPLACE;
    }

    public void toggleMode() {
        if (state == VimState.INSERT) {
            enterNormal();
        } else if (state == VimState.NORMAL) {
            enterInsert();
        }
    }

    public boolean isDotRepeat() {
        return dotRepeat;
    }

    public void setDotRepeat(boolean repeat) {
        this.dotRepeat = repeat;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String action) {
        this.lastAction = action;
        this.dotRepeat = true;
    }

    public void saveToRegister(String name, String content) {
        registers.put(name, content);
    }

    public String getFromRegister(String name) {
        return registers.getOrDefault(name, "");
    }

    public String renderMode() {
        return state.getDisplay();
    }

    public String renderIndicator() {
        if (state == VimState.NORMAL) {
            return "";
        }
        return " " + state.getDisplay() + " ";
    }
}
