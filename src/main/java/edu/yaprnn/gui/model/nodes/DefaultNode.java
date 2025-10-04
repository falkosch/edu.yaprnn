package edu.yaprnn.gui.model.nodes;

import java.util.List;
import java.util.function.Supplier;
import javax.swing.Icon;
import lombok.Getter;

@Getter
public class DefaultNode implements ModelNode {

  private final Supplier<Icon> iconSupplier;
  private final Supplier<String> labelSupplier;
  private final Supplier<List<? extends ModelNode>> childrenSupplier;

  private Icon icon;
  private String label;
  private List<? extends ModelNode> children;

  public DefaultNode(Supplier<Icon> iconSupplier, Supplier<String> labelSupplier,
      Supplier<List<? extends ModelNode>> childrenSupplier) {
    this.iconSupplier = iconSupplier;
    this.labelSupplier = labelSupplier;
    this.childrenSupplier = childrenSupplier;
    thisRefresh();
  }

  private void thisRefresh() {
    icon = iconSupplier.get();
    label = labelSupplier.get();
    children = childrenSupplier.get();
  }

  @Override
  public void refresh() {
    thisRefresh();
    ModelNode.super.refresh();
  }
}
