---
name: build-project
description: Build the project with Gradle
---

Build the project:

```bash
./gradlew build
```

If the build fails:

1. Parse compiler errors from output
2. Identify affected source files and line numbers
3. Report each error with file path and suggested fix
