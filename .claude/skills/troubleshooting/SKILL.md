---
name: troubleshooting
description: Consult known error cases and their resolutions for local development and the build pipeline.
---

General troubleshooting guide. For conventions and anti-patterns, see `CLAUDE.md`.

## Testing

### AssertJ `allMatch` unavailable on float array assertions

**Symptom:** `assertThat(float[]).allMatch(Float::isFinite)` does not compile.
**Reason:** `AbstractFloatArrayAssert` does not expose `allMatch`. This is an AssertJ limitation.
**Resolution:** Write a static helper that iterates the array:
```java
static void assertAllFinite(float[] values) {
  for (int i = 0; i < values.length; i++) {
    assertThat(values[i]).as("index %d", i).isFinite();
  }
}
```

### AssertJ `hasSize` unavailable on 2D array assertions

**Symptom:** `assertThat(float[][]).hasSize(n)` does not compile.
**Reason:** AssertJ's generic object array assert for `float[][]` lacks `hasSize`.
**Resolution:** Use `assertThat(result.length).isEqualTo(n)`.

### JaCoCo report not generated when tests fail

**Symptom:** `finalizedBy jacocoTestReport` does not produce a report.
**Reason:** Gradle fails the build on test failures before running finalized tasks.
**Resolution:** Add `ignoreFailures = true` to the `test` task.

## Build

### Python not found on MSYS2/Windows

**Symptom:** `python3` command fails with Microsoft Store redirect.
**Reason:** MSYS2 bash does not resolve `python3`; Windows aliases it to Store.
**Resolution:** Use full path `/c/Python311/python` or whichever version is installed.
