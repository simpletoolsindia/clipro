package com.clipro.ui.components;

import com.clipro.ui.tamboui.ThemeManager;
import java.util.*;
import java.util.regex.*;

/**
 * Syntax highlighter for 18+ programming languages.
 * Token-based highlighting with ANSI colors.
 */
public class SyntaxHighlighter {

    private final ThemeManager themeManager;
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    // Language patterns
    private static final Map<String, LanguageDef> LANGUAGES = new HashMap<>();

    static {
        // JavaScript/TypeScript
        LANGUAGES.put("javascript", new LanguageDef("javascript", Set.of("js", "mjs", "cjs"),
            Map.of("keyword", "\\b(function|const|let|var|if|else|for|while|return|class|extends|import|export|from|async|await|try|catch|throw|new|this|super|static|get|set|typeof|instanceof|in|of|yield|default|switch|case|break|continue)\\b",
                "string", "\"[^\"]*\"|'[^']*'|`[^`]*`",
                "comment", "//.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?\\b",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:]+"),
            Set.of()));

        LANGUAGES.put("typescript", new LanguageDef("typescript", Set.of("ts", "tsx", "mts"),
            Map.of("keyword", "\\b(function|const|let|var|if|else|for|while|return|class|extends|import|export|from|async|await|try|catch|throw|new|this|super|static|get|set|typeof|instanceof|in|of|yield|default|switch|case|break|continue|type|interface|enum|implements|private|public|protected|readonly|abstract|as|is|keyof|never|unknown|void)\\b",
                "string", "\"[^\"]*\"|'[^']*'|`[^`]*`",
                "comment", "//.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?\\b",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:]+"),
            Set.of("javascript")));

        // Python
        LANGUAGES.put("python", new LanguageDef("python", Set.of("py", "pyw", "pyi"),
            Map.of("keyword", "\\b(def|class|if|elif|else|for|while|return|import|from|as|try|except|finally|raise|with|lambda|yield|global|nonlocal|pass|break|continue|and|or|not|in|is|True|False|None|async|await|assert|del|print)\\b",
                "string", "\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"[^\"]*\"|'[^']*'",
                "comment", "#.*$",
                "number", "\\b\\d+(\\.\\d+)?\\b",
                "function", "@\\w+|\\b\\w+(?=\\s*\\()",
                "decorator", "@\\w+",
                "operator", "[+\\-*/%=<>!&|^~@:]+"),
            Set.of()));

        // Java
        LANGUAGES.put("java", new LanguageDef("java", Set.of("java"),
            Map.of("keyword", "\\b(public|private|protected|static|final|abstract|synchronized|volatile|transient|native|strictfp|class|interface|extends|implements|new|this|super|if|else|for|while|do|switch|case|default|break|continue|return|try|catch|finally|throw|throws|import|package|void|byte|short|int|long|float|double|char|boolean|enum|assert|instanceof|var)\\b",
                "string", "\"[^\"]*\"|'[^']*'",
                "comment", "//.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?[fFdDlL]?\\b",
                "annotation", "@\\w+",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:]+"),
            Set.of()));

        // Go
        LANGUAGES.put("go", new LanguageDef("go", Set.of("go"),
            Map.of("keyword", "\\b(func|package|import|var|const|type|struct|interface|map|chan|go|defer|select|case|default|if|else|for|range|switch|return|break|continue|goto|fallthrough|range)\\b",
                "string", "\"[^\"]*\"|`[^`]*`|'[^']*'",
                "comment", "//.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?\\b",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:]+"),
            Set.of()));

        // Rust
        LANGUAGES.put("rust", new LanguageDef("rust", Set.of("rs"),
            Map.of("keyword", "\\b(fn|let|mut|const|static|struct|enum|impl|trait|type|where|pub|crate|mod|use|self|super|if|else|match|for|while|loop|break|continue|return|async|await|move|ref|unsafe|extern|as|in|dyn|box|self|Some|None|Ok|Err)\\b",
                "string", "\"[^\"]*\"|r#*\"[\\s\\S]*?\"#*",
                "comment", "//.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?([fiu]\\d+)?\\b",
                "macro", "\\w+!",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:]+"),
            Set.of()));

        // C/C++
        LANGUAGES.put("c", new LanguageDef("c", Set.of("c", "h"),
            Map.of("keyword", "\\b(if|else|for|while|do|switch|case|default|break|continue|return|goto|sizeof|typedef|struct|union|enum|extern|static|const|volatile|register|auto|void|char|short|int|long|float|double|signed|unsigned|inline|restrict|_Bool|_Complex|_Imaginary)\\b",
                "string", "\"[^\"]*\"|'[^']*'",
                "comment", "//.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?[ulUL]*\\b|0x[0-9a-fA-F]+",
                "preprocessor", "#\\w+.*$",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:]+"),
            Set.of()));

        LANGUAGES.put("cpp", new LanguageDef("cpp", Set.of("cpp", "hpp", "cc", "cxx", "c++"),
            Map.of("keyword", "\\b(if|else|for|while|do|switch|case|default|break|continue|return|goto|sizeof|typedef|struct|union|enum|extern|static|const|volatile|register|auto|void|char|short|int|long|float|double|signed|unsigned|inline|restrict|class|public|private|protected|virtual|override|final|new|delete|this|template|typename|namespace|using|try|catch|throw|noexcept|constexpr|decltype|auto|nullptr|true|false|static_assert|alignas|alignof|friend|operator)\\b",
                "string", "\"[^\"]*\"|'[^']*'",
                "comment", "//.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?[ulUL]*\\b|0x[0-9a-fA-F]+",
                "preprocessor", "#\\w+.*$",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:]+"),
            Set.of("c")));

        // JSON
        LANGUAGES.put("json", new LanguageDef("json", Set.of("json"),
            Map.of("string", "\"[^\"]*\"",
                "number", "-?\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b",
                "keyword", "\\b(true|false|null)\\b",
                "punctuation", "[{}\\[\\]:,]"),
            Set.of()));

        // YAML
        LANGUAGES.put("yaml", new LanguageDef("yaml", Set.of("yaml", "yml"),
            Map.of("comment", "#.*$",
                "key", "^\\s*\\w+(?=\\s*:)",
                "string", "\"[^\"]*\"|'[^']*'",
                "number", "\\b\\d+(\\.\\d+)?\\b",
                "keyword", "\\b(true|false|null|yes|no|on|off)\\b",
                "anchor", "&\\w+|\\*\\w+",
                "punctuation", "[\\[\\]{}:,]"),
            Set.of()));

        // SQL
        LANGUAGES.put("sql", new LanguageDef("sql", Set.of("sql"),
            Map.of("keyword", "\\b(SELECT|FROM|WHERE|INSERT|UPDATE|DELETE|CREATE|DROP|ALTER|TABLE|INDEX|VIEW|DATABASE|SCHEMA|JOIN|INNER|LEFT|RIGHT|OUTER|ON|AND|OR|NOT|IN|BETWEEN|LIKE|IS|NULL|AS|ORDER|BY|GROUP|HAVING|LIMIT|OFFSET|UNION|ALL|DISTINCT|COUNT|SUM|AVG|MIN|MAX|INTO|VALUES|SET|CASCADE|RESTRICT|PRIMARY|KEY|FOREIGN|REFERENCES|UNIQUE|CHECK|DEFAULT|CONSTRAINT)\\b",
                "string", "'[^']*'",
                "comment", "--.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?\\b",
                "operator", "[=<>!+\\-*/%|&]+"),
            Set.of()));

        // Bash
        LANGUAGES.put("bash", new LanguageDef("bash", Set.of("sh", "bash", "zsh"),
            Map.of("keyword", "\\b(if|then|else|elif|fi|for|while|do|done|case|esac|function|return|exit|break|continue|local|export|readonly|declare|typeset|unset|shift|source|alias|unalias|set|shopt|trap|exec|eval|let|test)\\b",
                "string", "\"[^\"]*\"|'[^']*'",
                "comment", "#.*$",
                "variable", "\\$\\w+|\\$\\{[^}]+\\}",
                "number", "\\b\\d+\\b",
                "operator", "[|<>;]&?"),
            Set.of()));

        // Markdown
        LANGUAGES.put("markdown", new LanguageDef("markdown", Set.of("md", "markdown"),
            Map.of("header", "^#{1,6}\\s.*$",
                "bold", "\\*\\*[^*]+\\*\\*|__[^_]+__",
                "italic", "\\*[^*]+\\*|_[^_]+_",
                "code", "`[^`]+`",
                "link", "\\[([^\\]]+)\\]\\([^)]+\\)",
                "image", "!\\[([^\\]]*)]\\([^)]+\\)",
                "list", "^[\\s]*[-*+]\\s",
                "quote", "^>\\s.*$",
                "codeblock", "```\\w*\\n[\\s\\S]*?```"),
            Set.of()));

        // Shell scripting
        LANGUAGES.put("shell", new LanguageDef("shell", Set.of("shell"),
            Map.of("keyword", "\\b(if|then|else|elif|fi|for|while|do|done|case|esac|function|return|exit|break|continue|local|export|readonly|declare|typeset|unset|shift|source|alias|unalias|set|shopt|trap|exec|eval|let|test)\\b",
                "string", "\"[^\"]*\"|'[^']*'",
                "comment", "#.*$",
                "variable", "\\$\\w+|\\$\\{[^}]+\\}",
                "number", "\\b\\d+\\b",
                "operator", "[|<>;]&?"),
            Set.of()));

        // PHP
        LANGUAGES.put("php", new LanguageDef("php", Set.of("php"),
            Map.of("keyword", "\\b(function|class|interface|trait|extends|implements|new|return|if|else|for|foreach|while|do|switch|case|default|break|continue|try|catch|finally|throw|echo|print|public|private|protected|static|final|abstract|const|var|global|use|namespace|include|require|include_once|require_once|array|list|match|fn|yield|await|async|isset|unset|empty|die|exit)\\b",
                "string", "\"[^\"]*\"|'[^']*'|`[^`]*`",
                "comment", "//.*$|#.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?\\b",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:$.]+"),
            Set.of()));

        // Ruby
        LANGUAGES.put("ruby", new LanguageDef("ruby", Set.of("rb", "rake"),
            Map.of("keyword", "\\b(def|class|module|if|elsif|else|unless|case|when|for|while|until|do|begin|rescue|ensure|raise|return|yield|break|next|redo|retry|self|super|nil|true|false|and|or|not|in|then|end|alias|attr_reader|attr_writer|attr_accessor|private|public|protected|require|require_relative|include|extend|prepend|raise)\\b",
                "string", "\"[^\"]*\"|'[^']*'|<<?-?[^>]*-??>",
                "comment", "#.*$",
                "number", "\\b\\d+(\\.\\d+)?\\b",
                "function", "\\b\\w+(?=\\s*\\()",
                "symbol", ":[\\w:]+",
                "operator", "[+\\-*/%=<>!&|^~?:.]+"),
            Set.of()));

        // Kotlin
        LANGUAGES.put("kotlin", new LanguageDef("kotlin", Set.of("kt", "kts"),
            Map.of("keyword", "\\b(fun|val|var|if|else|for|while|do|when|return|break|continue|class|interface|object|data|sealed|enum|annotation|open|abstract|final|override|private|public|protected|internal|companion|init|constructor|by|lazy|lateinit| suspend|inline|noinline|crossinline|reified|operator|infix|tailrec|external|override|vararg|out|in|is|as|try|catch|finally|throw|import|package|typealias|where|companion|constructor|init|delegate)\\b",
                "string", "\"[^\"]*\"|\"\"\"[\\s\\S]*?\"\"\"",
                "comment", "//.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?[fFdDlL]?\\b",
                "annotation", "@\\w+",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:]+"),
            Set.of()));

        // Swift
        LANGUAGES.put("swift", new LanguageDef("swift", Set.of("swift"),
            Map.of("keyword", "\\b(func|var|let|if|else|for|while|repeat|switch|case|default|break|continue|return|guard|defer|do|try|catch|throw|throws|rethrows|async|await|actor|class|struct|enum|protocol|extension|init|deinit|subscript|typealias|associatedtype|in|where|is|as|import|public|private|internal|fileprivate|open|static|final|override|mutating|lazy|weak|unowned|inout|convenience|required|optional|some|any)\\b",
                "string", "\"[^\"]*\"|\"\"\"[\\s\\S]*?\"\"\"",
                "comment", "//.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?\\b",
                "attribute", "@\\w+",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:]+"),
            Set.of()));

        // Scala
        LANGUAGES.put("scala", new LanguageDef("scala", Set.of("scala"),
            Map.of("keyword", "\\b(def|val|var|if|else|for|while|do|match|case|class|object|trait|interface|package|import|extends|implements|new|override|private|protected|public|final|abstract|sealed|lazy|implicit|concurrent|synchronized|yield|return|throw|try|catch|finally|with|type|implicit|def|macro)\\b",
                "string", "\"[^\"]*\"|\"\"\"[\\s\\S]*?\"\"\"",
                "comment", "//.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?\\b",
                "annotation", "@\\w+",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:]+"),
            Set.of()));

        // C#
        LANGUAGES.put("csharp", new LanguageDef("csharp", Set.of("cs"),
            Map.of("keyword", "\\b(if|else|for|foreach|while|do|switch|case|default|break|continue|return|try|catch|finally|throw|using|class|struct|interface|enum|delegate|event|public|private|protected|internal|static|readonly|const|volatile|virtual|override|abstract|sealed|partial|new|this|base|null|true|false|var|dynamic|async|await|lock|checked|unchecked|unsafe|sizeof|typeof|is|as|in|out|ref|yield|namespace|using|import|extern|volatile)\\b",
                "string", "\"[^\"]*\"|@\"[^\"]*\"|'[^']*'",
                "comment", "//.*$|/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?[fFdDlLmM]?\\b|0x[0-9a-fA-F]+",
                "attribute", "\\[\\w+\\]",
                "function", "\\b\\w+(?=\\s*\\()",
                "operator", "[+\\-*/%=<>!&|^~?:]+"),
            Set.of()));

        // XML/HTML
        LANGUAGES.put("xml", new LanguageDef("xml", Set.of("xml", "html", "xhtml", "svg"),
            Map.of("tag", "<\\/?\\w+",
                "attribute", "\\s\\w+(?==)",
                "string", "\"[^\"]*\"|'[^']*'",
                "comment", "<!--[\\s\\S]*?-->",
                "cdata", "<!\\[CDATA\\[[\\s\\S]*?\\]\\]>",
                "doctype", "<!DOCTYPE[^>]*>"),
            Set.of()));

        // CSS
        LANGUAGES.put("css", new LanguageDef("css", Set.of("css", "scss", "sass", "less"),
            Map.of("selector", "[.#]?[\\w-]+(?=\\s*\\{)",
                "property", "[\\w-]+(?=\\s*:)",
                "keyword", "\\b(inherit|initial|unset|none|auto|normal|bold|italic|block|inline|flex|grid|absolute|relative|fixed|static)\\b",
                "string", "\"[^\"]*\"|'[^']*'",
                "comment", "/\\*[\\s\\S]*?\\*/",
                "number", "\\b\\d+(\\.\\d+)?(px|em|rem|%|vh|vw|pt|cm|mm|in)?\\b",
                "color", "#[0-9a-fA-F]{3,8}\\b|rgb\\([^)]+\\)|rgba\\([^)]+\\)"),
            Set.of()));
    }

    public SyntaxHighlighter() {
        this.themeManager = ThemeManager.getInstance();
    }

    /**
     * Highlight code string with the given language.
     */
    public String highlight(String code, String language) {
        if (code == null || code.isEmpty()) return code;
        if (language == null || language.isEmpty()) return code;

        String lang = language.toLowerCase();
        LanguageDef def = LANGUAGES.get(lang);

        if (def == null) {
            // Try to find by extension
            for (Map.Entry<String, LanguageDef> entry : LANGUAGES.entrySet()) {
                if (entry.getValue().extensions().contains(lang)) {
                    def = entry.getValue();
                    break;
                }
            }
        }

        if (def == null) {
            return code; // Unknown language, return as-is
        }

        return highlightWithDef(code, def);
    }

    /**
     * Highlight code using language definition.
     * Applies regex patterns in priority order with theme colors.
     */
    private String highlightWithDef(String code, LanguageDef def) {
        var theme = themeManager.getTheme();
        if (theme == null) return code;

        // Color map for token types
        Map<String, String> colors = new HashMap<>();
        colors.put("keyword", theme.getRainbowColor(0) + BOLD);     // Bold orange
        colors.put("string", theme.getRainbowColor(2));              // Green
        colors.put("comment", theme.getRainbowColor(6));            // Dim grey
        colors.put("number", theme.getRainbowColor(3));              // Cyan
        colors.put("function", theme.getRainbowColor(4));            // Blue
        colors.put("operator", theme.getRainbowColor(5));           // Magenta
        colors.put("annotation", theme.getSuggestion() + BOLD);      // Blue + bold
        colors.put("macro", theme.getError());                       // Red
        colors.put("decorator", theme.getSuggestion() + BOLD);
        colors.put("preprocessor", theme.getWarning());              // Amber
        colors.put("variable", theme.getRainbowColor(1));           // Yellow
        colors.put("type", theme.getRainbowColor(4));               // Blue
        colors.put("anchor", theme.getSuggestion());                  // Blue
        colors.put("tag", theme.getError());                         // Red for XML/HTML tags
        colors.put("attribute", theme.getSuggestion());               // Blue
        colors.put("color", theme.getSuccess());                     // Green for CSS colors
        colors.put("selector", theme.getError() + BOLD);             // Bold red for CSS selectors
        colors.put("property", theme.getSuggestion());               // Blue
        colors.put("header", BOLD + theme.getRainbowColor(1));        // Bold yellow
        colors.put("bold", BOLD);
        colors.put("italic", "");
        colors.put("code", theme.getRainbowColor(3));                // Cyan
        colors.put("link", theme.getSuggestion() + BOLD);
        colors.put("image", theme.getSuccess());
        colors.put("list", theme.getRainbowColor(2));
        colors.put("quote", theme.getRainbowColor(6));
        colors.put("codeblock", theme.getRainbowColor(3));
        colors.put("punctuation", "");
        colors.put("default", "");

        // Priority order for token application (most specific first)
        String[] priorities = {
            "comment", "string", "number", "function", "type",
            "annotation", "macro", "decorator", "preprocessor", "variable",
            "keyword", "anchor", "tag", "attribute", "property", "selector", "color",
            "header", "bold", "italic", "code", "link", "image", "list", "quote", "codeblock",
            "operator", "punctuation"
        };

        // Build combined pattern: higher priority patterns are matched first
        // We use a StringBuilder approach with markers to apply colors
        // For each token type, find matches and apply colors

        // Track all positions that have been colored to avoid overlaps
        // We'll color character by character - simple approach
        String colored = applyColors(code, def.patterns(), colors, priorities);
        return colored;
    }

    /**
     * Apply syntax highlighting colors to code.
     * Uses priority-based pattern matching without overlap.
     */
    private String applyColors(String code, Map<String, String> patterns,
                               Map<String, String> colors, String[] priorities) {
        // Create a character array to track colors at each position
        String[] charColors = new String[code.length()];
        Arrays.fill(charColors, "");

        // Build position list for each token type
        Map<String, List<int[]>> matches = new LinkedHashMap<>();

        for (String tokenType : priorities) {
            String pattern = patterns.get(tokenType);
            if (pattern == null) continue;

            String color = colors.getOrDefault(tokenType, "");
            if (color.isEmpty()) continue;

            try {
                Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
                Matcher m = p.matcher(code);
                List<int[]> tokenMatches = new ArrayList<>();

                while (m.find()) {
                    int start = m.start();
                    int end = m.end();
                    // Check if this position is already covered by a higher-priority token
                    boolean alreadyCovered = false;
                    for (int i = start; i < end; i++) {
                        if (!charColors[i].isEmpty()) {
                            alreadyCovered = true;
                            break;
                        }
                    }
                    if (!alreadyCovered) {
                        tokenMatches.add(new int[]{start, end, priorities.length - Arrays.asList(priorities).indexOf(tokenType)});
                    }
                }

                if (!tokenMatches.isEmpty()) {
                    matches.put(tokenType, tokenMatches);
                }
            } catch (PatternSyntaxException e) {
                // Skip invalid patterns
            }
        }

        // Sort all matches by priority (higher priority first), then by start position
        List<int[]> allMatches = new ArrayList<>();
        for (List<int[]> typeMatches : matches.values()) {
            allMatches.addAll(typeMatches);
        }
        allMatches.sort((a, b) -> {
            if (a[2] != b[2]) return b[2] - a[2]; // Higher priority first
            return a[0] - b[0]; // Then by position
        });

        // Apply colors
        Set<String> appliedColors = new HashSet<>();
        for (int[] match : allMatches) {
            int start = match[0];
            int end = match[1];

            // Find what color this token should use
            String color = "";
            for (Map.Entry<String, List<int[]>> entry : matches.entrySet()) {
                for (int[] m : entry.getValue()) {
                    if (m[0] == start && m[1] == end) {
                        color = colors.getOrDefault(entry.getKey(), "");
                        break;
                    }
                }
                if (!color.isEmpty()) break;
            }

            if (color.isEmpty()) continue;

            // Mark positions as covered
            for (int i = start; i < end; i++) {
                if (charColors[i].isEmpty()) {
                    charColors[i] = color;
                }
            }
        }

        // Build result string with ANSI codes
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            String color = charColors[i];
            if (!color.isEmpty() && !appliedColors.contains(i + ":" + color)) {
                // Determine the span of this color
                int spanEnd = i;
                while (spanEnd < code.length() && charColors[spanEnd].equals(color)) {
                    spanEnd++;
                }
                result.append(color);
                result.append(code, i, spanEnd);
                result.append(RESET);
                appliedColors.add(i + ":" + color + ":" + spanEnd);
                i = spanEnd - 1;
            } else {
                result.append(code.charAt(i));
            }
        }

        return result.toString();
    }

    /**
     * Render line numbers for code block.
     */
    public static String renderLineNumbers(int lineCount, int gutterWidth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lineCount; i++) {
            String num = String.format("%" + gutterWidth + "d", i);
            sb.append("\u001B[2m").append(num).append(" ").append(RESET);
        }
        return sb.toString();
    }

    /**
     * Detect language from filename.
     */
    public static String detectLanguage(String filename) {
        if (filename == null || filename.isEmpty()) return "text";

        // Check for shebang
        if (filename.startsWith("#!")) return "bash";

        int dot = filename.lastIndexOf('.');
        if (dot == -1) return "text";

        String ext = filename.substring(dot + 1).toLowerCase();

        for (Map.Entry<String, LanguageDef> entry : LANGUAGES.entrySet()) {
            if (entry.getValue().extensions().contains(ext)) {
                return entry.getKey();
            }
        }

        return "text";
    }

    /**
     * Get supported languages.
     */
    public static Set<String> getSupportedLanguages() {
        return Set.copyOf(LANGUAGES.keySet());
    }

    /**
     * Language definition.
     */
    private record LanguageDef(String name, Set<String> extensions, Map<String, String> patterns, Set<String> inherits) {
        public LanguageDef(String name, Set<String> extensions, Map<String, String> patterns) {
            this(name, extensions, patterns, Set.of());
        }
    }
}
