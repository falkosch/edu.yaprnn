package edu.yaprnn.gui.model;

import edu.yaprnn.gui.model.nodes.ModelNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.stream.Stream;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

/**
 * Manages JFace TreeViewer refresh operations. Replaces Swing TreeModelListener notifications with
 * SWT-thread-safe viewer updates.
 */
@Singleton
public class NetworksTreeRefreshService {

  @Inject
  Display display;
  @Inject
  RootNode rootNode;

  private TreeViewer treeViewer;

  public void setTreeViewer(TreeViewer treeViewer) {
    this.treeViewer = treeViewer;
  }

  /**
   * Refreshes the given sub-root and optional additional nodes, then updates the tree viewer.
   */
  public void refreshNodes(ModelNode subRoot, ModelNode... additional) {
    subRoot.refresh();
    Arrays.stream(additional).forEach(ModelNode::refresh);
    asyncRefresh();
  }

  /**
   * Refreshes a chain of nodes from root to the given node and updates the tree viewer.
   */
  public void refreshPath(ModelNode... pathNodes) {
    Stream.of(pathNodes).forEach(ModelNode::refresh);
    asyncRefresh();
  }

  /**
   * Schedules a full tree viewer refresh on the SWT UI thread.
   */
  public void asyncRefresh() {
    if (treeViewer == null) {
      return;
    }
    display.asyncExec(() -> {
      if (!treeViewer.getControl().isDisposed()) {
        treeViewer.refresh();
      }
    });
  }

  /**
   * Schedules a tree viewer update for a specific element on the SWT UI thread.
   */
  public void asyncUpdate(Object element) {
    if (treeViewer == null) {
      return;
    }
    display.asyncExec(() -> {
      if (!treeViewer.getControl().isDisposed()) {
        treeViewer.update(element, null);
      }
    });
  }
}
