package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.gui.model.NetworksTreeModel;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;

/**
 * Represents a node in the {@link NetworksTreeModel}.
 */
public interface ModelNode {

  String getLabel();

  Icon getIcon();

  default void refresh() {
    getChildren().forEach(ModelNode::refresh);
  }

  default ModelNode getChild(int index) {
    return getChildren().get(index);
  }

  default List<? extends ModelNode> getChildren() {
    return Collections.emptyList();
  }

  default boolean isLeaf() {
    return getChildCount() == 0;
  }

  default int getChildCount() {
    return getChildren().size();
  }

  default <T extends ModelNode> int getIndexOf(T child) {
    return getChildren().indexOf(child);
  }
}
