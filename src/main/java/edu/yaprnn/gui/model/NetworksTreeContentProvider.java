package edu.yaprnn.gui.model;

import edu.yaprnn.gui.model.nodes.ModelNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * JFace content provider for the networks tree. Delegates to ModelNode's children/parent methods.
 */
@Singleton
public class NetworksTreeContentProvider implements ITreeContentProvider {

  @Inject
  RootNode rootNode;

  @Override
  public Object[] getElements(Object inputElement) {
    return rootNode.getChildren().toArray();
  }

  @Override
  public Object[] getChildren(Object parentElement) {
    if (parentElement instanceof ModelNode node) {
      return node.getChildren().toArray();
    }
    return new Object[0];
  }

  @Override
  public Object getParent(Object element) {
    if (element instanceof ModelNode node) {
      return node.getParent();
    }
    return null;
  }

  @Override
  public boolean hasChildren(Object element) {
    if (element instanceof ModelNode node) {
      return !node.isLeaf();
    }
    return false;
  }
}
