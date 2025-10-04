package edu.yaprnn.gui.model.editors;

import edu.yaprnn.gui.model.NetworksTreeCellRenderer;
import edu.yaprnn.gui.model.nodes.LayerSizeNode;
import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.views.di.NetworksTree;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;

@ApplicationScoped
public class LayerSizeTreeCellEditor extends SelectableTreeCellEditorWithComponent<JTextField> {

  @Inject
  public LayerSizeTreeCellEditor(@NetworksTree JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, ControlsService controlsService) {
    this(networksTree, networksTreeCellRenderer, new JTextField());
    getComponent().addKeyListener(controlsService.onlyNumbersKeyListener(true, true));
  }

  private LayerSizeTreeCellEditor(JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, JTextField component) {
    super(networksTree, networksTreeCellRenderer, new DefaultCellEditor(component), component);
  }

  @Override
  public boolean isEditorOf(Object value) {
    return value instanceof LayerSizeNode;
  }

  @Override
  protected void setEditorValueOnStartEdit(Object value) {
    if (value instanceof LayerSizeNode node) {
      getComponent().setText(String.valueOf(node.getLayerTemplateSupplier().get().getSize()));
    }
  }

  @Override
  protected Object mapCellEditorValue(Object value) {
    return Integer.valueOf(String.valueOf(value));
  }
}
