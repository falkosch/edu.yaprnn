---
paths:
  - "src/**/gui/**/*.java"
---

# GUI Conventions

- Framework: SWT/JFace (migrated from Swing)
- Use CDI events for communication between shells/composites (not direct references)
- Tree nodes implement `ModelNode` sealed interface with lazy-loading suppliers
- JFace `TreeViewer` with `NetworksTreeContentProvider` and `NetworksTreeLabelProvider`
- SWT `Shell` for top-level windows, `Composite` for embedded views
- Icons via `SwtIconsService` (loads from resource paths, caches per Display)
- AWT `BufferedImage` still used for image generation; convert to SWT via `SwtImages.toSwtImageData()`
- Modal dialogs: use `setVisible(false)` in OK handler, read widget values, then `dispose()` in finally block (not `close()` which disposes widgets before values can be read)
- Audio playback on image click: `javax.sound.sampled.Clip` with `AtomicReference` for stop/restart lifecycle
- Threading: virtual threads + `Display.asyncExec()` for UI updates (not `SwingWorker`)
