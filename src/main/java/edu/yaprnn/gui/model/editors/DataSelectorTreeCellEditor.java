package edu.yaprnn.gui.model.editors;

import edu.yaprnn.gui.model.NetworksTreeCellRenderer;
import edu.yaprnn.gui.model.nodes.DataSelectorNode;
import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.views.di.NetworksTree;
import edu.yaprnn.training.DataSelector;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTree;

@ApplicationScoped
public class DataSelectorTreeCellEditor extends
    SelectableTreeCellEditorWithComponent<JComboBox<DataSelector>> {

  @Inject
  public DataSelectorTreeCellEditor(@NetworksTree JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, ControlsService controlsService) {
    this(networksTree, networksTreeCellRenderer,
        controlsService.dataSelectorsComboBox(_ -> {/*NOOP*/}));
  }

  private DataSelectorTreeCellEditor(JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, JComboBox<DataSelector> component) {
    super(networksTree, networksTreeCellRenderer, new DefaultCellEditor(component), component);
  }

  @Override
  public boolean isEditorOf(Object value) {
    return value instanceof DataSelectorNode;
  }

  @Override
  protected void setEditorValueOnStartEdit(Object value) {
    if (value instanceof DataSelectorNode node) {
      getComponent().setSelectedItem(node.getTrainingDataSupplier().get().getDataSelector());
    }
  }

  @Override
  protected Object mapCellEditorValue(Object value) {
    return value;
  }
}
