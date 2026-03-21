# Medium Priority Issues

## C1: SelectableTreeCellEditor has empty listener methods

**File**: `src/main/java/edu/yaprnn/gui/model/editors/SelectableTreeCellEditor.java:51-56`
**Category**: Cleanup

**Problem**: `addCellEditorListener()` and `removeCellEditorListener()` are no-op implementations. Combined with `NetworksTreeCellEditor` broadcasting listeners to all editors (see 2-high J2), the listener lifecycle is fragile.

**Fix**: Either implement with a backing collection, or add a comment documenting that `NetworksTreeCellEditor` manages listeners externally.

## U1: MainFrame uses keyTyped with VK_DELETE instead of keyPressed

**File**: `src/main/java/edu/yaprnn/gui/views/MainFrame.java:597-598`
**Category**: UI

**Problem**: `RemoveFromNetworksTreeKeyAdapter.keyTyped()` compares `getKeyChar()` against `KeyEvent.VK_DELETE`. While `VK_DELETE` (127) matches the DEL character on most platforms, this is fragile. The standard Swing pattern for key codes is to use `keyPressed()` with `getKeyCode()`.

**Fix**:

```java
@Override
public void keyPressed(KeyEvent e) {
  if (e.getKeyCode() == KeyEvent.VK_DELETE) {
    removeFromNetworksTree();
  }
}
```

## C2: VisualizationService ThreadLocal DecimalFormat never cleaned up

**File**: `src/main/java/edu/yaprnn/gui/services/VisualizationService.java:18-19`
**Category**: Cleanup

**Problem**: `ThreadLocal<DecimalFormat>` is a static field that is never explicitly removed. In a GUI app with thread pools (virtual threads for training), orphaned `ThreadLocal` entries can accumulate.

**Fix**: Since this is a `@Singleton` with static ThreadLocal, and the app uses virtual threads (which are short-lived), this is low risk. However, consider making it a plain static field since `DecimalFormat` is only used in the singleton's methods.

## J1: SampleControlsService uses overly defensive type parsing

**File**: `src/main/java/edu/yaprnn/gui/services/SampleControlsService.java:40-45`
**Category**: Java

**Problem**: Spinner value handling has a fallback path `Integer.parseInt(String.valueOf(value))` that masks type inconsistencies. If the `SpinnerNumberModel` is configured correctly, the value is always `Integer`.

**Fix**: Use a direct cast with a clear error:

```java
consumer.accept((Integer) value);
```

## C3: ControlsService listener suppression creates race window

**File**: `src/main/java/edu/yaprnn/gui/services/ControlsService.java:102-111`
**Category**: Cleanup

**Problem**: `setWithoutListeners()` removes all item listeners, runs the action, then re-adds them. If another thread or event adds a listener during the action, it won't be re-added. Also creates unnecessary allocations per call.

**Fix**: Use a flag-based suppression approach, or document that this method must only be called on the EDT.

## J2: PersistenceService wraps IOException without context

**File**: `src/main/java/edu/yaprnn/gui/services/PersistenceService.java:60-74`
**Category**: Java

**Problem**: All `IOException` instances are wrapped as `UncheckedIOException` without logging or adding context about which file operation failed.

**Fix**: Add a log message or use a message that includes the file path:

```java
throw new UncheckedIOException("Failed to read: " + file, e);
```

## C4: FilesService creates new FileNameExtensionFilter on every call

**File**: `src/main/java/edu/yaprnn/gui/services/FilesService.java:17-41`
**Category**: Cleanup

**Problem**: Six factory methods create new `FileNameExtensionFilter` instances on every invocation. These are stateless and could be cached as static constants.

**Fix**: Define filters as static final constants:

```java
private static final FileNameExtensionFilter MLN_FILTER =
    new FileNameExtensionFilter("Network (*.yaprnn-mln)", "yaprnn-mln");
```
