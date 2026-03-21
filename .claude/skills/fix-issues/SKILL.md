---
name: fix-issues
description: "Work on code review issues from .planning/ — implement fixes with tests and verify coverage."
---

Work on the `$ARGUMENTS` issues found in `.planning/` files.

## Workflow

For each issue ID in `$ARGUMENTS`:

1. **Locate**: Read `.planning/*.md` files to find the finding by ID (e.g., `C1`, `J3`, `U1`)
2. **Understand**: Read the referenced source file(s) and any existing tests
3. **Fix**: Implement the suggested fix in production code
4. **Test**: Write or update JUnit 5 tests for the changed code — target 100% branch coverage
   of the modified class (ignore `gui/` packages for coverage)
5. **Verify**: After all fixes, run `./gradlew test` and check JaCoCo coverage

## Finding resolution

- UI findings (`U*`): fix the production code; no unit tests required (SWT/Display testing
  is out of scope), but add tests if the fix touches testable non-UI logic
- Java findings (`J*`): fix and add unit tests covering the new/changed branches
- Cleanup findings (`C*`): apply the cleanup; update or remove affected tests
- Performance findings (`P*`): implement the optimization; verify existing tests still pass
- Validation findings (`V*`): add the validation/guard; write tests for valid and invalid inputs

## Test conventions

Follow existing project test patterns (see `CLAUDE.md` Testing section). Key points:

- CDI mocks: inject via reflection for `@Inject` fields (see `RepositoryTest.setField()`)
- Error contract: `assertThatThrownBy(...).isInstanceOf(...).hasMessageContaining(...)`
- Consult `troubleshooting/SKILL.md` for known testing pitfalls

## Coverage check

After tests pass, read the JaCoCo CSV report at `build/reports/jacoco/test/jacocoTestReport.csv`
and report branch coverage for each modified class. Flag any class below 100% branch coverage
(excluding `edu.yaprnn.gui.*` packages).

## Rules

- Do NOT modify test-only code unless the finding requires it (e.g., C1 removing dead code)
- Do NOT fix findings not listed in `$ARGUMENTS`
- Run `/build-project` if unsure whether changes compile
- Run `/run-tests` to verify all tests pass after changes
- Consult `troubleshooting/SKILL.md` if tests fail unexpectedly
