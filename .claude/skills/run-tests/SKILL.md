---
name: run-tests
description: Run the JUnit 5 test suite and report results
disable-model-invocation: true
---

Run the project tests and report results:

```bash
./gradlew test
```

If tests fail:
1. Read the test report at `build/reports/tests/test/index.html`
2. Identify failing tests and root causes
3. Report: test name, assertion failure, relevant stack trace
