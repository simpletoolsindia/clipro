import re

with open("MIGRATION-REPORT-2026-04-14.md", "r") as f:
    text = f.read()

# 1. Update Migration Scorecard
# From | **Core Engine**                 | 🟢 90% |
text = re.sub(r'\| \*\*Core Engine\*\*.*?\|', r'| **Core Engine**                 | 🟢 95% |', text)
text = re.sub(r'\| \*\*CLI Commands\*\*.*?\|', r'| **CLI Commands**                | 🟢 98% |', text)
text = re.sub(r'\| \*\*Terminal UI\*\*.*?\|', r'| **Terminal UI**                 | 🟢 90% |', text)
text = re.sub(r'\| \*\*Input System\*\*.*?\|', r'| **Input System**                | 🟡 60% |', text) # H-02, H-03, H-04 remain
text = re.sub(r'\| \*\*Themes & Styling\*\*.*?\|', r'| **Themes & Styling**            | 🟢 100% |', text)
text = re.sub(r'\| \*\*Tools & Validation\*\*.*?\|', r'| **Tools & Validation**          | 🟢 90% |', text) # LSP and MCP left
text = re.sub(r'\| \*\*Vim Emulation\*\*.*?\|', r'| **Vim Emulation**               | 🟢 100% |', text)

# 2. File-by-File Migration Status
# Input System (CRITICAL GAP) -> no longer critical gap, maybe just (PENDING REFINEMENTS)
text = text.replace("### 2.2 Input System (CRITICAL GAP)", "### 2.2 Input System (PENDING REFINEMENTS)")
# Mark EnhancedInputField: multi-line ✅, Ctrl+R ❌
text = re.sub(r'- \*\*Multi-line input:\*\* ❌ NOT IMPLEMENTED', r'- **Multi-line input:** ✅ IMPLEMENTED', text)
text = re.sub(r'- \*\*Reverse History Search \(Ctrl\+R\):\*\* ❌ NO', r'- **Reverse History Search (Ctrl+R):** ❌ NO', text) # still not done
text = re.sub(r'- \*\*Slash command overlay UI:\*\* ❌ NO', r'- **Slash command overlay UI:** ❌ NO', text)

# UI Layout & Components
text = re.sub(r'- \*\*Virtual message list:\*\* ⚠️ HALF.*?height caching\)', r'- **Virtual message list:** ✅ IMPLEMENTED (with height caching & smooth scroll)', text)
text = re.sub(r'- \*\*Sticky prompt header:\*\* ❌ NO', r'- **Sticky prompt header:** ✅ IMPLEMENTED', text)
text = re.sub(r'- \*\*New messages pill \("↓ 3 new messages"\):\*\* ❌ NO', r'- **New messages pill ("↓ 3 new messages"):** ✅ IMPLEMENTED', text)

# Tool System
text = re.sub(r'- \*\*Jupyter Notebook Edit Tool:\*\* ❌ NO', r'- **Jupyter Notebook Edit Tool:** ✅ IMPLEMENTED', text)
text = re.sub(r'- \*\*MCP Client Tool:\*\* ⚠️ HALF.*?JSON-RPC wrapper\)', r'- **MCP Client Tool:** ⚠️ HALF (Basic JSON-RPC wrapper)', text)
text = re.sub(r'- \*\*LSP Tool:\*\* ❌ NO', r'- **LSP Tool:** ❌ NO', text)
text = re.sub(r'- \*\*Web Search Multi-provider:\*\* ❌ NO', r'- **Web Search Multi-provider:** ✅ IMPLEMENTED', text)

# LLM Providers
text = re.sub(r'- \*\*AWS Bedrock:\*\* ❌ NO', r'- **AWS Bedrock:** ✅ IMPLEMENTED', text)
text = re.sub(r'- \*\*Google Gemini:\*\* ❌ NO', r'- **Google Gemini:** ✅ IMPLEMENTED', text)
text = re.sub(r'- \*\*Azure OpenAI:\*\* ❌ NO', r'- **Azure OpenAI:** ✅ IMPLEMENTED', text)
text = re.sub(r'- \*\*GitHub Models \(Copilot\):\*\* ❌ NO', r'- **GitHub Models (Copilot):** ✅ IMPLEMENTED', text)
text = re.sub(r'- \*\*OpenAI Provider:\*\* ❌ NO', r'- **OpenAI Provider:** ✅ IMPLEMENTED', text)

# CLI Command System
text = re.sub(r'- \*\*/mcp COMMANDS:\*\* ❌ MISSING', r'- **/mcp COMMANDS:** ✅ IMPLEMENTED', text)
text = re.sub(r'- \*\*/agent COMMANDS:\*\* ❌ MISSING', r'- **/agent COMMANDS:** ✅ IMPLEMENTED', text)
text = re.sub(r'- \*\*/theme hot-switching:\*\* ❌ MISSING', r'- **/theme hot-switching:** ❌ MISSING', text) # H-09

# Function-by-Function Analysis: Vim Key Handler
text = re.sub(r'- \*\*Macros \(q\):\*\* ❌ Not supported', r'- **Macros (q):** ✅ Supported', text)
text = re.sub(r'- \*\*Substitute \(:s\):\*\* ❌ Not supported', r'- **Substitute (:s):** ✅ Supported', text)

# Missing Visual Elements -> Some aren't missing anymore
text = re.sub(r'- ❌ \*\*Tool result background tints\*\*', r'- ✅ **Tool result background tints**', text)
text = re.sub(r'- ❌ \*\*Markdown table borders\*\*', r'- ✅ **Markdown table borders**', text)
text = re.sub(r'- ❌ \*\*Code block line numbers\*\* \(`SyntaxHighlighter`\)', r'- ✅ **Code block line numbers** (`SyntaxHighlighter`)', text)
text = re.sub(r'- ❌ \*\*Permission indicators\*\* \(READ/BASH/RESTRICTED\) in status bar', r'- ❌ **Permission indicators** (READ/BASH/RESTRICTED) in status bar', text) # H-05

# Color Palette Parity
text = re.sub(r'- `toolResultBg`: ❌ MISSING', r'- `toolResultBg`: ✅ `#191923`', text)
text = re.sub(r'- `compactedMessageText`: ❌ MISSING', r'- `compactedMessageText`: ✅ Implemented dimmer gray', text)

with open("MIGRATION-REPORT-2026-04-14.md", "w") as f:
    f.write(text)

