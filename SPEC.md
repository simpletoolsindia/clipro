# CLIPRO - Complete Specification

## Project Overview

**Mission:** Build a pixel-perfect, high-performance AI coding CLI in Java with **LOCAL-FIRST** approach - Ollama and native tools as priority.

---

## CORE PRINCIPLES

### 1. LOCAL-FIRST (Highest Priority)
```
┌─────────────────────────────────────────────────────────────────────────┐
│                    PRIORITY ORDER                                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  1️⃣  OLLAMA (Local)              ████████████████████  FIRST         │
│      • Qwen3-Coder-Next (32B)    │ Tool calling: ✅ YES              │
│      • Qwen2.5-Coder (7B/14B)    │ Agentic coding: ✅ YES            │
│      • Llama 3.3 (70B)            │ No API costs, no latency         │
│                                                                          │
│  2️⃣  OPENSOURCE via OpenRouter   ████████░░░░░░░░░░░░  SECOND       │
│      • Claude Sonnet via OR        │ Best open models                 │
│      • GPT-4o via OR              │ Fallback when local insufficient   │
│                                                                          │
│  3️⃣  CLOUD (Only if needed)      ████░░░░░░░░░░░░░░░  THIRD        │
│      • Direct Anthropic            │ Only for features local can't do  │
│      • Direct OpenAI              │ High cost, last resort             │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2. ALL NATIVE TOOLS (No External MCP)
```
┌─────────────────────────────────────────────────────────────────────────┐
│                    NATIVE TOOLS ONLY                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  NO external MCP server dependency!                                     │
│                                                                          │
│  Built-in Tools:                                                        │
│  ├── Web Search      → SearXNG (https://search.sridharhomelab.in)     │
│  ├── Web Fetch       → Direct HTTP + content extraction                │
│  ├── File Ops       → Java NIO.2                                      │
│  ├── Bash/Shell      → ProcessBuilder + VirtualThreads                 │
│  ├── Git            → JGit library                                    │
│  ├── Search/Grep    → Java regex + NIO.2                              │
│  ├── Code Execute   → Runtime.exec / JShell                          │
│  └── LLM Calls      → Native HTTP client                              │
│                                                                          │
│  All tools token-optimized:                                             │
│  ├── Schema trimming (40-60% reduction)                              │
│  ├── Output truncation (configurable)                                 │
│  └── Lazy loading (only load when needed)                             │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## RESEARCH SUMMARY

### TamboUI (UI Framework)
- **What:** React-like TUI framework for Java (Feb 2026)
- **Creator:** Cédric Champeau & Max Rydahl Andersen
- **Features:** CSS styling, virtual scrolling, GraalVM native
- **Match:** 90% pattern match to OpenClaude's Ink/React

### LLM Provider Priority

| Priority | Provider | Models | Why |
|----------|----------|--------|-----|
| **1** | Ollama | Qwen3-Coder, Llama 3.3, Gemma 4 | Free, fast, local |
| **2** | OpenRouter | 300+ models | Unified API, good pricing |
| **3** | DeepSeek | V3, R1 | Cheap, good coding |
| **4** | Anthropic | Claude 4 | Best reasoning |
| **5** | OpenAI | GPT-5 | Last resort |

### Tool Calling Models (Tested)

| Model | Tool Calling | Agentic | Local? | RECOMMENDED |
|-------|-------------|---------|--------|-------------|
| **Qwen3-Coder-Next** | ✅ | ✅ | ✅ | ⭐⭐⭐ BEST |
| **Qwen2.5-Coder** | ✅ | ✅ | ✅ | ⭐⭐ |
| **Llama 3.3 70B** | ✅ | ✅ | ✅ | ⭐⭐ |
| **DeepSeek-R1** | ⚠️ | ✅ | ✅ | ⭐ (reasoning) |
| **Claude Sonnet** | ✅ | ✅ | ❌ | ⭐⭐⭐ |
| **GPT-4o** | ✅ | ✅ | ❌ | ⭐⭐ |

---

## ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    CLIPRO ARCHITECTURE                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    TAMBUI LAYER (UI)                            │  │
│  │  • React-like components                                         │  │
│  │  • Virtual scrolling                                            │  │
│  │  • Vim mode                                                     │  │
│  │  • TCSS styling                                                 │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                              │                                          │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    SESSION LAYER                                 │  │
│  │  • Conversation history                                          │  │
│  │  • Project context                                              │  │
│  │  • Memory store                                                 │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                              │                                          │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    AGENT ENGINE                                   │  │
│  │  • Tool executor                                                │  │
│  │  • ReAct loop (Reasoning + Acting)                            │  │
│  │  • Token budget management                                      │  │
│  │  • Model routing                                                │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                              │                                          │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    LLM BRIDGE                                    │  │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │  │
│  │  │   OLLAMA   │ │ OPENROUTER  │ │  DEEPSEEK   │               │  │
│  │  │  (local)   │ │ (aggregator)│ │   (cheap)   │               │  │
│  │  └─────────────┘ └─────────────┘ └─────────────┘               │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                              │                                          │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    NATIVE TOOLS                                  │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐            │  │
│  │  │ Web Search  │ │ File Ops    │ │ Bash/Shell  │            │  │
│  │  │ SearXNG    │ │ Java NIO.2  │ │ ProcessBuilder│            │  │
│  │  └──────────────┘ └──────────────┘ └──────────────┘            │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐            │  │
│  │  │ Git (JGit) │ │ Grep/Search │ │ Code Exec   │            │  │
│  │  │             │ │             │ │             │            │  │
│  │  └──────────────┘ └──────────────┘ └──────────────┘            │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## TECHNOLOGY STACK

| Component | Technology | Version | Justification |
|-----------|------------|---------|---------------|
| **TUI** | TamboUI | 0.2.0+ | React-like, GraalVM native |
| **Backend** | Quarkus | 3.12+ | Subatomic startup |
| **Build** | Gradle | 8.x | Kotlin DSL |
| **Native** | GraalVM | 24.x | <100ms startup |
| **HTTP** | Mutiny | - | Reactive, async |
| **JSON** | Jackson | 2.17+ | Kotlin support |
| **Git** | JGit | 7.0+ | Pure Java Git |
| **Search** | SearXNG | - | Your instance |

---

## OLLAMA CONFIGURATION

### Recommended Models (In Priority Order)

```yaml
# ollama configured models
models:
  # PRIMARY: Best for tool calling & agentic coding
  - name: qwen3-coder:32b
    display: Qwen3-Coder (32B)
    tool_calling: true
    agentic: true
    vram: 20GB
    reason: "BEST local model for coding agents"

  - name: qwen2.5-coder:14b
    display: Qwen2.5-Coder (14B)
    tool_calling: true
    agentic: true
    vram: 10GB
    reason: "Good balance of speed and capability"

  - name: llama3.3:70b
    display: Llama 3.3 (70B)
    tool_calling: true
    agentic: true
    vram: 40GB
    reason: "Best general purpose local"

  - name: mistral-nemo:12b
    display: Mistral Nemo (12B)
    tool_calling: true
    agentic: true
    vram: 8GB
    reason: "Fast inference"

  # REASONING (non-tool tasks)
  - name: deepseek-r1:70b
    display: DeepSeek R1 (70B)
    tool_calling: false
    agentic: true
    vram: 40GB
    reason: "Best reasoning, no tool calling"
```

### Ollama API Endpoints

```java
// Local Ollama
OLLAMA_BASE_URL = "http://localhost:11434/v1"

// Chat completions
POST /chat/completions

// Models
GET /api/tags

// Embeddings
POST /embeddings
```

---

## SEARXNG CONFIGURATION

```java
// Your SearXNG Instance
SEARXNG_BASE_URL = "https://search.sridharhomelab.in"

// API Format
SEARXNG_API_URL = "https://search.sridharhomelab.in/search"

// Parameters
?q={query}
&format=json
&engines=wikipedia,github,hackernews
&safe_search=1
```

---

## NATIVE TOOLS SPECIFICATION

### Tool 1: Web Search
```java
@Tool(name = "web_search", description = "Search the web using SearXNG")
public class WebSearchTool {
    // Uses: https://search.sridharhomelab.in
    // Token optimization: Limit results to 10
    // Returns: Title, URL, snippet
}
```

### Tool 2: Web Fetch
```java
@Tool(name = "web_fetch", description = "Fetch and extract content from URLs")
public class WebFetchTool {
    // Token optimization:
    // - Max 4000 tokens output
    // - Strip HTML, keep markdown
    // - Remove ads, nav, scripts
}
```

### Tool 3: File Read
```java
@Tool(name = "file_read", description = "Read file contents")
public class FileReadTool {
    // Token optimization:
    // - Truncate files > 100KB
    // - Syntax highlighting in output
    // - Line numbers
}
```

### Tool 4: File Write
```java
@Tool(name = "file_write", description = "Write content to files")
public class FileWriteTool {
    // Creates directories if needed
    // Atomic writes
    // Backup option
}
```

### Tool 5: File Edit
```java
@Tool(name = "file_edit", description = "Edit specific parts of files")
public class FileEditTool {
    // Unified diff format
    // Validation before write
    // Undo support
}
```

### Tool 6: Glob (File Pattern)
```java
@Tool(name = "glob", description = "Find files matching patterns")
public class GlobTool {
    // Glob patterns: **/*.java, *.ts, etc.
    // Token optimization:
    // - Max 100 files
    // - Relative paths
}
```

### Tool 7: Grep (Search in Files)
```java
@Tool(name = "grep", description = "Search for patterns in files")
public class GrepTool {
    // Regex support
    // Case insensitive option
    // Line numbers and context
}
```

### Tool 8: Bash
```java
@Tool(name = "bash", description = "Execute shell commands")
public class BashTool {
    // Timeout: 30 seconds default
    // Working directory support
    // Environment variables
    // Output streaming
}
```

### Tool 9: Git Status
```java
@Tool(name = "git_status", description = "Show git working tree status")
public class GitStatusTool {
    // Short format by default
    // Include untracked files
    // Branch information
}
```

### Tool 10: Git Diff
```java
@Tool(name = "git_diff", description = "Show changes between commits")
public class GitDiffTool {
    // Unified diff format
    // File filtering
    // Line-by-line
}
```

### Tool 11: Git Log
```java
@Tool(name = "git_log", description = "Show commit logs")
public class GitLogTool {
    // Limit: 20 commits default
    // Format: hash, message, author, date
}
```

### Tool 12: Git Commit
```java
@Tool(name = "git_commit", description = "Commit changes")
public class GitCommitTool {
    // Auto-stage option
    // Message required
    // GPG signing optional
}
```

### Tool 13: Code Execute
```java
@Tool(name = "code_execute", description = "Execute code snippets")
public class CodeExecuteTool {
    // Language detection
    // Supported: JavaScript, Python, Shell
    // Timeout: 10 seconds
    // Output capture
}
```

### Tool 14: Memory Store
```java
@Tool(name = "memory_store", description = "Store information for later retrieval")
public class MemoryStoreTool {
    // Key-value storage
    // Session-based
    // Search capability
}
```

---

## TOKEN OPTIMIZATION

### Built-in Optimizations

| Optimization | Reduction | Implementation |
|-------------|-----------|----------------|
| Schema trimming | 40-60% | Remove verbose descriptions |
| Output truncation | 80% | Max 4000 tokens per tool |
| Lazy loading | 91% | Only load tool when called |
| Context mode | 98% | Store large outputs externally |

### Token Budget

```
Default per request:
├── System prompt: 2000 tokens
├── Conversation: 8000 tokens
├── Tool schemas: 2000 tokens (optimized)
├── Tool outputs: 4000 tokens (max)
└── Response: 4000 tokens
Total: 20000 tokens max
```

---

## PERFORMANCE TARGETS

| Metric | Target | Method |
|--------|--------|--------|
| Startup time | <100ms | GraalVM native |
| Memory usage | <50MB | GraalVM native |
| UI render | 60fps | Immediate mode |
| Tool execution | <100ms | Virtual threads |
| LLM streaming | Real-time | Async HTTP |
| Web search | <500ms | Your SearXNG |

---

## FILE STRUCTURE

```
clipro/
├── build.gradle.kts
├── settings.gradle.kts
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/clipro/
│   │   │       ├── App.java
│   │   │       ├── ui/
│   │   │       │   ├── AppLayout.java
│   │   │       │   ├── components/
│   │   │       │   │   ├── MessageBox.java
│   │   │       │   │   ├── MessageList.java
│   │   │       │   │   ├── InputField.java
│   │   │       │   │   ├── HeaderBar.java
│   │   │       │   │   ├── StatusBar.java
│   │   │       │   │   └── dialogs/
│   │   │       │   ├── vim/
│   │   │       │   │   ├── VimMode.java
│   │   │       │   │   └── VimKeyHandler.java
│   │   │       │   └── theme/
│   │   │       │       └── Colors.java
│   │   │       ├── llm/
│   │   │       │   ├── LlmClient.java
│   │   │       │   ├── providers/
│   │   │       │   │   ├── OllamaProvider.java
│   │   │       │   │   └── OpenRouterProvider.java
│   │   │       │   └── models/
│   │   │       │       └── ChatCompletion.java
│   │   │       ├── tools/
│   │   │       │   ├── ToolRegistry.java
│   │   │       │   ├── ToolExecutor.java
│   │   │       │   ├── WebSearchTool.java
│   │   │       │   ├── WebFetchTool.java
│   │   │       │   ├── FileReadTool.java
│   │   │       │   ├── FileWriteTool.java
│   │   │       │   ├── FileEditTool.java
│   │   │       │   ├── GlobTool.java
│   │   │       │   ├── GrepTool.java
│   │   │       │   ├── BashTool.java
│   │   │       │   ├── GitTool.java
│   │   │       │   └── CodeExecuteTool.java
│   │   │       ├── agent/
│   │   │       │   ├── AgentEngine.java
│   │   │       │   ├── ToolCallingLoop.java
│   │   │       │   └── TokenBudget.java
│   │   │       └── session/
│   │   │           ├── HistoryManager.java
│   │   │           ├── ConfigManager.java
│   │   │           └── MemoryStore.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── tcss/
│   │           └── theme.tcss
│   └── test/
│       └── java/
│           └── com/clipro/
├── native-image/
│   └── native-image.properties
└── README.md
```

---

## IMPLEMENTATION PHASES

### Phase 1: Foundation (1 week)
- [ ] Gradle project setup
- [ ] TamboUI integration
- [ ] GraalVM native build
- [ ] Basic terminal output

### Phase 2: UI (2 weeks)
- [ ] Message components
- [ ] Input field
- [ ] Vim mode
- [ ] Dialogs

### Phase 3: LLM Bridge (1 week)
- [ ] Ollama client (priority)
- [ ] Streaming responses
- [ ] Tool calling protocol

### Phase 4: Native Tools (2 weeks)
- [ ] File tools
- [ ] Bash tool
- [ ] Git tools
- [ ] Web tools (SearXNG)

### Phase 5: Agent Engine (1 week)
- [ ] Tool executor
- [ ] ReAct loop
- [ ] Token budget

### Phase 6: Polish (1 week)
- [ ] Testing
- [ ] Documentation
- [ ] Native build

---

## SUCCESS CRITERIA

- [ ] Startup < 100ms
- [ ] Memory < 50MB
- [ ] Ollama tool calling works
- [ ] All 14 native tools functional
- [ ] SearXNG search works
- [ ] Vim mode functional
- [ ] Single native binary

---

**Project:** CLIPRO - Java AI CLI
**Repository:** https://github.com/simpletoolsindia/clipro
**Updated:** 2026-04-13
