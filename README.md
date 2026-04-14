# CLIPRO - Java AI Coding CLI

**LOCAL-FIRST** AI coding assistant using Ollama and native tools.
Built with TDD (Test-Driven Development) workflow.

## Features

- **Native Tools** - No external MCP dependencies
  - Web Search (SearXNG), Web Fetch, File Operations, Git, Bash, Grep
  - MCP Tool, Task Tool, AskUserQuestion Tool, ScheduleCron Tool
- **Multi-Provider LLM** - Ollama, OpenRouter, Anthropic
  - Supports qwen3-coder:32b, qwen3.6-plus, Claude models
- **Tool Calling** - Full ReAct loop implementation
- **Vim Mode** - Modal editing (NORMAL, INSERT, VISUAL, COMMAND)
- **Session Management** - Conversation history with compaction
- **Agent System** - Multi-agent support with /agent commands
- **Configuration** - Secure API key storage
- **TDD Workflow** - 40+ test files, all passing

## Quick Start

```bash
# Build
./gradlew build

# Run (requires Ollama)
java -jar build/libs/clipro-0.1.0-uber.jar

# Or use Gradle
./gradlew run
```

## Requirements

- Java 17+
- Ollama (http://localhost:11434)
  ```bash
  # Install Ollama
  curl -fsSL https://ollama.com/install.sh | sh

  # Pull a model
  ollama pull qwen3-coder:32b
  ```

## Architecture

```
src/main/java/com/clipro/
├── agent/          # AgentEngine, AgentManager, TokenBudget
├── cli/           # CommandRegistry (55+ commands)
├── llm/           # OllamaProvider, AnthropicProvider, SSE parser
│   └── providers/ # LlmProvider, ProviderManager
├── logging/       # Logger with levels
├── session/      # HistoryManager, ConfigManager, VirtualMessageStore
├── tools/         # 10+ native tools
│   ├── file/     # FileRead, FileWrite, FileEdit, Glob, Grep
│   ├── git/       # GitStatus, GitDiff, GitLog, GitCommit
│   ├── shell/     # BashTool with security sandbox
│   └── web/       # WebSearch, WebFetch, QuickFetch
├── ui/            # Terminal, Input, Messages, Vim mode
│   ├── components/ # HeaderBar, MessageBox, StatsComponent, CommandCompleter
│   └── vim/       # VimState, VimMode, VimKeyHandler
└── tools/registry/ # ToolRegistry, ToolExecutor
```

## Commands (55+)

| Command | Description |
|---------|-------------|
| `/help` | Show available commands |
| `/clear` | Clear conversation |
| `/exit` | Exit CLIPRO |
| `/model [name]` | Show or set model |
| `/models` | List available models |
| `/commit` | Git commit (with AI message) |
| `/diff` | Show git diff |
| `/status` | Show git status |
| `/agent` | Multi-agent management |
| `/stats` | Session statistics |
| `/compact` | Compact conversation |

## Configuration

- Config: `~/.clipro/config.json`
- Secrets: `~/.clipro/secrets.properties`
- Session history: `~/.clipro/history/`

## Testing

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "com.clipro.llm.OllamaProviderTest"

# Build JAR
./gradlew uberJar
```

## Project Status

| Phase | Status |
|-------|--------|
| P1: Project Foundation | ✅ Complete |
| P2: UI Foundation | ✅ Complete |
| P3: Vim Mode | ✅ Complete |
| P4: Ollama Bridge | ✅ Complete |
| P5: Native Tools | ✅ Complete |
| P6: Tool Registry | ✅ Complete |
| P7: Agent Engine | ✅ Complete |
| P8: CLI Commands | ✅ Complete |
| P9: Session | ✅ Complete |
| P10.1: Integration Tests | ✅ Complete |
| P10.2: Performance Tests | ✅ Complete |
| P11: Migration Audit Fixes | ✅ Complete |

**Total: 45 test files, 100+ source files, all tests passing**

**Note:** P10.2 tests measure startup, memory, UI render, and IO performance. Run with `CLIPRO_PERF_TEST=true` for full measurements.

## License

MIT - Sridhar Karuppusamy (SimpleTools India)
