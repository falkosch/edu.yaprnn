package edu.yaprnn.gui.model.editors;

import edu.yaprnn.gui.model.NetworksTreeCellRenderer;
import edu.yaprnn.gui.model.nodes.BiasNode;
import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.views.di.NetworksTree;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;

@ApplicationScoped
public class BiasTreeCellEditor extends SelectableTreeCellEditorWithComponent<JTextField> {

  @Inject
  public BiasTreeCellEditor(@NetworksTree JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, ControlsService controlsService) {
    this(networksTree, networksTreeCellRenderer, new JTextField());
    getComponent().addKeyListener(controlsService.onlyNumbersKeyListener(false, false));
  }

  private BiasTreeCellEditor(JTree networksTree, NetworksTreeCellRenderer networksTreeCellRenderer,
      JTextField component) {
    super(networksTree, networksTreeCellRenderer, new DefaultCellEditor(component), component);
  }

  @Override
  public boolean isEditorOf(Object value) {
    return value instanceof BiasNode;
  }

  @Override
  protected void setEditorValueOnStartEdit(Object value) {
    if (value instanceof BiasNode node) {
      getComponent().setText(
          String.valueOf(node.getMultiLayerNetworkTemplateSupplier().get().getBias()));
    }
  }

  @Override
  protected Object mapCellEditorValue(Object value) {
    return Float.valueOf(String.valueOf(value));
  }
}
