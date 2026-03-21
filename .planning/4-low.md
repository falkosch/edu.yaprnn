# Low Priority Issues

## C1: Event record fields named "value" lack specificity

**Files**:
- `src/main/java/edu/yaprnn/events/OnModelNodeSelected.java:5`
- `src/main/java/edu/yaprnn/events/OnMultiLayerNetworkSelected.java:5`
- `src/main/java/edu/yaprnn/events/OnSampleSelected.java:5`
- `src/main/java/edu/yaprnn/events/OnTrainingDataSelected.java:5`
**Category**: Cleanup

**Problem**: All selection event records use a generic field name `value`. This reduces readability at call sites (e.g., `event.value()` vs `event.sample()`).

**Fix**: Rename to domain-specific names: `modelNode`, `multiLayerNetwork`, `sample`, `trainingData`.

## C2: ImportImagesPanel redundant setVisible(true)

**File**: `src/main/java/edu/yaprnn/gui/views/ImportImagesPanel.java:74`
**Category**: Cleanup

**Problem**: `setVisible(true)` is called on a panel that will be embedded in a dialog via `dialogsService.confirm()`. The dialog manages visibility.

**Fix**: Remove the `setVisible(true)` call.

## C3: Editor classes share near-identical two-constructor pattern

**Files**: `src/main/java/edu/yaprnn/gui/model/editors/ActivationFunctionTreeCellEditor.java` (and 7 other editor classes)
**Category**: Cleanup

**Problem**: Each editor repeats the same two-constructor boilerplate: a public `@Inject` constructor and a private delegate constructor. The pattern is identical across 8 classes.

**Fix**: Acceptable for CDI injection constraints. No action needed unless a factory pattern is introduced.

## C4: HEAT_LOOKUP is an alias for SCIENTIFIC_COLORS_BERLIN_LOOKUP

**File**: `src/main/java/edu/yaprnn/gui/services/VisualizationService.java:31`
**Category**: Cleanup

**Problem**: `HEAT_LOOKUP` is assigned directly from `SCIENTIFIC_COLORS_BERLIN_LOOKUP`. The indirection adds no value since there's only one color scheme.

**Fix**: Use `SCIENTIFIC_COLORS_BERLIN_LOOKUP` directly, or rename to `HEAT_LOOKUP` and remove the original constant.

## C5: SimpleSample has two identical preview methods

**File**: `src/main/java/edu/yaprnn/samples/model/SimpleSample.java:45-51`
**Category**: Cleanup

**Problem**: `createPreviewFromOriginal()` and `createPreviewFromInput()` both delegate to `createPreviewFrom(input)` with the same argument, making them identical.

**Fix**: If this is intentional (SimpleSample has no original/input distinction), add a comment. Otherwise, `createPreviewFromOriginal()` should use a different data source.
