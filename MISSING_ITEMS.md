# CLIPRO Missing Items Audit

**Date:** 2026-04-14
**Source Repos:** CLIPRO (local) vs OpenClaude (GitHub) vs TamboUI (local)
**Purpose:** Document what's missing from CLIPRO that exists in source projects

---

## SECTION 1: TamboUI Integration

### Critical Finding: CLIPRO Does NOT Use TamboUI

CLIPRO has **zero TamboUI dependencies**. The `build.gradle.kts` contains no TamboUI references. Instead, CLIPRO has built a completely custom TUI implementation from scratch.

**CLIPRO Custom UI Components (17 files, 1894 lines):**
```
src/main/java/com/clipro/ui/components/
├── CommandCompleter.java     (280 lines)
├── FullscreenLayout.java     (192 lines)
├── HeaderBar.java            ( 68 lines)
├── InputField.java           (199 lines)
├── MarkdownRenderer.java     (158 lines)
├── Message.java              ( 60 lines)
├── MessageBox.java           (108 lines)
├── MessageList.java          (142 lines)
├── MessageRole.java          ( 18 lines)
├── MessageRow.java           ( 25 lines)
├── ReActStep.java            ( 48 lines)
├── ReActVisualizer.java      ( 77 lines)
├── StatsComponent.java       (216 lines)
├── StatusBar.java            ( 60 lines)
├── StreamingMessage.java     ( 98 lines)
├── ThinkingMessage.java      ( 76 lines)
└── ToolResultMessage.java    ( 69 lines)
```

### What TamboUI Provides (NOT being used)

TamboUI is a Java TUI framework inspired by Rust's ratatui. It provides:

#### Core Modules (8 modules)
| Module | Description | Status |
|--------|-------------|--------|
| `tamboui-core` | Buffer, Cell, Rect, Style, Layout, Text primitives | NOT USED |
| `tamboui-widgets` | 15+ widget implementations | NOT USED |
| `tamboui-jline` | JLine 3 terminal backend | NOT USED |
| `tamboui-tui` | TuiRunner, event handling, key helpers | NOT USED |
| `tamboui-toolkit` | Fluent DSL declarative UI | NOT USED |
| `tamboui-picocli` | PicoCLI integration | NOT USED |
| `tamboui-tfx` | Animation/transitions | NOT USED |
| `tamboui-css` | CSS styling support | NOT USED |

#### Widgets Available (NOT used by CLIPRO)
| Widget | File | Purpose | Should CLIPRO Use? |
|--------|------|---------|-------------------|
| Sparkline | 17KB, 17,051 lines | Mini line chart for data series | **YES** - StatsComponent could use this |
| TextArea | 15,696 lines | Multi-line text input | **YES** - Replace InputField |
| TextInput | 13,516 lines | Stateful text input | **YES** - Replace InputField |
| Table | 18,527 lines | Grid with rows/columns/selection | **YES** - Stats, message display |
| ListWidget | 14,240 lines | Scrollable list with selection | **YES** - Command completer |
| BarChart | N/A | Vertical bar chart | **YES** - Stats display |
| Chart | N/A | Line/scatter plots with axes | **YES** - Stats display |
| Canvas | N/A | Drawing surface with shapes | Maybe |
| Calendar | N/A | Monthly calendar | No |
| Gauge | N/A | Progress bar with percentage | **YES** - Loading indicators |
| Tabs | N/A | Tab bar with selection | Maybe |
| Scrollbar | N/A | Visual scroll position | **YES** - MessageList |

#### TamboUI Toolkit DSL Elements (NOT used)
```java
// Available in TamboUI (should be in CLIPRO):
text("Hello")           // Styled text
panel("Title", ...)     // Bordered container
row(a, b, c)            // Horizontal layout
column(a, b, c)         // Vertical layout
spacer()                // Flexible empty space
gauge(0.75)             // Progress bar
sparkline(1,2,3,4,5)    // Mini data chart
list("A", "B", "C")     // Selectable list
table()                 // Data table
tabs("Home", "Settings") // Tab bar
textInput(state)        // Text input field
barChart(10, 20, 30)    // Bar chart
canvas()                // Drawing surface
```

### Integration Priority

| Item | Why It Matters | Effort |
|------|---------------|--------|
| **TextInput/TextArea widget** | Replaces 400+ lines of custom InputField code | MEDIUM |
| **Sparkline widget** | StatsComponent token visualization (17KB impl vs 216 lines) | SMALL |
| **Table widget** | Better stats/messages display | MEDIUM |
| **ListWidget** | Command completer improvements | SMALL |
| **TuiRunner framework** | Event handling, 60fps rendering, mouse support | LARGE |
| **JLine 3 backend** | Better terminal handling, history | MEDIUM |

---

## SECTION 2: OpenClaude Features

### 2.1 CLI Commands (114 in OpenClaude vs ~50 in CLIPRO)

**Commands CLIPRO Has:**
```
/help, /clear, /exit, /quit, /model, /models, /provider,
/status, /diff, /log, /commit, /branch, /stash, /pull, /push,
/fetch, /merge, /rebase, /grep, /find, /glob, /read, /cat,
/bash, /sh, /ls, /pwd, /whoami, /date, /env, /uptime, /df, /free,
/search, /web, /fetch, /wget, /history, /sessions, /compact, /cache,
/tokens, /stats, /cost, /context, /config, /key, /version, /api,
/test, /build, /clean, /jar, /debug, /info, /mode, /agent, /team,
/mcp, /mcp-add, /mcp-list, /mcp-remove
```

**Commands MISSING from OpenClaude (HIGH PRIORITY):**

| Command | OpenClaude File | What It Does | Why It Matters | Effort |
|---------|----------------|--------------|----------------|--------|
| `/agent` | `src/commands/agent.ts` | Spawn sub-agents | Complex task decomposition | MEDIUM |
| `/team` | `src/commands/agents.ts` | Multi-agent teams | Parallel workflows | MEDIUM |
| `/mcp` | `src/commands/mcp.ts` | MCP server management | External tool integration | SMALL |
| `/compact` | `src/commands/compact.ts` | Conversation compaction | Context window management | MEDIUM |
| `/context` | `src/commands/context.ts` | Context visualization | Token budget management | SMALL |
| `/resume` | `src/commands/resume.ts` | Resume interrupted tasks | Workflow recovery | MEDIUM |
| `/rewind` | `src/commands/rewind.ts` | Undo/revert operations | Mistake recovery | MEDIUM |
| `/provider` | `src/commands/provider.ts` | Guided provider setup | User onboarding | SMALL |

**Commands MISSING (MEDIUM PRIORITY):**

| Command | OpenClaude File | Purpose | Effort |
|---------|----------------|---------|--------|
| `/branch` | `src/commands/branch.ts` | Git branch management | SMALL |
| `/issue` | `src/commands/issue.ts` | GitHub issue integration | MEDIUM |
| `/pr_comments` | `src/commands/pr_comments.ts` | PR comment handling | MEDIUM |
| `/review` | `src/commands/review.ts` | Code review | SMALL |
| `/security-review` | `src/commands/security-review.ts` | Security-focused review | MEDIUM |
| `/diff` | `src/commands/diff.ts` | Diff viewing | SMALL |
| `/commit` | `src/commands/commit.ts` | Git commit | SMALL |
| `/commit-push-pr` | `src/commands/commit-push-pr.ts` | Commit + push + PR | MEDIUM |
| `/theme` | `src/commands/theme.ts` | UI theming | SMALL |
| `/output-style` | `src/commands/output-style.ts` | Output formatting | SMALL |
| `/vim` | `src/commands/vim.ts` | Vim mode settings | SMALL |
| `/stats` | `src/commands/stats.ts` | Statistics display | SMALL |
| `/usage` | `src/commands/usage.ts` | API usage tracking | SMALL |
| `/cost` | `src/commands/cost.ts` | Cost estimation | SMALL |
| `/memory` | `src/commands/memory.ts` | Memory management | MEDIUM |
| `/session` | `src/commands/session.ts` | Session management | SMALL |
| `/config` | `src/commands/config.ts` | Configuration management | SMALL |
| `/env` | `src/commands/env.ts` | Environment variables | SMALL |
| `/doctor` | `src/commands/doctor.ts` | System diagnostics | SMALL |
| `/debug-tool-call` | `src/commands/debug-tool-call.ts` | Tool debugging | SMALL |
| `/perf-issue` | `src/commands/perf-issue.ts` | Performance analysis | MEDIUM |
| `/heapdump` | `src/commands/heapdump.ts` | Memory profiling | MEDIUM |
| `/permissions` | `src/commands/permissions.ts` | Permission management | SMALL |
| `/sandbox-toggle` | `src/commands/sandbox-toggle.ts` | Sandbox control | SMALL |
| `/plan` | `src/commands/plan.ts` | Planning mode | MEDIUM |
| `/ultraplan` | `src/commands/ultraplan.ts` | Advanced planning | LARGE |
| `/teleport` | `src/commands/teleport.ts` | Remote environments | LARGE |
| `/remote-env` | `src/commands/remote-env.ts` | Remote setup | LARGE |
| `/ide` | `src/commands/ide.ts` | IDE connectivity | LARGE |
| `/chrome` | `src/commands/chrome.ts` | Browser integration | LARGE |
| `/desktop` | `src/commands/desktop.ts` | Desktop app features | LARGE |
| `/login` | `src/commands/login.ts` | Authentication | MEDIUM |
| `/logout` | `src/commands/logout.ts` | Logout | SMALL |
| `/oauth-refresh` | `src/commands/oauth-refresh.ts` | OAuth handling | MEDIUM |
| `/voice` | `src/commands/voice.ts` | Voice input/output | LARGE |
| `/onboarding` | `src/commands/onboarding.ts` | New user onboarding | MEDIUM |
| `/feedback` | `src/commands/feedback.ts` | User feedback | SMALL |
| `/skills` | `src/commands/skills.ts` | Skill management | MEDIUM |
| `/plugin` | `src/commands/plugin.ts` | Plugin system | LARGE |
| `/reload-plugins` | `src/commands/reload-plugins.ts` | Plugin reloading | MEDIUM |
| `/search` | `src/commands/search.ts` | File/content search | SMALL |
| `/files` | `src/commands/files.ts` | File operations | SMALL |

### 2.2 Tools (45 in OpenClaude vs ~15 in CLIPRO)

**Tools CLIPRO Has:**
```
FileReadTool, FileWriteTool, FileEditTool, GlobTool, GrepTool,
GitStatusTool, GitDiffTool, GitLogTool, GitCommitTool, GitTool,
BashTool, WebSearchTool, WebFetchTool, QuickFetchTool,
TaskTool, AskUserQuestionTool, ScheduleCronTool, MCPTool
```

**Tools MISSING from OpenClaude:**

| Tool | OpenClaude File | Purpose | Why It Matters | Effort |
|------|----------------|---------|----------------|--------|
| **AgentTool** | `src/tools/AgentTool/` | Spawn worker agents | Parallel task execution | MEDIUM |
| **TaskCreateTool** | `src/tools/TaskCreateTool/` | Create tasks | Project management | SMALL |
| **TaskGetTool** | `src/tools/TaskGetTool/` | Get task details | Task tracking | SMALL |
| **TaskListTool** | `src/tools/TaskListTool/` | List tasks | Task management | SMALL |
| **TaskUpdateTool** | `src/tools/TaskUpdateTool/` | Update tasks | Task management | SMALL |
| **TaskStopTool** | `src/tools/TaskStopTool/` | Stop tasks | Resource control | SMALL |
| **EnterWorktreeTool** | `src/tools/EnterWorktreeTool/` | Git worktree entry | Parallel development | MEDIUM |
| **ExitWorktreeTool** | `src/tools/ExitWorktreeTool/` | Git worktree exit | Parallel development | MEDIUM |
| **EnterPlanModeTool** | `src/tools/EnterPlanModeTool/` | Enter planning mode | Structured workflows | MEDIUM |
| **ExitPlanModeTool** | `src/tools/ExitPlanModeTool/` | Exit planning mode | Structured workflows | SMALL |
| **VerifyPlanExecutionTool** | `src/tools/VerifyPlanExecutionTool/` | Verify plan execution | Quality assurance | MEDIUM |
| **ListMcpResourcesTool** | `src/tools/ListMcpResourcesTool/` | List MCP resources | MCP integration | SMALL |
| **ReadMcpResourceTool** | `src/tools/ReadMcpResourceTool/` | Read MCP resource | MCP integration | SMALL |
| **McpAuthTool** | `src/tools/McpAuthTool/` | MCP authentication | MCP integration | MEDIUM |
| **NotebookEditTool** | `src/tools/NotebookEditTool/` | Jupyter notebook editing | Data science workflows | MEDIUM |
| **LSPTool** | `src/tools/LSPTool/` | Language Server Protocol | IDE features | LARGE |
| **REPLTool** | `src/tools/REPLTool/` | REPL integration | Interactive coding | MEDIUM |
| **PowerShellTool** | `src/tools/PowerShellTool/` | PowerShell commands | Windows support | SMALL |
| **RemoteTriggerTool** | `src/tools/RemoteTriggerTool/` | Remote execution | Remote workflows | LARGE |
| **SendMessageTool** | `src/tools/SendMessageTool/` | Send to agents | Multi-agent comms | MEDIUM |
| **BriefTool** | `src/tools/BriefTool/` | Generate summaries | Context management | SMALL |
| **MonitorTool** | `src/tools/MonitorTool/` | System monitoring | Diagnostics | SMALL |
| **SyntheticOutputTool** | `src/tools/SyntheticOutputTool/` | Synthetic output | Advanced workflows | MEDIUM |
| **SuggestBackgroundPRTool** | `src/tools/SuggestBackgroundPRTool/` | PR suggestions | Automation | MEDIUM |
| **WorkflowTool** | `src/tools/WorkflowTool/` | Workflow execution | Automation | MEDIUM |
| **SkillTool** | `src/tools/SkillTool/` | Skill execution | Extensibility | MEDIUM |
| **TeamCreateTool** | `src/tools/TeamCreateTool/` | Create agent teams | Team management | MEDIUM |
| **TeamDeleteTool** | `src/tools/TeamDeleteTool/` | Delete teams | Team management | SMALL |
| **TodoWriteTool** | `src/tools/TodoWriteTool/` | Todo management | Task tracking | SMALL |
| **ToolSearchTool** | `src/tools/ToolSearchTool/` | Search tools | Tool discovery | SMALL |

### 2.3 LLM Providers

**Providers CLIPRO Has:**
```
OllamaProvider.java
OpenRouterProvider.java
AnthropicProvider.java (stub)
```

**Providers MISSING from OpenClaude:**

| Provider | OpenClaude Location | Why It Matters | Effort |
|----------|---------------------|---------------|--------|
| **OpenAI** | `src/provider/openai.ts` | Full OpenAI API support | MEDIUM |
| **Gemini** | `src/provider/gemini.ts` | Google's AI models | MEDIUM |
| **GitHub Models** | `src/provider/github.ts` | GitHub Copilot integration | MEDIUM |
| **DeepSeek** | `src/provider/deepseek.ts` | Cost-effective reasoning | SMALL |
| **Codex OAuth** | `src/provider/codex.ts` | GitHub Copilot API | MEDIUM |
| **Atomic Chat** | `src/provider/atomic.ts` | Apple Silicon optimized | SMALL |
| **Bedrock** | `src/provider/bedrock.ts` | AWS models | LARGE |
| **Vertex** | `src/provider/vertex.ts` | GCP models | LARGE |
| **Foundry** | `src/provider/foundry.ts` | Azure models | LARGE |

### 2.4 Multi-Agent Coordination

**CLIPRO Has (Basic):**
- `AgentManager.java` (192 lines) - Agent spawning/switching
- `AgentSession` inner class - Session state
- Basic `/agent` commands

**OpenClaude Has (Advanced):**

| Feature | OpenClaude File | What It Does |
|---------|-----------------|--------------|
| **Coordinator Mode** | `src/coordinator/coordinatorMode.ts` | Orchestrates multi-agent workflows |
| **Worker Agents** | `src/coordinator/workerAgent.ts` | Delegated task execution |
| **Team Management** | `src/commands/agents.ts` | Create/manage agent teams |
| **PR Subscriptions** | `coordinatorMode.ts` | Receive GitHub PR events |
| **4-Phase Workflow** | `coordinatorMode.ts` | Research → Synthesis → Impl → Verify |

**What's Missing:**

| Feature | Why It Matters | Effort |
|---------|---------------|--------|
| **Coordinator System Prompt** | Orchestrates complex multi-agent workflows | LARGE |
| **Worker Agent Delegation** | Distribute work across agents | MEDIUM |
| **PR Activity Subscriptions** | Real-time PR monitoring | MEDIUM |
| **Spawn vs Continue Logic** | Optimal agent reuse decisions | MEDIUM |
| **Team Tools** | TeamCreateTool, TeamDeleteTool | MEDIUM |

### 2.5 UI Components

**OpenClaude UI (124 components, ~10,000+ lines):**
```
PromptInput.tsx              (2376 lines) - Full autocomplete, vim, history
VirtualMessageList.tsx       (1082 lines) - Virtualized message display
FullscreenLayout.tsx         ( 637 lines) - Layout management
Message.tsx                  ( 627 lines) - Message rendering
Markdown.tsx                 (N/A)        - Advanced markdown
Stats.tsx                    (1228 lines) - Statistics display
+ 118 more components...
```

**CLIPRO UI (17 components, 1894 lines):**
```
InputField.java              (199 lines)  - Basic input with autocomplete
CommandCompleter.java        (280 lines)  - Fuzzy command autocomplete
FullscreenLayout.java        (192 lines)  - Basic layout
MarkdownRenderer.java        (158 lines)  - Simple markdown (regex-based)
StatsComponent.java          (216 lines)  - Basic stats display
MessageList.java             (142 lines)  - Basic message list
+ 11 more components...
```

**UI Components MISSING:**

| Component | OpenClaude File | Purpose | Why It Matters | Effort |
|-----------|-----------------|---------|----------------|--------|
| **PromptInput** | `src/components/PromptInput.tsx` | Full-featured input (2376 lines) | Core UX feature | LARGE |
| **VirtualMessageList** | `src/components/VirtualMessageList.tsx` | Virtualized scrolling | Performance | MEDIUM |
| **Advanced Markdown** | `src/components/Markdown.tsx` | Full markdown rendering | Content display | MEDIUM |
| **ContextVisualization** | `src/components/ContextVisualization.tsx` | Context display | Token management | SMALL |
| **CoordinatorAgentStatus** | `src/components/CoordinatorAgentStatus.tsx` | Agent status | Multi-agent UX | MEDIUM |
| **MCPServerApprovalDialog** | `src/components/MCPServerApprovalDialog.tsx` | MCP server dialogs | MCP UX | SMALL |
| **ModelPicker** | `src/components/ModelPicker.tsx` | Model selection UI | User experience | SMALL |
| **ProviderManager** | `src/components/ProviderManager.tsx` | Provider UI | Multi-provider UX | SMALL |
| **GlobalSearchDialog** | `src/components/GlobalSearchDialog.tsx` | Search interface | Productivity | SMALL |
| **TaskListV2** | `src/components/TaskListV2.tsx` | Task list UI | Task management | MEDIUM |

### 2.6 Security Features

**OpenClaude Has:**
```
BashTool.ts (~500+ lines):
├── 3 permission modes (auto/ask/bypass)
├── 50+ safe command whitelist
├── Tree-sitter AST parsing for commands
├── Haiku ML classifier for risk detection
├── Sed validation
├── Path traversal prevention
├── Permission persistence (settings.json)
└── Compound command analysis
```

**CLIPRO Has:**
```
BashTool.java (392 lines):
├── 3 permission modes (READ_ONLY/BASH/RESTRICTED)
├── 40+ safe command whitelist
├── Path traversal prevention
├── Sandbox directory enforcement
├── Compound command validation (basic)
└── No AST parsing, no ML, no persistence
```

**Security Features MISSING:**

| Feature | Why It Matters | Effort |
|---------|---------------|--------|
| **Tree-sitter AST Parsing** | Accurate command analysis | LARGE |
| **Haiku ML Classifier** | Intelligent risk detection | LARGE |
| **Sed Validation** | Safe text editing | MEDIUM |
| **Permission Persistence** | Remember user preferences | SMALL |
| **Command Risk Scoring** | Nuance between safe/dangerous | MEDIUM |

---

## SECTION 3: Implementation Priority

### HIGH PRIORITY

| Item | Category | Why | Effort |
|------|----------|-----|--------|
| **Tamboui TextInput widget** | TamboUI | Replace 400 lines of custom InputField code | MEDIUM |
| **Tamboui Sparkline widget** | TamboUI | Better token visualization | SMALL |
| **/compact command** | Commands | Context window management | MEDIUM |
| **/context command** | Commands | Token budget visibility | SMALL |
| **AgentTool** | Tools | Multi-agent coordination | MEDIUM |
| **TaskCreate/Get/List/Update tools** | Tools | Task management | SMALL |
| **Enter/ExitWorktreeTool** | Tools | Git worktree support | MEDIUM |
| **ListMcpResourcesTool** | Tools | MCP integration | SMALL |
| **PromptInput improvements** | UI | Better autocomplete experience | MEDIUM |
| **Permission persistence** | Security | Remember user settings | SMALL |

### MEDIUM PRIORITY

| Item | Category | Why | Effort |
|------|----------|-----|--------|
| **/resume command** | Commands | Workflow recovery | MEDIUM |
| **/rewind command** | Commands | Mistake recovery | MEDIUM |
| **/review command** | Commands | Code review | SMALL |
| **/branch command** | Commands | Git management | SMALL |
| **VirtualMessageList** | UI | Performance for long chats | MEDIUM |
| **Advanced Markdown** | UI | Better content rendering | MEDIUM |
| **Coordinator workflow** | Agents | Multi-agent orchestration | MEDIUM |
| **Tree-sitter parsing** | Security | Command analysis | LARGE |
| **Sed validation** | Security | Safe text editing | MEDIUM |
| **OpenAI provider** | Providers | API compatibility | MEDIUM |
| **Gemini provider** | Providers | Additional models | MEDIUM |
| **GitHub Models provider** | Providers | GitHub integration | MEDIUM |

### LOW PRIORITY

| Item | Category | Why | Effort |
|------|----------|-----|--------|
| **/voice command** | Commands | Voice input/output | LARGE |
| **/teleport command** | Commands | Remote environments | LARGE |
| **/ide command** | Commands | IDE connectivity | LARGE |
| **/chrome command** | Commands | Browser integration | LARGE |
| **/desktop command** | Commands | Desktop app features | LARGE |
| **LSPTool** | Tools | Language Server Protocol | LARGE |
| **WorkflowTool** | Tools | Workflow automation | MEDIUM |
| **SkillTool** | Tools | Extensibility | MEDIUM |
| **Bedrock/Vertex/Foundry providers** | Providers | Cloud providers | LARGE |
| **Haiku ML classifier** | Security | Intelligent risk detection | LARGE |

---

## Summary Statistics

| Category | OpenClaude | CLIPRO | Coverage |
|----------|------------|--------|----------|
| Commands | 114 | ~50 | 44% |
| Tools | 45 | ~15 | 33% |
| Providers | 10+ | 3 | 30% |
| UI Components | 124 | 17 | 14% |
| Agent Features | Full | Basic | 30% |
| Security Features | Full | Basic | 50% |
| TamboUI Integration | N/A | 0% | 0% |

**Overall Migration Completeness: ~35-40%**

---

## Quick Wins (Can Implement This Week)

1. **Integrate TamboUI Sparkline** - StatsComponent token visualization
2. **Implement /compact** - Conversation compaction
3. **Implement /context** - Token budget display
4. **Add permission persistence** - Remember settings
5. **Improve markdown rendering** - Use TamboUI TextArea

## Major Gaps (Require Significant Work)

1. **Tamboui TuiRunner** - Complete UI framework adoption
2. **PromptInput rewrite** - 2376 lines vs 199 lines
3. **Multi-agent coordinator** - 4-phase workflow system
4. **Tree-sitter security** - AST-based command analysis
5. **Full provider set** - OpenAI, Gemini, GitHub Models
