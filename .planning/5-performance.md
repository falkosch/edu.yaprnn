# Performance Issues

## P1: VisualizationService.fromOutput and fromWeights use per-pixel setRGB

**File**: `src/main/java/edu/yaprnn/gui/services/VisualizationService.java:76-80, 102-109`
**Category**: Performance

**Problem**: Both `fromOutput` and `fromWeights` call `image.setRGB(x, y, ...)` per pixel. For large weight matrices, this is slow because each `setRGB` call involves coordinate validation and color model conversion.

**Fix**: Use `BufferedImage.setRGB(x, y, w, h, rgbArray, offset, scansize)` or write to the underlying `DataBufferInt` directly:

```java
var raster = image.getRaster();
var dataBuffer = (DataBufferInt) raster.getDataBuffer();
var pixels = dataBuffer.getData();
// Then write pixels[y * width + x] = rgb directly
```

## P2: NetworksTreeModel fires treeStructureChanged for every modification

**File**: `src/main/java/edu/yaprnn/gui/model/NetworksTreeModel.java:131`
**Category**: Performance

**Problem**: Every add/remove/refresh calls `fireStructureChanged`, which causes the entire JTree to re-layout. For batch operations (e.g., importing hundreds of MNIST images), this fires per-batch, causing multiple full tree rebuilds.

**Fix**: For bulk operations, consider batching notifications or using `treeNodesInserted`/`treeNodesRemoved` for more targeted updates that preserve tree expansion state.

## P3: Repository.removeSamples calls samplesGroupedByName.remove per sample

**File**: `src/main/java/edu/yaprnn/model/Repository.java:79`
**Category**: Performance

**Problem**: `removeSamples` iterates the collection twice: once for the name index removal, once for the list removal. For large sample sets, `list.removeAll(items)` is O(n*m) with ArrayList.

**Fix**: For large removals, consider converting `items` to a Set first, or using `removeIf`:

```java
public void removeSamples(Collection<? extends Sample> items) {
    var toRemove = Set.copyOf(items);
    toRemove.forEach(sample -> samplesGroupedByName.remove(sample.getName(), sample));
    samples.removeAll(toRemove);
    onRepositoryElementsRemovedRouter.fireEvent(Sample.class, List.copyOf(items));
}
```

## P4: IconsService mixes static and instance icon loading

**File**: `src/main/java/edu/yaprnn/gui/services/IconsService.java:15-69`
**Category**: Performance

**Problem**: Static fields (lines 15-40) load resources at class-load time, while instance fields (lines 42-69) load at CDI bean creation. The static icons are created eagerly even if the class is referenced for type checks only. This also means any missing resource causes class initialization failure.

**Fix**: Unify to instance fields loaded in `@PostConstruct`, or make all constants static with lazy initialization.
