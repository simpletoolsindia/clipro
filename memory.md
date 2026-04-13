---
name: clipro-project-rules-2026-04-13
description: CLIPRO project rules for PC Agent and MB Agent collaboration
type: project
---

# CLIPRO Project Rules

## Repository
- **Repo:** https://github.com/simpletoolsindia/clipro
- **Source:** OpenClaude (https://github.com/Gitlawb/openclaude) - 2,087 files, 544K lines

## Agent Configuration
| Agent | Machine | Tasks |
|-------|---------|-------|
| **PC Agent** | This machine | UI components, TAMBUI setup, core infrastructure |
| **MB Agent** | Different machine | Services, LLM integration, MCP, tools, commands |

## Workflow (MANDATORY)

### Before Any Task
1. Read `TASKS.md`
2. Pull: `git pull origin main`
3. Check task status

### After Completing Task
1. Run tests
2. Update TASKS.md status
3. Commit: `git add . && git commit -m "[TICKET-X] Task description #done"`
4. Push: `git push origin main`

## Commit Author
```
Name: Sridhar Karuppusamy
Email: support@simpletools.in
```

## Key Files
- `SPEC.md` - Full specification
- `TASKS.md` - Task tracker (116 tickets, 9 phases)
- `README.md` - Coming soon

## Technology Stack
- TAMBUI (React-like TUI for Java)
- Quarkus (GraalVM native support)
- OpenAI-compatible API (all LLM providers)
- MCP server (port 7710 - your existing one)
