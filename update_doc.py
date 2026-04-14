import re

with open("MIGRATION-REPORT-2026-04-14.md", "r") as f:
    content = f.read()

# 1. Update C-03 and C-04 table rows to DONE
content = re.sub(r'\| C-03 \| .*? \| ⚠️ HALF \|', r'| C-03 | NewMessagesPill — "↓ N new messages" overlay | `FullscreenLayout.java` | ✅ DONE (commit silent) |', content)
content = re.sub(r'\| C-04 \| .*? \| ⚠️ HALF \|', r'| C-04 | StickyPromptHeader — context row when scrolled | `FullscreenLayout.java` | ✅ DONE (commit silent) |', content)

# 2. Update H-01 and H-08 to DONE
content = re.sub(r'\| H-01 \| .*? \| ⚠️ HALF \|', r'| H-01 | PromptInput multi-line: Tab indent | `EnhancedInputField.java` | ✅ DONE (commit 17d72f8) |', content)
content = re.sub(r'\| H-08 \| .*? \| ❌ NOT STARTED \|', r'| H-08 | OpenAI Provider | `OpenAIProvider.java` | ✅ DONE (commit 456386e) |', content)

# 3. Update Summary blocks
content = re.sub(r'\| — CRITICAL tickets \| 2 remaining \|', r'| — CRITICAL tickets | 0 remaining |', content)
content = re.sub(r'\| — HIGH priority \| 9 \|', r'| — HIGH priority | 7 remaining |', content)

# 4. Remove Part 5 completely (from Part 5 to Part 6)
content = re.sub(r'## Part 5: Pending Work — File-by-File Task List.*?## Part 6: Pending Tickets', r'## Part 6: Pending Tickets', content, flags=re.DOTALL)

# 5. Remove Ticket Detail: CRITICAL (it goes until Ticket Detail: HIGH)
content = re.sub(r'### Ticket Detail: CRITICAL.*?### Ticket Detail: HIGH', r'### Ticket Detail: HIGH', content, flags=re.DOTALL)

# 6. Remove Ticket Detail: MEDIUM and LOW (they go from MEDIUM until Part 8)
content = re.sub(r'### Ticket Detail: MEDIUM.*?## Part 8:', r'## Part 8:', content, flags=re.DOTALL)

with open("MIGRATION-REPORT-2026-04-14.md", "w") as f:
    f.write(content)

