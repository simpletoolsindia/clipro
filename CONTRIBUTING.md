# Contributing to CLIPRO

Thank you for your interest in contributing to CLIPRO!

## Development Setup

```bash
# Clone the repository
git clone https://github.com/simpletoolsindia/clipro.git
cd clipro

# Build the project
./gradlew build

# Run tests
./gradlew test
```

## Requirements

- Java 17+
- Ollama (for E2E tests)
- Gradle (or use gradlew wrapper)

## TDD Workflow (Mandatory)

All features must follow Test-Driven Development:

```
1. Write FAILING test first
2. Write minimal code to pass
3. Refactor
4. Repeat
```

### Test Structure

```java
// File: src/test/java/com/clipro/{module}/{Feature}Test.java

package com.clipro.{module};

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class {Feature}Test {

    @Test
    void should_{expected_behavior}_when_{condition}() {
        // Given - setup
        // When - action
        // Then - assertions
    }
}
```

## Code Standards

### File Organization
- One class = One task
- Max 100 lines per class (guideline, not strict)
- Max 1 feature per commit

### Naming Conventions
- Classes: PascalCase (`VimState.java`)
- Methods: camelCase (`getModelName()`)
- Tests: `should_{behavior}_when_{condition}()`

### Commit Messages

Follow conventional commits:
```
type(scope): description

types: feat, fix, docs, chore, refactor, test
```

Example:
```
feat(llm): add Ollama streaming support
fix(ui): correct terminal resize handling
docs(readme): update project status
```

## Agent Collaboration

This project uses two agents working in parallel:

| Agent | Tasks |
|-------|-------|
| MB Agent | Ollama/LLM, Native tools, Agent engine |
| PC Agent | UI components, TamboUI, Vim mode |

### Communication
- Use `TEAM_DISCUSSION.md` for inter-agent coordination
- Check file every 5 minutes for replies
- Push changes after each major milestone

## Reference: OpenClaude

CLIPRO is a Java port of OpenClaude. Always reference:
```
~/openclaude/src/
├── ink/           # TUI framework
├── components/    # React components
├── services/api/  # LLM clients
└── tools/         # Tool implementations
```

## Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test --info

# Run specific module
./gradlew test --tests "com.clipro.llm.*"

# Integration tests (requires Ollama)
./gradlew test --tests "*E2ETest"
```

## Building

```bash
# Build JAR
./gradlew uberJar

# Native image (requires GraalVM)
./gradlew nativeCompile
native-image -jar build/libs/clipro-0.1.0-uber.jar
```

## Pull Request Process

1. Create a branch from `main`
2. Write tests for new functionality
3. Ensure all tests pass: `./gradlew test`
4. Update TASKS.md if adding new features
5. Push and create PR with description

## Questions?

- GitHub Issues: https://github.com/simpletoolsindia/clipro/issues
- Email: support@simpletools.in

---

**Author:** Sridhar Karuppusamy (SimpleTools India)
**License:** MIT