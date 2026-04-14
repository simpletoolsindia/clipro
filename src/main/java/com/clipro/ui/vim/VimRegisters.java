package com.clipro.ui.vim;

import java.util.Map;
import java.util.HashMap;

/**
 * Vim registers for yank/delete operations.
 * Supports: 0-9, a-z, ", +, *
 */
public class VimRegisters {

    private final Map<Character, String> registers = new HashMap<>();
    private char defaultRegister = '"';
    private char lastYankRegister = '0';

    public VimRegisters() {
        // Initialize number registers (0-9)
        for (char c = '0'; c <= '9'; c++) {
            registers.put(c, "");
        }
        // Initialize letter registers (a-z)
        for (char c = 'a'; c <= 'z'; c++) {
            registers.put(c, "");
        }
    }

    public void set(char register, String value) {
        registers.put(register, value);
        if (register == lastYankRegister) {
            registers.put('"', value); // Update default
        }
    }

    public String get(char register) {
        return registers.getOrDefault(register, "");
    }

    public void yank(String text) {
        set('"', text);
        set(lastYankRegister, text);
    }

    public void delete(String text) {
        set('1', text);
        // Shift register 1->2, 2->3, etc.
        for (char c = '8'; c >= '1'; c--) {
            registers.put(c, registers.getOrDefault((char)(c + 1), ""));
        }
    }

    public void clear(char register) {
        registers.put(register, "");
    }

    public void setDefaultRegister(char r) { this.defaultRegister = r; }
    public char getDefaultRegister() { return defaultRegister; }

    // Clipboard registers
    public void setClipboard(String text) {
        registers.put('+', text);
        registers.put('*', text);
    }

    public String getClipboard() {
        return registers.getOrDefault('+', "");
    }
}
