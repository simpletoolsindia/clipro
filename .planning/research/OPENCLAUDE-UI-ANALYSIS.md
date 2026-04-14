# OpenClaude UI Architecture Analysis

**Purpose:** Understand the UI layout structure of Gitlawb/openclaude for replication in Java with TamboUI
**Source:** https://github.com/Gitlawb/openclaude
**Date:** 2026-04-14

## Executive Summary

OpenClaude uses a React-based TUI architecture built on **Ink** (React for CLI) with a **fullscreen layout** pattern. The UI is split into:
1. **Scrollable message area** (top ~70%)
2. **Fixed bottom area** (prompt, spinner, permissions)

This is the core pattern to replicate in Java/TamboUI.

---

## Core Layout Architecture

### FullscreenLayout Component
**File:** `src/components/FullscreenLayout.tsx`

```
┌─────────────────────────────────────────────┐
│  StickyPromptHeader (optional, 1 row)       │  ← Shows when scrolled up
├─────────────────────────────────────────────┤
│                                             │
│           ScrollBox (flexGrow=1)            │  ← Virtual scrolling
│           Messages / Tool Output            │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │   NewMessagesPill (absolute)          │  │  ← "3 new messages ▼"
│  └───────────────────────────────────────┘  │
│                                             │
├─────────────────────────────────────────────┤  ← ▔ (permission divider)
│  PromptInput / Spinner / Permissions        │  ← Fixed bottom, maxHeight=50%
└─────────────────────────────────────────────┘
```

### Key Props:
- `scrollable` - Message content (virtualized)
- `bottom` - Fixed prompt/spinner area
- `overlay` - Permission requests (scrollable context above modal)
- `modal` - Slash-command dialog / settings
- `bottomFloat` - Floating elements (e.g., companion speech bubble)
- `scrollRef` - Scroll control handle
- `dividerYRef` - Unseen message divider position

### Fullscreen Mode Detection
```typescript
isFullscreenEnvEnabled() // CLAUDE_CODE_NO_FLICKER env var
```
- **ants (internal):** Default ON
- **external users:** Default OFF (opt-in)

---

## Component Hierarchy

```
App (providers: FPS, Stats, AppState)
└── REPL (main screen)
    └── FullscreenLayout
        ├── StickyPromptHeader (conditional)
        ├── ScrollBox (virtualized)
        │   └── Messages
        │       └── VirtualMessageList
        │           └── MessageRow[]
        │               └── Message (switch on type)
        │                   ├── AssistantTextMessage
        │                   ├── AssistantToolUseMessage
        │                   ├── UserTextMessage
        │                   ├── SystemTextMessage
        │                   └── ...
        ├── NewMessagesPill (absolute overlay)
        └── Bottom Area
            ├── SuggestionsOverlay (slash commands)
            ├── DialogOverlay
            └── Box (prompt container)
                ├── PromptInput
                └── StatusLine (optional)
```

---

## Key Components

### 1. VirtualMessageList
**File:** `src/components/VirtualMessageList.tsx`

- Uses `useVirtualScroll` hook for viewport culling
- Tracks `ScrollBoxHandle` with methods:
  - `scrollTo(y)` / `scrollBy(dy)` / `scrollToBottom()`
  - `getScrollTop()` / `getScrollHeight()` / `getViewportHeight()`
  - `subscribe(listener)` for scroll events
  - `isSticky()` - whether pinned to bottom

### 2. Messages
**File:** `src/components/Messages.tsx`

- Message types handled in switch:
  - `attachment` - Image/file attachments
  - `assistant` - Claude responses (iterates content blocks)
  - `user` - User messages (text, images, tool results)
  - `system` - System events (compact boundaries, local commands)
  - `grouped_tool_use` - Collapsed tool calls
  - `collapsed_read_search` - Collapsed search results

### 3. Message
**File:** `src/components/Message.tsx`

- Dispatches to sub-components based on content block type:
  - `tool_use` → `AssistantToolUseMessage`
  - `text` → `AssistantTextMessage`
  - `thinking` → `AssistantThinkingMessage`
  - `redacted_thinking` → `AssistantRedactedThinkingMessage`
  - `image` → `UserImageMessage`
  - `tool_result` → `UserToolResultMessage`

### 4. PromptInput
**Directory:** `src/components/PromptInput/`

Files:
- `PromptInput.tsx` - Main input component
- `PromptInputFooter.tsx` - Below input (suggestions)
- `PromptInputFooterSuggestions.tsx` - Slash command suggestions
- `PromptInputModeIndicator.tsx` - Mode indicator (normal/edit/vim)
- `ShimmeredInput.tsx` - Animated prompt character
- `VoiceIndicator.tsx` - Voice mode indicator

### 5. Stats
**File:** `src/components/Stats.tsx`

- ASCII chart rendering with `asciichart`
- Tab-based layout with `Tabs` component
- Metrics: cost, tokens, duration, session stats

### 6. StatusLine
**File:** `src/components/StatusLine.tsx`

- Optional bar below prompt
- Shows: model, cwd, permission mode, cost, context %, rate limits
- Configurable via settings

---

## Ink/React Components (src/ink.ts exports)

### Core Primitives:
```typescript
import { Box, Text, Spacer, Link, Button, Newline, Ansi } from '../ink.js'

// Hooks
import { useInput, useStdin, useTerminalFocus, useTheme, useApp } from '../ink.js'
```

### Layout Primitives (from `src/ink/styles.ts`):
```typescript
type Styles = {
  // Flexbox
  flexGrow?: number
  flexShrink?: number
  flexDirection?: 'row' | 'column' | 'row-reverse' | 'column-reverse'
  flexWrap?: 'nowrap' | 'wrap' | 'wrap-reverse'
  alignItems?: 'flex-start' | 'center' | 'flex-end' | 'stretch'
  justifyContent?: 'flex-start' | 'center' | 'flex-end' | 'space-between' | ...

  // Dimensions
  width?: number | string
  height?: number | string
  minWidth?, maxWidth?, minHeight?, maxHeight?

  // Spacing
  padding?: number
  paddingX?, paddingY?, paddingTop?, paddingBottom?, paddingLeft?, paddingRight?
  margin?, marginX?, marginY?, marginTop?, marginBottom?, marginLeft?, marginRight?
  gap?, rowGap?, columnGap?

  // Positioning
  position?: 'absolute' | 'relative'
  top?, bottom?, left?, right?

  // Borders
  borderStyle?: BorderStyle
  borderColor?: Color
  borderTop?, borderBottom?, borderLeft?, borderRight?

  // Text
  textWrap?: 'wrap' | 'truncate-end' | 'truncate' | ...

  // Overflow
  overflow?: 'visible' | 'hidden' | 'scroll'
  overflowX?, overflowY?

  // Other
  backgroundColor?: Color
  opaque?: boolean  // Fills with spaces, blocks transparency
  display?: 'flex' | 'none'
}
```

### Color Types:
```typescript
type Color = RGBColor | HexColor | Ansi256Color | AnsiColor
// Examples: 'rgb(215,119,87)', '#ff0000', 'ansi256(196)', 'ansi:red'
```

---

## Theme System

**File:** `src/utils/theme.ts`

### Theme Colors (dark theme examples):
```typescript
const darkTheme: Theme = {
  claude: 'rgb(215,119,87)',        // Brand orange
  text: 'rgb(232,230,227)',          // Off-white
  background: 'rgb(13,13,13)',        // Near-black
  permission: 'rgb(87,105,247)',     // Medium blue
  success: 'rgb(46,160,67)',          // Green
  error: 'rgb(210,47,47)',            // Red
  warning: 'rgb(181,131,90)',        // Amber
  subtle: 'rgb(134,130,123)',         // Dim text
  userMessageBackground: 'rgb(30,30,30)',
  userMessageBackgroundHover: 'rgb(40,40,40)',
  diffAdded: 'rgb(46,160,67)',
  diffRemoved: 'rgb(210,47,47)',
  // ... agent colors, rainbow colors, etc.
}
```

### Themes Available:
- `dark` (default)
- `light`
- `dark-daltonized`
- `light-daltonized`
- `dark-ansi` (16-color fallback)
- `light-ansi`

---

## Unicode Figures / Glyphs

**File:** `src/constants/figures.ts`

```typescript
// Status indicators
BLACK_CIRCLE = '⏺'      // macOS, '●' on Linux
UP_ARROW = '↑'          // Scroll hint
DOWN_ARROW = '↓'
LIGHTNING_BOLT = '↯'    // Fast mode

// Effort levels
EFFORT_LOW = '○'        // ◯
EFFORT_MEDIUM = '◐'     // ◑
EFFORT_HIGH = '●'       // Black circle
EFFORT_MAX = '◉'        // Filled diamond (Opus 4.6)

// Borders
BLOCKQUOTE_BAR = '▎'    // Left quarter block
HEAVY_HORIZONTAL = '━'  // Box-drawing

// Media controls
PLAY_ICON = '▶'
PAUSE_ICON = '⏸'

// Special
DIAMOND_OPEN = '◇'
DIAMOND_FILLED = '◆'
FLAG_ICON = '⚑'
```

---

## Design System Components

**Directory:** `src/components/design-system/`

| Component | Purpose |
|-----------|---------|
| `Box.tsx` / `ThemedBox.tsx` | Layout container with theme |
| `Text.tsx` / `ThemedText.tsx` | Text with theme colors |
| `Pane.tsx` | Bordered container |
| `Divider.tsx` | Horizontal rule |
| `Tabs.tsx` | Tab navigation |
| `Dialog.tsx` | Modal dialog |
| `ProgressBar.tsx` | ASCII progress bar |
| `KeyboardShortcutHint.tsx` | Keybinding display |
| `Spinner.tsx` | Loading indicator |
| `ThemeProvider.tsx` | Theme context provider |

---

## Markdown Rendering

**File:** `src/components/Markdown.tsx`

- Uses `marked` library for parsing
- Token caching (500 entries) for performance
- Fast path: plain text skips markdown parser
- Tables rendered with flexbox layout
- Syntax highlighting via `marked.highlight`

---

## Key Patterns to Replicate in Java

### 1. Virtual Scrolling
```typescript
// React: useVirtualScroll hook + ScrollBox component
const { scrollTo, scrollBy, scrollToBottom } = scrollRef.current
```
**Java equivalent:** Implement viewport culling with TamboUI's scrolling

### 2. Fullscreen Layout
```typescript
// React: FullscreenLayout with flexbox
<Box flexDirection="column" flexGrow={1}>
  <ScrollBox flexGrow={1} />
  <Box flexShrink={0} />
</Box>
```
**Java equivalent:** BorderLayout or BoxLayout with JScrollPane

### 3. Sticky Bottom Prompt
```typescript
// React: flexShrink={0} on bottom, flexGrow={1} on scroll
```
**Java equivalent:** South panel fixed, Center scrolls

### 4. Message Type Dispatch
```typescript
// React: switch on message.type
switch (message.type) {
  case 'assistant': return <AssistantMessage />;
  case 'user': return <UserMessage />;
  // ...
}
```
**Java equivalent:** Strategy pattern or visitor

### 5. Theme System
```typescript
// React: ThemedBox/ThemedText with useTheme()
const { theme } = useTheme()
<Box color={theme.claude}>Hello</Box>
```
**Java equivalent:** Theme enum + color map + styled components

### 6. New Messages Pill
```typescript
// React: absolute positioned at bottom
<Box position="absolute" bottom={0}>
  <Text>3 new messages ▼</Text>
</Box>
```
**Java equivalent:** Overlay label at scroll bottom

### 7. Sticky Prompt Header
```typescript
// React: fixed 1-row header above scroll
<Box height={1}>
  <Text>Context: what user asked...</Text>
</Box>
```
**Java equivalent:** Header row above message list

---

## File Locations Summary

| React Component | TamboUI Equivalent | Notes |
|-----------------|---------------------|-------|
| `FullscreenLayout.tsx` | Main layout class | Flexbox container |
| `VirtualMessageList.tsx` | MessageListPanel | Virtualized message view |
| `Messages.tsx` | MessageRenderer | Message dispatch |
| `Message.tsx` | MessageView | Single message render |
| `PromptInput/` | InputPanel | User input |
| `Stats.tsx` | StatsPanel | ASCII charts |
| `StatusLine.tsx` | StatusBar | Bottom info bar |
| `design-system/Box.tsx` | StyledBox | Layout container |
| `design-system/Text.tsx` | StyledText | Themed text |
| `ink/ScrollBox.tsx` | JScrollPane | Scrolling container |

---

## Implementation Priority

1. **FullscreenLayout** - Core layout container
2. **Message rendering** - Dispatch + rendering
3. **PromptInput** - User input
4. **Virtual scrolling** - Performance
5. **Theme system** - Colors
6. **StatusLine** - Bottom info
7. **Stats panel** - ASCII charts
8. **Dialogs/modals** - Slash commands

---

## Sources

- Main repo: https://github.com/Gitlawb/openclaude
- Ink (React for CLI): https://github.com/vadimdemedes/ink
- Ink docs via Context7: ink library
