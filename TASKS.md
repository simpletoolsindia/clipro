# CLIPRO - Java AI CLI Task Tracker

> **Mission:** Build pixel-perfect, high-performance AI coding CLI in Java matching OpenClaude

---

## AGENT RULES (MUST READ BEFORE ANY TASK)

### Agent Configuration
| Agent | Machine | Primary Tasks |
|-------|---------|---------------|
| **PC Agent** (Primary Coder) | This machine | UI components, TAMBUI setup, core infrastructure |
| **MB Agent** (Migrate Builder) | Different machine | Services, tools, LLM integration, commands |

### Workflow Rules
1. **Before starting any task:**
   - Read this TASKS.md file
   - Check task status (TODO/IN PROGRESS/DONE)
   - Pull latest from repo: `git pull origin main`

2. **After completing any task:**
   - Run tests (if applicable)
   - Update this TASKS.md status
   - Commit with format: `git add . && git commit -m "[TICKET-X] Task description #done"`
   - Push: `git push origin main`

3. **Commit Author (MANDATORY)**
   ```
   Name: Sridhar Karuppusamy
   Email: support@simpletools.in
   ```

4. **Branch Strategy**
   - Main branch: `main` (production-ready code only)
   - Feature branches: `feat/ticket-{number}-{description}`
   - Commit & PR in one step for simplicity

---

## PROJECT STATISTICS

| Metric | Count |
|--------|-------|
| Total Source Files | 2,087 |
| Total Lines of Code | 544,822 |
| Directories | 320 |
| React Components | 357 |
| CLI Commands | 129 |
| Tool Implementations | 176 |

---

## PHASE 1: Project Foundation

### 1.1 Project Setup
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P1-T1 | Create Gradle project with Quarkus + TamboUI + GraalVM | PC Agent | TODO | `build.gradle.kts`, `settings.gradle.kts` |
| P1-T2 | Configure GraalVM native build settings | PC Agent | TODO | `native-image/native-image.properties` |
| P1-T3 | Set up logging and error handling | PC Agent | TODO | `src/main/java/com/clipro/logging/` |
| P1-T4 | Create base application entry point | PC Agent | TODO | `src/main/java/com/clipro/App.java` |
| P1-T5 | Configure Git hooks for commit validation | PC Agent | TODO | `.git/hooks/` |

### 1.2 Dependencies
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P1-T6 | Add TAMBUI dependencies (core, widgets, tui, css) | PC Agent | TODO | `build.gradle.kts` |
| P1-T7 | Add Quarkus dependencies (arc, config-yaml, resteasy) | PC Agent | TODO | `build.gradle.kts` |
| P1-T8 | Add HTTP client (Mutiny Vertx WebClient) | PC Agent | TODO | `build.gradle.kts` |
| P1-T9 | Add JSON processing (Jackson Kotlin) | PC Agent | TODO | `build.gradle.kts` |
| P1-T10 | Verify build compiles and native image works | PC Agent | TODO | CI/ |

### 1.3 CI/CD Setup
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P1-T11 | Create GitHub Actions workflow | PC Agent | TODO | `.github/workflows/build.yml` |
| P1-T12 | Add native image build step | PC Agent | TODO | `.github/workflows/native.yml` |

---

## PHASE 2: UI Foundation (TAMBUI)

### 2.1 Core UI Structure
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P2-T1 | Create main App layout with header/content/footer | PC Agent | TODO | `src/main/java/com/clipro/ui/AppLayout.java` |
| P2-T2 | Implement terminal size detection and resize handling | PC Agent | TODO | `src/main/java/com/clipro/ui/TerminalManager.java` |
| P2-T3 | Create theme system with OpenClaude colors | PC Agent | TODO | `src/main/java/com/clipro/ui/theme/` |
| P2-T4 | Implement TCSS stylesheet matching OpenClaude | PC Agent | TODO | `src/main/resources/tcss/theme.tcss` |

### 2.2 Message Components
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P2-T5 | Create MessageBox component (assistant/user distinction) | PC Agent | TODO | `src/main/java/com/clipro/ui/components/MessageBox.java` |
| P2-T6 | Create MessageList with virtual scrolling | PC Agent | TODO | `src/main/java/com/clipro/ui/components/MessageList.java` |
| P2-T7 | Implement message rendering with markdown support | PC Agent | TODO | `src/main/java/com/clipro/ui/components/MarkdownRenderer.java` |
| P2-T8 | Create MessageRow with syntax highlighting | PC Agent | TODO | `src/main/java/com/clipro/ui/components/MessageRow.java` |
| P2-T9 | Implement streaming message update | PC Agent | TODO | `src/main/java/com/clipro/ui/components/StreamingMessage.java` |

### 2.3 Input Components
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P2-T10 | Create InputField with history | PC Agent | TODO | `src/main/java/com/clipro/ui/components/InputField.java` |
| P2-T11 | Implement command completion (Ctrl+Space) | PC Agent | TODO | `src/main/java/com/clipro/ui/components/CommandCompleter.java` |
| P2-T12 | Create multi-line input mode | PC Agent | TODO | `src/main/java/com/clipro/ui/components/MultiLineInput.java` |

### 2.4 Navigation & Layout
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P2-T13 | Create HeaderBar with model picker | PC Agent | TODO | `src/main/java/com/clipro/ui/components/HeaderBar.java` |
| P2-T14 | Create StatusBar with token counter | PC Agent | TODO | `src/main/java/com/clipro/ui/components/StatusBar.java` |
| P2-T15 | Implement FullscreenLayout | PC Agent | TODO | `src/main/java/com/clipro/ui/components/FullscreenLayout.java` |
| P2-T16 | Create ScrollKeybindingHandler | PC Agent | TODO | `src/main/java/com/clipro/ui/components/ScrollHandler.java` |

### 2.5 Dialogs & Overlays
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P2-T17 | Create ModalDialog base | PC Agent | TODO | `src/main/java/com/clipro/ui/dialogs/ModalDialog.java` |
| P2-T18 | Implement ConfirmationDialog | PC Agent | TODO | `src/main/java/com/clipro/ui/dialogs/ConfirmationDialog.java` |
| P2-T19 | Implement ProgressDialog | PC Agent | TODO | `src/main/java/com/clipro/ui/dialogs/ProgressDialog.java` |
| P2-T20 | Create ModelPickerDialog | PC Agent | TODO | `src/main/java/com/clipro/ui/dialogs/ModelPickerDialog.java` |
| P2-T21 | Implement ApiKeyInputDialog | PC Agent | TODO | `src/main/java/com/clipro/ui/dialogs/ApiKeyInputDialog.java` |

---

## PHASE 3: VIM MODE

### 3.1 Vim State Management
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P3-T1 | Create VimState enum (NORMAL, INSERT, VISUAL, COMMAND) | PC Agent | TODO | `src/main/java/com/clipro/ui/vim/VimState.java` |
| P3-T2 | Implement VimMode context and transitions | PC Agent | TODO | `src/main/java/com/clipro/ui/vim/VimMode.java` |
| P3-T3 | Create VimStateManager | PC Agent | TODO | `src/main/java/com/clipro/ui/vim/VimStateManager.java` |

### 3.2 Vim Keybindings
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P3-T4 | Implement motion commands (hjkl, w, b, e, 0, $, gg, G) | PC Agent | TODO | `src/main/java/com/clipro/ui/vim/MotionCommand.java` |
| P3-T5 | Implement operator commands (d, y, p, c, x) | PC Agent | TODO | `src/main/java/com/clipro/ui/vim/OperatorCommand.java` |
| P3-T6 | Implement text objects (iw, aw, i", a", etc) | PC Agent | TODO | `src/main/java/com/clipro/ui/vim/TextObject.java` |
| P3-T7 | Create VimKeyHandler integrating with InputField | PC Agent | TODO | `src/main/java/com/clipro/ui/vim/VimKeyHandler.java` |

### 3.3 Command Line Mode
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P3-T8 | Implement :command parsing | PC Agent | TODO | `src/main/java/com/clipro/ui/vim/CommandLineParser.java` |
| P3-T9 | Create VimCommandRegistry | PC Agent | TODO | `src/main/java/com/clipro/ui/vim/VimCommandRegistry.java` |
| P3-T10 | Implement :w :q :wq :q! :set commands | PC Agent | TODO | `src/main/java/com/clipro/ui/vim/BasicCommands.java` |

---

## PHASE 4: LLM BRIDGE (MB Agent)

### 4.1 HTTP Client Foundation
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P4-T1 | Create OpenAICompatibleClient base class | MB Agent | TODO | `src/main/java/com/clipro/llm/OpenAICompatibleClient.java` |
| P4-T2 | Implement ChatCompletionRequest/Response models | MB Agent | TODO | `src/main/java/com/clipro/llm/models/` |
| P4-T3 | Create streaming response handler (SSE) | MB Agent | TODO | `src/main/java/com/clipro/llm/streaming/StreamHandler.java` |
| P4-T4 | Implement retry with exponential backoff | MB Agent | TODO | `src/main/java/com/clipro/llm/RetryHandler.java` |

### 4.2 Provider Implementations
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P4-T5 | Implement OllamaProvider (local) | MB Agent | TODO | `src/main/java/com/clipro/llm/providers/OllamaProvider.java` |
| P4-T6 | Implement OpenRouterProvider (aggregator) | MB Agent | TODO | `src/main/java/com/clipro/llm/providers/OpenRouterProvider.java` |
| P4-T7 | Implement AnthropicProvider | MB Agent | TODO | `src/main/java/com/clipro/llm/providers/AnthropicProvider.java` |
| P4-T8 | Implement DeepSeekProvider | MB Agent | TODO | `src/main/java/com/clipro/llm/providers/DeepSeekProvider.java` |
| P4-T9 | Implement GoogleProvider (Gemini) | MB Agent | TODO | `src/main/java/com/clipro/llm/providers/GoogleProvider.java` |
| P4-T10 | Implement MistralProvider | MB Agent | TODO | `src/main/java/com/clipro/llm/providers/MistralProvider.java` |

### 4.3 Tool Calling
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P4-T11 | Create ToolCallRequest/Response models | MB Agent | TODO | `src/main/java/com/clipro/llm/tools/Models.java` |
| P4-T12 | Implement function calling protocol | MB Agent | TODO | `src/main/java/com/clipro/llm/tools/FunctionCalling.java` |
| P4-T13 | Create ToolResult serializer | MB Agent | TODO | `src/main/java/com/clipro/llm/tools/ToolResult.java` |

### 4.4 Provider Management
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P4-T14 | Create ProviderManager for switching | MB Agent | TODO | `src/main/java/com/clipro/llm/ProviderManager.java` |
| P4-T15 | Implement provider health checks | MB Agent | TODO | `src/main/java/com/clipro/llm/ProviderHealthCheck.java` |
| P4-T16 | Create LlmClient facade | MB Agent | TODO | `src/main/java/com/clipro/llm/LlmClient.java` |

---

## PHASE 5: MCP INTEGRATION (MB Agent)

### 5.1 TCP Client
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P5-T1 | Create TCP JSON-RPC client | MB Agent | TODO | `src/main/java/com/clipro/mcp/TCPClient.java` |
| P5-T2 | Implement connection pooling | MB Agent | TODO | `src/main/java/com/clipro/mcp/ConnectionPool.java` |
| P5-T3 | Create JSON-RPC request/response models | MB Agent | TODO | `src/main/java/com/clipro/mcp/JsonRpcModels.java` |
| P5-T4 | Implement error handling and reconnection | MB Agent | TODO | `src/main/java/com/clipro/mcp/ReconnectionHandler.java` |

### 5.2 Token Optimization
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P5-T5 | Integrate token optimizer from existing MCP | MB Agent | TODO | `src/main/java/com/clipro/mcp/TokenOptimizer.java` |
| P5-T6 | Implement context store (external storage) | MB Agent | TODO | `src/main/java/com/clipro/mcp/ContextStore.java` |
| P5-T7 | Create lazy tool loader | MB Agent | TODO | `src/main/java/com/clipro/mcp/LazyToolLoader.java` |
| P5-T8 | Implement semantic tool search | MB Agent | TODO | `src/main/java/com/clipro/mcp/SemanticSearch.java` |

### 5.3 MCP Client Facade
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P5-T9 | Create McpClient facade | MB Agent | TODO | `src/main/java/com/clipro/mcp/McpClient.java` |
| P5-T10 | Implement tool registry | MB Agent | TODO | `src/main/java/com/clipro/mcp/ToolRegistry.java` |
| P5-T11 | Create ToolExecutor | MB Agent | TODO | `src/main/java/com/clipro/mcp/ToolExecutor.java` |

---

## PHASE 6: TOOL IMPLEMENTATIONS (MB Agent)

### 6.1 File Operations
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P6-T1 | Implement FileReadTool | MB Agent | TODO | `src/main/java/com/clipro/tools/FileReadTool.java` |
| P6-T2 | Implement FileWriteTool | MB Agent | TODO | `src/main/java/com/clipro/tools/FileWriteTool.java` |
| P6-T3 | Implement FileEditTool | MB Agent | TODO | `src/main/java/com/clipro/tools/FileEditTool.java` |
| P6-T4 | Implement GlobTool (pattern matching) | MB Agent | TODO | `src/main/java/com/clipro/tools/GlobTool.java` |
| P6-T5 | Implement GrepTool | MB Agent | TODO | `src/main/java/com/clipro/tools/GrepTool.java` |

### 6.2 Shell Operations
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P6-T6 | Implement BashTool | MB Agent | TODO | `src/main/java/com/clipro/tools/BashTool.java` |
| P6-T7 | Implement PowerShellTool | MB Agent | TODO | `src/main/java/com/clipro/tools/PowerShellTool.java` |
| P6-T8 | Create shell output formatter | MB Agent | TODO | `src/main/java/com/clipro/tools/ShellFormatter.java` |

### 6.3 Git Operations
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P6-T9 | Implement GitStatusTool | MB Agent | TODO | `src/main/java/com/clipro/tools/GitStatusTool.java` |
| P6-T10 | Implement GitDiffTool | MB Agent | TODO | `src/main/java/com/clipro/tools/GitDiffTool.java` |
| P6-T11 | Implement GitCommitTool | MB Agent | TODO | `src/main/java/com/clipro/tools/GitCommitTool.java` |

### 6.4 Search & Web
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P6-T12 | Integrate web search from MCP server | MB Agent | TODO | `src/main/java/com/clipro/tools/WebSearchTool.java` |
| P6-T13 | Integrate web fetch from MCP server | MB Agent | TODO | `src/main/java/com/clipro/tools/WebFetchTool.java` |

---

## PHASE 7: CLI COMMANDS (MB Agent)

### 7.1 Core Commands
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P7-T1 | Create CommandRegistry | MB Agent | TODO | `src/main/java/com/clipro/commands/CommandRegistry.java` |
| P7-T2 | Implement /help command | MB Agent | TODO | `src/main/java/com/clipro/commands/HelpCommand.java` |
| P7-T3 | Implement /clear command | MB Agent | TODO | `src/main/java/com/clipro/commands/ClearCommand.java` |
| P7-T4 | Implement /model command | MB Agent | TODO | `src/main/java/com/clipro/commands/ModelCommand.java` |
| P7-T5 | Implement /exit command | MB Agent | TODO | `src/main/java/com/clipro/commands/ExitCommand.java` |

### 7.2 Development Commands
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P7-T6 | Implement /commit command | MB Agent | TODO | `src/main/java/com/clipro/commands/CommitCommand.java` |
| P7-T7 | Implement /review command | MB Agent | TODO | `src/main/java/com/clipro/commands/ReviewCommand.java` |
| P7-T8 | Implement /init command | MB Agent | TODO | `src/main/java/com/clipro/commands/InitCommand.java` |
| P7-T9 | Implement /diff command | MB Agent | TODO | `src/main/java/com/clipro/commands/DiffCommand.java` |
| P7-T10 | Implement /status command | MB Agent | TODO | `src/main/java/com/clipro/commands/StatusCommand.java` |

### 7.3 Configuration Commands
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P7-T11 | Implement /config command | MB Agent | TODO | `src/main/java/com/clipro/commands/ConfigCommand.java` |
| P7-T12 | Implement /set command | MB Agent | TODO | `src/main/java/com/clipro/commands/SetCommand.java` |
| P7-T13 | Implement /theme command | MB Agent | TODO | `src/main/java/com/clipro/commands/ThemeCommand.java` |

---

## PHASE 8: SESSION MANAGEMENT (MB Agent)

### 8.1 History
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P8-T1 | Create HistoryManager | MB Agent | TODO | `src/main/java/com/clipro/session/HistoryManager.java` |
| P8-T2 | Implement conversation persistence | MB Agent | TODO | `src/main/java/com/clipro/session/ConversationStore.java` |
| P8-T3 | Create history search | MB Agent | TODO | `src/main/java/com/clipro/session/HistorySearch.java` |

### 8.2 Configuration
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P8-T4 | Create ConfigManager | MB Agent | TODO | `src/main/java/com/clipro/session/ConfigManager.java` |
| P8-T5 | Implement API key management | MB Agent | TODO | `src/main/java/com/clipro/session/ApiKeyManager.java` |
| P8-T6 | Create session persistence | MB Agent | TODO | `src/main/java/com/clipro/session/SessionPersistence.java` |

### 8.3 Memory
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P8-T7 | Create MemoryStore | MB Agent | TODO | `src/main/java/com/clipro/session/MemoryStore.java` |
| P8-T8 | Implement project context tracking | MB Agent | TODO | `src/main/java/com/clipro/session/ProjectContext.java` |

---

## PHASE 9: TESTING & POLISH

### 9.1 Unit Tests
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P9-T1 | Create UI component tests | PC Agent | TODO | `src/test/java/com/clipro/ui/` |
| P9-T2 | Create LLM client tests | MB Agent | TODO | `src/test/java/com/clipro/llm/` |
| P9-T3 | Create MCP client tests | MB Agent | TODO | `src/test/java/com/clipro/mcp/` |
| P9-T4 | Create tool tests | MB Agent | TODO | `src/test/java/com/clipro/tools/` |

### 9.2 Integration Tests
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P9-T5 | Test Ollama connection | MB Agent | TODO | `src/test/java/com/clipro/integration/OllamaIntegrationTest.java` |
| P9-T6 | Test OpenRouter connection | MB Agent | TODO | `src/test/java/com/clipro/integration/OpenRouterIntegrationTest.java` |
| P9-T7 | Test MCP server connection | MB Agent | TODO | `src/test/java/com/clipro/integration/McpIntegrationTest.java` |

### 9.3 Performance
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P9-T8 | Benchmark startup time (<100ms target) | PC Agent | TODO | `src/test/java/com/clipro/performance/StartupBenchmark.java` |
| P9-T9 | Benchmark UI rendering | PC Agent | TODO | `src/test/java/com/clipro/performance/UIRenderingBenchmark.java` |
| P9-T10 | Memory profiling | PC Agent | TODO | `src/test/java/com/clipro/performance/MemoryProfile.java` |

### 9.4 Documentation
| Ticket | Task | Developer | Status | File Location |
|--------|------|-----------|--------|---------------|
| P9-T11 | Write README with setup instructions | Both | TODO | `README.md` |
| P9-T12 | Write CONTRIBUTING guide | Both | TODO | `CONTRIBUTING.md` |

---

## TASK STATUS SUMMARY

| Phase | Total | TODO | IN PROGRESS | DONE |
|-------|-------|------|-------------|------|
| Phase 1 | 12 | 12 | 0 | 0 |
| Phase 2 | 21 | 21 | 0 | 0 |
| Phase 3 | 10 | 10 | 0 | 0 |
| Phase 4 | 16 | 16 | 0 | 0 |
| Phase 5 | 11 | 11 | 0 | 0 |
| Phase 6 | 13 | 13 | 0 | 0 |
| Phase 7 | 13 | 13 | 0 | 0 |
| Phase 8 | 8 | 8 | 0 | 0 |
| Phase 9 | 12 | 12 | 0 | 0 |
| **TOTAL** | **116** | **116** | **0** | **0** |

---

## QUICK REFERENCE

### Commands
```bash
# Pull latest
git pull origin main

# Check status
git status

# Commit
git add . && git commit -m "[TICKET-X] Task description #done"

# Push
git push origin main

# View history
git log --oneline -10
```

### Build
```bash
# Compile
./gradlew build

# Native image
./gradlew nativeCompile

# Run
./gradlew run

# Test
./gradlew test
```

---

**Last Updated:** 2026-04-13
**Project:** CLIPRO - Java AI CLI
**Repository:** https://github.com/simpletoolsindia/clipro
