# CLIPRO Migration Report — 2026-04-14
## Pixel-Perfect UI + Functionality Analysis vs OpenClaude

**Repository:** github.com/simpletoolsindia/clipro
**Source Reference:** github.com/Gitlawb/openclaude (TypeScript/React Ink)
**Analysis Date:** 2026-04-14
**Author:** Claude Code Analysis

---

## Executive Summary

CLIPRO is a Java port of OpenClaude (originally derived from Claude Code). It has been under active development with 12+ major commits across multiple migration waves. The project has achieved **functional completeness** for core agent operations but falls short of **pixel-perfect UI parity** in several critical areas.

### Implementation Status Quick Reference

| File | Status | Key Functions |
|------|--------|---------------|
| `AgentEngine.java` | ⚠️ HALF | ReAct loop ✅, streaming ✅, multi-turn ❌, auto-mode ❌ |
| `BashTool.java` | ⚠️ HALF | Security ✅, sandbox ✅, AST parsing ❌ |
| `AnthropicProvider.java` | ✅ DONE | All features implemented |
| `OllamaProvider.java` | ✅ DONE | All features implemented |
| `OpenRouterProvider.java` | ✅ DONE | All features implemented |
| `ProviderManager.java` | ✅ DONE | All features implemented |
| `VimMode.java` + `VimKeyHandler.java` | ⚠️ MOSTLY | 85% — macros ❌, :s ❌ |
| `ThemeManager.java` + `Theme.java` | ✅ DONE | 6 themes, shimmer, daltonized |
| `CommandRegistry.java` | ⚠️ HALF | 55 commands ✅, fuzzy ✅, agent commands ❌ |
| `ThinkingParser.java` | ✅ DONE | Full parsing implemented |
| `ThinkingBlock.java` | ✅ DONE | Rainbow + shimmer ✅, integrated into MessageBox ✅ |
| `RainbowRenderer.java` | ✅ DONE | Full rainbow + shimmer |
| `ShimmerAnimator.java` | ✅ DONE | 120ms frame rate |
| `MessageBox.java` | ✅ DONE | Basic boxes ✅, thinking blocks ✅, tool formatting ✅ |
| `FullscreenLayout.java` | ⚠️ HALF | Basic layout ✅, NewMessagesPill ❌, StickyPrompt ❌ |
| `VirtualMessageList.java` | ⚠️ HALF | Basic offset ✅, height cache ❌, smooth scroll ❌ |
| `InputField.java` + `EnhancedInputField.java` | ⚠️ HALF | Basic input ✅, multi-line ❌, Ctrl+R ❌ |
| `CommandCompleter.java` | ✅ DONE | 60+ commands, fuzzy search |
| `TypeaheadEngine.java` | ⚠️ HALF | Command completion ✅, file path ❌ |
| `HistorySearch.java` | ⚠️ HALF | Up/Down nav ✅, reverse-i-search ❌ |
| `MarkdownRenderer.java` | ✅ DONE | Bold/italic/code ✅, tables ✅, line numbers ✅, syntax highlighting ✅ |
| `SyntaxHighlighter.java` | ✅ DONE | 20+ languages ✅, patterns applied ✅, priority coloring ✅ |
| `StatusBar.java` | ⚠️ HALF | Tokens/latency ✅, permission ❌, cost ❌, rate limits ❌ |
| `HeaderBar.java` | ✅ DONE | Model + status |
| `StatsComponent.java` | ⚠️ HALF | Basic stats ✅, charts ❌, heatmap ❌ |
| `McpClient.java` | ⚠️ HALF | JSON-RPC ✅, discovery ❌, server mgmt ❌ |
| `AgentManager.java` | ⚠️ HALF | Spawn ✅, tool schema ❌ |
| `AgentTeam.java` | ⚠️ HALF | Basic team ✅, visual ❌ |
| `SubAgent.java` | ⚠️ HALF | Basic parallel ✅ |
| `VirtualMessageStore.java` | ✅ DONE | Windowing + pagination |
| `ConversationCompactor.java` | ⚠️ HALF | Compaction logic ✅, UI feedback ❌ |
| `ConfigManager.java` | ✅ DONE | Settings persistence |
| `HistoryManager.java` | ✅ DONE | History storage |
| `LlmHttpClient.java` | ✅ DONE | HTTP + retry |
| `SseParser.java` | ✅ DONE | SSE streaming |
| `FileReadTool.java` | ⚠️ HALF | Basic read ✅, image processing ❌ |
| `FileWriteTool.java` | ✅ DONE | Create/overwrite |
| `FileEditTool.java` | ⚠️ HALF | Basic edit ✅, inline diff ❌ |
| `GlobTool.java` | ✅ DONE | Pattern matching |
| `GrepTool.java` | ✅ DONE | Regex search |
| `GitTool.java` + `Git*Tool.java` | ✅ DONE | All git commands |
| `WebSearchTool.java` | ⚠️ HALF | SearXNG ✅, providers ❌ |
| `WebFetchTool.java` | ⚠️ HALF | Fetch ✅, HTML parse ❌ |
| `JavaFXAdapter.java` | ❌ STUB | Empty adapter class |
| `OpenAIProvider.java` | ❌ NOT STARTED | Does not exist |
| `LSPTool.java` | ❌ NOT STARTED | Does not exist |
| `NotebookEditTool.java` | ❌ NOT STARTED | Does not exist |
| `SkillTool.java` | ❌ NOT STARTED | Does not exist |
| `BedrockProvider.java` | ❌ NOT STARTED | Does not exist |
| `GeminiProvider.java` | ❌ NOT STARTED | Does not exist |

### Key Numbers
| Metric | Value |
|--------|-------|
| **Total Java Source** | ~12,455 lines (main + test) |
| **Java Main Files** | 101 files |
| **Java Test Files** | 55 files |
| **Passing Tests** | 283+ |
| **CLI Commands** | 55+ registered |
| **LLM Providers** | 3 (Ollama, OpenRouter, Anthropic) |
| **Theme Count** | 6 (dark, light, 2 ANSI, 2 daltonized) |
| **Migration Score** | **~70%** overall |
| **UI Parity Score** | **~50%** |
| **Code Size vs Original** | ~9% of OpenClaude LOC |
| **Pending Tickets** | **38 remaining** (2 done: C-01, C-05) |
| — CRITICAL tickets | 3 remaining |
| — HIGH priority | 9 |
| — MEDIUM priority | 17 |
| — LOW priority | 15 |

### Migration Scorecard

| Category | Coverage | Trend | Priority |
|----------|----------|-------|----------|
| Core Agent (ReAct Loop) | 80% | ✅ Stable | DONE |
| Tool System | 55% | ⚠️ Growing | HIGH |
| LLM Providers | 75% | ✅ Stable | DONE |
| UI/TUI Components | 40% | ⚠️ Lagging | CRITICAL |
| Input System | 20% | ❌ Behind | CRITICAL |
| Theme System | 90% | ✅ Done | DONE |
| Vim Mode | 85% | ✅ Done | DONE |
| CLI Commands | 50% | ⚠️ Growing | MEDIUM |
| MCP Integration | 30% | ⚠️ Early | HIGH |
| State Management | 60% | ✅ Stable | MEDIUM |
| Security (Bash) | 85% | ✅ Done | DONE |
| Virtual Scrolling | 25% | ⚠️ Basic | HIGH |
| Syntax Highlighting | 100% | ✅ Done | DONE |
| Thinking Block Render | 100% | ✅ Done | DONE |

---

## Part 1: Architecture Overview

### 1.1 Technology Stack Comparison

| Layer | OpenClaude | CLIPRO | Assessment |
|-------|-----------|--------|------------|
| **Language** | TypeScript | Java 21 | Different runtime |
| **UI Framework** | React 19 + Ink + Yoga | TamboUI + JavaFX (stub) | Different paradigms |
| **Build Tool** | Bun + TypeScript | Gradle (Kotlin DSL) | — |
| **State Management** | Redux + React Context | Manual POJO + observers | Gap |
| **Styling** | CSS flexbox (yoga) | ANSI escape codes | Different approach |
| **Syntax Highlight** | cli-highlight (18+ langs) | Basic regex patterns | Gap |
| **Markdown** | marked + custom | Basic word-wrap | Gap |
| **Virtual Scroll** | react-virtual (1,082 lines) | Basic offset calc (138 lines) | Major Gap |
| **Theming** | 6 themes + shimmer | 6 themes + shimmer | ✅ Matched |

### 1.2 Source File Map

#### OpenClaude Key Files (~50,000+ LOC TypeScript)
```
src/
├── main.tsx                          (4,668 lines) — Entry point
├── QueryEngine.ts                    (1,309 lines) — Agent core
├── App.tsx                           (root)
├── components/
│   ├── PromptInput/                  (2,376+ lines, 23 files) ← BIGGEST GAP
│   │   ├── PromptInput.tsx           (main input)
│   │   ├── useTypeahead.tsx          (1,392 lines)
│   │   ├── HistorySearchDialog.tsx
│   │   ├── PromptInputModeIndicator.tsx
│   │   ├── ShimmeredInput.tsx
│   │   └── ...
│   ├── FullscreenLayout.tsx          (637 lines)
│   ├── VirtualMessageList.tsx        (1,082 lines) ← BIGGEST GAP
│   ├── Message.tsx                   (627 lines)
│   ├── Messages.tsx                  (1,000+ lines)
│   ├── Stats.tsx                     (1,227 lines)
│   ├── Spinner.tsx                   (561 lines)
│   ├── StatusLine.tsx                (500 lines)
│   ├── design-system/                (12 files)
│   └── ink/                          (terminal primitives)
├── tools/                            (47 directories)
│   ├── BashTool/                    (10,987 lines total)
│   ├── FileReadTool/                (1,400+ lines)
│   ├── FileEditTool/                (1,527 lines)
│   ├── LSPTool/                     (2,100+ lines) ← MISSING
│   ├── AgentTool/                   (2,500+ lines) ← MISSING
│   ├── MCPTool/                     (700+ lines)    ← PARTIAL
│   └── ...30+ more
├── services/providers/              (8+ providers)
├── utils/theme.ts                    (640 lines)
├── utils/messages.ts                (5,517 lines)
└── commands.ts                       (767 lines, 80+ commands)
```

#### CLIPRO Java Files (~12,455 LOC)
```
src/main/java/com/clipro/
├── App.java                          (146 lines) — Entry
├── agent/
│   ├── AgentEngine.java              (364 lines) — ReAct loop
│   ├── AgentManager.java             (191 lines)
│   ├── ModelRouter.java              (131 lines)
│   ├── TokenBudget.java              (96 lines)
│   ├── AgentTeam.java                (84 lines)
│   └── SubAgent.java                 (67 lines)
├── ui/
│   ├── UIController.java             (264 lines)
│   ├── StreamingUIController.java    (175 lines)
│   ├── Terminal.java                 (112 lines)
│   ├── TerminalResizeHandler.java    (42 lines)
│   ├── tamboui/
│   │   ├── TamboUIAdapter.java       (538 lines) — Main TUI bridge
│   │   ├── TuiAdapter.java           (180 lines) — Interface
│   │   ├── Theme.java                (330 lines, 80+ colors)
│   │   ├── ThemeManager.java         (253 lines, 6 themes)
│   │   ├── OpenClaudeTheme.java      (120 lines)
│   │   ├── ThemeName.java            (45 lines)
│   │   ├── DiffColorizer.java        (156 lines)
│   │   └── ...
│   ├── javafx/
│   │   └── JavaFXAdapter.java       (STUB — 18 lines)
│   ├── vim/
│   │   ├── VimMode.java              (110 lines)
│   │   ├── VimState.java             (44 lines)
│   │   ├── VimKeyHandler.java        (298 lines)
│   │   ├── VimRegisters.java         (68 lines)
│   │   ├── VimMarks.java             (41 lines)
│   │   └── TextObject.java           (89 lines)
│   └── components/
│       ├── FullscreenLayout.java     (192 lines)
│       ├── InputField.java           (199 lines)
│       ├── EnhancedInputField.java   (345 lines)
│       ├── MessageBox.java           (141 lines)
│       ├── MessageList.java          (142 lines)
│       ├── VirtualMessageList.java   (138 lines)
│       ├── Message.java              (60 lines)
│       ├── MessageRow.java           (25 lines)
│       ├── StreamingMessage.java     (98 lines)
│       ├── ThinkingMessage.java      (76 lines)
│       ├── ThinkingBlock.java        (309 lines) ← rainbow + shimmer
│       ├── MarkdownRenderer.java     (158 lines)
│       ├── SyntaxHighlighter.java    (222 lines) ← 18+ languages
│       ├── RainbowRenderer.java       (233 lines)
│       ├── ShimmerAnimator.java      (112 lines)
│       ├── HeaderBar.java            (68 lines)
│       ├── StatusBar.java            (60 lines)
│       ├── CommandCompleter.java     (280 lines, 60+ commands)
│       ├── TypeaheadEngine.java      (199 lines)
│       ├── HistorySearch.java         (69 lines)
│       ├── ReActVisualizer.java      (130 lines)
│       ├── ToolResultMessage.java    (72 lines)
│       ├── StatsComponent.java       (216 lines)
│       └── ...
├── llm/
│   ├── LlmHttpClient.java            (208 lines)
│   ├── SseParser.java                (163 lines)
│   ├── ThinkingParser.java           (294 lines)
│   ├── providers/
│   │   ├── ProviderManager.java      (286 lines)
│   │   ├── OllamaProvider.java       (140 lines)
│   │   ├── OpenRouterProvider.java   (157 lines)
│   │   ├── AnthropicProvider.java    (158 lines)
│   │   └── LlmProvider.java         (35 lines)
│   └── models/                       (6 model classes)
├── tools/
│   ├── Tool.java                     (15 lines) — base interface
│   ├── ToolExecutor.java             (145 lines)
│   ├── ToolRegistry.java             (130 lines)
│   ├── BashTool.java                 (391 lines)
│   ├── file/
│   │   ├── FileReadTool.java        (158 lines)
│   │   ├── FileWriteTool.java       (117 lines)
│   │   ├── FileEditTool.java        (131 lines)
│   │   ├── GlobTool.java            (116 lines)
│   │   └── GrepTool.java            (157 lines)
│   ├── git/
│   │   ├── GitTool.java             (83 lines)
│   │   ├── GitStatusTool.java       (72 lines)
│   │   ├── GitDiffTool.java         (79 lines)
│   │   ├── GitLogTool.java          (102 lines)
│   │   └── GitCommitTool.java       (78 lines)
│   ├── web/
│   │   ├── WebSearchTool.java       (117 lines)
│   │   ├── WebFetchTool.java        (114 lines)
│   │   └── QuickFetchTool.java      (91 lines)
│   ├── TaskTool.java                (151 lines)
│   ├── AskUserQuestionTool.java     (67 lines)
│   ├── ScheduleCronTool.java        (96 lines)
│   └── MCPTool.java                 (143 lines)
├── session/
│   ├── ConfigManager.java            (303 lines)
│   ├── HistoryManager.java           (173 lines)
│   ├── VirtualMessageStore.java      (261 lines)
│   └── ConversationCompactor.java    (89 lines)
├── mcp/
│   └── McpClient.java               (186 lines)
├── cli/
│   └── CommandRegistry.java         (755 lines, 55+ commands)
└── logging/
    ├── Logger.java                   (67 lines)
    └── LogLevel.java                 (28 lines)
```

---

## Part 2: File-by-File Migration Status

### 2.1 Agent Engine

| OpenClaude File | Java File | LOC Ratio | Status | Functions Implemented |
|----------------|-----------|-----------|--------|---------------------|
| `QueryEngine.ts` (1,309L) | `AgentEngine.java` | 364L (28%) | ⚠️ PARTIAL | `runAgentLoop()`, `submitMessage()`, `interrupt()`, `getMessages()`, `setModel()` |
| `QueryEngine.ts` multi-turn | — | — | ❌ MISSING | Auto-mode, Haiku classifier, plan mode |
| `QueryEngine.ts` teams | `AgentTeam.java` | 84L | ⚠️ PARTIAL | Basic team creation, member spawning |
| `QueryEngine.ts` sub-agents | `SubAgent.java` | 67L | ⚠️ PARTIAL | Basic parallel execution |
| `QueryEngine.ts` remote sessions | — | — | ❌ MISSING | Remote session support |
| `QueryEngine.ts` budget tracking | `TokenBudget.java` | 96L | ✅ OK | Token counting, budget enforcement |
| `QueryEngine.ts` context mgmt | `ConversationCompactor.java` | 89L | ✅ OK | Context window management |
| `ModelRouter.ts` | `ModelRouter.java` | 131L | ✅ OK | Per-task model selection |
| `AgentManager.ts` | `AgentManager.java` | 191L | ✅ OK | Agent lifecycle, tool schema loading |

**Coverage: 45%** — Core ReAct loop works, but advanced features (auto-mode, team coordination, remote sessions) are missing.

### 2.2 Input System (CRITICAL GAP)

| OpenClaude File | Java File | LOC Ratio | Status | Gap |
|----------------|-----------|-----------|--------|-----|
| `PromptInput.tsx` (main) | — | 0L / 2,376L | ❌ **NOT MIGRATED** | Entire input component |
| `useTypeahead.tsx` (1,392L) | `TypeaheadEngine.java` | 199L (14%) | ⚠️ PARTIAL | File path completion, @mentions |
| `useTypeahead.tsx` suggestions | `CommandCompleter.java` | 280L (20%) | ✅ OK | Slash command fuzzy search |
| `HistorySearchDialog.tsx` | `HistorySearch.java` | 69L (15%) | ⚠️ PARTIAL | Basic navigation, missing Ctrl+R, `/` mode |
| `PromptInputModeIndicator.tsx` | `EnhancedInputField.java` | 345L | ⚠️ PARTIAL | Mode display (NORMAL/INSERT/VISUAL) |
| `ShimmeredInput.tsx` | `TamboUIAdapter.java` | — | ⚠️ PARTIAL | Animated prompt char |
| `useVimInput.ts` (500L) | `VimKeyHandler.java` + `VimMode.java` | 408L (82%) | ✅ OK | Full vim motions, operators, registers |
| `BaseTextInput.tsx` | `InputField.java` | 199L | ⚠️ PARTIAL | Basic input, cursor movement |
| Multi-line input | `EnhancedInputField.java` | 345L | ⚠️ PARTIAL | Tab indent exists, arrow nav missing |
| Image paste | — | — | ❌ MISSING | PNG/JPEG paste support |
| Queued commands | — | — | ❌ MISSING | Command queuing/editing |
| Permission mode UI | — | — | ❌ MISSING | READ_ONLY/BASH/RESTRICTED display |

**Coverage: 20%** — This is the **largest functional gap**. OpenClaude's PromptInput is 2,376 lines of sophisticated UX; CLIPRO has 200 lines of basic input.

### 2.3 UI Layout & Components

| OpenClaude File | Java File | LOC Ratio | Status | Gap |
|----------------|-----------|-----------|--------|-----|
| `FullscreenLayout.tsx` (637L) | `FullscreenLayout.java` | 192L (30%) | ⚠️ PARTIAL | Missing: NewMessagesPill, StickyPromptHeader, SuggestionOverlay |
| `VirtualMessageList.tsx` (1,082L) | `VirtualMessageList.java` | 138L (13%) | ❌ **MAJOR GAP** | No height caching, smooth scroll, j/k navigation |
| `Messages.tsx` (1,000+L) | `MessageList.java` | 142L | ⚠️ PARTIAL | Basic message rendering, missing grouped_tool_use, collapsed_read_search |
| `Message.tsx` (627L) | `MessageBox.java` | 141L (22%) | ⚠️ PARTIAL | Missing: thinking blocks, tool_use formatting, code blocks, attachments |
| `StatusLine.tsx` (500L) | `StatusBar.java` | 60L (12%) | ⚠️ PARTIAL | Tokens, latency, vim mode — missing cost, rate limits, cwd |
| `HeaderBar.tsx` | `HeaderBar.java` | 68L | ✅ OK | Model name, connection status |
| `Stats.tsx` (1,227L) | `StatsComponent.java` | 216L (18%) | ⚠️ PARTIAL | Basic stats; missing ASCII charts, tab layout, heatmap |
| `Spinner.tsx` (561L) | Multiple shimmer files | 309L | ✅ OK | Rainbow shimmer, flicker-free animation |
| `HighlightedCode.tsx` | `SyntaxHighlighter.java` | 222L | ⚠️ PARTIAL | 18+ languages, but regex patterns not fully applied |
| `design-system/Box.tsx` | `Terminal.java` box helpers | 112L | ✅ OK | Box drawing, borders |
| `design-system/Text.tsx` | `Terminal.java` ANSI helpers | 112L | ✅ OK | Colors, formatting |

**Coverage: 35%**

### 2.4 Theme System

| Feature | OpenClaude | CLIPRO | Status |
|---------|-----------|--------|--------|
| Theme count | 6 | 6 | ✅ EXACT MATCH |
| `dark` theme | ✅ | ✅ | ✅ |
| `light` theme | ✅ | ✅ | ✅ |
| `dark-ansi` (16-color) | ✅ | ✅ | ✅ |
| `light-ansi` (16-color) | ✅ | ✅ | ✅ |
| `dark-daltonized` | ✅ | ✅ | ✅ |
| `light-daltonized` | ✅ | ✅ | ✅ |
| Rainbow colors (14) | ✅ | ✅ | ✅ |
| Agent colors (8) | ✅ | ✅ | ✅ |
| Shimmer effects | ✅ | ✅ | ✅ |
| Diff colors | ✅ | ✅ | ✅ |
| Theme persistence | ✅ | ✅ | ✅ |
| Auto theme detection | ✅ | ✅ | ✅ |
| Theme switching hotkey | ✅ | ❌ MISSING | Runtime switch via config only |

**Coverage: 90%** — Theme system is the most complete parity with OpenClaude.

### 2.5 Tool System

| OpenClaude Tool | Java File | LOC Ratio | Status | Notes |
|----------------|-----------|-----------|--------|-------|
| BashTool (10,987L total) | `BashTool.java` | 391L (4%) | ⚠️ PARTIAL | Permission modes, sandbox, path validation ✅; tree-sitter AST ❌ |
| FileReadTool (1,400L) | `FileReadTool.java` | 158L (11%) | ⚠️ PARTIAL | Basic read ✅; image processing ❌ |
| FileWriteTool (437L) | `FileWriteTool.java` | 117L (27%) | ✅ OK | Create/overwrite with validation |
| FileEditTool (1,527L) | `FileEditTool.java` | 131L (9%) | ⚠️ PARTIAL | Basic edit ✅; inline diff ❌ |
| GlobTool | `GlobTool.java` | 116L | ✅ OK | Pattern matching |
| GrepTool (577L) | `GrepTool.java` | 157L (27%) | ✅ OK | Regex search with context |
| WebSearchTool (800L) | `WebSearchTool.java` | 117L (15%) | ⚠️ PARTIAL | SearXNG ✅; provider logic ❌ |
| WebFetchTool (536L) | `WebFetchTool.java` | 114L (21%) | ⚠️ PARTIAL | URL fetch ✅; HTML parsing ❌ |
| GitTool | `GitTool.java` + 4 files | 414L | ✅ OK | Status, diff, log, commit |
| LSPTool (2,100L) | — | — | ❌ **MISSING** | Language Server Protocol |
| AgentTool (2,500L) | `AgentManager.java` | 191L | ⚠️ PARTIAL | Basic spawning; tool schema loading ❌ |
| MCPTool (700L) | `McpClient.java` | 186L | ⚠️ PARTIAL | Phase 1 JSON-RPC ✅; server discovery ❌ |
| TaskTool | `TaskTool.java` | 151L | ⚠️ PARTIAL | Basic CRUD; nested tasks ❌ |
| NotebookEditTool | — | — | ❌ MISSING | Jupyter notebook editing |
| SkillTool (1,118L) | — | — | ❌ MISSING | Skills system |
| TeamCreateTool | `AgentTeam.java` | 84L | ⚠️ PARTIAL | Basic team; teardown ❌ |
| AskUserQuestionTool | `AskUserQuestionTool.java` | 67L | ✅ OK | User prompts |
| ScheduleCronTool | `ScheduleCronTool.java` | 96L | ✅ OK | Cron scheduling |

**Coverage: 55%** — Core tools work; advanced tools (LSP, Skill, Notebook) are missing.

### 2.6 LLM Providers

| Provider | Java File | Status | Features |
|----------|-----------|--------|----------|
| **Ollama** | `OllamaProvider.java` (140L) | ✅ DONE | Streaming, tool calling, local models |
| **OpenRouter** | `OpenRouterProvider.java` (157L) | ✅ DONE | 300+ models, streaming |
| **Anthropic** | `AnthropicProvider.java` (158L) | ✅ DONE | Claude API, tool calling |
| **OpenAI** | — | ❌ MISSING | GPT-4, GPT-4o models |
| **AWS Bedrock** | — | ❌ MISSING | Claude on AWS |
| **Google Gemini** | — | ❌ MISSING | Gemini models |
| **GitHub Models** | — | ❌ MISSING | Copilot models |
| **Azure OpenAI** | — | ❌ MISSING | Azure-hosted models |
| **ProviderManager** | `ProviderManager.java` (286L) | ✅ DONE | Health checks, switching, model discovery |

**Coverage: 75%** (3 of 8 providers, but 3 are the most commonly used)

### 2.7 CLI Command System

| Category | OpenClaude | CLIPRO | Status |
|----------|-----------|--------|--------|
| Git Commands | 8+ | 12 | ✅ **EXCEEDS** |
| File Commands | 5+ | 6 | ✅ OK |
| Search Commands | 3+ | 5 | ✅ OK |
| Shell Commands | 5+ | 8 | ✅ OK |
| Web Commands | 5+ | 4 | ⚠️ PARTIAL |
| Session Commands | 8+ | 4 | ⚠️ PARTIAL |
| Stats Commands | 6+ | 3 | ⚠️ PARTIAL |
| Config Commands | 8+ | 3 | ⚠️ PARTIAL |
| Model Commands | 5+ | 3 | ⚠️ PARTIAL |
| Agent Commands | 10+ | 0 | ❌ MISSING |
| MCP Commands | 6+ | 0 | ❌ MISSING |
| Theme Commands | 5+ | 0 | ❌ MISSING |
| Plugin Commands | 10+ | 0 | ❌ MISSING |

**Total:** 50+ commands in CLIPRO vs 112+ in OpenClaude
**Coverage: 45%** — Core commands covered; agent/theme/plugin commands missing.

### 2.8 State Management & Session

| OpenClaude | CLIPRO | LOC Ratio | Status |
|-----------|--------|-----------|--------|
| Redux Store | Manual POJO | — | ❌ Different pattern |
| AppStateStore | `ConfigManager.java` | 303L | ⚠️ PARTIAL |
| Session Storage | `HistoryManager.java` | 173L | ✅ OK |
| Message Store | `VirtualMessageStore.java` | 261L | ✅ OK — windowing + pagination |
| Settings | `ConfigManager.java` | 303L | ✅ OK |

**Coverage: 60%**

---

## Part 3: Function-by-Function Analysis

### 3.1 ReAct Agent Loop (AgentEngine)

| Function | OpenClaude | CLIPRO | Status |
|----------|-----------|--------|--------|
| `runAgentLoop()` | `submitMessage()` async gen | ✅ | Streaming works |
| `interrupt()` | `interrupt()` | ✅ | Abort controller |
| `getMessages()` | `getMessages()` | ✅ | Message list |
| `setModel()` | `setModel()` | ✅ | Model switch |
| `observe→reason→act` | ✅ | ✅ | ReAct implemented |
| `toolCall()` | `ToolExecutor` | ✅ | Executes tools |
| `streamTokens()` | `StreamingUIController` | ✅ | SSE streaming |
| Multi-turn auto-mode | ❌ | ❌ | Both missing |
| Plan mode | ❌ | ❌ | Both missing |
| Team coordination | ⚠️ partial | ⚠️ partial | Both partial |

### 3.2 Vim Key Handler

| Feature | OpenClaude | CLIPRO | Status |
|---------|-----------|--------|--------|
| Modes: NORMAL/INSERT/VISUAL | ✅ | ✅ | Matched |
| Motions: h/j/k/l/w/b/e/0/$/^/f/F | ✅ | ✅ | Matched |
| Operators: d/y/c/x/p/r/~ | ✅ | ✅ | Matched |
| Text objects: iw/aw/i"/a"/ib/ab | ✅ | ✅ | Matched |
| Registers: 0-9, a-z, ", +, * | ✅ | ✅ | Matched |
| Marks: a-z, A-Z | ✅ | ✅ | Matched |
| Count prefix: 3d2w | ✅ | ✅ | Matched |
| Dot repeat | ✅ | ✅ | Matched |
| Visual line mode | ✅ | ✅ | Matched |
| Command mode (:) | ✅ | ✅ | Matched |
| Macros (q/) | ❌ | ❌ | Both missing |
| :s (substitute) | ❌ | ❌ | Both missing |
| :w/:q/:wq | ❌ | ⚠️ | Via CLI commands |

**Coverage: 85%** — Vim mode is the most complete feature parity.

### 3.3 Thinking Block Renderer

| Feature | OpenClaude | CLIPRO | Status |
|---------|-----------|--------|--------|
| Parse `<thinking>` tags | ✅ | ✅ `ThinkingParser.java` (294L) | Matched |
| Rainbow color rendering | ✅ | ✅ `RainbowRenderer.java` (233L) | Matched |
| Shimmer animation (120ms) | ✅ | ✅ `ShimmerAnimator.java` (112L) | Matched |
| Collapsible expansion | ✅ | ⚠️ PARTIAL | Toggle works, click missing |
| ultrathink mode | ✅ | ✅ | Keyword detection |
| Inline thinking display | ✅ | ✅ | `renderInlineThinking()` |
| Block count indicators | ✅ | ❌ MISSING | Number of thinking blocks |

**Critical Gap:** `ThinkingBlock.java` (309 lines) exists but is **NOT integrated into `MessageBox.java`**. The `MessageBox.renderAssistant()` method renders raw content — thinking blocks are parsed but the UI doesn't display them with rainbow styling.

### 3.4 Command Registry

| Function | OpenClaude | CLIPRO | Status |
|----------|-----------|--------|--------|
| Command registration | ✅ | ✅ `CommandRegistry.java` (755L) | 55+ commands |
| Fuzzy search | ✅ | ✅ `CommandCompleter.java` (280L) | Matched |
| Command aliases | ✅ | ✅ | Matched |
| Help text | ✅ | ✅ | Matched |
| Argument parsing | ✅ | ⚠️ PARTIAL | Basic only |
| Sub-commands | ⚠️ | ❌ MISSING | Nested commands |

---

## Part 4: Pixel-Perfect UI Gap Analysis

### 4.1 Visual Layout Comparison

```
OPENCLAUDE:
┌──────────────────────────────────────────────────────┐
│  Claude Code  [●] Connected        [qwen3-coder:32b]  │ ← Model picker icon
├──────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────────────────────────────────────────┐   │
│  │ USER                                           │   │ ← User message box
│  │  Message content here with word wrap...       │   │    (dark grey bg)
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  ┌──────────────────────────────────────────────┐   │
│  │ CLAUDE  ●                                      │   │ ← Claude response
│  │  <thinking>                                    │   │    (rainbow thinking)
│  │  <thinking> I need to analyze the codebase... │   │    (collapsible)
│  │  </thinking>                                   │   │
│  │                                                │   │
│  │  Here's my analysis:                           │   │
│  │                                                │   │
│  │  ```python                                     │   │ ← Syntax highlighted
│  │  def hello():                                  │   │    code block
│  │      print("world")                            │   │
│  │  ```                                           │   │
│  │                                                │   │
│  │  [TOOL_CALL] bash: ls -la                     │   │ ← Tool use display
│  │  > Success: 5 files                            │   │
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  ↓ 3 new messages                                   │ ← NewMessagesPill
├──────────────────────────────────────────────────────┤
│  [INSERT] ▶ Type your message...                   ▌ │ ← Vim input
├──────────────────────────────────────────────────────┤
│  Tokens: 12,345 / 200,000 (6%)  │  234ms  │ READ ● │ ← Status bar
└──────────────────────────────────────────────────────┘

CLIPRO (current):
┌──────────────────────────────────────────────────────┐
│  CLIPRO  ● Connected               qwen3-coder:32b   │ ← Simple header
├──────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────┐   │
│  │ USER  [message]                               │   │ ← Simple box
│  │                                                │   │
│  │  Message content here with word wrap...        │   │    (no bg color)
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  ┌──────────────────────────────────────────────┐   │
│  │ CLAUDE  [assistant]                          │   │ ← Assistant box
│  │                                                │   │
│  │  <thinking>raw thinking text</thinking>        │   │    (NOT rainbow)
│  │  Here's my analysis:                          │   │    (no syntax hl)
│  │  def hello():                                 │   │
│  │      print("world")                           │   │
│  │                                                │   │
│  │  [TOOL_CALL] bash: ls -la                     │   │ ← Tool call (basic)
│  │  > Success: 5 files                           │   │
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  ↑ 5 more ↑                                          │ ← Basic scroll hint
├──────────────────────────────────────────────────────┤
│  ▶ Type your message...                           ▌ │ ← Simple input
├──────────────────────────────────────────────────────┤
│  ─────────────────────────────────────────────────  │
│  Tokens: 12345 / 20000 │ 234ms │ Ready              │ ← Status (simple)
└──────────────────────────────────────────────────────┘
```

### 4.2 Missing Visual Elements

| Element | OpenClaude | CLIPRO | Impact |
|---------|-----------|--------|--------|
| User message background | #373737 grey box | No background | MEDIUM |
| Thinking blocks rainbow | 7-color rainbow | Plain text | **HIGH** |
| Thinking block shimmer | Animated | Static | **HIGH** |
| Thinking collapsible | Click to expand | Not collapsible | **HIGH** |
| Syntax highlighting | cli-highlight (18+) | None (regex only) | **HIGH** |
| Tool use icons | `[TOOL_CALL]` badge | Plain text | MEDIUM |
| Tool result bg | #191923 indigo tint | Plain text | MEDIUM |
| NewMessagesPill | "↓ N new messages" | Missing | MEDIUM |
| StickyPromptHeader | 1-row context header | Missing | LOW |
| Spinner animation | Flicker-free shimmer | Basic dots | MEDIUM |
| Permission indicator | READ ● / BASH ● | Hidden | MEDIUM |
| Rate limit display | 5h/7d progress | Missing | LOW |
| Diff syntax color | Added/removed colors | Plain text | MEDIUM |

### 4.3 Color Palette Parity

| Role | OpenClaude (Dark) | CLIPRO (Dark) | Match |
|------|-----------------|--------------|-------|
| Claude brand | `rgb(215,119,87)` | `#D77757` | ✅ EXACT |
| Text | `rgb(255,255,255)` | `#E8E6E3` | ⚠️ ~3% off |
| Background | `rgb(0,0,0)` | `#0D0D0D` | ✅ EXACT |
| User message bg | `rgb(55,55,55)` | `#1E1E1E` | ⚠️ Different |
| Success | `rgb(78,186,101)` | `#2EA043` | ⚠️ ~10% off |
| Error | `rgb(255,107,128)` | `#D22F2F` | ⚠️ ~10% off |
| Warning | `rgb(255,193,7)` | `#B5835A` | ⚠️ Different |
| Subtle | `rgb(80,80,80)` | `#868283` | ⚠️ ~5% off |
| Border | `rgb(136,136,136)` | `#323232` | ⚠️ Different |
| Permission blue | `rgb(87,105,247)` | `#D77757` | ❌ Wrong (uses brand) |
| Tool result bg | `rgb(25,25,35)` | — | ❌ MISSING |

---

## Part 5: Pending Work — File-by-File Task List

Legend:
- ❌ **NOT STARTED** — File doesn't exist in CLIPRO
- ⚠️ **HALF-IMPLEMENTED** — File exists but major functions are stubbed or incomplete
- ✅ **DONE** — File exists and functions are implemented

---

### CRITICAL Priority

#### ❌ NOT STARTED — ThinkingBlock not integrated into MessageBox
**Files:** `MessageBox.java:44` (`renderAssistant()`)

**What it does now:**
```java
public static String renderAssistant(String content, boolean streaming) {
    // Renders raw content — <thinking> tags show as plain text!
    sb.append(Terminal.boxRow(content, Terminal.getColumns()));
    // No call to ThinkingBlock.render() or ThinkingParser
}
```

**What needs to be done:**
- Modify `MessageBox.renderAssistant(String content)` to:
  1. Call `ThinkingParser.parse(content)` to extract thinking blocks
  2. Render thinking blocks via `ThinkingBlock.renderStreaming()` with rainbow colors
  3. Render remaining content via `MarkdownRenderer.render()`
  4. Call `MessageBox.renderAssistantWithThinking()` instead of raw text
- Reference: OpenClaude `src/components/Message.tsx` line 100-200 (`switch (contentBlock.type)`)

**New file needed:** None — existing `ThinkingBlock.java` and `ThinkingParser.java` are ready but unused in rendering.

---

#### ❌ NOT STARTED — Virtual scrolling height caching
**File:** `VirtualMessageList.java` (138 lines — basic)

**What it does now:**
```java
public int getScrollOffset() { return scrollOffset; } // Just a number
public void scrollUp() { scrollOffset++; }
public void scrollDown() { scrollOffset--; }
// No height tracking, no smooth scroll, no j/k navigation
```

**What needs to be done:**
- Add `Map<Integer, Integer> itemHeights` — cache each message's rendered height
- Add `heightCache` invalidation on content change
- Add smooth scroll methods: `scrollTo(int index)`, `scrollToBottom()`, `scrollBy(int delta)`
- Add `ScrollBoxHandle` interface with: `scrollTo(y)`, `scrollBy(dy)`, `getScrollTop()`, `subscribe(listener)`
- Add j/k keyboard navigation: handle 'j' (scroll down 1 item), 'k' (scroll up 1 item)
- Add `isSticky()` — detect when at bottom for auto-scroll
- Reference: OpenClaude `src/components/VirtualMessageList.tsx` (1,082 lines)

---

#### ❌ NOT STARTED — NewMessagesPill
**File:** `FullscreenLayout.java` (192 lines)

**What needs to be done:**
- Add field: `private int newMessageCount = 0`
- Add method: `showNewMessagesPill(int count)` — renders "↓ N new messages ▼" overlay
- Trigger when: user scrolls UP during streaming, new messages arrive
- On click: `scrollToBottom()` + clear count
- Position: absolute bottom of message area
- Reference: OpenClaude `src/components/FullscreenLayout.tsx` line 200+ (`NewMessagesPill`)

---

#### ❌ NOT STARTED — StickyPromptHeader
**File:** `FullscreenLayout.java`

**What needs to be done:**
- Add 1-row sticky header that appears when user scrolls up
- Shows truncated version of last user message as context
- Auto-hides when scrolled to bottom
- Reference: OpenClaude `src/components/FullscreenLayout.tsx` (`StickyPromptHeader`)

---

#### ⚠️ HALF-IMPLEMENTED — Syntax highlighting in code blocks
**Files:** `MarkdownRenderer.java:158`, `SyntaxHighlighter.java:222`

**What it does now:**
```java
// MarkdownRenderer.renderCodeBlock() just wraps in backticks:
sb.append("```").append(lang).append("\n");
sb.append(code);
sb.append("\n```\n");
// No call to SyntaxHighlighter.highlight()
```

**What needs to be done:**
- In `MarkdownRenderer.renderCodeBlock()`:
  1. Detect language from fence (python, java, javascript, etc.)
  2. Call `SyntaxHighlighter.highlight(code, language)`
  3. Apply ANSI colors to code tokens
  4. Add line numbers via `renderLineNumbers(int count, int gutterWidth)`
- `SyntaxHighlighter.highlightWithDef()` (line 184) returns basic styling — needs regex application
- Reference: OpenClaude `src/components/HighlightedCode.tsx`

---

### HIGH Priority

#### ⚠️ HALF-IMPLEMENTED — PromptInput multi-line
**Files:** `InputField.java:199`, `EnhancedInputField.java:345`

**What it does now:**
```java
// EnhancedInputField has multiline flag:
private boolean multiline = false;  // Line 40
// But Tab handling just calls setPrompt() — no actual indent
```

**What needs to be done:**
- `EnhancedInputField` Tab key → insert 4 spaces at cursor
- `EnhancedInputField` Shift-Tab → remove up to 4 spaces at line start
- Arrow keys in multi-line: Up/Down navigate lines, not just scroll
- Track cursor as (row, col), not just char offset
- Reference: OpenClaude `src/components/PromptInput/useMultilineInput.ts`

---

#### ⚠️ HALF-IMPLEMENTED — Typeahead file path completion
**File:** `TypeaheadEngine.java:199`

**What it does now:**
```java
// Only registers commands:
public void registerCommand(String name, String description) { ... }
// No filesystem traversal at all
```

**What needs to be done:**
- Add `indexFiles(String directory, int depth)` — recursively index files
- Add `completeFilePath(String partial)` — fuzzy match from indexed files
- Add `completeDirectory(String partial)` — suggest subdirectories
- Detect `/read ` prefix → trigger file path completion
- Detect `from "` or `import ` → trigger module completion
- Reference: OpenClaude `src/components/PromptInput/useTypeahead.tsx` line 300+

---

#### ⚠️ HALF-IMPLEMENTED — History search Ctrl+R
**File:** `HistorySearch.java:69`

**What it does now:**
```java
// Basic history navigation only:
public String handleHistoryUp() { return history.get(prevIndex--); }
public String handleHistoryDown() { return history.get(nextIndex++); }
// No reverse-i-search mode
```

**What needs to be done:**
- Add search mode: entered via Ctrl+R or `/` key
- Add `searchMode: boolean` field
- On each keystroke in search mode: filter history matching query
- Display: `(reverse-i-search) query: matched_text`
- Ctrl+R again: cycle to next match
- Enter: accept match and exit search mode
- Escape: cancel search, restore current input
- Reference: OpenClaude `src/components/PromptInput/HistorySearchDialog.tsx`

---

#### ❌ NOT STARTED — Slash command overlay (SuggestionsOverlay)
**File:** `CommandCompleter.java:280` (works but no UI overlay)

**What needs to be done:**
- Add `SuggestionsOverlay` component for dropdown UI
- Trigger on `/` prefix in input field
- Show top 5-8 matching commands with descriptions
- Arrow keys to navigate, Enter to select, Tab to complete
- Highlight matching characters in fuzzy match
- Reference: OpenClaude `src/components/PromptInput/PromptInputFooterSuggestions.tsx`

---

#### ⚠️ HALF-IMPLEMENTED — Permission mode display in UI
**Files:** `StatusBar.java:60`, `UIController.java:264`

**What it does now:**
```java
// StatusBar.renderCompact() has no permission indicator:
return String.format("Tokens: %d/%d │ %dms │ %s",
    tokens, max, latency, status);
// No READ_ONLY ● / BASH ● / RESTRICTED ● display
```

**What needs to be done:**
- Add `private PermissionMode permissionMode` to `StatusBar`
- Add `setPermissionMode(PermissionMode mode)` method
- In `renderCompact()`: append ` │ READ ●` / `BASH ●` / `RESTRICTED ●`
- Use color: green for READ_ONLY, yellow for BASH, red for RESTRICTED
- Read permission mode from `ConfigManager` or `BashTool`
- Reference: OpenClaude `src/components/StatusLine.tsx` permission section

---

#### ⚠️ HALF-IMPLEMENTED — MCP server discovery
**File:** `McpClient.java:186`

**What it does now:**
```java
// Only has basic JSON-RPC methods:
public CompletableFuture<String> callTool(String toolName, Map args) { ... }
// No stdio server auto-discovery
```

**What needs to be done:**
- Add `discoverServers()` — scan `~/.config/clipro/mcp.json` for server configs
- Add `startServer(String name, String command)` — spawn stdio process
- Add `listTools()` per server
- Add `stopServer(String name)` — kill process
- Parse MCP tool schemas into CLIPRO `Tool` format
- Reference: OpenClaude `src/tools/MCPTool/` (700+ lines)

---

#### ❌ NOT STARTED — LSP Tool
**File:** None — `LSPTool.java` does not exist

**What needs to be done:**
- Create `LSPTool.java` implementing Language Server Protocol
- Features needed:
  - `initialize()` — connect to LSP server (python/pyright, typescript/tsserver, rust/rust-analyzer, go/gopls)
  - `textDocument/definition()` — go-to-definition
  - `textDocument/references()` — find all references
  - `textDocument/hover()` — show type info
  - `textDocument/completion()` — auto-complete
  - `textDocument/diagnostic()` — lint errors
- Reference: OpenClaude `src/tools/LSPTool/` (2,100 lines)

---

#### ❌ NOT STARTED — OpenAI Provider
**File:** None — `OpenAIProvider.java` does not exist

**What needs to be done:**
- Create `OpenAIProvider.java` similar to `AnthropicProvider.java:158`
- Use OpenAI chat completions API: `https://api.openai.com/v1/chat/completions`
- Support: GPT-4, GPT-4o, GPT-4-turbo, GPT-3.5-turbo
- Implement streaming via SSE
- Implement tool calling (function calling)
- Reference: `OpenRouterProvider.java:157` as template

---

#### ⚠️ HALF-IMPLEMENTED — 6-theme hot-switching
**Files:** `ThemeManager.java:253`, `CommandRegistry.java:755`

**What it does now:**
- Theme switching works via `ConfigManager` (JSON config file)
- No runtime `/theme` command to switch without restart

**What needs to be done:**
- Add `/theme dark`, `/theme light`, `/theme dark-ansi`, `/theme light-ansi`, `/theme dark-daltonized`, `/theme light-daltonized` commands
- Register in `CommandRegistry.java`
- On theme change: re-render all UI components with new colors
- Persist to `~/.config/clipro/settings.json`
- Reference: OpenClaude `src/commands/theme/theme.tsx`

---

### MEDIUM Priority

#### ❌ NOT STARTED — StatsComponent ASCII charts
**File:** `StatsComponent.java:216`

**What needs to be done:**
- Add ASCII chart rendering for token usage over time
- Use `asciichart` library or implement simple bar chart with Unicode blocks
- Tab layout: Overview | Tokens | Cost | Session
- Show: tokens used per minute, cost per hour, message count
- Reference: OpenClaude `src/components/Stats.tsx` (1,227 lines)

---

#### ⚠️ HALF-IMPLEMENTED — Conversation compaction UI
**File:** `ConversationCompactor.java:89`

**What it does now:**
```java
// Compactor exists but no UI notification:
public List<Message> compact(List<Message> messages, int targetTokens) {
    // Removes middle messages to fit budget
    // No UI feedback when compaction happens
}
```

**What needs to be done:**
- Add `COMPACT` message type in `MessageRole.java`
- Show "Context compacted — X messages removed" in message list
- Add collapsible section for compacted messages
- Reference: OpenClaude `src/utils/messages.ts` compaction events

---

#### ❌ NOT STARTED — Agent CLI commands
**File:** `CommandRegistry.java:755` (no agent commands)

**What needs to be done:**
- Add `/spawn <model>` — spawn sub-agent with model
- Add `/kill <agent-id>` — kill running agent
- Add `/list` — list all running agents
- Add `/team create <name>` — create agent team
- Add `/team add <team> <agent>` — add agent to team
- Register in `CommandRegistry.java`
- Reference: OpenClaude `src/commands/agent/` directory

---

#### ❌ NOT STARTED — MCP CLI commands
**File:** `CommandRegistry.java:755` (no MCP commands)

**What needs to be done:**
- Add `/mcp list` — list configured MCP servers
- Add `/mcp add <name> <command>` — add MCP server
- Add `/mcp remove <name>` — remove MCP server
- Add `/mcp tools <server>` — list tools from server
- Reference: OpenClaude `src/commands/mcp/` directory

---

#### ❌ NOT STARTED — Theme CLI commands
**File:** `CommandRegistry.java:755` (no theme commands)

**What needs to be done:**
- Add `/theme` — show current theme
- Add `/theme dark | light | dark-ansi | light-ansi | dark-daltonized | light-daltonized`
- Add `/theme auto` — auto-detect from system
- Add `/theme preview` — preview all themes
- Reference: OpenClaude `src/commands/theme/theme.tsx`

---

#### ⚠️ HALF-IMPLEMENTED — Tool result background tint
**File:** `MessageBox.java:141` (`renderTool()`)

**What it does now:**
```java
public static String renderTool(String content) {
    sb.append(Terminal.boxRow(content, Terminal.getColumns()));
    // No background tint — plain text
}
```

**What needs to be done:**
- Add `toolResultBg` color to `Theme.java` (use OpenClaude's `#191923`)
- In `renderTool()`: wrap content in background-colored box
- Add `setBackgroundColor(Terminal.TOOL_RESULT_BG)` around content
- Reference: OpenClaude `src/components/Message.tsx` (`UserToolResultMessage`)

---

#### ❌ NOT STARTED — Cost tracking in status bar
**File:** `StatusBar.java:60`

**What needs to be done:**
- Track API cost per request from `OpenRouterProvider` (it returns usage data)
- Add `private double sessionCost = 0.0` field
- Add `recordCost(double cost)` method
- Display in status bar: `│ $12.34`
- Reference: OpenClaude `src/components/StatusLine.tsx` (`cost` field)

---

#### ❌ NOT STARTED — Rate limit display
**File:** `StatusBar.java:60`

**What needs to be done:**
- Track requests in rolling 5-hour and 7-day windows
- Display: `│ 5h: 45/100 │ 7d: 234/1000`
- Parse rate limit headers from API responses
- Reference: OpenClaude `src/components/StatusLine.tsx` (`rateLimits`)

---

#### ⚠️ HALF-IMPLEMENTED — Markdown tables
**File:** `MarkdownRenderer.java:158`

**What it does now:**
```java
// No table support — tables render as code:
if (line.startsWith("|")) {
    // Not handled — renders as plain text
}
```

**What needs to be done:**
- Parse table syntax: `| col1 | col2 |`, `| --- | --- |`
- Calculate column widths
- Render with borders using box-drawing characters
- Alignment: left/center/right based on `---:` syntax
- Reference: OpenClaude `src/components/Markdown.tsx` table rendering

---

#### ❌ NOT STARTED — Code block line numbers
**File:** `SyntaxHighlighter.java:222`

**What needs to be done:**
- Add `renderLineNumbers(int lineCount, int gutterWidth)` method
- Use `Terminal.DIM` for line number color
- Right-align numbers with spaces: `  1`, `  2`, ` 10`, `100`
- Integrate into `MarkdownRenderer.renderCodeBlock()`

---

#### ❌ NOT STARTED — Image attachments
**File:** None

**What needs to be done:**
- Parse `[Image: /path/to/image.png]` from message content
- Display: `[Image: filename.png]` with clickable link
- Or use terminal image protocol (Kitty/ITerm2) if supported
- Reference: OpenClaude `src/components/Message.tsx` (`UserImageMessage`)

---

### LOW Priority

#### ❌ NOT STARTED — Vim macros (q/)
**File:** `VimKeyHandler.java:298`

**What needs to be done:**
- Add `Map<Character, String> macros` to `VimMode.java`
- 'q' followed by register → enter macro recording mode
- 'q' in normal mode → replay macro from register
- Store keystrokes during recording

---

#### ❌ NOT STARTED — Vim substitute (:s)
**File:** `VimKeyHandler.java:298` (command mode partial)

**What needs to be done:**
- Parse `:s/pattern/replacement/flags` in command mode
- Apply regex replacement to current line or selection
- Support `:%s/` for entire buffer

---

#### ❌ NOT STARTED — Teammate view
**Files:** `AgentTeam.java:84`, `AgentManager.java:191`

**What needs to be done:**
- Visual display of team members in sidebar or status
- Show: agent name, model, current status (thinking/acting/idle)
- Agent speech bubbles (companion sprite)
- Reference: OpenClaude `src/components/agents/` directory

---

#### ❌ NOT STARTED — NotebookEditTool
**File:** None

**What needs to be done:**
- Create `NotebookEditTool.java`
- Edit `.ipynb` Jupyter notebook files
- Cell operations: add, delete, update, reorder
- Reference: OpenClaude `src/tools/NotebookEditTool/`

---

#### ❌ NOT STARTED — SkillTool
**File:** None

**What needs to be done:**
- Create `SkillTool.java`
- Load skills from `~/.config/clipro/skills/`
- Skills are prompt templates with parameters
- Execute via agent with skill context
- Reference: OpenClaude `src/tools/SkillTool/` (1,118 lines)

---

#### ❌ NOT STARTED — Additional LLM Providers
**Files:** None for these providers

| Provider | Create File | Template |
|----------|-------------|----------|
| AWS Bedrock | `BedrockProvider.java` | `AnthropicProvider.java` |
| Google Gemini | `GeminiProvider.java` | `AnthropicProvider.java` |
| GitHub Models | `GitHubModelsProvider.java` | `OpenRouterProvider.java` |
| Azure OpenAI | `AzureOpenAIProvider.java` | `OpenAIProvider.java` |

---

#### ❌ NOT STARTED — Remote session support
**File:** None

**What needs to be done:**
- SSH-based remote session: `clipro ssh user@host`
- Connect to remote CLIPRO instance via API
- Mirror messages between local and remote
- Reference: OpenClaude `QueryEngine.ts` remote session handling

---

## Part 6: Pending Tickets — 100% Migration Checklist

**Legend:**
- ❌ **NOT STARTED** — File doesn't exist, must be created from scratch
- ⚠️ **HALF-IMPLEMENTED** — File exists but needs completion/integration
- ✅ **DONE** — No ticket needed

Once all tickets below are closed, CLIPRO will be **100% migrated** from OpenClaude.

---

### CRITICAL Tickets (Must Fix for Pixel-Perfect UI)

| # | Ticket | File(s) | Type | OpenClaude Reference |
|---|--------|---------|------|---------------------|
| C-01 | Integrate ThinkingBlock into MessageBox rendering | `MessageBox.java` | ✅ DONE | `src/components/Message.tsx` |
| C-02 | Virtual scrolling with height caching + smooth scroll | `VirtualMessageList.java` | ⚠️ HALF | `src/components/VirtualMessageList.tsx` (1,082L) |
| C-03 | NewMessagesPill — "↓ N new messages" overlay | `FullscreenLayout.java` | ⚠️ HALF | `src/components/FullscreenLayout.tsx` |
| C-04 | StickyPromptHeader — context row when scrolled | `FullscreenLayout.java` | ⚠️ HALF | `src/components/FullscreenLayout.tsx` |
| C-05 | Syntax highlighting in MarkdownRenderer code blocks | `MarkdownRenderer.java`, `SyntaxHighlighter.java` | ✅ DONE | `src/components/HighlightedCode.tsx` |

---

### HIGH Priority Tickets

| # | Ticket | File(s) | Type | OpenClaude Reference |
|---|--------|---------|------|---------------------|
| H-01 | PromptInput multi-line: Tab indent, Shift-Tab, arrow nav | `EnhancedInputField.java` | ⚠️ HALF | `src/components/PromptInput/` (2,376L) |
| H-02 | Typeahead file path completion with filesystem traversal | `TypeaheadEngine.java` | ⚠️ HALF | `src/components/PromptInput/useTypeahead.tsx` (1,392L) |
| H-03 | History search Ctrl+R reverse-i-search mode | `HistorySearch.java` | ⚠️ HALF | `src/components/PromptInput/HistorySearchDialog.tsx` |
| H-04 | Slash command overlay SuggestionsDropdown | `CommandCompleter.java` | ⚠️ HALF | `src/components/PromptInput/PromptInputFooterSuggestions.tsx` |
| H-05 | Permission mode indicator in StatusBar (READ ● / BASH ● / RESTRICTED ●) | `StatusBar.java` | ⚠️ HALF | `src/components/StatusLine.tsx` |
| H-06 | MCP server discovery + server management in McpClient | `McpClient.java` | ⚠️ HALF | `src/tools/MCPTool/` (700L) |
| H-07 | LSP Tool — Language Server Protocol (go-to-def, refs, hover) | `src/main/java/com/clipro/tools/lsp/LSPTool.java` | ❌ NOT STARTED | `src/tools/LSPTool/` (2,100L) |
| H-08 | OpenAI Provider — GPT-4, GPT-4o, streaming, tool calling | `src/main/java/com/clipro/llm/providers/OpenAIProvider.java` | ❌ NOT STARTED | `src/services/providers/` |
| H-09 | Theme hot-switching via /theme CLI command | `CommandRegistry.java`, `ThemeManager.java` | ⚠️ HALF | `src/commands/theme/theme.tsx` |

---

### MEDIUM Priority Tickets

| # | Ticket | File(s) | Type | OpenClaude Reference |
|---|--------|---------|------|---------------------|
| M-01 | StatsComponent ASCII charts with token/cost graphs | `StatsComponent.java` | ⚠️ HALF | `src/components/Stats.tsx` (1,227L) |
| M-02 | Conversation compaction UI notification | `ConversationCompactor.java`, `MessageRole.java` | ⚠️ HALF | `src/utils/messages.ts` |
| M-03 | Agent CLI commands: /spawn, /kill, /list | `CommandRegistry.java` | ❌ NOT STARTED | `src/commands/agent/` |
| M-04 | MCP CLI commands: /mcp list, /mcp add, /mcp remove | `CommandRegistry.java` | ❌ NOT STARTED | `src/commands/mcp/` |
| M-05 | Theme CLI commands: /theme dark, /theme light, /theme preview | `CommandRegistry.java` | ❌ NOT STARTED | `src/commands/theme/theme.tsx` |
| M-06 | Tool result background tint (indigo #191923) | `MessageBox.java`, `Theme.java` | ⚠️ HALF | `src/components/Message.tsx` |
| M-07 | Cost tracking in StatusBar (OpenRouter usage data) | `StatusBar.java` | ❌ NOT STARTED | `src/components/StatusLine.tsx` |
| M-08 | Rate limit display in StatusBar (5h/7d windows) | `StatusBar.java` | ❌ NOT STARTED | `src/components/StatusLine.tsx` |
| M-09 | Markdown table rendering with alignment | `MarkdownRenderer.java` | ⚠️ HALF | `src/components/Markdown.tsx` |
| M-10 | Code block line numbers in SyntaxHighlighter | `SyntaxHighlighter.java`, `MarkdownRenderer.java` | ❌ NOT STARTED | `src/components/HighlightedCode.tsx` |
| M-11 | Image attachment rendering [Image: path] | `MessageBox.java` | ❌ NOT STARTED | `src/components/Message.tsx` |
| M-12 | User message background color (#373737) | `MessageBox.java`, `Theme.java` | ⚠️ HALF | `src/components/Message.tsx` |
| M-13 | grouped_tool_use message type rendering | `MessageList.java`, `MessageBox.java` | ❌ NOT STARTED | `src/components/Messages.tsx` |
| M-14 | collapsed_read_search message type rendering | `MessageList.java` | ❌ NOT STARTED | `src/components/Messages.tsx` |
| M-15 | BashTool AST parsing (tree-sitter or regex) for command validation | `BashTool.java` | ⚠️ HALF | `src/tools/BashTool/` (10,987L) |
| M-16 | BashTool Haiku classifier for auto-permission | `BashTool.java` | ❌ NOT STARTED | `src/tools/BashTool/haiku.ts` |
| M-17 | BashTool sed command validation | `BashTool.java` | ❌ NOT STARTED | `src/tools/BashTool/` |
| M-18 | BashTool permission persistence across sessions | `BashTool.java`, `ConfigManager.java` | ❌ NOT STARTED | `src/tools/BashTool/` |
| M-19 | FileReadTool image processing (PNG/JPEG dimension extraction) | `FileReadTool.java` | ❌ NOT STARTED | `src/tools/FileReadTool/` (1,400L) |
| M-20 | FileEditTool inline diff display with color | `FileEditTool.java` | ⚠️ HALF | `src/tools/FileEditTool/` (1,527L) |
| M-21 | WebSearchTool multi-provider support (SearXNG, Tavily, Serper) | `WebSearchTool.java` | ⚠️ HALF | `src/tools/WebSearchTool/` (800L) |
| M-22 | WebFetchTool HTML parsing and content extraction | `WebFetchTool.java` | ⚠️ HALF | `src/tools/WebFetchTool/` (536L) |
| M-23 | TaskTool nested task support (parent/child relationships) | `TaskTool.java` | ⚠️ HALF | `src/tools/TaskTool/` |
| M-24 | AgentManager tool schema loading from agent config | `AgentManager.java` | ⚠️ HALF | `src/tools/AgentTool/` (2,500L) |
| M-25 | AgentTeam teardown and member cleanup | `AgentTeam.java` | ⚠️ HALAL | `src/tools/TeamCreateTool/` |

---

### LOW Priority Tickets

| # | Ticket | File(s) | Type | OpenClaude Reference |
|---|--------|---------|------|---------------------|
| L-01 | Vim macros (q/ register recording and playback) | `VimMode.java`, `VimKeyHandler.java` | ❌ NOT STARTED | `src/hooks/useVimInput.ts` |
| L-02 | Vim substitute (:s/pattern/replacement/flags) | `VimKeyHandler.java` | ❌ NOT STARTED | `src/hooks/useVimInput.ts` |
| L-03 | Teammate view — visual agent status in sidebar | `AgentTeam.java`, new `TeammateView.java` | ❌ NOT STARTED | `src/components/agents/` |
| L-04 | NotebookEditTool — Jupyter .ipynb cell editing | `src/main/java/com/clipro/tools/notebook/NotebookEditTool.java` | ❌ NOT STARTED | `src/tools/NotebookEditTool/` |
| L-05 | SkillTool — load and execute skills from ~/.config/clipro/skills/ | `src/main/java/com/clipro/tools/skill/SkillTool.java` | ❌ NOT STARTED | `src/tools/SkillTool/` (1,118L) |
| L-06 | AWS Bedrock Provider (Claude on AWS) | `src/main/java/com/clipro/llm/providers/BedrockProvider.java` | ❌ NOT STARTED | `src/services/providers/` |
| L-07 | Google Gemini Provider | `src/main/java/com/clipro/llm/providers/GeminiProvider.java` | ❌ NOT STARTED | `src/services/providers/` |
| L-08 | GitHub Models Provider (Copilot) | `src/main/java/com/clipro/llm/providers/GitHubModelsProvider.java` | ❌ NOT STARTED | `src/services/providers/` |
| L-09 | Azure OpenAI Provider | `src/main/java/com/clipro/llm/providers/AzureOpenAIProvider.java` | ❌ NOT STARTED | `src/services/providers/` |
| L-10 | Remote session support (SSH to remote CLIPRO) | `src/main/java/com/clipro/session/RemoteSession.java` | ❌ NOT STARTED | `src/utils/remote.ts` |
| L-11 | JavaFX rich UI completion (replace stub) | `JavaFXAdapter.java` | ❌ STUB | `src/ui/javafx/` (if present) |
| L-12 | Permission mode display in PromptInput | `EnhancedInputField.java` | ❌ NOT STARTED | `src/components/PromptInput/` |
| L-13 | Image paste support in input field | `InputField.java` | ❌ NOT STARTED | `src/components/PromptInput/PromptInput.tsx` |
| L-14 | Queued commands editing in PromptInput | `EnhancedInputField.java` | ❌ NOT STARTED | `src/components/PromptInput/QueuedCommands.tsx` |
| L-15 | Block count indicators for thinking blocks | `ThinkingBlock.java`, `MessageBox.java` | ❌ NOT STARTED | `src/components/Message.tsx` |

---

### Ticket Detail: CRITICAL

#### TICKET C-01: Integrate ThinkingBlock into MessageBox Rendering
**Status:** ✅ DONE (commit bc98587)
**File:** `src/main/java/com/clipro/ui/components/MessageBox.java`
**Lines:** ~353 (now)
**What was done:**
- Parse `<thinking>` blocks with `ThinkingParser.parseBlocks()`
- Render rainbow-colored thinking blocks (7-color cycle)
- Collapsible UI with `[click to expand]` hint
- Block count indicator in header: `[💭 N thinking block(s)]`
- Ultrathink support with `[ultrathink]` label
- Proper ANSI escape handling (no word-wrap on colored content)

---

#### TICKET C-02: Virtual Scrolling with Height Caching + Smooth Scroll
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/VirtualMessageList.java`
**Lines:** 138 (currently) → needs ~500+ lines
**Problem:** Only has basic scroll offset (integer). No height tracking, no smooth scroll, no keyboard navigation, no sticky detection.
**Fix Required:**
1. Add `Map<Integer, Integer> itemHeights` — cache each message's rendered height (call `MessageBox.render()` once per message to measure)
2. Add `heightCache` invalidation on content change
3. Add smooth scroll methods:
   - `scrollToIndex(int index)` — scroll to specific message
   - `scrollToBottom()` — auto-scroll to latest
   - `scrollBy(int delta)` — smooth delta scroll
4. Add `ScrollBoxHandle` interface:
   - `scrollTo(int y)`, `scrollBy(int dy)`, `getScrollTop()`
   - `subscribe(ScrollListener listener)` for scroll events
   - `isSticky()` — detect if at bottom for auto-scroll
5. Add j/k keyboard navigation in `handleKey(KeyEvent e)`:
   - 'j' → `scrollToIndex(currentIndex + 1)`
   - 'k' → `scrollToIndex(currentIndex - 1)`
6. Reference: OpenClaude `src/components/VirtualMessageList.tsx` (1,082 lines)

---

#### TICKET C-03: NewMessagesPill Overlay
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/FullscreenLayout.java`
**Lines:** 192 (currently)
**Problem:** When user scrolls UP during streaming, there is no indicator showing new messages arrived below.
**Fix Required:**
1. Add field: `private int newMessageCount = 0;`
2. Add method: `showNewMessagesPill(int count)` — renders "↓ N new messages ▼"
3. Trigger logic:
   - When user calls `scrollUp()` → set `newMessageCount++` and show pill
   - When user calls `scrollToBottom()` → clear count and hide pill
   - When streaming completes → increment count if user is scrolled up
4. Position: overlay at bottom of message area (above prompt divider)
5. On click/Enter: `scrollToBottom()` + clear count
6. Reference: OpenClaude `src/components/FullscreenLayout.tsx` lines ~200+ (`NewMessagesPill`)

---

#### TICKET C-04: StickyPromptHeader
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/FullscreenLayout.java`
**Lines:** 192 (currently)
**Problem:** When user scrolls up to read old messages, they lose context of what they're discussing.
**Fix Required:**
1. Add 1-row sticky header that appears when `scrollOffset > 0`
2. Shows truncated last user message: max 50 chars + "..."
3. Format: `User: <truncated message>`
4. Auto-hide when `scrollOffset == 0` (at bottom)
5. Position: top of message area, fixed
6. Reference: OpenClaude `src/components/FullscreenLayout.tsx` (`StickyPromptHeader`)

---

#### TICKET C-05: Syntax Highlighting in MarkdownRenderer Code Blocks
**Status:** ✅ DONE (commit bc98587)
**Files:** `src/main/java/com/clipro/ui/components/MarkdownRenderer.java`, `src/main/java/com/clipro/ui/components/SyntaxHighlighter.java`
**What was done:**
- Full rewrite of `SyntaxHighlighter` with 20+ language support
- Priority-based token coloring: comment > string > number > function > keyword > operator
- `MarkdownRenderer` integrates `SyntaxHighlighter` into code blocks
- Line numbers with gutter, language header
- 20 languages: JS, TS, Python, Java, Go, Rust, C/C++, JSON, YAML, SQL, Bash, Markdown, PHP, Ruby, Kotlin, Swift, Scala, C#, XML, CSS

---

### Ticket Detail: HIGH

#### TICKET H-01: PromptInput Multi-line Support
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/EnhancedInputField.java`
**Lines:** 345 (currently)
**Problem:** `multiline` flag exists (line 40) but Tab/Shift-Tab don't indent, arrow keys don't navigate lines.
**Fix Required:**
1. Track cursor as `(int cursorRow, int cursorCol)` instead of single char offset
2. Tab key → insert 4 spaces at cursor position
3. Shift-Tab → remove up to 4 spaces from start of current line
4. Up/Down arrows in multi-line mode:
   - Up → move cursor to same column in previous line
   - Down → move cursor to same column in next line
5. Track line boundaries in a `List<Integer> lineStarts` array
6. Reference: OpenClaude `src/components/PromptInput/useMultilineInput.ts`

---

#### TICKET H-02: Typeahead File Path Completion
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/TypeaheadEngine.java`
**Lines:** 199 (currently)
**Problem:** Only registers commands. No filesystem traversal at all.
**Fix Required:**
1. Add `indexFiles(String directory, int maxDepth)` — recursively index files
   - Skip `node_modules/`, `.git/`, `target/`, `build/`, `__pycache__/`
   - Store in `Map<String, FileSuggestion> fileIndex`
2. Add `completeFilePath(String partial)`:
   - Extract directory part and filename part from partial path
   - Fuzzy match filename against `fileIndex`
   - Return top 5 matches with full paths
3. Add `completeDirectory(String partial)`:
   - Suggest subdirectories for tab completion
4. Trigger detection in `EnhancedInputField`:
   - On `/read ` → trigger file path completion
   - On `/edit ` → trigger file path completion
   - On `from "` or `import ` → trigger module completion
5. Reference: OpenClaude `src/components/PromptInput/useTypeahead.tsx` lines ~300+

---

#### TICKET H-03: History Search Ctrl+R (reverse-i-search)
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/HistorySearch.java`
**Lines:** 69 (currently)
**Problem:** Only has `handleHistoryUp()` / `handleHistoryDown()`. No search mode.
**Fix Required:**
1. Add `searchMode: boolean` field
2. Add `searchQuery: String` field
3. On Ctrl+R keypress:
   - Enter search mode
   - Show `(reverse-i-search) : ` prompt
   - Each keystroke filters history matching `searchQuery`
4. Display: `(reverse-i-search) query: matched_text`
5. Ctrl+R again in search mode → cycle to next older match
6. Enter → accept current match, exit search mode, set input to matched text
7. Escape → cancel search, restore original input, exit search mode
8. Reference: OpenClaude `src/components/PromptInput/HistorySearchDialog.tsx`

---

#### TICKET H-04: Slash Command Overlay (SuggestionsDropdown)
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/CommandCompleter.java`
**Lines:** 280 (currently — works but no overlay UI)
**Problem:** `CommandCompleter` has fuzzy matching logic but no dropdown UI overlay.
**Fix Required:**
1. Add `SuggestionsOverlay` class that renders dropdown at bottom of input area
2. Trigger on `/` prefix in input field
3. Show top 8 matching commands with:
   - Command name (highlighted fuzzy match characters)
   - Description from `CommandInfo`
4. Arrow Up/Down → navigate suggestions
5. Enter/Tab → select and complete
6. Escape → dismiss overlay
7. In `FullscreenLayout.renderWithInput()`: render overlay above input field
8. Reference: OpenClaude `src/components/PromptInput/PromptInputFooterSuggestions.tsx`

---

#### TICKET H-05: Permission Mode Indicator in StatusBar
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/StatusBar.java`
**Lines:** 60 (currently)
**Problem:** StatusBar shows `Tokens: X/Y │ Latency │ Status` but no permission mode.
**Fix Required:**
1. Add `private PermissionMode permissionMode` field
2. Add `setPermissionMode(PermissionMode mode)` method
3. In `renderCompact()`: append ` │ [MODE ●]`
   - `READ_ONLY` → `READ ●` in green
   - `BASH` → `BASH ●` in yellow
   - `RESTRICTED` → `RESTRICTED ●` in red
4. Read permission mode from `ConfigManager` or `BashTool.getPermissionMode()`
5. Update on permission change (from BashTool prompts)
6. Reference: OpenClaude `src/components/StatusLine.tsx` permission section

---

#### TICKET H-06: MCP Server Discovery and Management
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/mcp/McpClient.java`
**Lines:** 186 (currently — basic JSON-RPC only)
**Problem:** Can call tools but can't discover/start/stop MCP servers.
**Fix Required:**
1. Add `discoverServers()`:
   - Read `~/.config/clipro/mcp.json`
   - Parse server configs: `{ "name": "filesystem", "command": "npx", "args": ["-y", "@modelcontextprotocol/server-filesystem", "/home"] }`
2. Add `startServer(String name)`:
   - Spawn process with stdio (stdin/stdout)
   - Parse JSON-RPC messages from stdout
   - Send JSON-RPC messages to stdin
3. Add `listServers()` → return configured server names
4. Add `listTools(String server)` → return tools from server's tool list
5. Add `stopServer(String name)` → kill process
6. Parse MCP tool schemas → convert to CLIPRO `ToolDefinition` format
7. Reference: OpenClaude `src/tools/MCPTool/` (700+ lines)

---

#### TICKET H-07: LSP Tool — Language Server Protocol
**Status:** ❌ NOT STARTED
**File:** Create `src/main/java/com/clipro/tools/lsp/LSPTool.java`
**Lines:** ~600-800 (new file)
**Problem:** No LSP support for go-to-definition, find-references, hover, etc.
**Fix Required:**
1. Create `LSPTool.java` implementing Language Server Protocol over TCP/stdio
2. Implement:
   - `initialize(String language, String rootPath)` — start LSP server process
     - Python: `pip install pyright` → `pyright-langserver`
     - TypeScript: `typescript-language-server --stdio`
     - Rust: `rust-analyzer`
     - Go: `gopls`
     - Java: `jdtls` or `eclipse.jdt.ls`
   - `textDocument/definition(String uri, int line, int col)` → location
   - `textDocument/references(String uri, int line, int col)` → locations[]
   - `textDocument/hover(String uri, int line, int col)` → markdown content
   - `textDocument/completion(String uri, int line, int col)` → completions[]
   - `textDocument/publishDiagnostics(String uri)` → lint errors
   - `workspace/symbol(String query)` → symbols across project
3. Register LSP tools in `ToolRegistry`
4. Reference: OpenClaude `src/tools/LSPTool/` (2,100 lines of TypeScript)

---

#### TICKET H-08: OpenAI Provider
**Status:** ❌ NOT STARTED
**File:** Create `src/main/java/com/clipro/llm/providers/OpenAIProvider.java`
**Lines:** ~200 (new file)
**Problem:** No GPT-4, GPT-4o, GPT-3.5-turbo support.
**Fix Required:**
1. Create `OpenAIProvider.java` extending `LlmProvider`
2. API endpoint: `POST https://api.openai.com/v1/chat/completions`
3. Models: `gpt-4o`, `gpt-4-turbo`, `gpt-4`, `gpt-3.5-turbo`
4. Request format:
   ```json
   {
     "model": "gpt-4o",
     "messages": [...],
     "stream": true,
     "tools": [...]
   }
   ```
5. Implement SSE streaming via `text/event-stream` response
6. Parse `data: [DONE]` for completion
7. Implement function calling (tool_use)
8. Register in `ProviderManager`
9. Reference: `AnthropicProvider.java:158` as template

---

#### TICKET H-09: Theme Hot-Switching via /theme Command
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/cli/CommandRegistry.java`
**Problem:** Theme switching requires editing config file and restart. No runtime command.
**Fix Required:**
1. Add `/theme` command in `CommandRegistry.java`:
   - `/theme` → show current theme
   - `/theme dark` → switch to dark
   - `/theme light` → switch to light
   - `/theme dark-ansi` → switch to dark-ansi
   - `/theme light-ansi` → switch to light-ansi
   - `/theme dark-daltonized` → switch to daltonized
   - `/theme light-daltonized` → switch to daltonized
   - `/theme auto` → auto-detect from terminal
   - `/theme preview` → render all 6 themes side-by-side
2. On theme change: call `ThemeManager.setTheme(ThemeName.XYZ)`
3. Re-render all UI components: call `fullscreenLayout.render()` with new theme
4. Persist to `~/.config/clipro/settings.json`
5. Reference: OpenClaude `src/commands/theme/theme.tsx`

---

### Ticket Detail: MEDIUM

#### TICKET M-01: StatsComponent ASCII Charts
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/StatsComponent.java`
**Lines:** 216 (currently)
**Problem:** Shows basic stats text, no ASCII charts or graphs.
**Fix Required:**
1. Add ASCII bar chart rendering:
   - Token usage over time: `░░░░░░░█░░░░░` style
   - Message count per minute
   - Cost per hour
2. Tab layout: Overview | Tokens | Cost | Session
3. Implement simple bar chart with Unicode blocks: `░▒▓█`
4. Show metrics:
   - Tokens used: `████████░░ 80%`
   - API cost: `$12.34 this session`
   - Messages: `45 total (23 user, 22 assistant)`
   - Duration: `2h 34m`
5. Reference: OpenClaude `src/components/Stats.tsx` (1,227 lines) — simplify for TUI

---

#### TICKET M-02: Conversation Compaction UI Notification
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/session/ConversationCompactor.java`
**Lines:** 89 (currently)
**Problem:** Compactor removes old messages but no UI feedback.
**Fix Required:**
1. Add `COMPACT` entry in `MessageRole.java` enum
2. In `AgentEngine` after `compact()` call:
   - Create system message: `"Context compacted — X messages removed to stay within token budget"`
   - Add to `VirtualMessageStore`
   - Render with dimmed/italic styling
3. Make compaction collapsible:
   - `ConversationCompactor.getCompactedMessages()` returns removed messages
   - Show `[Show X compacted messages]` toggle
   - On expand: render compacted messages in dimmed style
4. Reference: OpenClaude `src/utils/messages.ts` compaction events

---

#### TICKET M-03: Agent CLI Commands
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/cli/CommandRegistry.java`
**Problem:** No CLI commands for managing agents.
**Fix Required:**
Add these commands in `CommandRegistry.java`:
- `/spawn <model> [prompt]` → spawn sub-agent with model, optional initial prompt
- `/kill <agent-id>` → terminate running agent
- `/list` → list all agents with status (idle/running/thinking)
- `/team create <name>` → create new agent team
- `/team add <team> <agent-id>` → add agent to team
- `/team remove <team> <agent-id>` → remove agent from team
- `/team list` → list all teams and their members
Register in `CommandRegistry.registerCommands()`

---

#### TICKET M-04: MCP CLI Commands
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/cli/CommandRegistry.java`
**Problem:** No CLI commands for managing MCP servers.
**Fix Required:**
Add these commands in `CommandRegistry.java`:
- `/mcp` → show all configured MCP servers
- `/mcp list` → list running MCP servers with tool count
- `/mcp add <name> <command> [args...]` → add server config to `~/.config/clipro/mcp.json`
- `/mcp remove <name>` → remove server config
- `/mcp start <name>` → start MCP server process
- `/mcp stop <name>` → stop MCP server process
- `/mcp tools <server>` → list tools available from server

---

#### TICKET M-05: Theme CLI Commands
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/cli/CommandRegistry.java`
**Problem:** No `/theme` commands (see TICKET H-09 for full detail).
**Fix Required:** See TICKET H-09 above.

---

#### TICKET M-06: Tool Result Background Tint
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/MessageBox.java`
**Lines:** 141 (currently)
**Problem:** Tool results render as plain text, no background color.
**Fix Required:**
1. Add `TOOL_RESULT_BG = "#191923"` to `Theme.java` colors
2. In `MessageBox.renderTool(String content)`:
   - Apply background color: ANSI `背景色` equivalent
   - Use `Terminal.inverse()` or custom escape: `\x1b[48;5;236m` (dark blue-grey)
3. Match OpenClaude: `#191923` indigo tint for tool output
4. Reference: OpenClaude `src/components/Message.tsx` (`UserToolResultMessage`)

---

#### TICKET M-07: Cost Tracking in StatusBar
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/ui/components/StatusBar.java`
**Problem:** No API cost display.
**Fix Required:**
1. Add `private double sessionCost = 0.0` field
2. Add `recordCost(double cost)` method
3. In `OpenRouterProvider.chat()`: parse `usage` from API response:
   ```json
   { "usage": { "cost": 0.0015 } }
   ```
4. In `StatusBar.renderCompact()`: append ` │ $12.34` (session cost)
5. Reset on new session
6. Reference: OpenClaude `src/components/StatusLine.tsx` cost section

---

#### TICKET M-08: Rate Limit Display in StatusBar
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/ui/components/StatusBar.java`
**Problem:** No 5-hour or 7-day rate limit indicators.
**Fix Required:**
1. Add `Map<Long, Long> requestTimestamps5h` and `requestTimestamps7d`
2. On each API request: add timestamp to both maps
3. Prune old timestamps on each request
4. In `StatusBar.renderCompact()`: append ` │ 5h: 45/100 │ 7d: 234/1000`
5. Parse `X-RateLimit-Remaining` headers from OpenRouter API
6. Show warning color when >80% used
7. Reference: OpenClaude `src/components/StatusLine.tsx` (`rateLimits`)

---

#### TICKET M-09: Markdown Table Rendering
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/MarkdownRenderer.java`
**Lines:** 158 (currently)
**Problem:** Tables render as plain text (pipes visible).
**Fix Required:**
1. Detect table lines: `if (line.startsWith("|") && line.endsWith("|"))`
2. Parse header row and separator row: `| col1 | col2 |`
3. Parse alignment: `| :--- | :---: | ---: |` (left/center/right)
4. Calculate max width per column
5. Render with box-drawing characters:
   ```
   ┌─────────┬─────────┐
   │ Name    │ Status  │
   ├─────────┼─────────┤
   │ Alice   │ Active  │
   │ Bob     │ Idle    │
   └─────────┴─────────┘
   ```
6. Reference: OpenClaude `src/components/Markdown.tsx` table rendering

---

#### TICKET M-10: Code Block Line Numbers
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/ui/components/SyntaxHighlighter.java`
**Problem:** No line numbers in code blocks.
**Fix Required:**
1. Add `renderLineNumbers(int lineCount, int gutterWidth)` method:
   ```java
   public static String renderLineNumbers(int lineCount, int gutterWidth) {
       StringBuilder sb = new StringBuilder();
       for (int i = 1; i <= lineCount; i++) {
           String num = String.valueOf(i);
           sb.append(Terminal.dim(String.format("%" + gutterWidth + "s", num)));
           sb.append(" ");
       }
       return sb.toString();
   }
   ```
2. In `MarkdownRenderer.renderCodeBlock()`:
   - Call `renderLineNumbers(lineCount, gutterWidth)`
   - Prepend to each line of code
3. `gutterWidth = String.valueOf(codeLines.size()).length()`
4. Reference: OpenClaude `src/components/HighlightedCode.tsx` (`gutterWidth`)

---

#### TICKET M-11: Image Attachment Rendering
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/ui/components/MessageBox.java`
**Problem:** No image display.
**Fix Required:**
1. Detect `[Image: /path/to/image.png]` in message content
2. Display as: `📎 image.png [1280x720]` with hyperlink
3. Optional: use Kitty/ITerm2 image protocol if terminal supports it:
   - `\x1b]1337;File=name=...;size=123;inline=1:$(base64 < image.png)\x7`
4. Fallback: show file path as clickable link via OSC 8
5. Reference: OpenClaude `src/components/Message.tsx` (`UserImageMessage`)

---

#### TICKET M-12: User Message Background Color
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/ui/components/MessageBox.java`, `src/main/java/com/clipro/ui/tamboui/Theme.java`
**Problem:** User messages render without background color.
**Fix Required:**
1. Add `USER_MESSAGE_BG = "#373737"` to `Theme.java` colors
2. Add `USER_MESSAGE_BG_HOVER = "#404040"` for hover
3. In `MessageBox.renderUser(String content)`:
   - Wrap content in background color box
   - Use `Terminal.setBackgroundColor(Terminal.USER_MESSAGE_BG)`
4. Match OpenClaude: `#373737` dark grey background for user messages
5. Reference: OpenClaude `src/components/Message.tsx` (`UserTextMessage`)

---

#### TICKET M-13: grouped_tool_use Message Type
**Status:** ❌ NOT STARTED
**Files:** `src/main/java/com/clipro/ui/components/MessageList.java`, `src/main/java/com/clipro/ui/components/MessageBox.java`
**Problem:** Multiple sequential tool calls don't collapse.
**Fix Required:**
1. In `MessageRole.java`: add `GROUPED_TOOL_USE` enum value
2. In `MessageList.render()`: detect consecutive `TOOL` messages
3. Group them under collapsible section:
   ```
   ┌─ [TOOL_CALL] 3 tools executed ─────────────┐
   │ ▶ /read src/main/App.java                   │
   │ ▶ /bash ls -la                              │
   │ ▶ /grep "TODO"                              │
   └─────────────────────────────────────────────┘
   ```
4. Click to expand/collapse
5. Reference: OpenClaude `src/components/Messages.tsx` (`grouped_tool_use`)

---

#### TICKET M-14: collapsed_read_search Message Type
**Status:** ❌ NOT STARTED
**Files:** `src/main/java/com/clipro/ui/components/MessageList.java`
**Problem:** Grep/search results don't collapse.
**Fix Required:**
1. In `MessageRole.java`: add `COLLAPSED_SEARCH` enum value
2. In `MessageList.render()`: detect search result messages
3. Render as collapsible:
   ```
   ┌─ [SEARCH] 47 matches in 12 files ────────────┐
   │ ▶ src/main/App.java:3                       │
   │ ▶ src/main/Agent.java:7                     │
   └─────────────────────────────────────────────┘
   ```
4. Click to expand full results
5. Reference: OpenClaude `src/components/Messages.tsx` (`collapsed_read_search`)

---

#### TICKET M-15: BashTool AST Parsing
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/tools/shell/BashTool.java`
**Lines:** 391 (currently)
**Problem:** Basic regex validation. OpenClaude uses tree-sitter for AST parsing.
**Fix Required:**
1. Option A: Use tree-sitter Java binding (complex)
2. Option B: Use regex-based command parsing:
   - Parse compound commands: `cmd1 && cmd2 || cmd3 ; cmd4 | cmd5`
   - Extract individual commands: `rm`, `mv`, `cp`, `dd`, etc.
   - Validate each command against safe command list
   - Detect command substitution: `$(cmd)`, `` `cmd` ``
   - Detect redirects: `>`, `>>`, `<`, `2>`, `&>`
3. Check each extracted command against `SAFE_COMMANDS` set
4. Flag suspicious patterns: `rm -rf`, `dd if=`, `mkfs`, etc.
5. Reference: OpenClaude `src/tools/BashTool/parser.ts` or `tree-sitter`

---

#### TICKET M-16: BashTool Haiku Classifier
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/tools/shell/BashTool.java`
**Problem:** No automatic permission classification for Haiku models.
**Fix Required:**
1. Create `HaikuClassifier.java` class
2. Load Haiku model (lightweight classifier)
3. On unknown command:
   - Classify risk level: LOW / MEDIUM / HIGH / DESTRUCTIVE
   - Match against known patterns (dangerous commands, path traversal, etc.)
   - Recommend permission level
4. Auto-mode: apply recommended permission without prompting
5. Reference: OpenClaude `src/tools/BashTool/haiku.ts` (if available in openclaude)

---

#### TICKET M-17: BashTool Sed Validation
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/tools/shell/BashTool.java`
**Problem:** Sed commands not validated.
**Fix Required:**
1. Detect `sed` command in input
2. Validate sed flags: `-e`, `-i`, `-n`, `-r`, `-E` (allow); `-f` (warn)
3. For `-i` (in-place edit):
   - Warn for destructive patterns: `sed -i 's/.*//g' file` (deletes content)
   - Check for `--in-place=.bak` backup flag
4. Validate regex patterns for correctness
5. Reference: OpenClaude `src/tools/BashTool/validators/sed.ts`

---

#### TICKET M-18: BashTool Permission Persistence
**Status:** ❌ NOT STARTED
**Files:** `src/main/java/com/clipro/tools/shell/BashTool.java`, `src/main/java/com/clipro/session/ConfigManager.java`
**Problem:** Permissions reset on each session.
**Fix Required:**
1. In `BashTool`: track approved commands in session
2. On command approval:
   - Save command pattern + permission level to `~/.config/clipro/permissions.json`
   - Format: `{ "command": "git commit *", "level": "BASH", "expires": "2026-04-15" }`
3. On session start:
   - Load permissions from `permissions.json`
   - Filter out expired permissions
4. Auto-approve if command matches persisted pattern
5. Reference: OpenClaude `src/tools/BashTool/permissions.ts`

---

#### TICKET M-19: FileReadTool Image Processing
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/tools/file/FileReadTool.java`
**Lines:** 158 (currently)
**Problem:** PNG/JPEG files not processed for dimension extraction.
**Fix Required:**
1. Detect image files by extension: `.png`, `.jpg`, `.jpeg`, `.gif`, `.webp`
2. Use Java image API to extract dimensions:
   ```java
   BufferedImage img = ImageIO.read(new File(path));
   int width = img.getWidth();
   int height = img.getHeight();
   ```
3. Append dimension info: `[PNG 1280x720]`
4. For very large images: show first 10KB + dimension info
5. Reference: OpenClaude `src/tools/FileReadTool/image.ts`

---

#### TICKET M-20: FileEditTool Inline Diff Display
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/tools/file/FileEditTool.java`
**Lines:** 131 (currently)
**Problem:** Edit results don't show colored diff.
**Fix Required:**
1. After edit, compute unified diff:
   - Get original file content
   - Get new content after edit
   - Compute line-level diff using `java-diff-utils` or custom algorithm
2. Render diff in `DiffColorizer`:
   - Green `+` lines for additions
   - Red `-` lines for deletions
   - Cyan ` ` lines for context
3. Format:
   ```
   - old line 1
   + new line 1
     unchanged line
   ```
4. Show in tool result with `DiffColorizer` ANSI colors
5. Reference: OpenClaude `src/tools/FileEditTool/diff.ts`

---

#### TICKET M-21: WebSearchTool Multi-Provider
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/tools/web/WebSearchTool.java`
**Lines:** 117 (currently)
**Problem:** Only SearXNG. OpenClaude supports multiple providers.
**Fix Required:**
1. Add provider interface: `SearchProvider` with `search(query) → results[]`
2. Implement providers:
   - `SearXNGProvider` — existing
   - `TavilyProvider` — requires API key
   - `SerperProvider` — requires API key
3. Provider selection:
   - Auto-select based on API key availability
   - Fallback: SearXNG (self-hosted)
4. Result format: `{ title, url, snippet }`
5. Reference: OpenClaude `src/tools/WebSearchTool/providers/` (800L total)

---

#### TICKET M-22: WebFetchTool HTML Parsing
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/tools/web/WebFetchTool.java`
**Lines:** 114 (currently)
**Problem:** Only fetches raw HTML. No content extraction.
**Fix Required:**
1. Use JSoup or similar for HTML parsing:
   - Extract `<article>`, `<main>`, `<p>` content
   - Remove `<script>`, `<style>`, `<nav>`, `<footer>`
   - Preserve code blocks (`<pre>`, `<code>`)
2. Detect content type:
   - Wikipedia → extract lead paragraph
   - GitHub README → render as markdown
   - StackOverflow → extract question + top answer
   - Generic → extract paragraphs
3. Truncate to 8,000 tokens with "..." if too long
4. Reference: OpenClaude `src/tools/WebFetchTool/fetch.ts`

---

#### TICKET M-23: TaskTool Nested Task Support
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/tools/TaskTool.java`
**Lines:** 151 (currently)
**Problem:** Only flat task list. No parent/child relationships.
**Fix Required:**
1. Add task fields: `parentId`, `children[]`, `depth`
2. Commands:
   - `/task create "name" --parent <parent-id>` → create subtask
   - `/task list --tree` → show task hierarchy
   - `/task complete <id>` → mark complete, may auto-complete parent if all children done
3. Rendering:
   ```
   ✓ Task 1
     ├─ ○ Task 1.1
     ├─ ✓ Task 1.2
     └─ ○ Task 1.3
   ```
4. Reference: OpenClaude `src/tools/TaskTool/`

---

#### TICKET M-24: AgentManager Tool Schema Loading
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/agent/AgentManager.java`
**Lines:** 191 (currently)
**Problem:** Agents spawn but don't load custom tool schemas.
**Fix Required:**
1. Add `loadToolSchema(String agentConfig)` method
2. Parse agent config from `~/.config/clipro/agents/<name>.json`:
   ```json
   {
     "name": "reviewer",
     "model": "qwen3-coder:32b",
     "tools": ["Read", "Grep", "Bash"],
     "systemPrompt": "You are a code reviewer..."
   }
   ```
3. Register only specified tools for this agent
4. Tool schema defines which tools agent can use
5. Reference: OpenClaude `src/tools/AgentTool/schema.ts`

---

#### TICKET M-25: AgentTeam Teardown and Cleanup
**Status:** ⚠️ HALF-IMPLEMENTED
**File:** `src/main/java/com/clipro/agent/AgentTeam.java`
**Lines:** 84 (currently)
**Problem:** Teams can be created but not properly torn down.
**Fix Required:**
1. Add `teardown()` method:
   - Send interrupt to all agent threads
   - Wait for completion (with timeout)
   - Force kill if stuck
2. Add `removeMember(String agentId)`:
   - Stop agent
   - Remove from team members map
3. Add `isActive()` check for all members
4. On exit: auto-call `teardown()` if team exists
5. Reference: OpenClaude `src/tools/TeamCreateTool/cleanup.ts`

---

### Ticket Detail: LOW

#### TICKET L-01: Vim Macros (q/)
**Status:** ❌ NOT STARTED
**Files:** `src/main/java/com/clipro/ui/vim/VimMode.java`, `src/main/java/com/clipro/ui/vim/VimKeyHandler.java`
**Problem:** No macro recording/playback.
**Fix Required:**
1. Add `Map<Character, String> macros` to `VimMode`
2. Recording: 'q' followed by register char (a-z) → enter recording mode
   - Store each keystroke in `recordingBuffer`
   - 'q' again → stop recording, save to `macros.get(register)`
3. Playback: 'q' followed by register → execute `macros.get(register)` as keystrokes
4. Count prefix: `3@x` → replay macro x 3 times

---

#### TICKET L-02: Vim Substitute (:s)
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/ui/vim/VimKeyHandler.java`
**Problem:** No `:s/pattern/replacement/flags` command.
**Fix Required:**
1. In command mode, parse `:s/([^/]*)/([^/]*)/([cg]?)`
2. Apply to current line or `:%s/` (entire input)
3. `g` flag → replace all occurrences
4. `c` flag → confirm each replacement
5. Reference: Standard vim `:s` behavior

---

#### TICKET L-03: Teammate View
**Status:** ❌ NOT STARTED
**Files:** `src/main/java/com/clipro/agent/AgentTeam.java`, new `src/main/java/com/clipro/ui/components/TeammateView.java`
**Problem:** No visual display of team members.
**Fix Required:**
1. Create `TeammateView.java` component
2. Render team members in status area or sidebar:
   ```
   Team: code-review
   ├─ alice (thinking...)  ██████░░ 60%
   ├─ bob (idle)
   └─ charlie (acting)  [====>    ] 40%
   ```
3. Show agent status: thinking / acting / idle / error
4. Show progress bar for long operations
5. Reference: OpenClaude `src/components/agents/TeammateView.tsx`

---

#### TICKET L-04: NotebookEditTool
**Status:** ❌ NOT STARTED
**File:** Create `src/main/java/com/clipro/tools/notebook/NotebookEditTool.java`
**Lines:** ~300 (new file)
**Problem:** No Jupyter notebook editing support.
**Fix Required:**
1. Parse `.ipynb` JSON format
2. Implement cell operations:
   - `addCell(String code, String type)` — type: code or markdown
   - `deleteCell(int index)`
   - `updateCell(int index, String content)`
   - `moveCell(int from, int to)`
3. Render notebook in TUI:
   ```
   ┌─ [1] Python ─────────────────────────────────┐
   │  import pandas as pd                          │
   └───────────────────────────────────────────────┘
   ┌─ [2] Markdown ───────────────────────────────┐
   │  # Data Analysis Results                      │
   └───────────────────────────────────────────────┘
   ```
4. Reference: OpenClaude `src/tools/NotebookEditTool/`

---

#### TICKET L-05: SkillTool
**Status:** ❌ NOT STARTED
**File:** Create `src/main/java/com/clipro/tools/skill/SkillTool.java`
**Lines:** ~400 (new file)
**Problem:** No skills system for extending capabilities.
**Fix Required:**
1. Create `~/.config/clipro/skills/` directory
2. Skill format (YAML or JSON):
   ```yaml
   name: "code-review"
   description: "Performs a code review"
   parameters:
     - name: path
       type: string
       required: true
   prompt: |
     Review the code at {{path}}.
     Focus on: bugs, security, performance.
   ```
3. `SkillTool` loads skills from directory
4. `/skill code-review --path src/` → execute skill
5. Inject skill prompt into agent messages
6. Reference: OpenClaude `src/tools/SkillTool/` (1,118 lines)

---

#### TICKET L-06 to L-10: Additional LLM Providers
**Status:** ❌ NOT STARTED
**Files:** Create in `src/main/java/com/clipro/llm/providers/`

| # | Provider | File | Notes |
|---|----------|------|-------|
| L-06 | AWS Bedrock | `BedrockProvider.java` | Claude via AWS, AWS SDK, IAM auth |
| L-07 | Google Gemini | `GeminiProvider.java` | Gemini API, vertex AI |
| L-08 | GitHub Models | `GitHubModelsProvider.java` | Copilot models via GitHub API |
| L-09 | Azure OpenAI | `AzureOpenAIProvider.java` | Azure-hosted GPT |

Use `AnthropicProvider.java` as template for all.

---

#### TICKET L-10: Remote Session Support
**Status:** ❌ NOT STARTED
**File:** Create `src/main/java/com/clipro/session/RemoteSession.java`
**Lines:** ~300 (new file)
**Problem:** No SSH-based remote session support.
**Fix Required:**
1. `clipro ssh user@host` command
2. Connect to remote CLIPRO via HTTP API
3. Mirror messages between local and remote
4. Show remote status in UI: `[remote: user@host]`
5. Reference: OpenClaude `src/utils/remote.ts`

---

#### TICKET L-11: JavaFX Rich UI
**Status:** ❌ STUB
**File:** `src/main/java/com/clipro/ui/javafx/JavaFXAdapter.java`
**Lines:** 18 (currently — empty stub)
**Problem:** No rich UI implementation.
**Fix Required:**
1. Implement `TuiAdapter` interface with JavaFX
2. Use `javafx.scene.control.ListView` for message list
3. Use `javafx.scene.web.WebView` for markdown rendering
4. Rich text: `TextFlow` with styled `Text` nodes
5. This is optional — TUI is primary

---

#### TICKET L-12: Permission Mode in PromptInput
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/ui/components/EnhancedInputField.java`
**Problem:** Permission mode not shown in input area.
**Fix Required:**
1. Add permission indicator in input prefix: `READ ▶` / `BASH ▶` / `REST ▶`
2. Color-coded: green READ, yellow BASH, red RESTRICTED
3. Show below input or in input prefix

---

#### TICKET L-13: Image Paste in Input
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/ui/components/InputField.java`
**Problem:** No paste handler for images.
**Fix Required:**
1. Detect paste event (Ctrl+V / Cmd+V)
2. Check clipboard for image data
3. Encode as base64: `data:image/png;base64,<base64>`
4. Insert as `[Image: /tmp/clipro-paste-1.png]` reference
5. Save to temp file

---

#### TICKET L-14: Queued Commands
**Status:** ❌ NOT STARTED
**File:** `src/main/java/com/clipro/ui/components/EnhancedInputField.java`
**Problem:** No command queue editing.
**Fix Required:**
1. Queue multiple commands before execution
2. Edit queued commands with arrow keys
3. `/` prefix → queue command
4. Show queue indicator: `[2 queued]`
5. Reference: OpenClaude `src/components/PromptInput/PromptInputQueuedCommands.tsx`

---

#### TICKET L-15: Thinking Block Count Indicators
**Status:** ❌ NOT STARTED
**Files:** `src/main/java/com/clipro/ui/components/ThinkingBlock.java`, `src/main/java/com/clipro/ui/components/MessageBox.java`
**Problem:** No indicator showing number of thinking blocks.
**Fix Required:**
1. Count thinking blocks in message: `int blockCount = countThinkingBlocks(content)`
2. Show: `[thinking: 2 blocks]` or `💭 2`
3. Position: top-right of assistant message box
4. Click → expand all thinking blocks

---

## Part 8: Working Well (What Was Done Right)

- ✅ **Permission modes** (READ_ONLY/BASH/RESTRICTED) with sandbox
- ✅ **BashTool security** — destructive command block, path traversal prevention
- ✅ **CommandRegistry** — 55+ commands with fuzzy autocomplete
- ✅ **6-theme system** — Full parity with OpenClaude themes including daltonized
- ✅ **Vim mode** — 85% of vim features (motions, operators, registers, marks, text objects)
- ✅ **ReAct agent loop** — Streaming, tool execution, token budget
- ✅ **LLM providers** — Ollama, OpenRouter, Anthropic all functional
- ✅ **ThinkingParser** — Full `<thinking>` tag parsing
- ✅ **RainbowRenderer + ShimmerAnimator** — Pixel-perfect color animation
- ✅ **VirtualMessageStore** — Windowing and pagination for large conversations
- ✅ **Git tools** — Status, diff, log, commit (exceeds OpenClaude)
- ✅ **ProviderManager** — Health checks and provider switching UI
- ✅ **DiffColorizer** — Syntax-colored diff rendering
- ✅ **ScheduleCronTool** — Cron job scheduling
- ✅ **AskUserQuestionTool** — User prompts

---

## Part 9: Line Count Summary

| Category | OpenClaude LOC | CLIPRO LOC | Ratio |
|----------|----------------|-----------|-------|
| Agent/Query Engine | ~5,200 | 364 | 7% |
| PromptInput + Typeahead | ~3,800 | 544 | 14% |
| FullscreenLayout + Messages | ~3,200 | 333 | 10% |
| Theme System | 640 | 450 | 70% |
| Tools (all) | ~18,000 | 1,700 | 9% |
| LLM Providers | ~5,000 | 576 | 12% |
| CLI Commands | 767 | 755 | 98% |
| Vim Mode | 500 | 408 | 82% |
| Spinner/Animation | 561 | 309 | 55% |
| **TOTAL** | **~50,000+** | **~5,439** | **~11%** |

---

*Report generated: 2026-04-14*
*Source: github.com/simpletoolsindia/clipro vs github.com/Gitlawb/openclaude*
*Analysis: File-by-file + function-by-function comparison*
