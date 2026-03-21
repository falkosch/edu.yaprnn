---
name: code-review
description: Perform a comprehensive code review of the Java app. Finds cleanup opportunities, UI bugs, and general code improvements. Writes findings to .planning/ grouped by severity.
rules:
  - .claude/rules/code-quality.md
---

Perform a code review of the Java application. Read all non-generated source files under
`src/main/java/` before writing findings.

## Review categories

Evaluate every file against these categories, in this order:

1. **Cleanup**: things to clean up, compact, or simplify (dead code, redundant logic, verbose
   patterns, unused imports, naming inconsistencies)
2. **UI handling**: bugs or issues in SWT/Display/event handling (threading violations, resource
   leaks, missing guards, incorrect listener lifecycle)
3. **General Java**: improvements in non-MLP application code (error handling, validation, API
   contracts, resource management, concurrency) — skip neural-network math

## Output format

Write each finding to a Markdown file in `.planning/` grouped by severity. Create one file per
severity level only if there are findings for that level:

| File                              | Contents                                                                                                           |
|-----------------------------------|--------------------------------------------------------------------------------------------------------------------|
| `.planning/1-critical.md`         | Bugs causing incorrect behavior, data corruption, or crashes                                                       |
| `.planning/2-high.md`             | Correctness risks, thread-safety, reliability under load                                                           |
| `.planning/3-medium.md`           | Code quality, maintainability, minor improvements                                                                  |
| `.planning/4-low.md`              | Style, naming, cleanup, nice-to-haves                                                                              |
| `.planning/5-performance.md`      | Performance bottlenecks, unnecessary allocations, scaling issues                                                   |
| `.planning/6-regression.md`       | Changes that may have broken existing behavior                                                                     |
| `.planning/7-needs-validation.md` | Suspicious patterns that need manual verification                                                                  |
| `.planning/8-network.md`          | Suggestions regarding MLP math/algorithm correctness (activation functions, gradient computation, loss  functions) |

### Finding format

Use this template for each finding within a severity file:

```markdown
## ID: Short title

**File**: `path/to/File.java:line`
**Category**: Cleanup | UI | Java

**Problem**: One-sentence description of what is wrong.

**Fix**: Concrete suggestion, with a code snippet if helpful.
```

- Assign a short unique ID prefix per category: `C` for Cleanup, `U` for UI, `J` for Java,  `P` for
  Performance, `R` for Regression, `V` for Validation
- Number sequentially within each file (e.g., `C1`, `C2`, `U1`, `J1`)
- Include the exact file path and line number
- Keep the problem statement factual — no speculation
- Keep the fix actionable — show the change, not just "fix this"

## Rules

- Use @.claude/rules/code-quality.md for coding guidelines
- Read source files thoroughly before writing any findings. Use Explore agents for broad search
- Do **not** review generated code (MapStruct `*Impl.java`, Lombok-generated methods)
- Cross-reference with `troubleshooting/SKILL.md` to avoid documenting already-known issues
- If a `.planning/` file already exists, consolidate the existing and the fresh findings, remove
  redundant ones
