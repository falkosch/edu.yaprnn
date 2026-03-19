---
paths:
  - "src/**/gui/**/*.java"
---

# GUI Conventions

- Framework: Swing (not JavaFX)
- Use CDI events for communication between panels (not direct references)
- Tree nodes implement `ModelNode` sealed interface with lazy-loading suppliers
- File dialogs via `FilesService`; persistence via `PersistenceService`
- Image rendering via `ImagePanel` and `Images` utility
