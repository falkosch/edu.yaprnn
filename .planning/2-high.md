# High Priority Issues

## U1: TrainingWorker.done() shows error dialog on user-initiated cancel

**File**: `src/main/java/edu/yaprnn/gui/views/TrainingFrame.java:392-402`
**Category**: UI

**Problem**: When training is cancelled via `cancel(true)`, `get()` throws `CancellationException`. The catch block catches all `Exception` and shows an error dialog. A user-initiated stop should not show an error dialog.

**Fix**: Add a specific catch for `CancellationException` before the generic `Exception` catch:

```java
@Override
protected void done() {
  try {
    get();
  } catch (CancellationException ignored) {
    // User stopped training — not an error
  } catch (Exception exception) {
    var cause = exception.getCause() != null ? exception.getCause() : exception;
    dialogsService.showError(TrainingFrame.this, TITLE, cause);
  } finally {
    trainingWorker = null;
    startTrainingButton.setEnabled(true);
    stopTrainingButton.setEnabled(false);
  }
}
```

## U2: PlayAudioSampleMouseAdapter fires for non-audio samples

**File**: `src/main/java/edu/yaprnn/gui/views/SampleDetailsView.java:256-259`
**Category**: UI

**Problem**: `from()` calls `AudioSystem.getAudioInputStream(sample.getFile())` for any sample type. For `ImageSample`, `getFile()` returns the MNIST binary package file, not an audio file. Clicking the preview panel for an image sample will throw `UnsupportedAudioFileException`, which is caught and shown as an error dialog.

**Fix**: Add a type check at the start of `from()`:

```java
private Clip from(Sample sample) throws Exception {
  if (!(sample instanceof SoundSample)) {
    return null;
  }
  // ... rest of method
}
```

## J1: TrainingDataNode integer division truncation

**File**: `src/main/java/edu/yaprnn/gui/model/nodes/TrainingDataNode.java:34-35`
**Category**: Java

**Problem**: `100 * trainingSize / sum` uses integer arithmetic, which truncates. If `trainingSize=1` and `sum=500`, the result is `0%` instead of showing a meaningful percentage. The same issue applies to `devTestWeight`.

**Fix**: Use float or round properly:

```java
var trainingWeight = Math.round(100f * trainingSize / sum);
var devTestWeight = Math.round(100f * devTestSize / sum);
```

## U3: NetworksTreeModel uses HashSet for listeners (ConcurrentModificationException risk)

**File**: `src/main/java/edu/yaprnn/gui/model/NetworksTreeModel.java:58`
**Category**: UI

**Problem**: `treeModelListeners` is a `HashSet` iterated in `fireStructureChanged()`. If a listener modifies the listener set during notification, this throws `ConcurrentModificationException`. Swing tree models frequently trigger listener changes during callbacks.

**Fix**: Use `CopyOnWriteArraySet` for safe concurrent iteration:

```java
private final Collection<TreeModelListener> treeModelListeners = new CopyOnWriteArraySet<>();
```

## J2: NetworksTreeCellEditor broadcasts listeners to all editors

**File**: `src/main/java/edu/yaprnn/gui/model/editors/NetworksTreeCellEditor.java:93-99`
**Category**: Java

**Problem**: `addCellEditorListener()` and `removeCellEditorListener()` register the same listener on every `SelectableTreeCellEditor` instance. This causes duplicate event delivery — a single edit triggers listener callbacks from all editors, not just the active one.

**Fix**: Only register listeners on the currently selected editor, or maintain a single listener list in `NetworksTreeCellEditor` and delegate from the active editor.
