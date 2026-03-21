# Swing to SWT (+JFace) Migration Plan

## 1. Why Migrate?

SWT provides native OS widgets (crisper look, better accessibility, platform-consistent behavior).
JFace adds structured viewers, data binding, and dialogs on top of SWT — useful for the tree, tables, and preference pages.

## 2. Current State Summary

| Aspect | Current (Swing) | Count |
|--------|-----------------|-------|
| Windows (JFrame) | MainFrame, TrainingFrame, ClassifyFrame, SamplePreprocessingFrame | 4 |
| Tree | JTree + TreeModel + TreeCellRenderer + TreeCellEditor (9 editor classes) | 1 tree, 12 node types |
| Tables | JTable (weights, classification) | 2 |
| Layouts | GroupLayout, BorderLayout | ~8 usages |
| Dialogs | JOptionPane, JFileChooser | ~6 usages |
| Custom painting | ImagePanel (centered image on JPanel) | 1 |
| Charting | JFreeChart ChartPanel | 1 (TrainingFrame) |
| Toolbars/Menus | JToolBar, JMenuBar, JMenu, JMenuItem | 3 toolbars, 1 menu bar |
| Controls | JComboBox, JSpinner, JSlider, JButton, JLabel, JTextField, JEditorPane | ~40 instances |
| Threading | SwingWorker, EventQueue.invokeLater, SwingUtilities.invokeLater | ~5 usages |
| CDI integration | @Inject, @Observes, @Singleton, @Produces, Instance<T> | Pervasive |

## 3. SWT/JFace Equivalents

| Swing | SWT/JFace Equivalent | Notes |
|-------|----------------------|-------|
| `JFrame` | `Shell` (SWT) / `ApplicationWindow` (JFace) | JFace adds menu/toolbar/status bar lifecycle |
| `JPanel` | `Composite` | Base container |
| `JTree` + `TreeModel` | `TreeViewer` (JFace) | ContentProvider + LabelProvider replaces model+renderer |
| `TreeCellEditor` | `TreeViewer.setColumnProperties()` + `EditingSupport` | JFace editing support replaces 9 editor classes |
| `JTable` | `TableViewer` (JFace) | ContentProvider + LabelProvider |
| `GroupLayout` | `GridLayout` / `FormLayout` (SWT) | SWT has no GroupLayout; GridLayout is the closest |
| `BorderLayout` | `FillLayout` / `GridLayout` with grabExcess | |
| `JSplitPane` | `SashForm` | Equivalent, simpler API |
| `JTabbedPane` | `CTabFolder` / `TabFolder` | CTabFolder for closeable tabs |
| `JScrollPane` | `ScrolledComposite` | Or viewer's built-in scrolling |
| `JToolBar` | `ToolBar` (SWT) / `ToolBarManager` (JFace) | JFace manages contribution items |
| `JMenuBar` | `Menu` (SWT) / `MenuManager` (JFace) | JFace preferred |
| `JComboBox` | `Combo` (SWT) / `ComboViewer` (JFace) | ComboViewer for model-backed combos |
| `JSpinner` | `Spinner` (SWT) | Integer-only; for float, use `Text` + validation |
| `JSlider` | `Scale` (SWT) | Integer-based, needs scaling for float values |
| `JButton` | `Button` (SWT) | SWT.PUSH style |
| `JLabel` | `Label` (SWT) | |
| `JTextField` | `Text` (SWT) | |
| `JEditorPane` (HTML) | `Browser` (SWT) | Or `StyledText` for simpler markup |
| `JFileChooser` | `FileDialog` (SWT) | Native file dialog |
| `JOptionPane` | `MessageDialog` (JFace) | Or `MessageBox` (SWT) |
| `ImageIcon` | `Image` (SWT) | Must be explicitly disposed |
| `SwingWorker` | `Job` (Eclipse Jobs) or `Display.asyncExec` | See threading section |
| `EventQueue.invokeLater` | `Display.asyncExec()` | |
| `DefaultComboBoxModel` | `ComboViewer` + `IStructuredContentProvider` | JFace viewer pattern |
| `SpinnerNumberModel` | `Spinner.setValues()` or custom `Text` | |

## 4. Key Architecture Decisions

### 4.1 Keep CDI (Weld SE)
CDI injection and events are **not** Swing-specific. Keep `@Inject`, `@Observes`, `@Singleton`, `Instance<T>`, and all event routing unchanged. SWT widgets will be injected/created the same way.

### 4.2 JFace for Viewers, SWT for Simple Widgets
- **Use JFace** for: TreeViewer (replaces JTree+model+renderer+editor), TableViewer, ComboViewer, ApplicationWindow, MenuManager, ToolBarManager, dialogs (MessageDialog, InputDialog), EditingSupport
- **Use raw SWT** for: Composite, layouts, Button, Label, Text, Scale, Spinner, Canvas, SashForm, CTabFolder

### 4.3 Display/Event Loop
Replace `EventQueue` with SWT `Display`. The main loop changes from Swing's implicit pump to:
```java
Display display = new Display();
Shell shell = new Shell(display);
// ... build UI ...
shell.open();
while (!shell.isDisposed()) {
    if (!display.readAndDispatch()) display.sleep();
}
display.dispose();
```

Weld SE bootstrap must create the Display first, then fire initialization.

### 4.4 Resource Management
SWT requires explicit disposal of `Image`, `Color`, `Font`. Introduce a `ResourceManager` (JFace provides `LocalResourceManager` / `JFaceResources`) to handle lifecycle. This replaces the current `IconsService` pattern where `ImageIcon` is GC'd automatically.

### 4.5 Threading Model
- Replace `SwingWorker` with SWT `Job` or plain virtual threads + `Display.asyncExec()`
- Replace `SwingUtilities.invokeLater()` with `Display.asyncExec()`
- The caller-contributes parallelism pattern in `MultiLayerNetwork` is UI-agnostic and stays unchanged

### 4.6 Charting
JFreeChart has an SWT module (`jfreechart-swt`). Replace `ChartPanel` (Swing) with `ChartComposite` (SWT). Alternatively, consider Eclipse BIRT Chart Engine or SWTChart, but JFreeChart-SWT is the lowest-risk option.

## 5. Migration Phases

### Phase 0: Preparation (Non-Breaking)

**Goal**: Create abstraction seams so Swing and SWT can coexist during migration.

1. **Extract UI-agnostic interfaces** from services that currently return Swing types:
   - `ControlsService` — split into toolkit-agnostic logic (validation, title formatting) and Swing-specific widget creation
   - `FilesService` — extract `FileSelection` interface (path + filter), implement for Swing and later SWT
   - `DialogsService` — extract `Dialogs` interface with `showError(String title, Throwable)`, `showConfirm(String title, String message)`
   - `IconsService` — extract icon keys/paths; defer actual `Image`/`ImageIcon` creation to toolkit layer

2. **Extract view interfaces** for frames/panels that other code references:
   - `SamplePreviewConsumer` (zoom, resolution, overlap)
   - `SelectionConsumer<T>` for sample/network/training-data selection updates
   - MainFrame's CDI `@Observes` handlers already provide this naturally

3. **Move non-UI logic out of frame classes**:
   - Training orchestration (currently in `TrainingWorker` inner class) into a standalone `TrainingService`
   - Classification logic into `ClassificationService`
   - Scenario setup into `ScenarioService`

4. **Add SWT/JFace dependencies** to `build.gradle`:
   ```groovy
   ext {
       swtVersion = '3.128.0'  // Eclipse 2025-03
   }
   // Platform-specific SWT (use Gradle variant selection or classifier)
   implementation "org.eclipse.platform:org.eclipse.swt.win32.win32.x86_64:${swtVersion}"
   implementation "org.eclipse.platform:org.eclipse.jface:${swtVersion}"
   ```

**Files changed**: ~6 service files split, ~3 new interfaces, `build.gradle`
**Risk**: Low — pure refactoring, no UI changes visible

---

### Phase 1: Foundation — Display, Shell, Event Loop

**Goal**: Boot into an SWT Shell instead of a Swing JFrame.

1. **Create `SwtApplication`** — replaces the implicit `ContainerInitialized -> MainFrame.start()` flow:
   - Create `Display` before Weld initialization (or as an eager CDI `@Singleton`)
   - Produce `Display` and root `Shell` as CDI beans
   - Run SWT event loop after container initialization
   - Dispose display on shutdown

2. **Create `MainShell`** (replaces `MainFrame`):
   - `Shell` with `FillLayout` or `GridLayout`
   - Menu bar via `MenuManager` (JFace)
   - `ToolBarManager` for toolbar
   - `SashForm` for tree | details split
   - `CTabFolder` for details tabs

3. **Migrate `ImagePanel`** to SWT `Canvas`:
   - Override `paintControl(PaintEvent)` to draw `Image`
   - Handle resize events for layout
   - Manage SWT `Image` lifecycle via `LocalResourceManager`

4. **Migrate `IconsService`**:
   - Load PNG resources into SWT `Image` (via `ImageDescriptor.createFromURL()`)
   - Register with `LocalResourceManager` for auto-disposal
   - Expose as `ImageDescriptor` for JFace viewers

5. **Update bootstrap** in `build.gradle`:
   - Change `mainClass` to new SWT entry point (or keep Weld SE and observe a different init event)

**Files changed**: ~5 new files, `MainFrame.java` retired, `build.gradle`
**Risk**: Medium — fundamental UI bootstrap change; must verify CDI lifecycle still works

---

### Phase 2: Tree Viewer (Largest Single Migration)

**Goal**: Replace JTree + TreeModel + 1 renderer + 9 editors with JFace TreeViewer.

1. **Create `NetworksTreeContentProvider`** (implements `ITreeContentProvider`):
   - `getElements(Object)` → root children (same as `NetworksTreeModel.getChild(root, i)`)
   - `getChildren(Object)` → delegates to `ModelNode.getChildren()`
   - `getParent(Object)` → needs parent tracking (add to `DefaultNode` or use viewer's internal map)
   - `hasChildren(Object)` → delegates to `ModelNode` suppliers

2. **Create `NetworksTreeLabelProvider`** (implements `ILabelProvider` or `DelegatingStyledCellLabelProvider`):
   - `getText(Object)` → `ModelNode.getLabel()`
   - `getImage(Object)` → `ModelNode.getIcon()` mapped to SWT `Image`

3. **Create `EditingSupport` subclasses** (replaces 9 TreeCellEditor classes):
   - `ActivationFunctionEditingSupport` — `ComboBoxCellEditor` with activation function items
   - `BiasEditingSupport` — `TextCellEditor` with float validation
   - `LayerSizeEditingSupport` — `TextCellEditor` with int validation
   - `LossFunctionEditingSupport` — `ComboBoxCellEditor`
   - `DataSelectorEditingSupport` — `ComboBoxCellEditor`
   - Consolidate: many editors are just "pick from enum" → a single generic `EnumEditingSupport<T>`

4. **Create `NetworksTreeViewerFactory`** (replaces `NetworksControlsService.networksTree()`):
   - Creates `TreeViewer`, sets providers, attaches editing support per column
   - Wires selection listener → fires `OnModelNodeSelected` via CDI event

5. **Wire tree refresh**:
   - `NetworksTreeModel.treeStructureChanged()` → `treeViewer.refresh()` or `treeViewer.refresh(element)`
   - Use `Display.asyncExec()` if model changes come from non-UI threads

6. **Retire**:
   - `NetworksTreeModel` (replaced by content provider)
   - `NetworksTreeCellRenderer` (replaced by label provider)
   - All 9 `*TreeCellEditor` classes (replaced by ~3 editing support classes)
   - `SelectableTreeCellEditor`, `SelectableTreeCellEditorWithComponent`

**Files changed**: ~5 new, ~12 retired
**Risk**: High — the tree is the central UI element; extensive testing needed

---

### Phase 3: Secondary Windows

**Goal**: Migrate TrainingFrame, ClassifyFrame, SamplePreprocessingFrame.

#### 3a. TrainingFrame → TrainingShell
1. Replace `JFrame` with `Shell` (SWT.SHELL_TRIM)
2. Replace `GroupLayout` with `GridLayout` (SWT) for preferences panel
3. Replace `JComboBox<T>` with `ComboViewer` (JFace) for training data / network selection
4. Replace `JSpinner` with `Spinner` (SWT) for integer values; `Text` + `VerifyListener` for float values
5. Replace `ChartPanel` (Swing) with JFreeChart's `ChartComposite` (SWT) — requires `jfreechart-swt` dependency
6. Replace `SwingWorker` with virtual threads + `Display.asyncExec()` for progress updates
7. Replace `WindowAdapter` with `ShellListener`

#### 3b. ClassifyFrame → ClassifyShell
1. Same JFrame→Shell pattern
2. Replace `JTable` with `TableViewer` (JFace) for classification results
3. Replace `ImagePanel` with SWT Canvas (from Phase 1)

#### 3c. SamplePreprocessingFrame → SamplePreprocessingShell
1. Same JFrame→Shell pattern
2. Simpler — fewer controls

**Files changed**: 3 frames → 3 shells
**Risk**: Medium — isolated windows, can be migrated independently

---

### Phase 4: Detail Views and Panels

**Goal**: Migrate tabbed detail panes and dialog panels.

1. **SampleDetailsView** → SWT Composite:
   - Replace `JEditorPane` (HTML metadata) with `Browser` widget or `StyledText`
   - Replace `ImagePanel` with Canvas (already done in Phase 1)
   - Replace `GroupLayout` with `GridLayout`

2. **WeightsDetailsTabbedPane** → SWT `CTabFolder`:
   - Weights heatmap → Canvas
   - Weights table → `TableViewer`
   - Gamma slider → `Scale` widget (integer-mapped)

3. **Dialog panels** (NewMultiLayerNetworkPanel, NewMultiLayerNetworkTemplatePanel, RandomizeTrainingDataPanel, ImportImagesPanel):
   - Replace `JOptionPane.showConfirmDialog` loop with JFace `TitleAreaDialog`
   - Form fields: `Text`, `Spinner`, `ComboViewer`
   - Validation: JFace `TitleAreaDialog.setErrorMessage()` replaces background-color validation

**Files changed**: ~8 panels → ~8 SWT composites/dialogs
**Risk**: Low-Medium — mostly mechanical widget replacement

---

### Phase 5: Services Cleanup

**Goal**: Remove all Swing references from service layer.

1. **`ControlsService`** → retire or rewrite as `SwtControlsService`:
   - `actionButton()` → creates SWT `Button` with `SelectionListener`
   - `actionMenuItem()` → JFace `Action` or `ContributionItem`
   - `validationColor()` → SWT `Color` (managed by `ResourceManager`)
   - `onlyNumbersKeyListener()` → SWT `VerifyListener`

2. **`FilesService`** → rewrite with SWT `FileDialog`:
   - `FileDialog` is simpler than `JFileChooser` (no custom filter object)
   - Extension filtering via `setFilterExtensions()`

3. **`DialogsService`** → rewrite with JFace `MessageDialog`:
   - `showError()` → `MessageDialog.openError()`
   - `showFinished()` → `MessageDialog.openInformation()`

4. **`NetworksControlsService`** → retire (absorbed into TreeViewer factory and widget factories)

5. **`VisualizationService`** → adapt image creation:
   - Currently creates `java.awt.image.BufferedImage`
   - Convert to SWT `ImageData` → `Image`
   - Or: keep `BufferedImage` internally, convert at display boundary

6. **`ZoomControlsService`, `SampleControlsService`, `ActivationFunctionControlsService`, etc.**:
   - Rewrite combo/spinner factories for SWT widgets

**Files changed**: ~10 services
**Risk**: Low — mechanical replacement

---

### Phase 6: Remove Swing Dependencies

1. Remove `javax.swing.*` imports from all files
2. Remove `java.awt.*` imports (except `java.awt.image.BufferedImage` if still used internally)
3. Remove JFreeChart Swing dependency (keep JFreeChart SWT)
4. Update `build.gradle`: remove implicit Swing classpath, ensure SWT platform JARs are correct for all target platforms
5. Update `CLAUDE.md` and `.claude/rules/gui.md` to reflect SWT/JFace

**Files changed**: All GUI files (import cleanup), `build.gradle`, docs
**Risk**: Low — compilation will catch any remaining references

---

## 6. Cross-Cutting Concerns

### 6.1 Platform-Specific SWT JARs
SWT is platform-specific. Use Gradle variant selection or a multi-platform dependency block:
```groovy
def swtPlatform = System.getProperty('os.name').toLowerCase().contains('win')
    ? 'org.eclipse.swt.win32.win32.x86_64'
    : System.getProperty('os.name').toLowerCase().contains('mac')
    ? 'org.eclipse.swt.cocoa.macosx.x86_64'
    : 'org.eclipse.swt.gtk.linux.x86_64'
implementation "org.eclipse.platform:${swtPlatform}:${swtVersion}"
```

### 6.2 SWT Resource Disposal
Every `Image`, `Color`, `Font` created via `new` must be disposed. Use JFace `LocalResourceManager` attached to the root shell — it auto-disposes when the shell is destroyed. Never cache SWT resources in static fields without disposal hooks.

### 6.3 CDI + SWT Lifecycle
Weld SE fires `ContainerInitialized` synchronously. The pattern becomes:
1. `@Observes ContainerInitialized` → build UI, open shell
2. Run SWT event loop (blocking)
3. After loop exits → Weld shutdown fires `@PreDestroy` on CDI beans → dispose remaining resources

### 6.4 JFreeChart SWT Integration
Add dependency:
```groovy
implementation "org.jfree:jfreechart-swt:${jfreechartVersion}"  // check availability
```
If `jfreechart-swt` is not available for 1.0.19, consider upgrading JFreeChart to 1.5.x which has better SWT support, or use JFreeChart's `BufferedImage` output rendered to an SWT Canvas.

### 6.5 Testing
GUI packages are already exempt from coverage requirements. No changes to test strategy needed. The non-UI services extracted in Phase 0 can be unit-tested independently.

## 7. Migration Order (Recommended)

```
Phase 0  ──── 1-2 weeks ──── Preparation (no UI changes)
Phase 1  ──── 1 week    ──── Foundation (Display, Shell, Canvas, Icons)
Phase 2  ──── 2 weeks   ──── Tree Viewer (biggest risk)
Phase 3  ──── 1 week    ──── Secondary Windows (parallel work possible)
Phase 4  ──── 1 week    ──── Detail Views and Panels
Phase 5  ──── 1 week    ──── Services Cleanup
Phase 6  ──── 1-2 days  ──── Remove Swing, update docs
```

Phases 3 and 4 can run in parallel after Phase 2.

## 8. Risk Summary

| Risk | Impact | Mitigation |
|------|--------|------------|
| JTree → TreeViewer is complex (editing, lazy-loading, selection events) | High | Migrate tree first; invest in manual testing |
| SWT platform JARs complicate cross-platform builds | Medium | Use Gradle variant selection; test on CI for all platforms |
| JFreeChart SWT support may be stale | Medium | Fallback: render chart to BufferedImage → SWT Canvas |
| CDI lifecycle conflicts with SWT event loop | Medium | Prototype in Phase 1; the pattern is well-known |
| Resource leaks (Image, Color, Font) | Medium | Use `LocalResourceManager` everywhere; add dispose checks |
| GroupLayout → GridLayout visual differences | Low | Accept minor layout changes; SWT GridLayout is flexible enough |

## 9. Files Inventory (What Changes Where)

### Retired (replaced entirely)
- `gui/views/MainFrame.java` → `MainShell`
- `gui/views/TrainingFrame.java` → `TrainingShell`
- `gui/views/ClassifyFrame.java` → `ClassifyShell`
- `gui/views/SamplePreprocessingFrame.java` → `SamplePreprocessingShell`
- `gui/model/NetworksTreeModel.java` → JFace `ITreeContentProvider`
- `gui/model/NetworksTreeCellRenderer.java` → JFace `ILabelProvider`
- `gui/model/editors/*.java` (9 files) → JFace `EditingSupport` (2-3 files)
- `support/swing/ImagePanel.java` → SWT `Canvas` subclass
- `support/swing/Images.java` → adapt for SWT `ImageData`
- `support/swing/DialogsService.java` → JFace `MessageDialog` wrapper

### Modified (service/model adaptations)
- `gui/services/ControlsService.java` → split + rewrite
- `gui/services/FilesService.java` → SWT `FileDialog`
- `gui/services/IconsService.java` → SWT `ImageDescriptor`
- `gui/services/NetworksControlsService.java` → retire/absorb
- `gui/services/VisualizationService.java` → SWT image conversion
- `gui/services/ZoomControlsService.java` → SWT widgets
- `gui/services/SampleControlsService.java` → SWT widgets
- `gui/services/ActivationFunctionControlsService.java` → SWT ComboViewer
- `gui/services/LossFunctionControlsService.java` → SWT ComboViewer
- `gui/services/DataSelectorControlsService.java` → SWT ComboViewer
- `gui/views/SampleDetailsView.java` → SWT Composite
- `gui/views/WeightsDetailsTabbedPane.java` → CTabFolder
- `gui/views/New*.java`, `RandomizeTrainingDataPanel.java`, `ImportImagesPanel.java` → JFace dialogs

### Unchanged (non-GUI)
- `events/` — all CDI event routing stays
- `model/` — Repository pattern stays
- `networks/` — core MLN stays
- `samples/` — sample types stay
- `training/` — training data stays
- `gui/model/nodes/` — ModelNode hierarchy stays (label/icon suppliers become JFace-compatible)
- `gui/views/mappings/` — MapStruct mappers stay
- `gui/views/di/` — CDI qualifiers updated (type changes from JTree to TreeViewer)

## 10. JFace Value Add

JFace is worth adopting for:
- **TreeViewer/TableViewer**: Content+label provider pattern is cleaner than Swing's model+renderer+editor split. EditingSupport consolidates 9 editor classes into ~3.
- **Dialogs**: `TitleAreaDialog` with built-in validation > `JOptionPane` loop pattern.
- **MenuManager/ToolBarManager**: Declarative contribution model, cleaner than manual `JMenu.add()`.
- **ResourceManager**: Solves SWT's manual disposal problem elegantly.

JFace is **not** needed for:
- Simple labels, buttons, text fields — raw SWT is fine.
- Layouts — SWT layouts are used directly regardless.
- The SWT event loop — that's pure SWT `Display`.
