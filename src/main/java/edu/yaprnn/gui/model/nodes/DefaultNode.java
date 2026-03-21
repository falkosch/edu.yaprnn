package edu.yaprnn.gui.model.nodes;

import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public non-sealed class DefaultNode implements ModelNode {

  private final Supplier<String> iconResourcePathSupplier;
  private final Supplier<String> labelSupplier;
  private final Supplier<List<? extends ModelNode>> childrenSupplier;

  @Getter(lombok.AccessLevel.NONE)
  private ModelNode parent;
  private String iconResourcePath;
  private String label;
  private List<? extends ModelNode> children;

  public DefaultNode(Supplier<String> iconResourcePathSupplier, Supplier<String> labelSupplier,
      Supplier<List<? extends ModelNode>> childrenSupplier) {
    this.iconResourcePathSupplier = iconResourcePathSupplier;
    this.labelSupplier = labelSupplier;
    this.childrenSupplier = childrenSupplier;
    thisRefresh();
  }

  private void thisRefresh() {
    iconResourcePath = iconResourcePathSupplier.get();
    label = labelSupplier.get();
    children = childrenSupplier.get();
    // Set parent reference for JFace TreeViewer.getParent() support
    for (var child : children) {
      if (child instanceof DefaultNode defaultChild) {
        defaultChild.parent = this;
      }
    }
  }

  @Override
  public void refresh() {
    thisRefresh();
    ModelNode.super.refresh();
  }

  @Override
  public ModelNode getParent() {
    return parent;
  }

  public void applyValueChange(Object newValue) {
    throw new UnsupportedOperationException(
        "Value change not supported for %s".formatted(getClass().getSimpleName()));
  }
}
