package edu.yaprnn.gui.model;

import edu.yaprnn.gui.model.nodes.ModelNode;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Renders icons for the {@link ModelNode} in the {@link NetworksTreeModel}.
 */
public class NetworksTreeCellRenderer extends DefaultTreeCellRenderer {

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    if (value instanceof ModelNode modelNode) {
      setIcon(modelNode.getIcon());
      setText(modelNode.getLabel());
    }

    return this;
  }
}
