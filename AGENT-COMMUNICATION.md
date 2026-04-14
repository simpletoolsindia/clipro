# Agent Communication Protocol

## Problem
PC Agent and MB Agent work on different machines with no real-time communication channel.

## Solution: Git-Based Async Messaging + Shared Memory

### 1. AGENT-MESSAGES.md (Message Queue via Git)

Both agents append messages here and push to sync:

```markdown
## Inbox (messages addressed to ME)

---
**[AGENT]**: Message content
**Time**: 2026-04-14 09:00
**Priority**: normal|high|urgent
```

### 2. How It Works

```
PC Agent                    MB Agent
    |                           |
    |-- write message --------->|
    |-- git push               |
    |                           |-- git pull
    |                           |-- read message
    |<-- reply in AGENT-MESSAGES|
    |<-- git push               |
    |-- git pull                |
```

### 3. Message Priority Levels

| Priority | When to Use |
|----------|-------------|
| `urgent` | Task blocking, need response ASAP |
| `high` | Important question, day timeframe |
| `normal` | Updates, async info |

### 4. Quick Response Protocol

1. **Check for messages** at start of each work session
2. **Reply within 15 min** for urgent, 2 hours for high
3. **Mark as read** by moving to "Archive" section

---

## Best Option Found: A2A Protocol

**Anthropic's A2A (Agent-to-Agent)** - Perfect for our use case!
- MCP = agent to tools
- A2A = agent to agent

But we need:
- Simple implementation now
- Works without extra infra
- Git-based sync for simplicity

**Recommendation: Start with AGENT-MESSAGES.md (git-based)**

---

## Alternative Options (Ranked)

| Option | Pros | Cons | Implementation |
|--------|------|------|----------------|
| **Git messages (chosen)** | Simple, no infra | 1-2 min lag | AGENT-MESSAGES.md |
| rustpad.io | Real-time | Requires manual sync | External |
| MCP server | Proper protocol | Complex setup | Later |
| A2A protocol | Standard | New, complex | Future |

---

## Usage

### PC Agent sends message:
```bash
echo "
---
**PC Agent**: Your question here
**Time**: $(date '+%Y-%m-%d %H:%M')
**Priority**: high
" >> AGENT-MESSAGES.md
git add AGENT-MESSAGES.md && git commit -m "[TEAM] PC Agent message" && git push
```

### MB Agent reads and replies:
```bash
git pull
# Read AGENT-MESSAGES.md
# Reply with your response
git push
```

---

**Last Updated:** 2026-04-14