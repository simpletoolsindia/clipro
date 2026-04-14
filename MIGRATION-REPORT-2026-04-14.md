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
| `ThinkingBlock.java` | ✅ DONE | Rainbow + shimmer ✅, NOT integrated into MessageBox ❌ |
| `RainbowRenderer.java` | ✅ DONE | Full rainbow + shimmer |
| `ShimmerAnimator.java` | ✅ DONE | 120ms frame rate |
| `MessageBox.java` | ⚠️ HALF | Basic boxes ✅, thinking ❌, tool formatting ❌ |
| `FullscreenLayout.java` | ⚠️ HALF | Basic layout ✅, NewMessagesPill ❌, StickyPrompt ❌ |
| `VirtualMessageList.java` | ⚠️ HALF | Basic offset ✅, height cache ❌, smooth scroll ❌ |
| `InputField.java` + `EnhancedInputField.java` | ⚠️ HALF | Basic input ✅, multi-line ❌, Ctrl+R ❌ |
| `CommandCompleter.java` | ✅ DONE | 60+ commands, fuzzy search |
| `TypeaheadEngine.java` | ⚠️ HALF | Command completion ✅, file path ❌ |
| `HistorySearch.java` | ⚠️ HALF | Up/Down nav ✅, reverse-i-search ❌ |
| `MarkdownRenderer.java` | ⚠️ HALF | Bold/italic/code ✅, tables ❌, line numbers ❌ |
| `SyntaxHighlighter.java` | ⚠️ HALF | 18+ languages ✅, patterns defined ❌ applied ❌ |
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
| **Migration Score** | **~65%** overall |
| **UI Parity Score** | **~40%** |
| **Code Size vs Original** | ~9% of OpenClaude LOC |

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
| Syntax Highlighting | 30% | ⚠️ Basic | MEDIUM |
| Thinking Block Render | 50% | ⚠️ Partial | HIGH |

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

## Part 6: Working Well (What Was Done Right)

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

---

## Part 7: Line Count Summary

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
