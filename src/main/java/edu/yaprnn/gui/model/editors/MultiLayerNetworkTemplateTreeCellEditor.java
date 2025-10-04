package edu.yaprnn.gui.model.editors;

import edu.yaprnn.gui.model.NetworksTreeCellRenderer;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkTemplateNode;
import edu.yaprnn.gui.views.di.NetworksTree;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;

@ApplicationScoped
public class MultiLayerNetworkTemplateTreeCellEditor extends
    SelectableTreeCellEditorWithComponent<JTextField> {

  @Inject
  public MultiLayerNetworkTemplateTreeCellEditor(@NetworksTree JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer) {
    this(networksTree, networksTreeCellRenderer, new JTextField());
  }

  private MultiLayerNetworkTemplateTreeCellEditor(JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, JTextField component) {
    super(networksTree, networksTreeCellRenderer, new DefaultCellEditor(component), component);
  }

  @Override
  public boolean isEditorOf(Object value) {
    return value instanceof MultiLayerNetworkTemplateNode;
  }

  @Override
  protected void setEditorValueOnStartEdit(Object value) {
    if (value instanceof MultiLayerNetworkTemplateNode node) {
      getComponent().setText(node.getMultiLayerNetworkTemplateSupplier().get().getName());
    }
  }

  @Override
  protected Object mapCellEditorValue(Object value) {
    return value;
  }
}
