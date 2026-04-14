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

    // Macro recording state
    private Character recordingRegister = null;
    private StringBuilder recordingBuffer = new StringBuilder();
    private final Map<Character, String> macros = new HashMap<>();

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

    // ===== Macro Support =====

    /**
     * Start recording a macro into the specified register.
     */
    public void startRecording(char register) {
        this.recordingRegister = register;
        this.recordingBuffer.setLength(0);
    }

    /**
     * Stop recording the current macro and save it.
     */
    public void stopRecording() {
        if (recordingRegister != null) {
            macros.put(recordingRegister, recordingBuffer.toString());
            recordingRegister = null;
            recordingBuffer.setLength(0);
        }
    }

    /**
     * Check if currently recording a macro.
     */
    public boolean isRecording() {
        return recordingRegister != null;
    }

    /**
     * Get the register being recorded to.
     */
    public Character getRecordingRegister() {
        return recordingRegister;
    }

    /**
     * Add a keystroke to the current recording.
     */
    public void recordKeystroke(String key) {
        if (isRecording()) {
            recordingBuffer.append(key);
        }
    }

    /**
     * Get a macro from a register.
     */
    public String getMacro(char register) {
        return macros.get(register);
    }

    /**
     * Check if a macro exists for the given register.
     */
    public boolean hasMacro(char register) {
        return macros.containsKey(register);
    }

    /**
     * Play back a macro from the specified register.
     * Returns the macro content or null if not found.
     */
    public String playbackMacro(char register) {
        String macro = macros.get(register);
        if (macro != null) {
            setLastAction("@" + register);
        }
        return macro;
    }
}
