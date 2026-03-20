---
name: retro
description: Review .claude/skills, CLAUDE.md files after a work session. Consolidate learnings, fix inconsistencies, reduce redundancies.
---

After completing implementation or test work, review project documentation for gaps and
inconsistencies based on what was learned during the session.

## Scope

Read all of these:

1. **Skills**: Every `SKILL.md` in `.claude/skills/`
2. **CLAUDE.md files**: Root `CLAUDE.md`, and any feature-specific CLAUDE.md files touched during
   the session

## Checklist

For each file, evaluate:

- [ ] **Numbering/references match**: Cross-references between files use correct principle numbers,
  file paths, and section names
- [ ] **No stale information**: Commands, imports, API signatures, and file paths still reflect the
  current codebase
- [ ] **No missing gotchas**: Errors hit during the session are documented in
  `troubleshooting/SKILL.md` with Symptom/Reason/Resolution format
- [ ] **No undocumented patterns**: Reusable implementation patterns discovered during the session
  are captured in the relevant CLAUDE.md or skill
- [ ] **No redundancy**: The same guidance does not appear in full in multiple files — use
  cross-references (`see troubleshooting skill`) instead of duplicating content
- [ ] **Single source of truth**: Each concern has exactly one authoritative location:
    - Error patterns and resolutions → `troubleshooting/SKILL.md`
    - Skills reference their authoritative source, not restate it

## Rules

- Keep all files concise. No prose — use bullet points, tables, and code blocks.
- Add only confirmed, reproducible findings. Do not speculate.
- When adding to `troubleshooting/SKILL.md`, follow the existing format: `### Title`,
  `**Symptom:**`, `**Reason:**`, `**Resolution:**`.
- When a fix applies to a specific dependency version, note it.

## Output

Report changes as a table:

| File | Change | Reason |
|------|--------|--------|
| ...  | ...    | ...    |

List files reviewed but not changed as "No changes needed".
