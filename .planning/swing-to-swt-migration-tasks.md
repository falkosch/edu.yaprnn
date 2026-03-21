# Swing → SWT/JFace Migration — Implementation Tasks

Cross-reference: [Migration Plan](swing-to-swt-migration.md)

---

## Phase 0: Preparation (Non-Breaking)

### P0-1: Add SWT/JFace dependencies to `build.gradle`

**File:** `build.gradle`

- Add version properties:
  ```groovy
  swtVersion = '3.128.0'
  jfaceVersion = '3.37.0'
  ```
- Add platform-specific SWT dependency with OS detection:
  ```groovy
  def swtPlatform = System.getProperty('os.name').toLowerCase().contains('win')
      ? 'org.eclipse.swt.win32.win32.x86_64'
      : System.getProperty('os.name').toLowerCase().contains('mac')
      ? 'org.eclipse.swt.cocoa.macosx.x86_64'
      : 'org.eclipse.swt.gtk.linux.x86_64'
  implementation "org.eclipse.platform:${swtPlatform}:${swtVersion}"
  implementation "org.eclipse.platform:org.eclipse.jface:${jfaceVersion}"
  ```
- Verify `./gradlew build` succeeds — SWT and Swing can coexist on the classpath.
- Check JFreeChart-SWT availability. If `jfreechart-swt` doesn't exist for 1.0.19, note the fallback plan (BufferedImage → Canvas, or upgrade to JFreeChart 1.5.x).

**Acceptance:** Build passes with both Swing and SWT/JFace on classpath.

---

### P0-2: Extract `TrainingService` from `TrainingFrame.TrainingWorker`

**Source file:** `gui/views/TrainingFrame.java` (inner class `TrainingWorker`, lines 358–490)
**New file:** `training/TrainingService.java`

- Move training orchestration out of the inner class:
  - The `train()` method (creates executor, iterates epochs, calls `learnMiniBatch`, computes accuracy)
  - The `measureIterationTime()` helper
  - The `getLearningRateState()` factory method
  - The `LearningRateModifier` enum (move to `training/` package or `networks/learningrate/`)
  - The `LearnSamplesConsumer` interface
- New service is a CDI `@Singleton` bean with `@Inject GradientMatrixService`, `@Inject ShuffleService`, `@Inject Repository`.
- Method signature: `train(TrainingParameters params, ProgressCallback callback)` where:
  - `TrainingParameters` is a new record (maxIterations, maxTrainingError, batchSize, maxParallelism, learningRate, learningRateModifier, learningRateChangeInterval, learningRateAscend, learningRateDescend, momentum, decayL1, decayL2, TrainingData, MultiLayerNetwork)
  - `ProgressCallback` is a functional interface: `void onEpochComplete(int iteration, float learningRate, AccuracyResult training, AccuracyResult devTest)`
- `TrainingFrame.TrainingWorker` becomes a thin adapter that calls `trainingService.train()` and forwards progress to the UI via `SwingUtilities.invokeLater()`.
- Add `cancel()` support via `Thread.currentThread().isInterrupted()` or an `AtomicBoolean`.

**Acceptance:** TrainingFrame still works identically. Training logic is testable without Swing.

---

### P0-3: Extract `ClassificationService` from `ClassifyFrame`

**Source file:** `gui/views/ClassifyFrame.java` (the `classify()` method's SwingWorker.doInBackground logic)
**New file:** `networks/ClassificationService.java`

- Extract: feedforward computation, output-to-image conversion, reconstruction visualization, classification table model data generation.
- New service method: `ClassificationResult classify(MultiLayerNetwork network, Sample sample, DataSelector selector, float zoom, int resolution, float overlap)`
- `ClassificationResult` record containing: `float[][] layerOutputs`, `List<String> labels`, `Image reconstructedImage`, table data (column names + row data as `Object[][]`).
- `ClassifyFrame` keeps the SwingWorker but calls `classificationService.classify()` in `doInBackground()`.

**Acceptance:** ClassifyFrame works identically. Classification logic is testable without Swing.

---

### P0-4: Extract `ScenarioService` from `MainFrame`

**Source file:** `gui/views/MainFrame.java` (method `setupDigitsScenario()`, lines 360–396)
**New file:** `model/ScenarioService.java`

- Move all resource loading and model assembly out of MainFrame.
- Method: `ScenarioData loadDigitsScenario()` returning lists of samples, training data, templates, networks.
- `MainFrame.setupDigitsScenario()` becomes: `scenarioService.loadDigitsScenario()` then passes results to `networksTreeModel`.
- Inject `PersistenceService`, `SamplesService` (the import logic, not the UI parts).

**Acceptance:** MainFrame.setupDigitsScenario() delegates to ScenarioService. Functional behavior unchanged.

---

### P0-5: Split `ControlsService` into toolkit-agnostic and Swing-specific parts

**Source file:** `gui/services/ControlsService.java`

- **Keep in `ControlsService`** (toolkit-agnostic):
  - `toTitlePart(T, Function<T,String>)` — pure logic
  - `silenceListModelListenersDuringRunnable()` — Swing-specific, but will be retired later; keep for now
  - `onlyNumbersKeyListener()` — Swing-specific, keep for now
- **Extract interface `ValidationSupport`** (or keep methods inline):
  - `validationColor(boolean valid)` — returns a toolkit-neutral result (e.g., boolean or enum) instead of `java.awt.Color`
- No functional changes in this phase. The split is preparation for Phase 5.
- Mark Swing-specific methods with `@Deprecated` comments noting SWT replacement target.

**Acceptance:** Builds, no behavioral changes.

---

### P0-6: Extract `Dialogs` interface from `DialogsService`

**Source file:** `support/swing/DialogsService.java`
**New file:** `support/Dialogs.java` (interface)

- Interface methods:
  ```java
  void showError(Object parent, String title, Throwable throwable);
  void showFinished(Object parent, String title);
  boolean confirm(Object parent, String title, String message);
  ```
- `DialogsService` implements `Dialogs` (using `Component` cast for `parent` in the Swing impl).
- Update all injection sites to inject `Dialogs` interface instead of `DialogsService` directly.
- In Phase 5, a new `SwtDialogsService implements Dialogs` will replace the Swing one.

**Acceptance:** All dialog calls work through the interface. Builds and runs.

---

### P0-7: Extract `FileSelection` interface from `FilesService`

**Source file:** `gui/services/FilesService.java`
**New file:** `gui/services/FileSelection.java` (interface)

- Interface methods:
  ```java
  void selectFile(Object parent, String filterDescription, String[] extensions, boolean isSave, Consumer<String> pathConsumer);
  ```
- `FilesService` implements `FileSelection` (adapts `FileNameExtensionFilter` → description + extensions).
- The `chooseFileButton()` and menu item helpers stay in `FilesService` for now (Swing-specific, retired in Phase 5).

**Acceptance:** Builds, no behavioral changes.

---

### P0-8: Decouple `ModelNode.getIcon()` from `javax.swing.Icon`

**Source files:**
- `gui/model/nodes/ModelNode.java` — returns `Icon`
- `gui/model/nodes/DefaultNode.java` — stores `Supplier<Icon>`, caches `Icon`
- `gui/services/IconsService.java` — produces `Icon` instances

- Option A (minimal): Add a `getIconKey()` or `getImageDescriptorKey()` method to `ModelNode` that returns a string key. The SWT label provider maps keys → SWT Images. Keep `getIcon()` for Swing compatibility during migration.
- Option B (cleaner): Change `ModelNode.getIcon()` return type to a custom `NodeIcon` record wrapping the raw image bytes or a resource path. Both Swing renderer and JFace label provider convert to their native type.
- **Recommended:** Option A — least disruptive. Add `default String getIconResourcePath()` to `ModelNode` returning the resource path. `DefaultNode` gets a `Supplier<String> iconResourcePathSupplier`. The Swing renderer keeps using `getIcon()`. The JFace label provider will use `getIconResourcePath()`.

**Acceptance:** `ModelNode` has both `getIcon()` (Swing) and `getIconResourcePath()` (toolkit-neutral). All existing Swing code still uses `getIcon()`.

---

## Phase 1: Foundation — Display, Shell, Event Loop

### P1-1: Create `SwtDisplayProducer` CDI bean

**New file:** `support/swt/SwtDisplayProducer.java`

- CDI `@Singleton` bean that creates and produces the SWT `Display`.
- `@Produces @Singleton Display getDisplay()` — creates `new Display()` (must be called on the main thread).
- `@Produces @Singleton Shell getRootShell(Display display)` — creates root `Shell` with `SWT.SHELL_TRIM`.
- **Important:** The Display must be created before any SWT widget. Weld SE creates beans lazily, so mark this as `@Startup` (CDI Lite) or use `@Observes ContainerInitialized` with `@Priority(1)` to ensure early creation.
- Add `@PreDestroy void dispose()` to call `display.dispose()`.

**Acceptance:** `Display` and root `Shell` are available for injection. No UI visible yet.

---

### P1-2: Create `SwtEventLoop` to run the SWT event loop

**New file:** `support/swt/SwtEventLoop.java`

- Observes `ContainerInitialized` with lower priority than `SwtDisplayProducer` and higher priority than `MainShell`.
- After `MainShell` opens the shell, runs the event loop:
  ```java
  void run(@Observes @Priority(Integer.MAX_VALUE) ContainerInitialized event) {
      while (!shell.isDisposed()) {
          if (!display.readAndDispatch()) display.sleep();
      }
  }
  ```
- **Alternative:** If `@Priority` on observers is insufficient, use a two-phase approach: `MainShell` calls `shell.open()`, then signals the event loop bean to start.
- The event loop blocks the CDI initialization thread. This is expected — SWT requires a blocking loop on the main thread.

**Acceptance:** An empty SWT Shell opens and responds to close. Application exits cleanly.

---

### P1-3: Create `SwtResourceManagerProducer`

**New file:** `support/swt/SwtResourceManagerProducer.java`

- Creates a JFace `LocalResourceManager` attached to the root shell.
- `@Produces @Singleton LocalResourceManager getResourceManager(Shell shell)` — `new LocalResourceManager(JFaceResources.getResources(), shell)`.
- This manager auto-disposes all created `Image`, `Color`, `Font` when the shell is destroyed.
- All SWT code that needs images/colors/fonts injects this `LocalResourceManager`.

**Acceptance:** `LocalResourceManager` injectable. No visible changes yet.

---

### P1-4: Migrate `IconsService` for SWT

**Source file:** `gui/services/IconsService.java`
**New file:** `gui/services/SwtIconsService.java`

- New CDI bean (separate from existing `IconsService` which stays for Swing during coexistence).
- Loads PNG resources as `ImageDescriptor` via `ImageDescriptor.createFromURL(url)`.
- Converts to SWT `Image` via `LocalResourceManager.create(imageDescriptor)`.
- Exposes: `Image getImage(String resourcePath)` and `ImageDescriptor getDescriptor(String resourcePath)`.
- Resizing: use SWT `ImageData.scaledTo(width, height)` instead of AWT `AffineTransformOp`.
- Cache `ImageDescriptor` instances (they're lightweight; `Image` is managed by `LocalResourceManager`).

**Acceptance:** SWT icons loadable for all 16+ icon resource paths. Old `IconsService` still works for remaining Swing code.

---

### P1-5: Create `ImageCanvas` (SWT replacement for `ImagePanel`)

**New file:** `support/swt/ImageCanvas.java`

- Extends `org.eclipse.swt.widgets.Canvas`.
- Constructor: `ImageCanvas(Composite parent, int style)`.
- Field: `Image image` (SWT Image, managed externally via ResourceManager).
- `setImage(Image image)` — stores reference, calls `redraw()`.
- `addPaintListener`: draws image centered on white background (matching `ImagePanel` behavior).
- `computeSize()` override: returns image dimensions for layout.
- Supports scroll via parent `ScrolledComposite`: set `setMinSize()` on the `ScrolledComposite` when image changes.

**Acceptance:** `ImageCanvas` renders an SWT `Image` centered, scrollable in a `ScrolledComposite`.

---

### P1-6: Create `SwtImages` utility (SWT replacement for `Images`)

**New file:** `support/swt/SwtImages.java`

- Static utility (final class, no instances).
- `static ImageData resize(ImageData source, float zoom, int filterType)` — scales `ImageData` by zoom factor.
- Uses `ImageData.scaledTo(newWidth, newHeight)` for basic scaling.
- For high-quality resize (bicubic equivalent), use `org.eclipse.swt.graphics.GC` to draw scaled or implement pixel-level interpolation.
- `static Image toSwtImage(Display display, BufferedImage bufferedImage)` — converts AWT `BufferedImage` to SWT `Image` via raw pixel data transfer (RGB byte arrays). This bridge is needed while `VisualizationService` still produces `BufferedImage`.
- Max size guard: mirror `Images.RESIZE_MAXSIZE` = 4095.

**Acceptance:** Can resize SWT `ImageData` and convert AWT `BufferedImage` → SWT `Image`.

---

### P1-7: Create `MainShell` skeleton (empty shell with menu and toolbar)

**New file:** `gui/views/MainShell.java`

- CDI `@Singleton` bean. Does NOT extend `Shell` (SWT widgets can't be subclassed easily in CDI). Instead, holds a `Shell` reference.
- `@Inject Display display`, `@Inject Shell shell` (root shell from P1-1).
- `void start(@Observes @Priority(100) ContainerInitialized event)`:
  1. Set shell title "YAPRNN"
  2. Set shell layout: `GridLayout(1, false)` (single column)
  3. Create menu bar (empty for now — populated in Phase 2+)
  4. Create toolbar composite (empty for now)
  5. Create `SashForm` (horizontal) as the main content area (empty left/right panes)
  6. Create `CTabFolder` in the right pane (empty tabs)
  7. `shell.setSize(1024, 768)` and `shell.open()`
- Wire `shell.addShellListener(new ShellAdapter() { shellClosed → ... })` for cleanup.
- **Do NOT** remove `MainFrame` yet — both can coexist in this phase for testing.

**Acceptance:** An SWT Shell opens with title "YAPRNN", a SashForm, and empty CTabFolder. Closes cleanly.

---

### P1-8: Disable `MainFrame` startup (switch to SWT entry)

**Source file:** `gui/views/MainFrame.java`

- Comment out or guard the `@Observes ContainerInitialized` in `MainFrame.start()` so it doesn't open a Swing window.
- The SWT `MainShell.start()` now drives the UI.
- Verify the application boots into the SWT shell, CDI events still fire, and the event loop runs.
- Keep `MainFrame.java` in the codebase (other phases will migrate its logic to `MainShell`).

**Acceptance:** Application starts with SWT shell only. No Swing JFrame appears.

---

## Phase 2: Tree Viewer

### P2-1: Add parent tracking to `DefaultNode`

**Source file:** `gui/model/nodes/DefaultNode.java`

- JFace `ITreeContentProvider.getParent(Object)` requires each node to know its parent.
- Add field: `ModelNode parent` (set during tree construction).
- Modify `DefaultNode.refresh()` or the children supplier evaluation to set `child.parent = this` for each child.
- Add `getParent()` method to `ModelNode` interface (default returns `null` for root).
- This is needed by `TreeViewer` for proper expand/collapse and `reveal()`.

**Acceptance:** Every `DefaultNode` knows its parent. Existing Swing tree still works (parent field is unused by Swing).

---

### P2-2: Create `NetworksTreeContentProvider`

**New file:** `gui/model/NetworksTreeContentProvider.java`

- Implements `org.eclipse.jface.viewers.ITreeContentProvider`.
- `@Inject` the same list nodes as `NetworksTreeModel`: `AllSamplesListNode`, `MultiLayerNetworkListNode`, `MultiLayerNetworkTemplateListNode`, `TrainingDataListNode`.
- Methods:
  - `getElements(Object inputElement)` — returns root children array (the 4 list nodes)
  - `getChildren(Object parentElement)` — casts to `ModelNode`, returns `getChildren().toArray()`
  - `getParent(Object element)` — casts to `ModelNode`, returns `getParent()` (from P2-1)
  - `hasChildren(Object element)` — `!modelNode.isLeaf()`
- `inputChanged()` — no-op (CDI events handle model changes).

**Acceptance:** Content provider compiles and returns correct tree structure for all node types.

---

### P2-3: Create `NetworksTreeLabelProvider`

**New file:** `gui/model/NetworksTreeLabelProvider.java`

- Implements `org.eclipse.jface.viewers.ILabelProvider` (or extends `LabelProvider`).
- `@Inject SwtIconsService swtIconsService`.
- `getText(Object element)` — `((ModelNode) element).getLabel()`.
- `getImage(Object element)` — `swtIconsService.getImage(((ModelNode) element).getIconResourcePath())` (from P0-8).

**Acceptance:** Label provider returns correct text and SWT Image for every node type.

---

### P2-4: Create `EnumEditingSupport<T>` for combo-based tree editing

**New file:** `gui/model/editors/EnumEditingSupport.java`

- Extends `org.eclipse.jface.viewers.EditingSupport`.
- Generic: `<T>` for the enum/value type.
- Constructor: `EnumEditingSupport(TreeViewer viewer, Class<T> valueType, T[] values, Function<ModelNode, Boolean> canEdit, Function<ModelNode, T> getValue, BiConsumer<ModelNode, T> setValue)`.
- `getCellEditor()` — returns `ComboBoxCellEditor` with string representations of `values`.
- `canEdit(Object)` — delegates to `canEdit` function.
- `getValue(Object)` — delegates to `getValue` function, returns index into values array.
- `setValue(Object, Object)` — converts index back to enum value, delegates to `setValue`, calls `viewer.update()`.
- This single class replaces: `ActivationFunctionTreeCellEditor`, `LossFunctionTreeCellEditor`, `DataSelectorTreeCellEditor`.

**Acceptance:** Enum editing works for ActivationFunction, LossFunction, DataSelector node types.

---

### P2-5: Create `TextEditingSupport` for text-based tree editing

**New file:** `gui/model/editors/TextEditingSupport.java`

- Extends `EditingSupport`.
- Constructor: `TextEditingSupport(TreeViewer viewer, Function<ModelNode, Boolean> canEdit, Function<ModelNode, String> getValue, BiConsumer<ModelNode, String> setValue)`.
- `getCellEditor()` — returns `TextCellEditor`.
- Optionally accepts a `Predicate<String> validator` for input validation.
- This replaces: `BiasTreeCellEditor` (float text), `LayerSizeTreeCellEditor` (int text).

**Acceptance:** Text editing works for bias and layer size nodes.

---

### P2-6: Create `NetworksTreeViewerFactory`

**New file:** `gui/model/NetworksTreeViewerFactory.java`

- CDI `@Singleton` bean.
- `@Inject NetworksTreeContentProvider contentProvider`
- `@Inject NetworksTreeLabelProvider labelProvider`
- Method: `TreeViewer createTreeViewer(Composite parent)`:
  1. Create `TreeViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL)`
  2. Set `contentProvider`, `labelProvider`
  3. Create `TreeViewerColumn` for the single column
  4. Attach editing support: dispatch by node type
     - For `ActivationFunctionNode`: `EnumEditingSupport` with `ActivationFunction.values()`
     - For `LossFunctionNode`: `EnumEditingSupport` with `LossFunction.values()`
     - For `DataSelectorNode`: `EnumEditingSupport` with `DataSelector` instances
     - For `BiasNode`: `TextEditingSupport` with float parsing
     - For `LayerSizeNode`: `TextEditingSupport` with int parsing
     - For `MultiLayerNetworkNode`, `MultiLayerNetworkTemplateNode`, `TrainingDataNode`: `TextEditingSupport` for name editing
  5. Add `ISelectionChangedListener` to fire CDI `OnModelNodeSelected` events
  6. Set input (triggers content provider)
  7. Add keyboard listener for Delete key → remove from tree

**Acceptance:** TreeViewer displays full node hierarchy with icons, labels, and inline editing.

---

### P2-7: Create `NetworksTreeRefreshService`

**New file:** `gui/model/NetworksTreeRefreshService.java`

- CDI `@Singleton` bean.
- Holds reference to the `TreeViewer` (set after creation in `MainShell`).
- Replaces `NetworksTreeModel`'s `TreeModelListener` notifications.
- Methods:
  - `refreshAll()` — `Display.asyncExec(() -> treeViewer.refresh())`
  - `refresh(ModelNode node)` — `Display.asyncExec(() -> treeViewer.refresh(node))`
  - `add(ModelNode parent, ModelNode child)` — `Display.asyncExec(() -> { treeViewer.refresh(parent); treeViewer.reveal(child); })`
  - `remove(ModelNode element)` — `Display.asyncExec(() -> treeViewer.refresh(element.getParent()))`
- Wire into `Repository` change events or replace `NetworksTreeModel`'s mutation methods.

**Acceptance:** Adding/removing nodes in the model triggers TreeViewer refresh. Tree stays in sync.

---

### P2-8: Integrate TreeViewer into `MainShell`

**Source file:** `gui/views/MainShell.java` (from P1-7)

- Inject `NetworksTreeViewerFactory`.
- In the left pane of the `SashForm`, create the `TreeViewer` via the factory.
- Wire selection changes to enable/disable toolbar buttons (port `MainFrame.setEnabledOfCommands()` logic).
- Wire selection changes to switch `CTabFolder` tabs (sample vs. weights details).
- Store `TreeViewer` reference for the CDI qualifier `@NetworksTree` (update qualifier from `JTree` to `TreeViewer` type, or introduce a new `@SwtNetworksTree` qualifier).

**Acceptance:** Tree is visible in the main shell, navigable, editable. Selection drives tab switching and button state.

---

### P2-9: Port tree mutation operations to `NetworksTreeRefreshService`

**Source file:** `gui/model/NetworksTreeModel.java` (methods: `add()`, `remove()`, `addSamples()`, `addLayerTemplateTo()`)

- The mutation logic (adding/removing from Repository, refreshing nodes) currently lives in `NetworksTreeModel`.
- Move the Repository interaction logic to a new `TreeMutationService` or keep in `NetworksTreeModel` but replace Swing `TreeModelEvent` firing with calls to `NetworksTreeRefreshService`.
- Port each mutation:
  - `add(TrainingData)` → add to repo, call `refreshService.add(trainingDataListNode, newNode)`
  - `remove(ModelNode)` → switch on type, remove from repo, call `refreshService.remove(node)`
  - `addSamples(List<Sample>)` → add to repo, call `refreshService.refreshAll()`
  - `addLayerTemplateTo(template)` → mutate template layers, call `refreshService.refresh(templateNode)`

**Acceptance:** All add/remove operations work through SWT TreeViewer. No Swing TreeModelListeners remain.

---

### P2-10: Retire Swing tree classes

**Delete files:**
- `gui/model/NetworksTreeCellRenderer.java`
- `gui/model/editors/NetworksTreeCellEditor.java`
- `gui/model/editors/SelectableTreeCellEditor.java`
- `gui/model/editors/SelectableTreeCellEditorWithComponent.java`
- `gui/model/editors/ActivationFunctionTreeCellEditor.java`
- `gui/model/editors/BiasTreeCellEditor.java`
- `gui/model/editors/DataSelectorTreeCellEditor.java`
- `gui/model/editors/LayerSizeTreeCellEditor.java`
- `gui/model/editors/LossFunctionTreeCellEditor.java`
- `gui/model/editors/MultiLayerNetworkTemplateTreeCellEditor.java`
- `gui/model/editors/MultiLayerNetworkTreeCellEditor.java`
- `gui/model/editors/TrainingDataTreeCellEditor.java`
- `gui/model/editors/di/DefaultSelectableTreeCellEditor.java`

**Modify:**
- `gui/model/NetworksTreeModel.java` — remove `implements TreeModel`, remove all `TreeModelListener` code. Keep repository mutation methods if not moved in P2-9.
- `gui/views/di/NetworksTree.java` — update `@NetworksTree` qualifier type or retire if replaced by direct injection.

**Acceptance:** No `javax.swing.tree.*` imports remain in the `gui/model/` package. Build succeeds.

---

## Phase 3: Secondary Windows

### P3-1: Create `TrainingShell` (replaces `TrainingFrame`)

**New file:** `gui/views/TrainingShell.java`
**Retires:** `gui/views/TrainingFrame.java`

- CDI bean (not `@Singleton` — create via `Instance<TrainingShell>`).
- `@Inject Display display`, `@Inject LocalResourceManager resourceManager`, `@Inject TrainingService trainingService` (from P0-2).

**Shell setup:**
- Create child `Shell(display, SWT.SHELL_TRIM)`, title "Training".
- Layout: `GridLayout(1, false)` — toolbar on top, SashForm below.

**Toolbar (top):**
- `ToolBar` with SWT `ToolItem`s: Start, Stop, Clear Graph.
- Two `ComboViewer`s (JFace) embedded in toolbar for TrainingData and MultiLayerNetwork selection.
  - Content provider: `ArrayContentProvider.getInstance()`
  - Label provider: `LabelProvider` with `getText()` returning `.getName()`
  - Selection change listener: update shell title, enable/disable Start/Stop.

**Preferences panel (left of SashForm):**
- `ScrolledComposite` containing a `Composite` with `GridLayout(2, false)` (label + control pairs).
- Integer fields (maxIterations, batchSize, maxParallelism, learningRateChangeInterval): SWT `Spinner` with `setValues(default, min, max, 0, increment, pageIncrement)`.
- Float fields (maxTrainingError, learningRate, momentum, decayL1, decayL2, learningRateAscend, learningRateDescend): SWT `Text` with `VerifyListener` for numeric validation. Use `Spinner` with `setDigits(3)` for 3 decimal places where range is known.
- Learning rate modifier: SWT `Combo` with CONSTANT/PERIODIC/ADAPTIVE items.

**Chart panel (right of SashForm):**
- Investigate JFreeChart SWT integration:
  - If `org.jfree.experimental.chart.swt.ChartComposite` is available: use directly.
  - Fallback: Create a `Canvas` that renders `JFreeChart.createBufferedImage()` converted to SWT `Image` via `SwtImages.toSwtImage()`. Redraw on data change.
- Same 4 series: trainingError, trainingHitRate, devTestError, devTestHitRate.

**Training execution:**
- Start button calls `trainingService.train(params, callback)` on a virtual thread.
- `callback.onEpochComplete()` calls `display.asyncExec(() -> { update chart series })`.
- Stop button sets cancellation flag on the training service.

**Window close:**
- `ShellListener.shellClosed()`: cancel training if in progress, then `shell.dispose()`.

**Acceptance:** Training shell opens, configures hyperparameters, trains with live chart updates, stops cleanly.

---

### P3-2: Create `ClassifyShell` (replaces `ClassifyFrame`)

**New file:** `gui/views/ClassifyShell.java`
**Retires:** `gui/views/ClassifyFrame.java`

- CDI bean via `Instance<ClassifyShell>`.
- `@Inject ClassificationService classificationService` (from P0-3).

**Shell setup:**
- Child `Shell(display, SWT.SHELL_TRIM)`, title "Classify".
- `GridLayout(1, false)` — toolbar, then `CTabFolder` with 3 tabs.

**Toolbar:**
- 3 `ComboViewer`s: samples, networks, data selectors. Classify button.

**Tab 1 — Sample Details:**
- Embed the SWT version of `SampleDetailsView` (from Phase 4, or a temporary placeholder).

**Tab 2 — Layers:**
- `TableViewer` (JFace) for classification results.
- Content provider: `ArrayContentProvider`.
- Column label providers for each output column.
- Replaces `networksControlsService.valuesTable()` + `visualizationService.classificationTableModel()`.

**Tab 3 — Output reconstruction:**
- `ImageCanvas` (from P1-5) displaying the reconstructed image.

**Classification execution:**
- Classify button spawns a virtual thread:
  ```java
  Thread.startVirtualThread(() -> {
      var result = classificationService.classify(...);
      display.asyncExec(() -> updateUI(result));
  });
  ```

**Acceptance:** Classify shell opens, selects sample + network, runs classification, displays layer outputs in table and reconstructed image.

---

### P3-3: Create `SamplePreprocessingShell` (replaces `SamplePreprocessingFrame`)

**New file:** `gui/views/SamplePreprocessingShell.java`
**Retires:** `gui/views/SamplePreprocessingFrame.java`

- Simplest of the secondary windows.
- Child `Shell(display, SWT.SHELL_TRIM)`, title "Sample Preprocessing".

**Toolbar:**
- Process button + sample `ComboViewer`.

**Content:**
- Embed SWT `SampleDetailsComposite` (from Phase 4).

**Process action:**
- Stream-based sub-sampling (same logic as current, calls tree mutation service).

**Acceptance:** Sample preprocessing shell opens, selects sample, processes, updates tree.

---

### P3-4: Wire secondary shells into `MainShell`

**Source file:** `gui/views/MainShell.java`

- Inject `Instance<TrainingShell>`, `Instance<ClassifyShell>`, `Instance<SamplePreprocessingShell>`.
- Toolbar buttons (Train, Classify, Sample Preprocessing) → `shell.get().open()` (lazy creation).
- Forward CDI events:
  - `@Observes OnMultiLayerNetworkSelected` → `classifyShell.setSelectedNetwork()`, `trainingShell.setSelectedNetwork()`
  - `@Observes OnTrainingDataSelected` → `trainingShell.setSelectedTrainingData()`
  - `@Observes OnRepositoryElementsChanged` → update ComboViewer inputs in all shells
  - `@Observes OnRepositoryElementsRemoved` → remove items from ComboViewer inputs

**Acceptance:** Main shell toolbar opens secondary shells. Selection state propagates correctly.

---

## Phase 4: Detail Views and Panels

### P4-1: Create `SampleDetailsComposite` (replaces `SampleDetailsView`)

**New file:** `gui/views/SampleDetailsComposite.java`
**Retires:** `gui/views/SampleDetailsView.java`

- Extends `Composite` or is a CDI bean holding a `Composite`.

**Layout:** `GridLayout(1, false)` containing:
1. **Controls bar** (`Composite` with `RowLayout`):
   - Zoom: SWT `Combo` with predefined zoom values {"0.5", "1.0", "2.0", "4.0", "8.0", "16.0"}. `VerifyListener` for custom values. Selection fires `OnSamplePreviewModified`.
   - Resolution: SWT `Spinner` (range 1–784, default 28).
   - Overlap: SWT `Spinner` with `setDigits(2)` (range 0–95 representing 0.00–0.95).

2. **Preview split** (`SashForm`, vertical):
   - Top: `ScrolledComposite` → `ImageCanvas` for original sample preview.
   - Bottom: `ScrolledComposite` → `ImageCanvas` for sub-sampled preview.

3. **Metadata panel:**
   - SWT `Browser` widget for HTML metadata display (replaces `JEditorPane`).
   - Alternative: `StyledText` if HTML rendering is not needed (simpler, no embedded browser).

**Audio playback:**
- Port `PlayAudioSampleMouseAdapter` to SWT `MouseListener` on the preview canvas.
- Same `javax.sound.sampled` API for audio (not Swing-dependent).

**Sample preview update:**
- Virtual thread + `display.asyncExec()` replaces `SwingWorker`.
- `setSamplePreview(Sample, float zoom, int resolution, float overlap)` triggers background computation and UI update.

**Acceptance:** Sample details composite shows sample image, sub-sampled preview, metadata HTML, and plays audio on click.

---

### P4-2: Create `WeightsDetailsComposite` (replaces `WeightsDetailsTabbedPane`)

**New file:** `gui/views/WeightsDetailsComposite.java`
**Retires:** `gui/views/WeightsDetailsTabbedPane.java`

**Layout:** `SashForm(SWT.VERTICAL)` containing:

1. **Top pane — Weights visualization:**
   - `Composite` with `GridLayout(2, false)`:
     - Left: Controls — zoom `Combo`, gamma `Scale` (integer 0–200 mapped to float 0.0–2.0).
     - Right: `ScrolledComposite` → `ImageCanvas` for weights heatmap.
   - Gamma change listener: recompute heatmap via `VisualizationService`, convert `BufferedImage` → SWT `Image` via `SwtImages.toSwtImage()`, update canvas.

2. **Bottom pane — Weights table:**
   - `TableViewer` (JFace) with dynamic columns.
   - Content provider: `ArrayContentProvider`.
   - Columns created dynamically based on layer weights dimensions.
   - Replaces `DefaultTableModel` + `JTable`.

**Acceptance:** Weights composite shows heatmap with zoom/gamma controls and scrollable weights table.

---

### P4-3: Create `NewMultiLayerNetworkDialog` (replaces `NewMultiLayerNetworkPanel`)

**New file:** `gui/views/NewMultiLayerNetworkDialog.java`
**Retires:** `gui/views/NewMultiLayerNetworkPanel.java`

- Extends `org.eclipse.jface.dialogs.TitleAreaDialog`.
- `createDialogArea(Composite parent)`:
  - `GridLayout(2, false)` — label + field pairs.
  - Name: SWT `Text`.
  - Template: `ComboViewer` with `MultiLayerNetworkTemplate` items from `Repository`.
- `okPressed()`:
  - Validate: name non-empty, template selected.
  - On validation failure: `setErrorMessage("...")` (JFace built-in).
  - On success: build `Parameters` record, call consumer, close.
- No more validation loop — `TitleAreaDialog` handles this natively.

**Acceptance:** Dialog opens, validates input, creates network on OK.

---

### P4-4: Create `NewMultiLayerNetworkTemplateDialog` (replaces `NewMultiLayerNetworkTemplatePanel`)

**New file:** `gui/views/NewMultiLayerNetworkTemplateDialog.java`
**Retires:** `gui/views/NewMultiLayerNetworkTemplatePanel.java`

- Extends `TitleAreaDialog`.
- `createDialogArea(Composite parent)`:
  - `GridLayout(2, false)`.
  - Name: `Text`.
  - Hidden layers count: `Spinner` (1–100).
  - Layer size: `Spinner` (1–10000).
  - Bias: `Text` with float validation (or `Spinner` with `setDigits()`).
  - Activation function: `ComboViewer` with `ActivationFunction.values()`.
  - Loss function: `ComboViewer` with `LossFunction.values()`.
- `okPressed()`: validate, build `Parameters`, call consumer.

**Acceptance:** Dialog creates network templates with validated parameters.

---

### P4-5: Create `RandomizeTrainingDataDialog` (replaces `RandomizeTrainingDataPanel`)

**New file:** `gui/views/RandomizeTrainingDataDialog.java`
**Retires:** `gui/views/RandomizeTrainingDataPanel.java`

- Extends `TitleAreaDialog`.
- Fields: name (`Text`), training size (`Spinner`), dev-test size (`Spinner`), data selector (`ComboViewer`).
- `okPressed()`: validate sizes > 0, name non-empty, build `Parameters`.

**Acceptance:** Dialog creates randomized training data splits.

---

### P4-6: Create `ImportImagesDialog` (replaces `ImportImagesPanel`)

**New file:** `gui/views/ImportImagesDialog.java`
**Retires:** `gui/views/ImportImagesPanel.java`

- Extends `TitleAreaDialog`.
- Two file path rows, each with `Text` (read-only) + `Button` ("...") that opens SWT `FileDialog`.
- `okPressed()`: validate both files exist.

**Acceptance:** Dialog selects image and label files for MNIST import.

---

### P4-7: Integrate detail composites into `MainShell`

**Source file:** `gui/views/MainShell.java`

- In the right pane of the `SashForm`:
  - Create `CTabFolder` with two tabs:
    - Tab 1: `SampleDetailsComposite` (from P4-1)
    - Tab 2: `WeightsDetailsComposite` (from P4-2)
- Wire `@Observes OnModelNodeSelected`:
  - `SampleNode` / `SampleNameNode` selection → switch to Sample Details tab.
  - `MultiLayerNetworkWeightsNode` selection → switch to Weights Details tab.
- Wire `@Observes OnSampleSelected` → update `SampleDetailsComposite`.
- Wire `@Observes OnMultiLayerNetworkWeightsPreviewModified` → update `WeightsDetailsComposite`.

**Acceptance:** Detail tabs switch and update based on tree selection. Sample preview and weights heatmap display correctly.

---

## Phase 5: Services Cleanup

### P5-1: Rewrite `ControlsService` for SWT

**Source file:** `gui/services/ControlsService.java`

- Remove all `javax.swing.*` imports.
- `actionButton(String title, Image icon, Runnable action)`:
  - Creates SWT `Button(parent, SWT.PUSH)`, sets text + image, adds `SelectionListener.widgetSelectedAdapter(e -> action.run())`.
  - Needs a `Composite parent` parameter (SWT widgets require parents).
- `actionMenuItem(String title, Image icon, Runnable action)`:
  - Replace with JFace `Action` subclass or `MenuItem` creation helper.
- `validationColor(boolean valid)`:
  - Return SWT `Color` via `ResourceManager` (or return `RGB` and let caller create `Color`).
- `onlyNumbersKeyListener()`:
  - Replace with SWT `VerifyListener` that checks `event.text` characters.
- `toTitlePart()`: unchanged (pure logic).
- Remove `silenceListModelListenersDuringRunnable()` (Swing-specific, no longer needed).

**Acceptance:** No `javax.swing` imports in `ControlsService`. All callers updated.

---

### P5-2: Rewrite `FilesService` for SWT

**Source file:** `gui/services/FilesService.java`

- Replace `JFileChooser` with SWT `FileDialog`:
  ```java
  FileDialog dialog = new FileDialog(shell, isSave ? SWT.SAVE : SWT.OPEN);
  dialog.setFilterExtensions(new String[] { "*.yaprnn-mln" });
  dialog.setFilterNames(new String[] { "Multilayer Network" });
  String path = dialog.open();
  if (path != null) { pathConsumer.accept(path); }
  ```
- Remove `FileNameExtensionFilter` usage.
- Remove `chooseFileButton()` (replaced by dialog button pattern in P4-6 and similar).
- Keep `ensureEndsWith()` (pure logic).

**Acceptance:** File dialogs use native SWT FileDialog. No `javax.swing.JFileChooser`.

---

### P5-3: Rewrite `DialogsService` for SWT (implement `Dialogs` interface)

**Source file:** `support/swing/DialogsService.java`
**New file:** `support/swt/SwtDialogsService.java`

- Implements `Dialogs` interface (from P0-6).
- `showError(Object parent, String title, Throwable t)`:
  - `MessageDialog.openError((Shell) parent, title, t.getMessage())` + `log.log(Level.SEVERE, ...)`.
- `showFinished(Object parent, String title)`:
  - `MessageDialog.openInformation((Shell) parent, title, "Finished")`.
- `confirm(Object parent, String title, String message)`:
  - `MessageDialog.openConfirm((Shell) parent, title, message)`.
- Register as `@Singleton` CDI bean. Remove old `DialogsService` from `support/swing/`.

**Acceptance:** All dialogs render as native OS dialogs via JFace MessageDialog.

---

### P5-4: Rewrite `IconsService` — retire Swing version

**Source files:**
- `gui/services/IconsService.java` (Swing, to retire)
- `gui/services/SwtIconsService.java` (from P1-4, becomes the primary)

- Rename `SwtIconsService` → `IconsService` (or merge into existing file).
- Replace all `Icon` / `ImageIcon` constants with SWT `ImageDescriptor` constants.
- Update all callers to use SWT `Image` (via `LocalResourceManager`).
- Delete the old Swing `IconsService`.

**Acceptance:** No `javax.swing.ImageIcon` references. All icons are SWT Images.

---

### P5-5: Adapt `VisualizationService` for SWT image output

**Source file:** `gui/services/VisualizationService.java`

- Currently produces `java.awt.Image` (via `BufferedImage`).
- Option A: Keep internal `BufferedImage` computation, add `toSwtImage(Display)` conversion at the boundary using `SwtImages.toSwtImage()`.
- Option B: Rewrite pixel manipulation to use SWT `ImageData` directly (construct `PaletteData`, set pixel values).
- **Recommended:** Option A for lower risk. The heatmap/gamma math stays in AWT land; only the final output converts.
- Update `fromWeights()` and `fromOutput()` return types to SWT `Image` (or provide overloads).
- Replace `DefaultTableModel classificationTableModel()` return type with a plain `Object[][]` + `String[]` column headers (toolkit-agnostic). The `TableViewer` in `ClassifyShell` consumes this directly.

**Acceptance:** Visualization outputs SWT Images. No `DefaultTableModel` usage remains.

---

### P5-6: Retire `NetworksControlsService`

**Source file:** `gui/services/NetworksControlsService.java`

- `networksTree()` — replaced by `NetworksTreeViewerFactory` (P2-6).
- `valuesTable()` — replaced by `TableViewer` creation in `WeightsDetailsComposite` and `ClassifyShell`.
- `gammaSlider()` — replaced by SWT `Scale` creation in `WeightsDetailsComposite`.
- `*SpinnerNumberModel()` methods — replaced by direct SWT `Spinner.setValues()` calls at the call site.
- Delete the file.

**Acceptance:** File deleted. No compilation errors.

---

### P5-7: Rewrite `ZoomControlsService` for SWT

**Source file:** `gui/services/ZoomControlsService.java`

- Replace `JComboBox` creation with SWT `Combo`:
  ```java
  Combo zoomCombo = new Combo(parent, SWT.DROP_DOWN);
  zoomCombo.setItems(ZOOM_OPTIONS);
  zoomCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { ... }));
  ```
- Or use `ComboViewer` if data binding is desired.
- Keep zoom value parsing and error handling logic.

**Acceptance:** Zoom combo is an SWT widget. No Swing imports.

---

### P5-8: Rewrite `SampleControlsService` for SWT

**Source file:** `gui/services/SampleControlsService.java`

- `sampleMetaEditorPane()` → returns SWT `Browser` or `StyledText` widget.
- `resolutionSpinner()` → returns SWT `Spinner(parent, SWT.BORDER)` with `setValues(28, 1, 784, 0, 1, 10)`.
- `overlapSpinner()` → returns SWT `Spinner` with `setDigits(2)` and range 0–95 (representing 0.00–0.95).

**Acceptance:** Sample controls are SWT widgets. No Swing imports.

---

### P5-9: Rewrite combo services (`ActivationFunctionControlsService`, `LossFunctionControlsService`, `DataSelectorControlsService`)

**Source files:** 3 service files in `gui/services/`

- Each currently creates a `JComboBox<T>` with the enum/interface values.
- Replace with `ComboViewer` creation:
  ```java
  ComboViewer viewer = new ComboViewer(parent, SWT.READ_ONLY);
  viewer.setContentProvider(ArrayContentProvider.getInstance());
  viewer.setLabelProvider(new LabelProvider() { getText... });
  viewer.setInput(ActivationFunction.values());
  ```
- Or consolidate into a single generic `EnumComboViewerFactory<T>`.

**Acceptance:** All combo services produce SWT `ComboViewer`. No Swing imports.

---

### P5-10: Rewrite `SamplesService` for SWT

**Source file:** `gui/services/SamplesService.java`

- `importAudioMenuItem()` / `importImagesMenuItem()` — currently return `JMenuItem`.
- Replace with methods that return JFace `Action` objects (used by `MenuManager`):
  ```java
  public Action importImagesAction(Shell parent, Consumer<List<Sample>> callback) { ... }
  ```
- `from(Sample, float zoom)` — currently returns `java.awt.Image`. Change to return SWT `Image` via `SwtImages.toSwtImage()` (or `ImageData`).
- `subSample()` — pure logic, no Swing dependency. Keep as-is.

**Acceptance:** No `javax.swing.JMenuItem` or `java.awt.Image` usage. Returns JFace Actions and SWT Images.

---

### P5-11: Retire `support/swing/` package

**Delete files:**
- `support/swing/ImagePanel.java` (replaced by `support/swt/ImageCanvas.java` from P1-5)
- `support/swing/Images.java` (replaced by `support/swt/SwtImages.java` from P1-6)
- `support/swing/DialogsService.java` (replaced by `support/swt/SwtDialogsService.java` from P5-3)

**Acceptance:** `support/swing/` directory is empty and deleted. No Swing utility classes remain.

---

## Phase 6: Remove Swing Dependencies

### P6-1: Remove all `javax.swing.*` and `java.awt.*` imports

**All files in `gui/`, `support/`**

- Run a project-wide search for `javax.swing` and `java.awt` imports.
- Remove all occurrences.
- Exception: `java.awt.image.BufferedImage` may still be used internally by `VisualizationService` if Option A was chosen in P5-5. If so, this is acceptable as an internal implementation detail (no AWT widgets on screen).
- Fix any remaining compilation errors from missed references.

**Acceptance:** `grep -r "javax.swing" src/` returns zero results. `grep -r "java.awt" src/` returns only `BufferedImage` (if applicable).

---

### P6-2: Update `build.gradle` — remove Swing, finalize SWT

**Source file:** `build.gradle`

- Remove JFreeChart Swing dependency if fully replaced. If JFreeChart is still used for chart math, keep the core dependency but remove Swing-specific modules.
- Verify SWT platform detection works correctly.
- Add a comment documenting the SWT platform selection logic.
- Update `mainClass` if changed in P1-2 (may still be Weld SE `StartMain`).
- Verify `./gradlew build`, `./gradlew run`, `./gradlew shadowJar` all work.
- The shadow JAR must include the correct platform-specific SWT native libraries.

**Acceptance:** Clean build. Application runs from `./gradlew run` and from fat JAR.

---

### P6-3: Retire `MainFrame.java` and all remaining Swing frame files

**Delete files:**
- `gui/views/MainFrame.java`
- `gui/views/TrainingFrame.java`
- `gui/views/ClassifyFrame.java`
- `gui/views/SamplePreprocessingFrame.java`
- `gui/views/SampleDetailsView.java`
- `gui/views/WeightsDetailsTabbedPane.java`
- `gui/views/NewMultiLayerNetworkPanel.java`
- `gui/views/NewMultiLayerNetworkTemplatePanel.java`
- `gui/views/RandomizeTrainingDataPanel.java`
- `gui/views/ImportImagesPanel.java`

**Acceptance:** No Swing view classes remain. Build succeeds.

---

### P6-4: Update `ModelNode` — remove `javax.swing.Icon` dependency

**Source file:** `gui/model/nodes/ModelNode.java`

- Remove `getIcon()` method (returns `javax.swing.Icon`).
- Keep `getIconResourcePath()` (added in P0-8) as the primary icon API.
- Update `DefaultNode`:
  - Remove `Supplier<Icon> iconSupplier` and `Icon icon` fields.
  - Keep `Supplier<String> iconResourcePathSupplier` and `String iconResourcePath`.
- Verify `NetworksTreeLabelProvider` (P2-3) still works with `getIconResourcePath()`.

**Acceptance:** `ModelNode` and `DefaultNode` have zero Swing imports.

---

### P6-5: Update documentation

**Files:**
- `CLAUDE.md` — update "GUI" references: Swing → SWT/JFace. Update the project structure description. Update key patterns section.
- `.claude/rules/gui.md` — rewrite:
  ```markdown
  # GUI Conventions
  - Framework: SWT + JFace (not Swing, not JavaFX)
  - Use CDI events for communication between composites
  - Tree nodes implement `ModelNode` sealed interface with lazy-loading suppliers
  - Tree displayed via JFace `TreeViewer` with `ITreeContentProvider` + `ILabelProvider`
  - Inline editing via JFace `EditingSupport` (not custom cell editors)
  - File dialogs via SWT `FileDialog`; message dialogs via JFace `MessageDialog`
  - Image rendering via `ImageCanvas` (SWT Canvas subclass) and `SwtImages` utility
  - Resource management: all SWT Image/Color/Font via JFace `LocalResourceManager`
  - Threading: `Display.asyncExec()` for UI updates from background threads
  ```

**Acceptance:** Documentation reflects SWT/JFace architecture. No Swing references in docs.

---

### P6-6: Final verification

- Run `./gradlew build` — clean compile, all tests pass.
- Run `./gradlew run` — application launches, all features work:
  - [ ] Tree navigation and inline editing (activation function, loss function, bias, layer size, data selector, names)
  - [ ] Add/remove networks, templates, training data, layers, samples
  - [ ] File menu: load/save all formats, import images, import audio, setup digits scenario
  - [ ] Sample details: preview image, sub-sampled preview, metadata HTML, audio playback
  - [ ] Weights details: heatmap with zoom/gamma, weights table
  - [ ] Training: hyperparameter config, start/stop, live chart
  - [ ] Classification: sample selection, network selection, layer output table, reconstruction image
  - [ ] Sample preprocessing: sample selection, process, tree update
  - [ ] Keyboard shortcuts: Delete key removes selected node
  - [ ] Window management: secondary shells open/close independently
- Run `./gradlew shadowJar` — fat JAR includes SWT natives, runs standalone.
- Verify on target platform (Windows). Note: Linux/macOS require different SWT JARs.

**Acceptance:** Full manual smoke test passes. No regressions.
