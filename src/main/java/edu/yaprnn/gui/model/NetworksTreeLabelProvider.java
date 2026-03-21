package edu.yaprnn.gui.model;

import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.gui.services.SwtIconsService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * JFace column label provider for the networks tree. Maps ModelNode labels and icon resource paths
 * to SWT text and images. Extends ColumnLabelProvider for compatibility with TreeViewerColumn.
 */
@Singleton
public class NetworksTreeLabelProvider extends ColumnLabelProvider {

  @Inject
  SwtIconsService swtIconsService;

  @Override
  public String getText(Object element) {
    if (element instanceof ModelNode node) {
      return node.getLabel();
    }
    return super.getText(element);
  }

  @Override
  public Image getImage(Object element) {
    if (element instanceof ModelNode node) {
      var resourcePath = node.getIconResourcePath();
      if (resourcePath != null) {
        return swtIconsService.getImage(resourcePath);
      }
    }
    return null;
  }
}
