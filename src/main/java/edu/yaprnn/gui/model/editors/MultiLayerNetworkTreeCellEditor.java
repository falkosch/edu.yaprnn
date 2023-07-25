package edu.yaprnn.gui.model.editors;

import edu.yaprnn.gui.model.NetworksTreeCellRenderer;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkNode;
import edu.yaprnn.gui.views.di.NetworksTree;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;

@ApplicationScoped
public class MultiLayerNetworkTreeCellEditor extends
    SelectableTreeCellEditorWithComponent<JTextField> {

  @Inject
  public MultiLayerNetworkTreeCellEditor(@NetworksTree JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer) {
    this(networksTree, networksTreeCellRenderer, new JTextField());
  }

  private MultiLayerNetworkTreeCellEditor(JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, JTextField component) {
    super(networksTree, networksTreeCellRenderer, new DefaultCellEditor(component), component);
  }

  @Override
  public boolean isEditorOf(Object value) {
    return value instanceof MultiLayerNetworkNode;
  }

  @Override
  protected void setEditorValueOnStartEdit(Object value) {
    if (value instanceof MultiLayerNetworkNode node) {
      getComponent().setText(node.getMultiLayerNetworkSupplier().get().getName());
    }
  }

  @Override
  protected Object mapCellEditorValue(Object value) {
    return value;
  }
}
