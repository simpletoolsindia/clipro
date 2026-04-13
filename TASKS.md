# CLIPRO - Task Tracker (TDD Focus)

> **Mission:** Pixel-perfect AI coding CLI - LOCAL-FIRST Ollama + Native Tools + TDD

---

## CRITICAL RULES (READ FIRST)

### Agent Configuration
| Agent | Machine | Primary Tasks | Code Reference |
|-------|---------|---------------|----------------|
| **PC Agent** | This machine | UI components, TAMBUI setup | `/Users/sridhar/openclaude/` |
| **MB Agent** | Different machine | Ollama/LLM, Native tools, Agent engine | `~/openclaude/` |

### Code Reference Repository (MUST CLONE)
```bash
# Both agents MUST clone OpenClaude for reference:
git clone https://github.com/Gitlawb/openclaude.git ~/openclaude

# Reference path for code migration:
~/openclaude/src/
├── components/     # React components to migrate
├── ink/           # TUI framework
├── services/api/   # LLM API clients
├── tools/         # Tool implementations
└── hooks/         # State management
```

### TDD Workflow (MANDATORY)
```
1. Write FAILING test first
2. Write minimal code to pass
3. Refactor
4. Repeat
```

### PIXEL-PERFECT VERIFICATION (MANDATORY - BEFORE EVERY COMMIT)
```
┌─────────────────────────────────────────────────────────────────┐
│                  VERIFICATION CHECKLIST                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. BEFORE WRITING CODE:                                       │
│     □ Read OpenClaude source (~/openclaude/)                   │
│     □ Understand exact behavior, not just structure               │
│     □ Note edge cases, error handling, states                    │
│                                                                  │
│  2. AFTER WRITING CODE:                                         │
│     □ Compare output with OpenClaude behavior                   │
│     □ Test edge cases (empty, max, error)                       │
│     □ Visual comparison if UI component                        │
│                                                                  │
│  3. IF NOT PIXEL-PERFECT:                                       │
│     □ MIGRATION FAILED - Do NOT commit                          │
│     □ Research OpenClaude again                                │
│     □ Re-read the specific file/function                       │
│     □ Fix the code                                             │
│     □ Verify again                                             │
│     □ Repeat until PASS                                        │
│                                                                  │
│  4. COMMIT ONLY WHEN:                                          │
│     □ All tests pass                                           │
│     □ Behavior matches OpenClaude exactly                       │
│     □ No visual differences (UI)                               │
│     □ Token output matches expected format                     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Commit Author
```
Name: Sridhar Karuppusamy
Email: support@simpletools.in
```

---

## CORE PRIORITIES

### LOCAL-FIRST
```
1️⃣  OLLAMA (Local) → FIRST
    • qwen3-coder:32b (BEST tool calling)
    • qwen2.5-coder:14b
    • llama3.3:70b

2️⃣  OPENSOURCE (OpenRouter) → SECOND
3️⃣  CLOUD (Direct API) → LAST
```

### NATIVE TOOLS (14 built-in)
```
• Web Search  → SearXNG (https://search.sridharhomelab.in)
• Web Fetch   → Direct HTTP
• File Ops    → Java NIO.2
• Bash/Shell  → ProcessBuilder
• Git         → JGit
• Grep/Search → Java regex
• Code Exec   → Runtime.exec
```

---

## FILE REFERENCE GUIDE

### OpenClaude Source Structure
```
openclaude/src/
├── ink/                    # Terminal rendering framework
│   ├── ink.tsx            # Main Ink class (1752 lines)
│   ├── components/         # Low-level components
│   ├── reconciler.ts      # React reconciler
│   └── ...
├── components/            # React UI components (154 files)
│   ├── App.tsx           # Main app component
│   ├── Message.tsx       # Message component
│   ├── MessageList.tsx   # Message list
│   ├── VirtualMessageList.tsx # Virtual scrolling
│   ├── Input.tsx        # Input field
│   └── ...
├── services/             # Business logic
│   ├── api/             # LLM API clients
│   │   ├── openaiShim.ts # OpenAI compat (56KB)
│   │   └── claude.ts    # Claude client
│   └── ...
├── tools/                # Tool implementations (42 tools)
│   ├── BashTool/
│   ├── FileReadTool/
│   └── ...
└── hooks/               # React hooks (89 files)
```

### Key Files to Reference
| Purpose | OpenClaude File | Lines | Notes |
|---------|----------------|-------|-------|
| TUI Main | `src/ink/ink.tsx` | 1752 | Start here |
| Message List | `src/components/VirtualMessageList.tsx` | 1081 | Virtual scrolling |
| Message | `src/components/Message.tsx` | 626 | Message rendering |
| Input | `src/components/Input.tsx` | - | User input |
| LLM Client | `src/services/api/openaiShim.ts` | 1400+ | API shim |
| Bash Tool | `src/tools/BashTool/index.ts` | - | Shell execution |

---

## MIGRATION RULES

### Rule 1: TDD First
- [ ] Write test case BEFORE writing implementation
- [ ] All public methods must have tests
- [ ] Integration tests for end-to-end flows

### Rule 2: Small Tasks
- [ ] One class = One task
- [ ] Max 100 lines per class
- [ ] Max 1 feature per commit

### Rule 3: Reference Original
- [ ] Read OpenClaude source before implementing
- [ ] Match behavior, not structure
- [ ] Add reference links in comments

### Rule 4: Token Optimization
- [ ] Use this TASKS.md as master
- [ ] Only read SPEC.md when needed
- [ ] Don't read full OpenClaude files - use grep

---

## PROJECT STATISTICS
| Metric | Count |
|--------|-------|
| Native Tools | 14 |
| Test Files | ~50 |
| Phases | 10 |
| Total Tickets | 115+ |

---

## PHASE 1: Project Foundation

### P1.1: Gradle Setup
| Ticket | Task | Test | Files | Agent |
|--------|------|------|-------|-------|
| P1.1.1 | Create Gradle wrapper | ✅ `BuildTest` | `settings.gradle.kts` | PC |
| P1.1.2 | Add Quarkus dependencies | ✅ `QuarkusBuildTest` | `build.gradle.kts` | PC |
| P1.1.3 | Add TamboUI dependencies | ⏳ Pending | `build.gradle.kts` | PC |
| P1.1.4 | Add Jackson, JGit | ✅ `DepsBuildTest` | `build.gradle.kts` | PC |
| P1.1.5 | Configure GraalVM | ⏳ Pending | `native-image/` | PC |

### P1.2: Base Structure
| Ticket | Task | Test | Reference | Agent |
|--------|------|------|-----------|-------|
| P1.2.1 | Create package `com.clipro` | ✅ `PackageTest` | - | PC |
| P1.2.2 | Create `App.java` entry | ✅ `AppStartTest` | `src/main.tsx` | PC |
| P1.2.3 | Create `logging/` package | ✅ `LoggingTest` | - | PC |

---

## PHASE 2: UI Foundation

### P2.1: Terminal Setup
| Ticket | Task | Test | Reference | Agent |
|--------|------|------|-----------|-------|
| P2.1.1 | Detect terminal size | ✅ `TerminalSizeTest` | `src/ink/` | PC |
| P2.1.2 | Handle resize events | ✅ `ResizeTest` | `src/ink/` | PC |
| P2.1.3 | Basic output (Hello World) | ✅ `BasicOutputTest` | `src/ink/` | PC |

### P2.2: Message Components
| Ticket | Task | Test | Reference | Agent |
|--------|------|------|-----------|-------|
| P2.2.1 | `MessageBox` (assistant/user) | ✅ `MessageBoxTest` | `src/components/Message.tsx` | PC |
| P2.2.2 | `MessageRow` (with timestamp) | ✅ `MessageRowTest` | `src/components/Message.tsx` | PC |
| P2.2.3 | `MessageList` (scrollable) | ✅ `MessageListTest` | `src/components/VirtualMessageList.tsx` | PC |
| P2.2.4 | `MarkdownRenderer` | ✅ `MarkdownRenderTest` | `src/components/Message.tsx` | PC |
| P2.2.5 | `StreamingMessage` | ✅ `StreamingTest` | - | PC |

### P2.3: Input Components
| Ticket | Task | Test | Reference | Agent |
|--------|------|------|-----------|-------|
| P2.3.1 | `InputField` basic | ✅ `InputFieldTest` | `src/components/Input.tsx` | PC |
| P2.3.2 | `InputField` history | ✅ `HistoryTest` | `src/hooks/useArrowKeyHistory.ts` | PC |
| P2.3.3 | `CommandCompleter` | ✅ `CompleterTest` | - | PC |

### P2.4: Layout Components
| Ticket | Task | Test | Reference | Agent |
|--------|------|------|-----------|-------|
| P2.4.1 | `HeaderBar` with model | ✅ `HeaderBarTest` | `src/components/Stats.tsx` | PC |
| P2.4.2 | `StatusBar` with tokens | ✅ `StatusBarTest` | `src/components/Stats.tsx` | PC |
| P2.4.3 | `FullscreenLayout` | ⏳ Pending | `src/components/FullscreenLayout.tsx` | PC |

---

## PHASE 3: Vim Mode

### P3.1: Vim State
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P3.1.1 | `VimState` enum | `VimStateTest` | PC |
| P3.1.2 | `VimMode` transitions | `VimTransitionTest` | PC |
| P3.1.3 | `VimStateManager` | `VimManagerTest` | PC |

### P3.2: Vim Keybindings
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P3.2.1 | Motion commands (hjkl) | `MotionTest` | PC |
| P3.2.2 | Word motion (w, b, e) | `WordMotionTest` | PC |
| P3.2.3 | Operators (d, y, p) | `OperatorTest` | PC |
| P3.2.4 | `VimKeyHandler` | `KeyHandlerTest` | PC |

### P3.3: Vim Commands
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P3.3.1 | `:w`, `:q`, `:wq` | `VimCommandTest` | PC |
| P3.3.2 | `:set` options | `SetOptionTest` | PC |

---

## PHASE 4: OLLAMA BRIDGE (TDD)

### P4.1: Ollama Client (HIGHEST PRIORITY)
| Ticket | Task | Test | Reference | Agent |
|--------|------|------|-----------|-------|
| P4.1.1 | HTTP client setup | `HttpClientTest` | - | ~~MB~~ DONE |
| P4.1.2 | `ChatCompletionRequest` model | `RequestModelTest` | `openaiShim.ts` | ~~MB~~ DONE |
| P4.1.3 | `ChatCompletionResponse` model | `ResponseModelTest` | `openaiShim.ts` | ~~MB~~ DONE |
| P4.1.4 | `OllamaProvider` basic | `OllamaBasicTest` | `src/services/api/` | ~~MB~~ DONE |
| P4.1.5 | **Streaming** responses | `StreamingTest` | `openaiShim.ts` | ~~MB~~ DONE |
| P4.1.6 | **Tool calling** | `ToolCallTest` | `openaiShim.ts` | ~~MB~~ DONE |
| P4.1.7 | Model health check | `HealthCheckTest` | - | ~~MB~~ DONE |

### P4.2: OpenRouter Provider
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P4.2.1 | `OpenRouterProvider` | `OpenRouterTest` | MB |
| P4.2.2 | API key handling | `ApiKeyTest` | MB |

### P4.3: Streaming & Error Handling
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P4.3.1 | SSE parsing | `SSEParseTest` | MB |
| P4.3.2 | Retry logic | `RetryTest` | MB |
| P4.3.3 | Timeout handling | `TimeoutTest` | MB |

---

## PHASE 5: NATIVE TOOLS (TDD)

### P5.1: Web Tools (SearXNG)
| Ticket | Task | Test | Endpoint | Agent |
|--------|------|------|----------|-------|
| P5.1.1 | `WebSearchTool` | `WebSearchTest` | `search.sridharhomelab.in` | MB |
| P5.1.2 | `WebFetchTool` | `WebFetchTest` | - | MB |
| P5.1.3 | `QuickFetchTool` | `QuickFetchTest` | - | MB |

### P5.2: File Tools (Java NIO.2)
| Ticket | Task | Test | Reference | Agent |
|--------|------|------|-----------|-------|
| P5.2.1 | `FileReadTool` | `FileReadTest` | `src/tools/FileReadTool/` | MB |
| P5.2.2 | `FileWriteTool` | `FileWriteTest` | `src/tools/FileWriteTool/` | MB |
| P5.2.3 | `FileEditTool` | `FileEditTest` | `src/tools/FileEditTool/` | MB |
| P5.2.4 | `GlobTool` | `GlobTest` | `src/tools/GlobTool/` | MB |

### P5.3: Search Tools
| Ticket | Task | Test | Reference | Agent |
|--------|------|------|-----------|-------|
| P5.3.1 | `GrepTool` basic | `GrepTest` | `src/tools/GrepTool/` | MB |
| P5.3.2 | Regex support | `GrepRegexTest` | - | MB |

### P5.4: Shell Tools
| Ticket | Task | Test | Reference | Agent |
|--------|------|------|-----------|-------|
| P5.4.1 | `BashTool` execution | `BashExecTest` | `src/tools/BashTool/` | MB |
| P5.4.2 | Timeout handling | `BashTimeoutTest` | - | MB |
| P5.4.3 | Streaming output | `BashStreamTest` | - | MB |

### P5.5: Git Tools (JGit)
| Ticket | Task | Test | Reference | Agent |
|--------|------|------|-----------|-------|
| P5.5.1 | `GitStatusTool` | `GitStatusTest` | - | MB |
| P5.5.2 | `GitDiffTool` | `GitDiffTest` | - | MB |
| P5.5.3 | `GitLogTool` | `GitLogTest` | - | MB |
| P5.5.4 | `GitCommitTool` | `GitCommitTest` | - | MB |

---

## PHASE 6: TOOL REGISTRY (TDD)

### P6.1: Registry
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P6.1.1 | `ToolRegistry` setup | `RegistryTest` | MB |
| P6.1.2 | Lazy loading | `LazyLoadTest` | MB |
| P6.1.3 | Schema optimizer | `SchemaOptimizeTest` | MB |

### P6.2: Executor
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P6.2.1 | `ToolExecutor` | `ExecutorTest` | MB |
| P6.2.2 | Async execution | `AsyncExecTest` | MB |
| P6.2.3 | Output truncation | `TruncateTest` | MB |

---

## PHASE 7: AGENT ENGINE (TDD)

### P7.1: ReAct Loop
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P7.1.1 | `AgentEngine` setup | `AgentSetupTest` | MB |
| P7.1.2 | Reasoning step | `ReasoningTest` | MB |
| P7.1.3 | Action step | `ActionTest` | MB |
| P7.1.4 | Observation | `ObservationTest` | MB |

### P7.2: Token Budget
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P7.2.1 | `TokenBudget` | `TokenBudgetTest` | MB |
| P7.2.2 | Max iterations | `IterationTest` | MB |
| P7.2.3 | Context management | `ContextTest` | MB |

### P7.3: Model Router
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P7.3.1 | Fallback chain | `FallbackTest` | MB |
| P7.3.2 | Provider selection | `SelectionTest` | MB |

---

## PHASE 8: CLI COMMANDS (TDD)

### P8.1: Core Commands
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P8.1.1 | `CommandRegistry` | `RegistryTest` | MB |
| P8.1.2 | `/help` | `HelpTest` | MB |
| P8.1.3 | `/clear` | `ClearTest` | MB |
| P8.1.4 | `/exit` | `ExitTest` | MB |

### P8.2: Model Commands
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P8.2.1 | `/model` | `ModelTest` | MB |
| P8.2.2 | `/models` | `ModelsTest` | MB |

### P8.3: Dev Commands
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P8.3.1 | `/commit` | `CommitTest` | MB |
| P8.3.2 | `/review` | `ReviewTest` | MB |
| P8.3.3 | `/diff` | `DiffTest` | MB |
| P8.3.4 | `/status` | `StatusTest` | MB |

---

## PHASE 9: SESSION (TDD)

### P9.1: History
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P9.1.1 | `HistoryManager` | `HistoryTest` | MB |
| P9.1.2 | Persistence | `PersistTest` | MB |
| P9.1.3 | Search | `HistorySearchTest` | MB |

### P9.2: Configuration
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P9.2.1 | `ConfigManager` | `ConfigTest` | MB |
| P9.2.2 | API keys | `ApiKeyStoreTest` | MB |

---

## PHASE 10: TESTING & POLISH

### P10.1: Integration Tests
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P10.1.1 | Ollama E2E | `OllamaE2ETest` | MB |
| P10.1.2 | Tool calling E2E | `ToolCallE2ETest` | MB |
| P10.1.3 | SearXNG E2E | `SearXNGE2ETest` | MB |

### P10.2: Performance Tests
| Ticket | Task | Test | Agent |
|--------|------|------|-------|
| P10.2.1 | Startup <100ms | `StartupPerfTest` | PC |
| P10.2.2 | Memory <50MB | `MemoryTest` | PC |
| P10.2.3 | UI render 60fps | `RenderPerfTest` | PC |

### P10.3: Documentation
| Ticket | Task | Agent |
|--------|------|-------|
| P10.3.1 | `README.md` | Both |
| P10.3.2 | `CONTRIBUTING.md` | Both |

---

## TEST TEMPLATE

```java
// File: src/test/java/com/clipro/{module}/{Feature}Test.java

package com.clipro.{module};

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class {Feature}Test {

    @Test
    void should_{expected_behavior}_when_{condition}() {
        // Given
        // When
        // Then
    }

    @Test
    void should_handle_{edge_case}() {
        // Given
        // When
        // Then
    }
}
```

---

## QUICK REFERENCE

| Item | Value |
|------|-------|
| Ollama | `http://localhost:11434/v1` |
| SearXNG | `https://search.sridharhomelab.in` |
| Best Model | `qwen3-coder:32b` |
| Build | `./gradlew build` |
| Native | `./gradlew nativeCompile` |
| Test | `./gradlew test` |

---

**Last Updated:** 2026-04-13
**Repository:** https://github.com/simpletoolsindia/clipro
