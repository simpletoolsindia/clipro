---
name: clipro-project-rules-2026-04-13
description: CLIPRO project rules - LOCAL-FIRST Ollama + TDD + Native Tools
type: project
---

# CLIPRO - Agent Rules

## Repository
- **Repo:** https://github.com/simpletoolsindia/clipro
- **Source:** OpenClaude (https://github.com/Gitlawb/openclaude)

## Agent Configuration
| Agent | Machine | Tasks |
|-------|---------|-------|
| **PC Agent** | This machine | UI, TAMBUI, core |
| **MB Agent** | Different machine | Ollama, tools, agent |

## Workflow (MANDATORY)

### Before Any Task
1. `git pull origin main`
2. Read TASKS.md
3. Check ticket status

### After Completing Task
1. Run tests
2. Update TASKS.md
3. `git add . && git commit -m "[TICKET-X] description #done"`
4. `git push origin main`

### Commit Author
```
Name: Sridhar Karuppusamy
Email: support@simpletools.in
```

## Core Priorities

### LOCAL-FIRST
```
1. Ollama (qwen3-coder:32b) - FIRST
2. OpenRouter - SECOND
3. Cloud API - LAST
```

### NATIVE TOOLS (14 built-in)
```
- Web Search  → SearXNG (search.sridharhomelab.in)
- File Ops    → Java NIO.2
- Bash        → ProcessBuilder
- Git         → JGit
- Grep        → Java regex
```

## TDD Rules
- Write test BEFORE implementation
- All public methods tested
- Small tasks (<100 lines)
- Reference OpenClaude source

## Key Files
| File | Purpose |
|------|---------|
| TASKS.md | Master task tracker (read this!) |
| SPEC.md | Full specification |

## Quick Commands
```bash
git pull origin main
./gradlew test
./gradlew build
./gradlew nativeCompile
```
