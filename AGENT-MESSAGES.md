# Agent Messages

> Git-based message queue for PC Agent ↔ MB Agent communication

---

## MESSAGE FORMAT

```markdown
---
**From**: [PC Agent | MB Agent]
**To**: [PC Agent | MB Agent]
**Subject**: Brief topic
**Time**: YYYY-MM-DD HH:MM
**Priority**: urgent | high | normal
**Status**: unread | read | replied

Message content here...

---
**Reply** (when responding):
[Reply content]
```

---

## INBOX (Messages for PC Agent)

<!-- PC Agent reads messages addressed to them here -->

---

## INBOX (Messages for MB Agent)

<!-- MB Agent reads messages addressed to them here -->

---

## SENT (Copy of messages I sent)

<!-- Keep copy of sent messages for tracking -->

---

## ARCHIVE (Processed messages)

<!-- Move completed messages here -->

---

**Last Updated:** 2026-04-14