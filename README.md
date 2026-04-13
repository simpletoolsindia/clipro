# CLIPRO - Java AI Coding CLI

**LOCAL-FIRST** AI coding assistant using Ollama and native tools.

## Features

- **14 Native Tools** - No external MCP dependencies
- **Ollama Integration** - Local LLM with OpenAI-compatible API
- **Tool Calling** - Full ReAct loop implementation
- **Session Management** - Conversation history with search
- **Configuration** - Secure API key storage

## Quick Start

```bash
# Build
./gradlew build

# Run (requires Ollama)
./gradlew run
```

## Requirements

- Java 17+
- Ollama (http://localhost:11434)

## Architecture

```
src/main/java/com/clipro/
├── agent/          # AgentEngine, TokenBudget
├── cli/           # CommandRegistry
├── llm/           # OllamaProvider, models
├── logging/       # Logger
├── session/       # HistoryManager, ConfigManager
├── tools/         # 14 native tools
│   ├── file/      # FileRead, FileWrite, FileEdit, Glob, Grep
│   ├── git/       # GitStatus, GitDiff, GitLog, GitCommit
│   ├── shell/     # Bash
│   └── web/       # WebSearch, WebFetch, QuickFetch
└── ui/            # Terminal utilities
```

## Commands

- `/help` - Show available commands
- `/clear` - Clear conversation
- `/exit` - Exit CLIPRO
- `/model` - Show current model

## Configuration

Config: `~/.clipro/config.json`
Secrets: `~/.clipro/secrets.properties`

## Testing

```bash
./gradlew test
```

## Build Status

- Phase 3: LLM Bridge ✓
- Phase 4: Native Tools ✓
- Phase 5: Agent Engine ✓
- Phase 9: Session Management ✓
- Phase 10: Testing ✓

**In Progress:**
- Phase 2: UI (TamboUI integration)
- Phase 6: GraalVM native build

## License

MIT
