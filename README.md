# CLIPRO - Java AI Coding CLI

**LOCAL-FIRST** AI coding assistant using Ollama and native tools.
Built with TDD (Test-Driven Development) workflow.

## Features

- **14 Native Tools** - No external MCP dependencies
  - Web Search (SearXNG), Web Fetch, File Operations, Git, Bash, Grep
- **Ollama Integration** - Local LLM with OpenAI-compatible API
  - Supports qwen3-coder:32b, qwen2.5-coder:14b, llama3.3:70b
- **Tool Calling** - Full ReAct loop implementation
- **Vim Mode** - Modal editing (NORMAL, INSERT, VISUAL, COMMAND)
- **Session Management** - Conversation history with search
- **Configuration** - Secure API key storage
- **TDD Workflow** - 34 test files, all passing

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
├── agent/          # AgentEngine, TokenBudget (ReAct loop)
├── cli/           # CommandRegistry (/help, /clear, etc.)
├── llm/           # OllamaProvider, models, SSE parser
│   └── providers/ # OllamaProvider, OpenRouterProvider
├── logging/       # Logger with levels
├── session/       # HistoryManager, ConfigManager
├── tools/         # 14 native tools
│   ├── file/      # FileRead, FileWrite, FileEdit, Glob, Grep
│   ├── git/       # GitStatus, GitDiff, GitLog, GitCommit
│   ├── shell/     # BashTool
│   └── web/       # WebSearch, WebFetch, QuickFetch
├── ui/            # Terminal, Input, Messages, Vim mode
│   ├── components/ # HeaderBar, MessageBox, StatusBar, FullscreenLayout
│   └── vim/       # VimState, VimMode, VimKeyHandler
└── tools/registry/ # ToolRegistry, ToolExecutor
```

## Commands

| Command | Description |
|---------|-------------|
| `/help` | Show available commands |
| `/clear` | Clear conversation |
| `/exit` | Exit CLIPRO |
| `/model [name]` | Show or set model |
| `/models` | List available models |
| `/commit` | Git commit (with AI message) |
| `/review` | Code review |
| `/diff` | Show git diff |
| `/status` | Show git status |

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
| P10.2: Performance (GraalVM) | ⏳ Pending |
| P10.3: Documentation | 🔄 In Progress |

**Total: 34 test files, 85 source files, all tests passing**

## License

MIT - Sridhar Karuppusamy (SimpleTools India)
