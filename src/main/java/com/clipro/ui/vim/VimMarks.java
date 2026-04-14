package com.clipro.ui.vim;

/**
 * Vim marks for quick navigation.
 * Supports: a-z (file-local), A-Z (file-global)
 */
public class VimMarks {

    private final java.util.Map<Character, Mark> marks = new java.util.HashMap<>();

    public record Mark(int position, String file) {}

    public void setMark(char name, int position) {
        marks.put(Character.toLowerCase(name), new Mark(position, null));
    }

    public void setMark(char name, int position, String file) {
        marks.put(Character.toLowerCase(name), new Mark(position, file));
    }

    public Mark getMark(char name) {
        return marks.get(Character.toLowerCase(name));
    }

    public Integer getMarkPosition(char name) {
        Mark m = getMark(name);
        return m != null ? m.position() : null;
    }

    public void clearMark(char name) {
        marks.remove(Character.toLowerCase(name));
    }

    public void clearAll() {
        marks.clear();
    }

    public java.util.Set<Character> getMarkNames() {
        return new java.util.HashSet<>(marks.keySet());
    }
}
