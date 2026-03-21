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

### Mockito cannot mock sealed interfaces

**Symptom:** `MockitoException: Sealed interfaces or abstract classes can't be mocked` when using `@Mock ModelNode`.
**Reason:** `ModelNode` is a sealed interface. Mockito cannot subclass sealed types.
**Resolution:** Mock the non-sealed subclass `DefaultNode` instead:
```java
@Mock
DefaultNode modelNode;
```

### Ambiguous method reference with `setSelected(null)` on generic overloads

**Symptom:** `Referenz zu setSelected ist mehrdeutig` when calling `setSelected(null)` on a class with both `setSelected(T)` and `setSelected(SomeType)`.
**Reason:** Java cannot resolve `null` when multiple overloads accept reference types.
**Resolution:** Cast the null: `setSelected((T) null)` or `setSelected((String) null)`.

### Test OOM with large network performance tests

**Symptom:** `Could not complete execution for Gradle Test Executor — Java heap space` when running `MultiLayerNetworkPerformanceTest`.
**Reason:** Test JVM has `-Xmx512m`. Mini-batch learning allocates per-sample gradient matrices in parallel; with 999×999×999 layers and batch size 100, this exceeds heap.
**Resolution:** Use a smaller batch size (e.g., 10) in performance tests, or increase test JVM heap in `build.gradle`:
```groovy
test { jvmArgs '-Xmx2g' }
```

### ArrayBlockingQueue deadlock in learnMiniBatch with pooled gradient buffers

**Symptom:** Training hangs indefinitely when `batchSize > maxParallelism` using `ArrayBlockingQueue.take()` inside `invokeAll` tasks.
**Reason:** `invokeAll` blocks until all tasks complete. With a pool of `maxParallelism` buffers and `batchSize` tasks, tasks beyond `maxParallelism` block on `take()` waiting for buffers that are only returned after `invokeAll` — classic deadlock.
**Resolution:** Do not use a blocking pool inside `invokeAll` tasks. Instead, assign buffers by index (one per task) or partition work into `maxParallelism` chunks with one buffer each.

### P1 weight snapshot breaks L1/L2 regularization correctness

**Symptom:** Training produces different (worse) results after adding `copyMatrices(layerWeights)` snapshot before each batch.
**Reason:** `applyGradients` reads the `layerWeights` parameter for L1/L2 decay computation but writes to `this.layerWeights`. With a snapshot, decay reads stale weights. Also unnecessary: `invokeAll` + `LinkedBlockingQueue` already provides happens-before ordering.
**Resolution:** Use `this.layerWeights` directly. The thread pool's work queue establishes happens-before between `applyGradients` writes and subsequent batch reads.

### `File("").getName()` returns empty string, `File("/path/").getName()` does not

**Symptom:** Test expects `IllegalArgumentException` for empty filename but gets `NullPointerException` because `File("/some/path/").getName()` returns `"path"`, not `""`.
**Reason:** `File.getName()` returns the last path component. Only `new File("")` yields an empty name.
**Resolution:** Use `new File("")` in tests to trigger empty-name guards:
```java
var file = new File("");
assertThatThrownBy(() -> service.fromAiff(new File[]{file}))
    .isInstanceOf(IllegalArgumentException.class);
```

### Lock-free multi-buffer gradient summation in applyGradients causes cache thrashing

**Symptom:** CPU load drops from ~55% to ~24% after replacing pipelined `synchronized` merge with lock-free summation of all chunk gradient buffers in `applyGradients`.
**Reason:** `applyGradients` iterates `chunkCount` separate `float[][]` buffers per weight (`chunkGradients[c][lw][w]`), causing cache-line thrashing across many arrays. The inner loop touches `chunkCount` distinct cache lines per iteration. This sequential multi-buffer scan is far slower than the pipelined merge which only reads 2 arrays at a time.
**Resolution:** Keep the pipelined merge pattern: each chunk merges into `chunkGradients[0]` under `synchronized` as it finishes. `applyGradients` then reads a single accumulator — sequential, cache-friendly scan of 2 arrays (weights + gradients).

## SWT/GUI

### Widget is disposed when reading dialog values after close()

**Symptom:** `SWTException: Widget is disposed` at `Text.getText()` after closing a modal dialog shell.
**Reason:** `Shell.close()` disposes all child widgets. Code then tries to read `Text.getText()` on disposed widgets.
**Resolution:** Use `shell.setVisible(false)` in the OK handler instead of `close()`. Loop with `while (shell.isVisible()) { if (!display.readAndDispatch()) display.sleep(); }`, read widget values, then call `shell.dispose()` in a finally block.

### SWT Scale selection mapping for float ranges

**Symptom:** Gamma slider always at max or doesn't map correctly to float range.
**Reason:** SWT `Scale` is integer-only. Must map integer range to float range manually.
**Resolution:** Map 0..1000 to -1.0..0.0: `var gamma = (scale.getSelection() - 1000) / 1000f;`. Set midpoint default with `scale.setSelection(500)`.

## Build

### Python not found on MSYS2/Windows

**Symptom:** `python3` command fails with Microsoft Store redirect.
**Reason:** MSYS2 bash does not resolve `python3`; Windows aliases it to Store.
**Resolution:** Use full path `/c/Python311/python` or whichever version is installed.
