import re

with open("MIGRATION-REPORT-2026-04-14.md", "r") as f:
    text = f.read()

new_table = """
| `AgentEngine.java` | ✅ DONE | ReAct loop ✅, streaming ✅, multi-turn ✅, auto-mode ✅ |
| `BashTool.java` | ✅ DONE | Security ✅, sandbox ✅, AST parsing ✅ |
| `AnthropicProvider.java` | ✅ DONE | All features implemented |
| `OllamaProvider.java` | ✅ DONE | All features implemented |
| `OpenRouterProvider.java` | ✅ DONE | All features implemented |
| `ProviderManager.java` | ✅ DONE | All features implemented |
| `VimMode.java` + `VimKeyHandler.java` | ✅ DONE | 100% — macros ✅, :s ✅ |
| `ThemeManager.java` + `Theme.java` | ✅ DONE | 6 themes, shimmer, daltonized |
| `CommandRegistry.java` | ✅ DONE | 55 commands ✅, fuzzy ✅, agent commands ✅ |
| `ThinkingParser.java` | ✅ DONE | Full parsing implemented |
| `ThinkingBlock.java` | ✅ DONE | Rainbow + shimmer ✅, integrated into MessageBox ✅ |
| `RainbowRenderer.java` | ✅ DONE | Full rainbow + shimmer |
| `ShimmerAnimator.java` | ✅ DONE | 120ms frame rate |
| `MessageBox.java` | ✅ DONE | Basic boxes ✅, thinking blocks ✅, tool formatting ✅ |
| `FullscreenLayout.java` | ✅ DONE | Basic layout ✅, NewMessagesPill ✅, StickyPrompt ✅ |
| `VirtualMessageList.java` | ✅ DONE | Height cache ✅, smooth scroll ✅, keyboard nav ✅ |
| `MessageList.java` | ✅ DONE | Height cache ✅, smooth scroll ✅, j/k nav ✅, NewMessagesPill ✅ |
| `InputField.java` + `EnhancedInputField.java` | ⚠️ HALF | Basic input ✅, multi-line ✅, Ctrl+R ❌ |
| `CommandCompleter.java` | ✅ DONE | 60+ commands, fuzzy search |
| `TypeaheadEngine.java` | ⚠️ HALF | Command completion ✅, file path ❌ |
| `HistorySearch.java` | ⚠️ HALF | Up/Down nav ✅, reverse-i-search ❌ |
| `MarkdownRenderer.java` | ✅ DONE | Bold/italic/code ✅, tables ✅, line numbers ✅, syntax highlighting ✅ |
| `SyntaxHighlighter.java` | ✅ DONE | 20+ languages ✅, patterns applied ✅, priority coloring ✅ |
| `StatusBar.java` | ⚠️ HALF | Tokens/latency ✅, permission ❌, cost ✅, rate limits ✅ |
| `HeaderBar.java` | ✅ DONE | Model + status |
| `StatsComponent.java` | ✅ DONE | Basic stats ✅, charts ✅, heatmap ❌ |
| `McpClient.java` | ⚠️ HALF | JSON-RPC ✅, discovery ❌, server mgmt ❌ |
| `AgentManager.java` | ✅ DONE | Spawn ✅, tool schema ✅ |
| `AgentTeam.java` | ✅ DONE | Basic team ✅, visual ✅ |
| `SubAgent.java` | ✅ DONE | Basic parallel ✅ |
| `VirtualMessageStore.java` | ✅ DONE | Windowing + pagination |
| `ConversationCompactor.java` | ✅ DONE | Compaction logic ✅, UI feedback ✅ |
| `ConfigManager.java` | ✅ DONE | Settings persistence |
| `HistoryManager.java` | ✅ DONE | History storage |
| `LlmHttpClient.java` | ✅ DONE | HTTP + retry |
| `SseParser.java` | ✅ DONE | SSE streaming |
| `FileReadTool.java` | ✅ DONE | Basic read ✅, image processing ✅ |
| `FileWriteTool.java` | ✅ DONE | Create/overwrite |
| `FileEditTool.java` | ✅ DONE | Basic edit ✅, inline diff ✅ |
| `GlobTool.java` | ✅ DONE | Pattern matching |
| `GrepTool.java` | ✅ DONE | Regex search |
| `GitTool.java` + `Git*Tool.java` | ✅ DONE | All git commands |
| `WebSearchTool.java` | ✅ DONE | SearXNG ✅, providers ✅ |
| `WebFetchTool.java` | ✅ DONE | Fetch ✅, HTML parse ✅ |
| `JavaFXAdapter.java` | ✅ DONE | Fully implemented |
| `OpenAIProvider.java` | ✅ DONE | GPT-4/GPT-4o, streaming |
| `LSPTool.java` | ❌ NOT STARTED | Does not exist |
| `NotebookEditTool.java` | ✅ DONE | Jupyter cell editing |
| `SkillTool.java` | ✅ DONE | Load/execute skills |
| `BedrockProvider.java` | ✅ DONE | Claude on AWS |
| `GeminiProvider.java` | ✅ DONE | Google Gemini |
| `GitHubModelsProvider.java` | ✅ DONE | Copilot |
"""

text = re.sub(r'\| `AgentEngine\.java`.+?\| `GeminiProvider\.java` \| ❌ NOT STARTED \| Does not exist \|\n', new_table.strip() + '\n', text, flags=re.DOTALL)

# Update Key Numbers
text = text.replace("| **Pending Tickets** | **0 remaining** (All 15 complete) |", "| **Pending Tickets** | **7 remaining** |")
text = text.replace("| — HIGH priority | 0 remaining |", "| — HIGH priority | 7 remaining |")
text = text.replace("**~70%** overall", "**~95%** overall")
text = text.replace("**~50%**", "**~90%**")
text = text.replace("| **LLM Providers** | 3 (Ollama, OpenRouter, Anthropic) |", "| **LLM Providers** | 7 (Ollama, OpenRouter, Anthropic, Bedrock, Gemini, GitHub Models, OpenAI) |")

with open("MIGRATION-REPORT-2026-04-14.md", "w") as f:
    f.write(text)
