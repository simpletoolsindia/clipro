package com.clipro.ui.vim;

/**
 * Vim text objects for advanced vim editing.
 * Supports: iw, aw, i", a", i', a', i{, a{, i(, a(, etc.
 */
public class TextObject {

    public enum TextObjectType {
        INNER_WORD("iw"),
        AROUND_WORD("aw"),
        INNER_QUOTE("i\""),
        AROUND_QUOTE("a\""),
        INNER_SINGLE_QUOTE("i'"),
        AROUND_SINGLE_QUOTE("a'"),
        INNER_BACKTICK("i`"),
        AROUND_BACKTICK("a`"),
        INNER_BRACE("i{"),
        AROUND_BRACE("a{"),
        INNER_PAREN("i("),
        AROUND_PAREN("a("),
        INNER_BRACKET("i["),
        AROUND_BRACKET("a["),
        INNER_ANGLE("i<"),
        AROUND_ANGLE("a<"),
        INNER_SENTENCE("is"),
        AROUND_SENTENCE("as"),
        INNER_PARAGRAPH("ip"),
        AROUND_PARAGRAPH("ap");

        private final String notation;

        TextObjectType(String notation) {
            this.notation = notation;
        }

        public String getNotation() { return notation; }

        public static TextObjectType fromNotation(String notation) {
            for (TextObjectType t : values()) {
                if (t.notation.equals(notation)) return t;
            }
            return null;
        }

        public boolean isInner() { return notation.startsWith("i"); }
    }

    public record Selection(int start, int end) {}

    /**
     * Get selection range for text object at cursor.
     */
    public static Selection getSelection(String text, int cursor, TextObjectType type) {
        return switch (type) {
            case INNER_WORD, AROUND_WORD -> selectWord(text, cursor, type.isInner());
            case INNER_QUOTE, AROUND_QUOTE -> selectQuote(text, cursor, '"', !type.isInner());
            case INNER_SINGLE_QUOTE, AROUND_SINGLE_QUOTE -> selectQuote(text, cursor, '\'', !type.isInner());
            case INNER_BACKTICK, AROUND_BACKTICK -> selectQuote(text, cursor, '`', !type.isInner());
            case INNER_BRACE, AROUND_BRACE -> selectBrace(text, cursor, '{', '}', !type.isInner());
            case INNER_PAREN, AROUND_PAREN -> selectBrace(text, cursor, '(', ')', !type.isInner());
            case INNER_BRACKET, AROUND_BRACKET -> selectBrace(text, cursor, '[', ']', !type.isInner());
            case INNER_ANGLE, AROUND_ANGLE -> selectBrace(text, cursor, '<', '>', !type.isInner());
            default -> new Selection(cursor, cursor);
        };
    }

    private static Selection selectWord(String text, int cursor, boolean inner) {
        if (cursor < 0 || cursor > text.length()) return new Selection(cursor, cursor);

        int start = cursor;
        int end = cursor;

        // Find word boundaries
        while (start > 0 && !isWordChar(text.charAt(start - 1))) start--;
        if (inner) {
            while (start < text.length() && !isWordChar(text.charAt(start))) start++;
        }

        while (end < text.length() && !isWordChar(text.charAt(end))) end++;
        while (end > 0 && isWordChar(text.charAt(end - 1))) end--;

        return new Selection(start, end);
    }

    private static Selection selectQuote(String text, int cursor, char quote, boolean includeQuotes) {
        int first = text.indexOf(quote);
        int last = text.lastIndexOf(quote);

        if (first == -1 || last == first) return new Selection(cursor, cursor);

        // Find quote pair containing cursor
        int start = -1, end = -1;
        for (int i = first; i < last; i++) {
            if (text.charAt(i) == quote && (i == 0 || text.charAt(i - 1) != '\\')) {
                if (start == -1) start = i;
                else if (cursor > start && cursor <= i) {
                    end = i;
                    break;
                }
            }
        }

        if (includeQuotes && start != -1) start--;
        if (includeQuotes && end != -1) end++;

        return new Selection(start, end + 1);
    }

    private static Selection selectBrace(String text, int cursor, char open, char close, boolean includeBraces) {
        int depth = 0;
        int start = -1, end = -1;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == open) {
                if (depth == 0 && start == -1) start = i;
                depth++;
            } else if (c == close) {
                depth--;
                if (depth == 0 && start != -1) {
                    end = i;
                    break;
                }
            }
        }

        if (includeBraces && start != -1) start--;
        if (includeBraces && end != -1) end++;

        return new Selection(start, end + 1);
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
